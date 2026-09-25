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
            settings.month = App.entryForm.today().slice(0, 7);
            m$.month.value = settings.month;

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
                b.addEventListener('click', function(){ m$.dialog.close(); });
            });
            m$.save.addEventListener('click', save);
            m$.del.addEventListener('click', remove);

            loadMaster().then(list);
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
            m$.monthTitle.textContent = p[0] + '년 ' + Number(p[1]) + '월';
            m$.monthRange.textContent = '달력 월 기준 · ' + Number(p[1]) + '월 1일~' + lastDay + '일';

            App.post(url.list, {
                month: settings.month,
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

        render = function(rows){
            m$.count.textContent = rows.length + '건';
            if (!rows.length) {
                m$.list.replaceChildren(filtered()
                    ? App.entryForm.empty('조건에 맞는 거래가 없어요', '검색어나 필터를 바꿔 보세요.')
                    : App.entryForm.empty('이 달의 첫 기록을 남겨볼까요?', '작은 지출부터 하나씩 기록하면 나의 소비가 보이기 시작해요.'));
                return;
            }
            m$.list.replaceChildren.apply(m$.list, App.entryForm.grouped(rows, openEdit));
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
            m$.dialog.showModal();
            m$.amount.focus();
        },

        openNew = function(){ open(null); },

        openEdit = function(e){ open(e); },

        save = function(){
            if (settings.submitting) return;
            var amount = App.parseAmount(m$.amount.value),
                categoryId = App.entryForm.checked(ED + '-category'),
                title = m$.title.value.trim();
            if (!amount) {
                _error('알림', '금액을 1원 이상 입력해주세요.');
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
