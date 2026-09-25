App.recurring = (function(){
    var ED = 'recurringEditor',
        ADJUST_LABEL = {NONE: '그대로', PREV_BIZ: '직전 평일', NEXT_BIZ: '다음 평일'},
        m$ = {
            newButton: document.getElementById('recurringNew'),
            cycleRange: document.getElementById('cycleRange'),
            daysLeft: document.getElementById('daysLeft'),
            monthlyFixed: document.getElementById('monthlyFixed'),
            pendingFixed: document.getElementById('pendingFixed'),
            recorded: document.getElementById('recorded'),
            body: document.getElementById('recurringBody'),
            dialog: document.getElementById(ED),
            heading: document.getElementById(ED + '-heading'),
            subtitle: document.getElementById(ED + '-subtitle'),
            name: document.getElementById(ED + '-name'),
            amount: document.getElementById(ED + '-amount'),
            day: document.getElementById(ED + '-day'),
            category: document.getElementById(ED + '-category'),
            payment: document.getElementById(ED + '-payment'),
            memo: document.getElementById(ED + '-memo'),
            active: document.getElementById(ED + '-active'),
            del: document.getElementById(ED + '-delete'),
            save: document.getElementById(ED + '-save')
        },
        settings = { submitting: false, master: {categories: [], paymentMethods: []}, editing: null },
        url = {
            master: contextPath + '/ledger/settings/master',
            summary: contextPath + '/ledger/dashboard/summary',
            list: contextPath + '/ledger/recurring/list',
            save: contextPath + '/ledger/recurring/save',
            toggle: contextPath + '/ledger/recurring/toggle',
            del: contextPath + '/ledger/recurring/delete'
        },

        init = function(){
            m$.newButton.addEventListener('click', function(){ open(null); });
            App.bindAmountInput(m$.amount);
            m$.dialog.querySelectorAll('input[name="' + ED + '-type"]').forEach(function(r){
                r.addEventListener('change', function(){ fillCategories(r.value, null); });
            });
            m$.dialog.querySelectorAll('[data-close]').forEach(function(b){
                b.addEventListener('click', function(){ m$.dialog.close(); });
            });
            m$.save.addEventListener('click', save);
            m$.del.addEventListener('click', remove);

            App.post(url.master)
                .then(function(res){ App.result(res, {ok: function(){ settings.master = res.data; }}); })
                .catch(function(){});
            reload();
        },

        reload = function(){
            loadSummary();
            loadList();
        },

        dotted = function(d){ return d.replace(/-/g, '.'); },

        // 주기 띠·아직 예정인 지출은 대시보드 계산을 그대로 쓴다
        loadSummary = function(){
            App.post(url.summary)
                .then(function(res){
                    App.result(res, {ok: function(){
                        var s = res.data;
                        m$.cycleRange.textContent = dotted(s.cycleStart) + ' — ' + dotted(s.cycleEnd).slice(5);
                        m$.daysLeft.textContent = s.daysLeft + '일 남음';
                        m$.pendingFixed.textContent = Number(s.pendingFixed).toLocaleString('ko-KR');
                    }});
                })
                .catch(function(){});
        },

        loadList = function(){
            App.post(url.list)
                .then(function(res){
                    App.result(res, {ok: function(){ render(res.data); }});
                })
                .catch(function(){});
        },

        render = function(data){
            m$.monthlyFixed.textContent = Number(data.monthlyFixed).toLocaleString('ko-KR');
            m$.recorded.textContent = Number(data.recorded).toLocaleString('ko-KR');
            if (!data.items.length) {
                m$.body.replaceChildren(App.h('tr', null, [App.h('td', {attrs: {colspan: 8}, text: '등록한 고정 항목이 없어요. 월세·구독료·월급을 추가해 보세요.'})]));
                return;
            }
            m$.body.replaceChildren.apply(m$.body, data.items.map(row));
        },

        statusChip = function(status){
            var map = {
                DONE: ['chip', '처리됨'],
                SKIPPED: ['chip neutral', '건너뜀'],
                PLANNED: ['chip warm', '예정'],
                NONE: ['chip neutral', '이번 주기 없음'],
                INACTIVE: ['chip neutral', '비활성']
            }[status] || ['chip neutral', '—'];
            return App.h('span', {className: map[0], text: map[1]});
        },

        row = function(item){
            var income = item.type === 'INCOME',
                money = (income ? '+' : '−') + App.money(item.amount),
                toggle = App.h('input', {type: 'checkbox', checked: !!item.active, attrs: {'aria-label': item.name + ' 활성'}});

            toggle.addEventListener('change', function(){
                setActive(item, toggle);
            });

            return App.h('tr', null, [
                App.h('th', {attrs: {scope: 'row'}}, [
                    item.name + ' ',
                    App.h('span', {className: 'chip ' + (!item.active ? 'neutral' : income ? '' : 'warm'), text: income ? '수입' : '지출'})
                ]),
                App.h('td', {attrs: {'data-label': '금액'}}, [
                    item.active ? App.h('span', {className: income ? 'income' : 'expense', text: money}) : money
                ]),
                App.h('td', {attrs: {'data-label': '분류 / 결제수단'}, text: item.categoryName + ' / ' + (item.paymentMethodName || '없음')}),
                App.h('td', {attrs: {'data-label': '매월 결제일'}, text: item.dayOfMonth + '일'}),
                App.h('td', {attrs: {'data-label': '휴일 보정'}, text: ADJUST_LABEL[item.adjust] || item.adjust}),
                App.h('td', {attrs: {'data-label': '이번 주기'}}, [statusChip(item.status)]),
                App.h('td', {attrs: {'data-label': '활성'}}, [
                    App.h('label', {className: 'switch'}, [toggle, App.h('span', {attrs: {'aria-hidden': 'true'}})])
                ]),
                App.h('td', {attrs: {'data-label': '관리'}}, [
                    App.h('button', {
                        type: 'button', className: 'button small edit-button', text: '수정',
                        attrs: {'aria-label': item.name + ' 수정'},
                        on: {click: function(){ open(item); }}
                    })
                ])
            ]);
        },

        // 활성 스위치는 바로 저장. 실패하면 스위치를 되돌린다
        setActive = function(item, toggle){
            if (settings.submitting) {
                toggle.checked = !toggle.checked;
                return;
            }
            settings.submitting = true;
            App.post(url.toggle, {id: item.id, active: toggle.checked})
                .then(function(res){
                    App.result(res, {ok: function(){
                        App.saved(toggle.checked ? '자동 기록을 켰어요' : '자동 기록을 껐어요');
                        reload();
                    }});
                    if (res.code !== App.CODE.SUCCESS) toggle.checked = !toggle.checked;
                })
                .catch(function(){ toggle.checked = !toggle.checked; })
                .then(function(){ settings.submitting = false; });
        },

        option = function(value, label, selected){
            return App.h('option', {value: String(value), text: label, selected: !!selected});
        },

        // 활성 카테고리 + 수정 중인 항목이 쓰는 비활성 카테고리
        fillCategories = function(type, selectedId){
            var list = settings.master.categories.filter(function(c){
                return c.type === type && (c.active || c.id === selectedId);
            });
            m$.category.replaceChildren.apply(m$.category, list.map(function(c){
                return option(c.id, c.name + (c.active ? '' : ' (숨김)'), c.id === selectedId);
            }));
        },

        fillPayments = function(selectedId){
            var options = [option('', '선택 안 함', selectedId === null || selectedId === undefined)];
            settings.master.paymentMethods.forEach(function(p){
                if (p.active || p.id === selectedId) {
                    options.push(option(p.id, p.name + (p.active ? '' : ' (숨김)'), p.id === selectedId));
                }
            });
            m$.payment.replaceChildren.apply(m$.payment, options);
        },

        setRadio = function(name, value){
            var el = m$.dialog.querySelector('input[name="' + name + '"][value="' + value + '"]');
            if (el) el.checked = true;
        },

        checked = function(name){
            var el = m$.dialog.querySelector('input[name="' + name + '"]:checked');
            return el ? el.value : '';
        },

        open = function(item){
            settings.editing = item;
            m$.heading.textContent = item ? '고정 항목 수정' : '고정 항목 등록';
            m$.subtitle.textContent = item ? item.name : '필요한 내용을 입력해 주세요.';
            m$.name.value = item ? item.name : '';
            setRadio(ED + '-type', item ? item.type : 'EXPENSE');
            m$.amount.value = item ? Number(item.amount).toLocaleString('ko-KR') : '';
            m$.day.value = item ? item.dayOfMonth : 1;
            fillCategories(item ? item.type : 'EXPENSE', item ? item.categoryId : null);
            fillPayments(item ? item.paymentMethodId : null);
            setRadio(ED + '-adjust', item ? item.adjust : 'NONE');
            m$.memo.value = item && item.memo ? item.memo : '';
            m$.active.checked = item ? !!item.active : true;
            m$.del.hidden = !item;
            m$.dialog.showModal();
            m$.name.focus();
        },

        save = function(){
            if (settings.submitting) return;
            var amount = App.parseAmount(m$.amount.value),
                day = Number(m$.day.value);
            if (!m$.name.value.trim()) {
                _error('알림', '항목명을 입력해주세요.');
                return;
            }
            if (!amount) {
                _error('알림', '금액을 1원 이상 입력해주세요.');
                return;
            }
            if (!(day >= 1 && day <= 31 && Math.floor(day) === day)) {
                _error('알림', '결제일은 1~31 사이로 입력해주세요.');
                return;
            }
            if (!m$.category.value) {
                _error('알림', '카테고리를 골라주세요.');
                return;
            }

            settings.submitting = true;
            App.post(url.save, {
                id: settings.editing ? settings.editing.id : '',
                name: m$.name.value.trim(),
                type: checked(ED + '-type'),
                amount: amount,
                dayOfMonth: day,
                categoryId: m$.category.value,
                paymentMethodId: m$.payment.value,
                adjust: checked(ED + '-adjust'),
                memo: m$.memo.value.trim(),
                active: m$.active.checked
            })
                .then(function(res){
                    App.result(res, {ok: function(){
                        m$.dialog.close();
                        App.saved();
                        reload();
                    }});
                })
                .catch(function(){})
                .then(function(){ settings.submitting = false; });
        },

        remove = function(){
            var item = settings.editing;
            if (!item) return;
            _confirm('삭제할까요?', '‘' + item.name + '’ 고정 항목을 삭제해요.\n이미 기록된 거래는 그대로 남아요.', function(){
                if (settings.submitting) return;
                settings.submitting = true;
                App.post(url.del, {id: item.id})
                    .then(function(res){
                        App.result(res, {ok: function(){
                            m$.dialog.close();
                            App.saved('삭제했어요');
                            reload();
                        }});
                    })
                    .catch(function(){})
                    .then(function(){ settings.submitting = false; });
            });
        };

    return { init: init };
}());

document.addEventListener('DOMContentLoaded', function(){
    App.recurring.init();
});
