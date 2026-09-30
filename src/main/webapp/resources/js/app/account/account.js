App.account = (function(){
    var AED = 'accountEditor', TED = 'transferEditor', RED = 'recurringTransferEditor',
        ADJUST_LABEL = {NONE: '', PREV_BIZ: ' · 직전 평일', NEXT_BIZ: ' · 다음 평일'},
        byId = function(id){ return document.getElementById(id); },
        m$ = {
            accountNew: byId('accountNew'),
            transferNew: byId('transferNew'),
            recurringNew: byId('recurringNew'),
            bankTotal: byId('bankTotal'),
            ledgerBalance: byId('ledgerBalance'),
            diff: byId('balanceDiff'),
            diffFoot: byId('balanceDiffFoot'),
            accountBody: byId('accountBody'),
            recurringBody: byId('recurringBody'),
            transferBody: byId('transferBody'),
            account: {
                dialog: byId(AED), heading: byId(AED + '-heading'), subtitle: byId(AED + '-subtitle'),
                name: byId(AED + '-name'), isDefault: byId(AED + '-default'),
                amount: byId(AED + '-amount'), date: byId(AED + '-date'),
                reset: byId(AED + '-reset'), del: byId(AED + '-delete'), save: byId(AED + '-save')
            },
            transfer: {
                dialog: byId(TED), heading: byId(TED + '-heading'),
                from: byId(TED + '-from'), to: byId(TED + '-to'), amount: byId(TED + '-amount'),
                date: byId(TED + '-date'), memo: byId(TED + '-memo'), del: byId(TED + '-delete'), save: byId(TED + '-save')
            },
            recurring: {
                dialog: byId(RED), heading: byId(RED + '-heading'),
                from: byId(RED + '-from'), to: byId(RED + '-to'), amount: byId(RED + '-amount'),
                day: byId(RED + '-day'), memo: byId(RED + '-memo'), active: byId(RED + '-active'),
                del: byId(RED + '-delete'), save: byId(RED + '-save')
            }
        },
        settings = { submitting: false, accounts: [], today: '', editing: {account: null, transfer: null, recurring: null} },
        url = {
            summary: contextPath + '/ledger/dashboard/summary',
            list: contextPath + '/ledger/account/list',
            save: contextPath + '/ledger/account/save',
            del: contextPath + '/ledger/account/delete',
            transferSave: contextPath + '/ledger/account/transfer/save',
            transferDel: contextPath + '/ledger/account/transfer/delete',
            recurringSave: contextPath + '/ledger/account/recurring/save',
            recurringDel: contextPath + '/ledger/account/recurring/delete'
        },

        num = function(n){ return Math.abs(Number(n)).toLocaleString('ko-KR'); },
        signed = function(n){ return (Number(n) < 0 ? '−' : '') + num(n); },
        dotted = function(d){ return d.replace(/-/g, '.'); },

        // 기준 잔액 칸: 앞의 -(음수)와 숫자·쉼표만 서식 정리(settings.js 시작 잔액과 같은 규칙)
        signedNumber = function(n){ return (n < 0 ? '-' : '') + Math.abs(n).toLocaleString('ko-KR'); },

        parseSigned = function(s){
            var t = String(s).replace(/[,\s]/g, '').replace('−', '-');
            return /^-?\d{1,11}$/.test(t) ? Number(t) : null;
        },

        formatSigned = function(input){
            var t = input.value.replace(/[,\s]/g, '').replace('−', '-');
            if (t === '' || t === '-') {
                input.removeAttribute('aria-invalid');
            } else if (/^-?\d{1,11}$/.test(t)) {
                input.value = signedNumber(Number(t));
                input.removeAttribute('aria-invalid');
            } else {
                input.setAttribute('aria-invalid', 'true');
            }
        },

        checkedAdjust = function(){
            var el = document.querySelector('input[name="' + RED + '-adjust"]:checked');
            return el ? el.value : 'NONE';
        },

        init = function(){
            m$.accountNew.addEventListener('click', function(){ openAccount(null); });
            m$.transferNew.addEventListener('click', function(){ openTransfer(null); });
            m$.recurringNew.addEventListener('click', function(){ openRecurring(null); });
            m$.account.amount.addEventListener('input', function(){ formatSigned(m$.account.amount); });
            m$.account.reset.addEventListener('click', resetToNow);
            m$.account.isDefault.addEventListener('change', showDefault);
            App.bindAmountInput(m$.transfer.amount);
            App.bindAmountInput(m$.recurring.amount);
            [m$.account, m$.transfer, m$.recurring].forEach(function(ed){
                ed.dialog.querySelectorAll('[data-close]').forEach(function(b){
                    b.addEventListener('click', function(){ ed.dialog.close(); });
                });
            });
            m$.account.save.addEventListener('click', saveAccount);
            m$.account.del.addEventListener('click', removeAccount);
            m$.transfer.save.addEventListener('click', saveTransfer);
            m$.transfer.del.addEventListener('click', removeTransfer);
            m$.recurring.save.addEventListener('click', saveRecurring);
            m$.recurring.del.addEventListener('click', removeRecurring);
            reload();
        },

        // 기본 통장 잔액은 대시보드의 가계부 잔액에서 구하므로 두 응답이 모두 와야 그린다
        reload = function(){
            Promise.all([App.post(url.list), App.post(url.summary)])
                .then(function(res){
                    App.result(res[0], {ok: function(){
                        App.result(res[1], {ok: function(){ render(res[0].data, Number(res[1].data.cycleBalance)); }});
                    }});
                })
                .catch(function(){});
        },

        // 기본 통장 = 가계부 잔액 − 다른 통장 잔액. 그래서 통장 합계가 가계부 잔액과 같다
        render = function(data, ledgerBalance){
            var def = data.accounts.filter(function(a){ return a.isDefault; })[0];
            if (def) {
                def.balance = data.accounts.reduce(function(rest, a){
                    return a === def ? rest : rest - Number(a.balance);
                }, ledgerBalance);
            }
            settings.accounts = data.accounts;
            settings.today = data.today;
            renderMetrics(data.accounts, ledgerBalance, !!def);
            renderAccounts(data.accounts);
            renderRecurring(data.recurring);
            renderTransfers(data.transfers);
        },

        renderMetrics = function(accounts, ledgerBalance, hasDefault){
            var total = accounts.reduce(function(sum, a){ return sum + Number(a.balance); }, 0);
            m$.bankTotal.textContent = signed(total);
            m$.ledgerBalance.textContent = signed(ledgerBalance);
            m$.diff.textContent = accounts.length ? signed(total - ledgerBalance) : '—';
            m$.diffFoot.textContent = !accounts.length ? '통장을 추가해 주세요'
                : hasDefault ? '기본 통장이 나머지를 맡아 항상 0이에요'
                : '기본 통장을 정하면 0이 돼요';
        },

        emptyRow = function(cols, text){
            return App.h('tr', null, [App.h('td', {attrs: {colspan: cols}, text: text})]);
        },

        editButton = function(label, fn){
            return App.h('button', {type: 'button', className: 'button small edit-button', text: '수정',
                attrs: {'aria-label': label + ' 수정'}, on: {click: fn}});
        },

        renderAccounts = function(accounts){
            if (!accounts.length) {
                m$.accountBody.replaceChildren(emptyRow(4, '등록한 통장이 없어요. 월급 통장을 기본 통장으로 추가해 보세요.'));
                return;
            }
            m$.accountBody.replaceChildren.apply(m$.accountBody, accounts.map(function(a){
                var balance = Number(a.balance);
                return App.h('tr', null, [
                    App.h('th', {attrs: {scope: 'row'}}, [a.name].concat(a.isDefault
                        ? [' ', App.h('span', {className: 'chip', text: '기본 통장'})] : [])),
                    App.h('td', {attrs: {'data-label': '현재 잔액'}}, [
                        App.h('strong', {className: balance < 0 ? 'expense' : '', text: App.money(balance)})
                    ]),
                    App.h('td', {attrs: {'data-label': '기준'},
                        text: a.isDefault ? '가계부 잔액에서 계산' : dotted(a.baseDate) + ' · ' + App.money(a.baseBalance)}),
                    App.h('td', {attrs: {'data-label': '관리'}}, [editButton(a.name, function(){ openAccount(a); })])
                ]);
            }));
        },

        // "10.1(목)"
        dueText = function(d){
            if (!d) return '—';
            var p = d.split('-'),
                date = new Date(Number(p[0]), Number(p[1]) - 1, Number(p[2]));
            return Number(p[1]) + '.' + Number(p[2]) + '(' + '일월화수목금토'.charAt(date.getDay()) + ')';
        },

        route = function(item){
            return item.fromAccountName + ' → ' + item.toAccountName;
        },

        renderRecurring = function(items){
            if (!items.length) {
                m$.recurringBody.replaceChildren(emptyRow(5, '등록한 정기 이체가 없어요. 월급날 생활비 이체를 추가해 보세요.'));
                return;
            }
            m$.recurringBody.replaceChildren.apply(m$.recurringBody, items.map(function(item){
                return App.h('tr', null, [
                    App.h('th', {attrs: {scope: 'row'}}, [route(item)]
                        .concat(item.memo ? [App.h('small', {text: ' · ' + item.memo})] : [])
                        .concat(item.active ? [] : [' ', App.h('span', {className: 'chip neutral', text: '비활성'})])),
                    App.h('td', {attrs: {'data-label': '금액'}, text: App.money(item.amount)}),
                    App.h('td', {attrs: {'data-label': '매월 이체일'}, text: item.dayOfMonth + '일' + (ADJUST_LABEL[item.adjust] || '')}),
                    App.h('td', {attrs: {'data-label': '다음 이체일'}, text: dueText(item.nextDue)}),
                    App.h('td', {attrs: {'data-label': '관리'}}, [editButton(route(item), function(){ openRecurring(item); })])
                ]);
            }));
        },

        renderTransfers = function(transfers){
            if (!transfers.length) {
                m$.transferBody.replaceChildren(emptyRow(5, '기록한 이체가 없어요.'));
                return;
            }
            m$.transferBody.replaceChildren.apply(m$.transferBody, transfers.map(function(t){
                return App.h('tr', null, [
                    App.h('th', {attrs: {scope: 'row'}, text: dotted(t.transferDate)}),
                    App.h('td', {attrs: {'data-label': '보내는 → 받는 통장'}}, [route(t)]
                        .concat(t.recurring ? [' ', App.h('span', {className: 'chip neutral', text: '정기'})] : [])),
                    App.h('td', {attrs: {'data-label': '금액'}, text: App.money(t.amount)}),
                    App.h('td', {attrs: {'data-label': '메모'}, text: t.memo || '—'}),
                    App.h('td', {attrs: {'data-label': '관리'}}, [editButton(dotted(t.transferDate) + ' ' + route(t), function(){ openTransfer(t); })])
                ]);
            }));
        },

        fillAccounts = function(select, selectedId){
            select.replaceChildren.apply(select, settings.accounts.map(function(a){
                return App.h('option', {value: String(a.id), text: a.name, selected: a.id === selectedId});
            }));
        },

        // 기본 통장이면 기준 잔액 칸을 숨긴다
        showDefault = function(){
            var ed = m$.account;
            ed.dialog.querySelectorAll('[data-base]').forEach(function(el){ el.hidden = ed.isDefault.checked; });
            ed.dialog.querySelectorAll('[data-default]').forEach(function(el){ el.hidden = !ed.isDefault.checked; });
            ed.reset.hidden = ed.isDefault.checked || !settings.editing.account;
        },

        needAccounts = function(){
            if (settings.accounts.length >= 2) return false;
            _error('통장을 먼저 추가해 주세요', '이체하려면 통장이 두 개 이상 있어야 해요.');
            return true;
        },

        openAccount = function(a){
            var ed = m$.account;
            settings.editing.account = a;
            ed.heading.textContent = a ? '통장 수정' : '통장 추가';
            ed.subtitle.textContent = a ? a.name : '필요한 내용을 입력해 주세요.';
            ed.name.value = a ? a.name : '';
            // 첫 통장은 기본 통장으로 제안한다
            ed.isDefault.checked = a ? !!a.isDefault : !settings.accounts.length;
            // 수정할 때는 저장된 기준을 그대로 보여 준다. 기준 금액·날짜가 바뀌어야 기준 시각을 새로 잡는다.
            // 기본 통장은 기준이 없으므로, 기본을 풀 때를 위해 지금 계산한 잔액·오늘을 채워 둔다
            ed.amount.value = !a ? '' : signedNumber(Number(a.isDefault ? a.balance : a.baseBalance));
            ed.amount.removeAttribute('aria-invalid');
            ed.date.value = a && !a.isDefault ? a.baseDate : settings.today;
            ed.date.max = settings.today;
            showDefault();
            ed.del.hidden = !a;
            ed.dialog.showModal();
            ed.name.focus();
        },

        // 은행 앱과 비교하기 쉽게 지금 계산한 잔액과 오늘 날짜를 채우고 금액 칸으로 옮긴다
        resetToNow = function(){
            var ed = m$.account, a = settings.editing.account;
            if (!a) return;
            ed.amount.value = signedNumber(Number(a.balance));
            ed.amount.removeAttribute('aria-invalid');
            ed.date.value = settings.today;
            ed.amount.focus();
            ed.amount.select();
        },

        saveAccount = function(){
            var ed = m$.account,
                isDefault = ed.isDefault.checked,
                amount = isDefault ? 0 : ed.amount.value.trim() ? parseSigned(ed.amount.value) : 0;
            if (settings.submitting) return;
            if (!ed.name.value.trim()) {
                _error('알림', '통장 이름을 입력해주세요.');
                return;
            }
            if (amount === null) {
                _error('알림', '금액은 숫자만 입력해주세요. (음수는 앞에 -, 소수점 불가, 최대 11자리)');
                return;
            }
            if (!isDefault && (!ed.date.value || ed.date.value > settings.today)) {
                _error('알림', '기준일은 오늘까지의 날짜로 골라주세요.');
                return;
            }
            settings.submitting = true;
            App.post(url.save, {id: settings.editing.account ? settings.editing.account.id : '',
                name: ed.name.value.trim(), isDefault: isDefault, baseBalance: amount, baseDate: ed.date.value})
                .then(function(res){ App.result(res, {ok: function(){ done(ed); }}); })
                .catch(function(){})
                .then(function(){ settings.submitting = false; });
        },

        removeAccount = function(){
            var a = settings.editing.account;
            if (!a) return;
            _confirm('삭제할까요?', '‘' + a.name + '’ 통장을 삭제해요. 이 통장의 정기 이체도 함께 삭제돼요.', function(){
                remove(url.del, a.id, m$.account);
            });
        },

        openTransfer = function(t){
            var ed = m$.transfer;
            if (!t && needAccounts()) return;
            settings.editing.transfer = t;
            ed.heading.textContent = t ? '이체 수정' : '이체 기록';
            fillAccounts(ed.from, t ? t.fromAccountId : settings.accounts[0].id);
            fillAccounts(ed.to, t ? t.toAccountId : settings.accounts[1].id);
            ed.amount.value = t ? num(t.amount) : '';
            ed.amount.removeAttribute('aria-invalid');
            ed.date.value = t ? t.transferDate : settings.today;
            ed.memo.value = t && t.memo ? t.memo : '';
            ed.del.hidden = !t;
            ed.dialog.showModal();
            ed.amount.focus();
        },

        // 보내는·받는 통장과 금액 확인. 올바르면 null
        routeError = function(ed){
            if (ed.from.value === ed.to.value) return '보내는 통장과 받는 통장을 다르게 골라주세요.';
            return App.amountError(ed.amount.value, false);
        },

        saveTransfer = function(){
            var ed = m$.transfer,
                error = routeError(ed) || (!ed.date.value ? '날짜를 골라주세요.' : null);
            if (settings.submitting) return;
            if (error) {
                _error('알림', error);
                return;
            }
            settings.submitting = true;
            App.post(url.transferSave, {id: settings.editing.transfer ? settings.editing.transfer.id : '',
                transferDate: ed.date.value, fromAccountId: ed.from.value, toAccountId: ed.to.value,
                amount: App.parseAmount(ed.amount.value), memo: ed.memo.value.trim()})
                .then(function(res){ App.result(res, {ok: function(){ done(ed); }}); })
                .catch(function(){})
                .then(function(){ settings.submitting = false; });
        },

        removeTransfer = function(){
            var t = settings.editing.transfer;
            if (!t) return;
            _confirm('삭제할까요?', dotted(t.transferDate) + ' ' + route(t) + ' ' + App.money(t.amount) + ' 이체를 삭제해요.'
                    + (t.recurring ? '\n정기 이체가 만든 기록이라 이번 달에는 다시 만들지 않아요.' : ''), function(){
                remove(url.transferDel, t.id, m$.transfer);
            });
        },

        openRecurring = function(item){
            var ed = m$.recurring;
            if (!item && needAccounts()) return;
            settings.editing.recurring = item;
            ed.heading.textContent = item ? '정기 이체 수정' : '정기 이체 추가';
            fillAccounts(ed.from, item ? item.fromAccountId : settings.accounts[0].id);
            fillAccounts(ed.to, item ? item.toAccountId : settings.accounts[1].id);
            ed.amount.value = item ? num(item.amount) : '';
            ed.amount.removeAttribute('aria-invalid');
            ed.day.value = item ? item.dayOfMonth : '';
            document.querySelectorAll('input[name="' + RED + '-adjust"]').forEach(function(r){
                r.checked = r.value === (item ? item.adjust : 'NONE');
            });
            ed.memo.value = item && item.memo ? item.memo : '';
            ed.active.checked = item ? !!item.active : true;
            ed.del.hidden = !item;
            ed.dialog.showModal();
            ed.amount.focus();
        },

        saveRecurring = function(){
            var ed = m$.recurring,
                day = Number(ed.day.value),
                error = routeError(ed) || (!/^\d{1,2}$/.test(ed.day.value) || day < 1 || day > 31 ? '매월 이체일을 1~31일로 입력해주세요.' : null);
            if (settings.submitting) return;
            if (error) {
                _error('알림', error);
                return;
            }
            settings.submitting = true;
            App.post(url.recurringSave, {id: settings.editing.recurring ? settings.editing.recurring.id : '',
                fromAccountId: ed.from.value, toAccountId: ed.to.value, amount: App.parseAmount(ed.amount.value),
                dayOfMonth: day, adjust: checkedAdjust(), memo: ed.memo.value.trim(), active: ed.active.checked})
                .then(function(res){ App.result(res, {ok: function(){ done(ed); }}); })
                .catch(function(){})
                .then(function(){ settings.submitting = false; });
        },

        removeRecurring = function(){
            var item = settings.editing.recurring;
            if (!item) return;
            _confirm('삭제할까요?', route(item) + ' 정기 이체를 삭제해요. 이미 기록된 이체는 남아요.', function(){
                remove(url.recurringDel, item.id, m$.recurring);
            });
        },

        done = function(ed, title){
            ed.dialog.close();
            App.saved(title);
            reload();
        },

        remove = function(u, id, ed){
            if (settings.submitting) return;
            settings.submitting = true;
            var handlers = {ok: function(){ done(ed, '삭제했어요'); }};
            handlers[App.CODE.IN_USE] = function(res){ _error('삭제할 수 없어요', res.message); };
            App.post(u, {id: id})
                .then(function(res){ App.result(res, handlers); })
                .catch(function(){})
                .then(function(){ settings.submitting = false; });
        };

    return { init: init };
}());

document.addEventListener('DOMContentLoaded', function(){
    App.account.init();
});
