App.entry = (function(){
    var ED = 'entryEditor',
        m$ = {
            newButton: document.getElementById('entryNew'),
            monthTitle: document.getElementById('monthTitle'),
            monthRange: document.getElementById('monthRange'),
            prev: document.getElementById('prevMonth'),
            next: document.getElementById('nextMonth'),
            month: document.getElementById('month'),
            keyword: document.getElementById('filterKeyword'),
            type: document.getElementById('filterType'),
            category: document.getElementById('filterCategory'),
            payment: document.getElementById('filterPayment'),
            reset: document.getElementById('filterReset'),
            search: document.getElementById('filterSearch'),
            count: document.getElementById('listCount'),
            totals: document.getElementById('listTotals'),
            truncated: document.getElementById('listTruncated'),
            period: document.getElementById('period'),
            monthField: document.getElementById('monthField'),
            list: document.getElementById('entryList'),
            dialog: document.getElementById(ED),
            heading: document.getElementById(ED + '-heading'),
            subtitle: document.getElementById(ED + '-subtitle'),
            amount: document.getElementById(ED + '-amount'),
            categories: document.getElementById(ED + '-categories'),
            title: document.getElementById(ED + '-title'),
            payments: document.getElementById(ED + '-payments'),
            date: document.getElementById(ED + '-date'),
            memoBox: document.getElementById(ED + '-memoBox'),
            memo: document.getElementById(ED + '-memo'),
            del: document.getElementById(ED + '-delete'),
            save: document.getElementById(ED + '-save')
        },
        settings = {
            submitting: false,
            month: '',                                        // yyyy-MM
            master: {categories: [], paymentMethods: []},
            editing: null                                     // 수정 중인 거래(추가면 null)
        },
        url = {
            master: contextPath + '/ledger/settings/master',
            list: contextPath + '/ledger/entry/list',
            save: contextPath + '/ledger/entry/save',
            del: contextPath + '/ledger/entry/delete'
        },

        init = function(){
            // 조회 조건은 주소(?period=&month=&q=&type=&category=&payment=)에 남겨 새로고침·뒤로 가기에도 유지한다
            var q = new URLSearchParams(location.search);
            settings.month = /^\d{4}-(0[1-9]|1[0-2])$/.test(q.get('month') || '') ? q.get('month') : App.entryForm.today().slice(0, 7);
            m$.month.value = settings.month;
            m$.period.value = ['CYCLE', 'RECENT12', 'ALL'].indexOf(q.get('period')) >= 0 ? q.get('period') : 'MONTH';
            // 주기 보기: 이 날짜가 속한 주기(없으면 오늘)
            settings.cycleDate = /^\d{4}-\d{2}-\d{2}$/.test(q.get('date') || '') ? q.get('date') : '';
            showPeriod();
            m$.period.addEventListener('change', function(){
                showPeriod();
                list();
            });
            m$.keyword.value = q.get('q') || '';
            m$.type.value = ['INCOME', 'EXPENSE'].indexOf(q.get('type')) >= 0 ? q.get('type') : '';
            settings.editId = q.get('edit');

            m$.prev.addEventListener('click', function(){ moveMonth(-1); });
            m$.next.addEventListener('click', function(){ moveMonth(1); });
            m$.month.addEventListener('change', function(){
                if (m$.month.value) {
                    settings.month = m$.month.value;
                    list();
                }
            });
            m$.search.addEventListener('click', list);
            m$.keyword.addEventListener('keydown', function(e){
                if (e.key === 'Enter') list();
            });
            m$.reset.addEventListener('click', function(){
                m$.keyword.value = '';
                m$.type.value = '';
                m$.category.value = '';
                m$.payment.value = '';
                list();
            });

            m$.newButton.addEventListener('click', openNew);
            App.bindAmountInput(m$.amount);
            m$.dialog.querySelectorAll('input[name="' + ED + '-type"]').forEach(function(r){
                r.addEventListener('change', function(){
                    App.entryForm.renderCategories(m$.categories, ED + '-category', settings.master.categories, r.value, null);
                });
            });
            m$.dialog.querySelectorAll('[data-close]').forEach(function(b){
                b.addEventListener('click', requestClose);
            });
            // Escape 로 닫을 때도 작성 중인 내용이 있으면 먼저 묻는다. cancel 이벤트를 막는 것만으로는
            // Chrome 이 연속된 Escape 에서 창을 그냥 닫으므로(close watcher), 키 입력 단계에서 닫기 요청 자체를 막는다.
            // (확인창 안의 Escape 는 확인창이 전파를 끊으므로 여기로 오지 않는다)
            m$.dialog.addEventListener('keydown', function(e){
                if (e.key === 'Escape') {
                    e.preventDefault();
                    requestClose();
                }
            });
            m$.dialog.addEventListener('cancel', function(e){   // 휴대폰 뒤로 가기 등 키 입력이 아닌 닫기 요청
                e.preventDefault();
                requestClose();
            });
            // 금액·내용·메모에서 Enter(휴대폰 키보드의 완료)로 저장. 한글 조합 중 Enter 는 조합 완료로 둔다
            [m$.amount, m$.title, m$.memo].forEach(function(el){
                el.setAttribute('enterkeyhint', 'done');
                el.addEventListener('keydown', function(e){
                    if (e.key === 'Enter' && !e.isComposing) {
                        e.preventDefault();
                        save();
                    }
                });
            });
            m$.save.addEventListener('click', save);
            m$.del.addEventListener('click', remove);

            loadMaster().then(function(){
                // 선택지에 없는 값(지운 항목 등)이면 select 가 빈 값으로 남는다
                m$.category.value = q.get('category') || '';
                m$.payment.value = q.get('payment') || '';
                list();
            });
        },

        // 달력 월일 때만 이전·다음 달과 조회 월을 보인다
        // 이전·다음은 달력 월(한 달씩)·주기(한 주기씩)에서, 조회 월 칸은 달력 월에서만 보인다
        showPeriod = function(){
            var period = m$.period.value;
            m$.prev.hidden = m$.next.hidden = period !== 'MONTH' && period !== 'CYCLE';
            m$.monthField.hidden = period !== 'MONTH';
            m$.prev.setAttribute('aria-label', period === 'CYCLE' ? '이전 주기' : '이전 달');
            m$.next.setAttribute('aria-label', period === 'CYCLE' ? '다음 주기' : '다음 달');
        },

        // "2026-09-23" 에서 n일 옮긴 날짜
        shiftDate = function(iso, n){
            var d = new Date(iso + 'T00:00:00');
            d.setDate(d.getDate() + n);
            return d.getFullYear() + '-' + String(d.getMonth() + 1).padStart(2, '0') + '-' + String(d.getDate()).padStart(2, '0');
        },

        saveQuery = function(){
            var q = new URLSearchParams({month: settings.month});
            if (m$.period.value !== 'MONTH') q.set('period', m$.period.value);
            if (m$.period.value === 'CYCLE' && settings.cycleDate) q.set('date', settings.cycleDate);
            if (m$.keyword.value.trim()) q.set('q', m$.keyword.value.trim());
            if (m$.type.value) q.set('type', m$.type.value);
            if (m$.category.value) q.set('category', m$.category.value);
            if (m$.payment.value) q.set('payment', m$.payment.value);
            history.replaceState(null, '', location.pathname + '?' + q.toString());
        },

        loadMaster = function(){
            return App.post(url.master)
                .then(function(res){
                    App.result(res, {ok: function(){
                        settings.master = res.data;
                        fillFilters();
                    }});
                })
                .catch(function(){});
        },

        fillFilters = function(){
            var categoryOptions = [App.h('option', {value: '', text: '전체'})],
                paymentOptions = [App.h('option', {value: '', text: '전체'})];
            settings.master.categories.forEach(function(c){
                var label = (c.type === 'INCOME' ? '수입 · ' : '지출 · ') + c.name + (c.active ? '' : ' (숨김)');
                categoryOptions.push(App.h('option', {value: String(c.id), text: label}));
            });
            settings.master.paymentMethods.forEach(function(p){
                paymentOptions.push(App.h('option', {value: String(p.id), text: p.name + (p.active ? '' : ' (숨김)')}));
            });
            m$.category.replaceChildren.apply(m$.category, categoryOptions);
            m$.payment.replaceChildren.apply(m$.payment, paymentOptions);
        },

        moveMonth = function(delta){
            if (m$.period.value === 'CYCLE') {
                // 이전 주기는 시작일 전날, 다음 주기는 종료일 다음 날이 속한 주기
                if (!settings.cycle) return;
                settings.cycleDate = delta < 0 ? shiftDate(settings.cycle.from, -1) : shiftDate(settings.cycle.to, 1);
                list();
                return;
            }
            var p = settings.month.split('-'),
                d = new Date(Number(p[0]), Number(p[1]) - 1 + delta, 1);
            settings.month = d.getFullYear() + '-' + (d.getMonth() < 9 ? '0' : '') + (d.getMonth() + 1);
            m$.month.value = settings.month;
            list();
        },

        filtered = function(){
            return m$.keyword.value.trim() || m$.type.value || m$.category.value || m$.payment.value;
        },

        list = function(){
            var p = settings.month.split('-'),
                lastDay = new Date(Number(p[0]), Number(p[1]), 0).getDate();
            if (m$.period.value === 'MONTH') {
                m$.monthTitle.textContent = p[0] + '년 ' + Number(p[1]) + '월';
                m$.monthRange.textContent = '달력 월 기준 · ' + Number(p[1]) + '월 1일~' + lastDay + '일';
            } else if (m$.period.value === 'CYCLE') {
                m$.monthTitle.textContent = '주기';        // 응답을 받으면 "2026년 9월 주기"와 기간으로 바꾼다
                m$.monthRange.textContent = '';
            } else {
                m$.monthTitle.textContent = m$.period.value === 'ALL' ? '전체 기간' : '최근 12개월';
                m$.monthRange.textContent = '';
            }
            saveQuery();

            App.post(url.list, {
                period: m$.period.value,
                month: settings.month,
                date: m$.period.value === 'CYCLE' ? settings.cycleDate : '',
                type: m$.type.value,
                categoryId: m$.category.value,
                paymentMethodId: m$.payment.value,
                keyword: m$.keyword.value.trim()
            })
                .then(function(res){
                    App.result(res, {ok: function(){ render(res.data); }});
                })
                .catch(function(){});
        },

        render = function(data){
            var rows = data.rows, t = data.totals, net = t.income - t.expense;
            if (m$.period.value === 'RECENT12' && data.from) {
                m$.monthRange.textContent = data.from.split('-').join('.') + '부터';
            }
            if (m$.period.value === 'CYCLE') {
                var cm = data.cycleMonth.split('-');
                settings.cycle = {from: data.from, to: data.to};
                m$.monthTitle.textContent = cm[0] + '년 ' + Number(cm[1]) + '월 주기';
                m$.monthRange.textContent = '주기 기준 · ' + data.from.split('-').join('.') + ' ~ ' + data.to.slice(5).split('-').join('.')
                    + (data.holidayMissing ? ' · 이 해의 공휴일이 없어 주말만 반영' : '');
            }
            // 합계: 목록이 잘려도 조건에 맞는 전체 거래 기준
            m$.totals.replaceChildren(
                '수입 ', App.h('b', {className: 'income', text: (t.income ? '+' : '') + App.money(t.income)}),
                ' · 지출 ', App.h('b', {className: 'expense', text: (t.expense ? '−' : '') + App.money(t.expense)}),
                ' · 차이 ', App.h('b', {text: (net > 0 ? '+' : '') + App.money(net)}));
            m$.truncated.hidden = !data.truncated;
            m$.count.textContent = data.truncated ? t.count + '건 중 ' + rows.length + '건' : rows.length + '건';
            if (!rows.length) {
                m$.list.replaceChildren(filtered()
                    ? App.entryForm.empty('조건에 맞는 거래가 없어요', '검색어나 필터를 바꿔 보세요.')
                    : App.entryForm.empty((m$.period.value === 'MONTH' ? '이 달' : '이 기간') + '의 첫 기록을 남겨볼까요?', '작은 지출부터 하나씩 기록하면 나의 소비가 보이기 시작해요.'));
                return;
            }
            m$.list.replaceChildren.apply(m$.list, App.entryForm.grouped(rows, openEdit));
            // 대시보드 최근 기록에서 "수정"으로 들어온 경우(?edit=id) 그 거래의 수정 창을 한 번 연다
            if (settings.editId) {
                var target = rows.filter(function(r){ return String(r.id) === settings.editId; })[0];
                settings.editId = null;
                if (target) openEdit(target);
            }
        },

        setType = function(type){
            m$.dialog.querySelector('input[name="' + ED + '-type"][value="' + type + '"]').checked = true;
        },

        open = function(e){
            settings.editing = e;
            m$.heading.textContent = e ? '거래 수정' : '거래 추가';
            m$.subtitle.textContent = e ? e.title : '필요한 내용을 입력해 주세요.';
            m$.amount.value = e ? Number(e.amount).toLocaleString('ko-KR') : '';
            setType(e ? e.type : 'EXPENSE');
            App.entryForm.renderCategories(m$.categories, ED + '-category', settings.master.categories, e ? e.type : 'EXPENSE', e ? e.categoryId : null);
            m$.title.value = e ? e.title : '';
            App.entryForm.renderPayments(m$.payments, ED + '-payment', settings.master.paymentMethods, e ? e.paymentMethodId : null);
            m$.date.value = e ? e.entryDate
                : (settings.month === App.entryForm.today().slice(0, 7) ? App.entryForm.today() : settings.month + '-01');
            m$.memo.value = e && e.memo ? e.memo : '';
            m$.memoBox.open = !!(e && e.memo);
            m$.del.hidden = !e;
            settings.snapshot = snapshot();
            m$.dialog.showModal();
            m$.amount.focus();
        },

        // 창에 입력된 값 전체(열었을 때와 비교해 작성 중인지 판단)
        snapshot = function(){
            return JSON.stringify([m$.amount.value, App.entryForm.checked(ED + '-type'), App.entryForm.checked(ED + '-category'),
                m$.title.value, App.entryForm.checked(ED + '-payment'), m$.date.value, m$.memo.value]);
        },

        requestClose = function(){
            if (snapshot() === settings.snapshot) {
                m$.dialog.close();
                return;
            }
            _confirm('작성 중인 내용을 버릴까요?', '저장하지 않은 입력은 사라져요.', function(){ m$.dialog.close(); });
        },

        openNew = function(){ open(null); },

        openEdit = function(e){ open(e); },

        save = function(){
            if (settings.submitting) return;
            var amount = App.parseAmount(m$.amount.value),
                categoryId = App.entryForm.checked(ED + '-category'),
                title = m$.title.value.trim();
            if (!amount) {
                _error('알림', App.amountError(m$.amount.value));
                return;
            }
            if (!categoryId) {
                _error('알림', '카테고리를 골라주세요.');
                return;
            }
            if (!title) {
                _error('알림', '내용을 입력해주세요.');
                return;
            }
            if (!m$.date.value) {
                _error('알림', '날짜를 입력해주세요.');
                return;
            }

            settings.submitting = true;
            App.post(url.save, {
                id: settings.editing ? settings.editing.id : '',
                entryDate: m$.date.value,
                type: App.entryForm.checked(ED + '-type'),
                categoryId: categoryId,
                title: title,
                amount: amount,
                paymentMethodId: App.entryForm.checked(ED + '-payment'),
                memo: m$.memo.value.trim()
            })
                .then(function(res){
                    App.result(res, {ok: function(){
                        m$.dialog.close();
                        App.saved();
                        list();
                    }});
                })
                .catch(function(){})
                .then(function(){ settings.submitting = false; });
        },

        remove = function(){
            var e = settings.editing;
            if (!e) return;
            _confirm('삭제할까요?', '‘' + e.title + '’ 거래를 삭제해요.', function(){
                if (settings.submitting) return;
                settings.submitting = true;
                App.post(url.del, {id: e.id})
                    .then(function(res){
                        App.result(res, {ok: function(){
                            m$.dialog.close();
                            App.saved('삭제했어요');
                            list();
                        }});
                    })
                    .catch(function(){})
                    .then(function(){ settings.submitting = false; });
            });
        };

    return { init: init };
}());

document.addEventListener('DOMContentLoaded', function(){
    App.entry.init();
});
