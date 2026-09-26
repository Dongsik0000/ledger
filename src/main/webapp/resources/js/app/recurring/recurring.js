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
            installment: document.getElementById(ED + '-installment'),
            installmentTotal: document.getElementById(ED + '-installment-total'),
            installmentMonths: document.getElementById(ED + '-installment-months'),
            installmentStart: document.getElementById(ED + '-installment-start'),
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
            App.bindAmountInput(m$.installmentTotal);
            m$.dialog.querySelectorAll('input[name="' + ED + '-type"]').forEach(function(r){
                r.addEventListener('change', function(){ fillCategories(r.value, null); });
            });
            m$.installment.addEventListener('change', function(){
                showInstallment(m$.installment.checked);
                if (m$.installment.checked) installmentDefaults();
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
                m$.body.replaceChildren(App.h('tr', null, [App.h('td', {attrs: {colspan: 9}, text: '등록한 고정 항목이 없어요. 월세·구독료·월급을 추가해 보세요.'})]));
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

        // 할부: "할부 2/3" (이번 주기 회차), 이번 주기에 회차가 없으면 "할부 3개월 · 2026.10부터"
        installmentChip = function(item){
            var text = item.installmentRound
                ? '할부 ' + item.installmentRound + '/' + item.installmentMonths
                : '할부 ' + item.installmentMonths + '개월 · ' + item.installmentStart.trim().replace('-', '.') + '부터';
            return App.h('span', {className: 'chip neutral', text: text});
        },

        // 금액 칸에 보이는 금액: 할부는 이번 주기 회차 금액(이번 주기에 회차가 없으면 2회차 이후 금액)
        shownAmount = function(item){
            return item.installmentMonths && item.dueAmount ? item.dueAmount : item.amount;
        },

        // "10.1(목)" — 휴일 보정·할부 기간·처리 여부를 반영한 서버 계산값. 금액 칸과 다른 회차 금액이면 금액도
        nextDueText = function(item){
            if (!item.nextDue) return '—';
            var p = item.nextDue.split('-'),
                d = new Date(Number(p[0]), Number(p[1]) - 1, Number(p[2])),
                text = Number(p[1]) + '.' + Number(p[2]) + '(' + '일월화수목금토'.charAt(d.getDay()) + ')';
            return item.installmentMonths && item.nextAmount && item.nextAmount !== shownAmount(item)
                ? text + ' · ' + App.money(item.nextAmount) : text;
        },

        row = function(item){
            var income = item.type === 'INCOME',
                installment = !!item.installmentMonths,
                money = (income ? '+' : '−') + App.money(shownAmount(item)),
                toggle = App.h('input', {type: 'checkbox', checked: !!item.active, attrs: {'aria-label': item.name + ' 활성'}});

            toggle.addEventListener('change', function(){
                setActive(item, toggle);
            });

            return App.h('tr', null, [
                App.h('th', {attrs: {scope: 'row'}}, [
                    item.name + ' ',
                    App.h('span', {className: 'chip ' + (!item.active ? 'neutral' : income ? '' : 'warm'), text: income ? '수입' : '지출'})
                ].concat(installment ? [' ', installmentChip(item)] : [])),
                App.h('td', {attrs: {'data-label': '금액', 'data-cell': 'amount'}}, [
                    item.active ? App.h('span', {className: income ? 'income' : 'expense', text: money}) : money
                ]),
                App.h('td', {attrs: {'data-label': '분류 / 결제수단', 'data-cell': 'category'}, text: item.categoryName + ' / ' + (item.paymentMethodName || '없음')}),
                App.h('td', {attrs: {'data-label': '매월 결제일', 'data-cell': 'day'}, text: item.dayOfMonth + '일'}),
                App.h('td', {attrs: {'data-label': '휴일 보정', 'data-cell': 'adjust'}, text: ADJUST_LABEL[item.adjust] || item.adjust}),
                App.h('td', {attrs: {'data-label': '다음 결제일', 'data-cell': 'next'}, text: nextDueText(item)}),
                App.h('td', {attrs: {'data-label': '이번 주기', 'data-cell': 'status'}}, [statusChip(item.status)]),
                App.h('td', {attrs: {'data-label': '활성', 'data-cell': 'active'}}, [
                    App.h('label', {className: 'switch'}, [toggle, App.h('span', {attrs: {'aria-hidden': 'true'}})])
                ]),
                App.h('td', {attrs: {'data-label': '관리', 'data-cell': 'manage'}}, [
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

        // 할부면 금액 칸 대신 총액·개월 수·첫 결제월. 할부는 지출만
        showInstallment = function(on){
            m$.dialog.querySelectorAll('[data-installment]').forEach(function(el){ el.hidden = !on; });
            m$.dialog.querySelectorAll('[data-regular]').forEach(function(el){ el.hidden = on; });
            var income = m$.dialog.querySelector('input[name="' + ED + '-type"][value="INCOME"]');
            income.disabled = on;
            if (on && income.checked) {
                setRadio(ED + '-type', 'EXPENSE');
                fillCategories('EXPENSE', null);
            }
        },

        // 할부를 새로 켤 때: 카드 결제일 1일·휴일이면 다음 평일(카드사 출금 규칙)·신용카드·다음 달부터
        installmentDefaults = function(){
            var card = settings.master.paymentMethods.filter(function(p){ return p.active && p.name === '신용카드'; })[0],
                next = new Date();
            next.setDate(1);
            next.setMonth(next.getMonth() + 1);
            m$.day.value = 1;
            setRadio(ED + '-adjust', 'NEXT_BIZ');
            if (card) m$.payment.value = String(card.id);
            if (!m$.installmentStart.value) {
                m$.installmentStart.value = next.getFullYear() + '-' + String(next.getMonth() + 1).padStart(2, '0');
            }
        },

        open = function(item){
            var installment = !!(item && item.installmentMonths);
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
            m$.installment.checked = installment;
            m$.installmentTotal.value = installment ? Number(item.installmentTotal).toLocaleString('ko-KR') : '';
            m$.installmentMonths.value = installment ? item.installmentMonths : '';
            m$.installmentStart.value = installment ? item.installmentStart.trim() : '';
            showInstallment(installment);
            m$.del.hidden = !item;
            m$.dialog.showModal();
            m$.name.focus();
        },

        save = function(){
            if (settings.submitting) return;
            var installment = m$.installment.checked,
                amount = App.parseAmount(m$.amount.value),
                total = App.parseAmount(m$.installmentTotal.value),
                months = Number(m$.installmentMonths.value),
                day = Number(m$.day.value);
            if (!m$.name.value.trim()) {
                _error('알림', '항목명을 입력해주세요.');
                return;
            }
            if (installment) {
                if (!(months >= 2 && months <= 60 && Math.floor(months) === months)) {
                    _error('알림', '개월 수는 2~60 사이로 입력해주세요.');
                    return;
                }
                if (App.amountError(m$.installmentTotal.value)) {
                    _error('알림', App.amountError(m$.installmentTotal.value));
                    return;
                }
                if (total < months) {
                    _error('알림', '할부 총액을 개월 수 이상으로 입력해주세요.');
                    return;
                }
                if (!/^\d{4}-\d{2}$/.test(m$.installmentStart.value)) {
                    _error('알림', '첫 결제월을 골라주세요.');
                    return;
                }
            } else if (!amount) {
                _error('알림', App.amountError(m$.amount.value));
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
                amount: installment ? '' : amount,
                installment: installment,
                installmentTotal: installment ? total : '',
                installmentMonths: installment ? months : '',
                installmentStart: installment ? m$.installmentStart.value : '',
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
