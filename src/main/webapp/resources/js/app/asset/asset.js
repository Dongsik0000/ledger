App.asset = (function(){
    var ED = 'assetEditor',
        m$ = {
            newButton: document.getElementById('assetNew'),
            totalBalance: document.getElementById('totalBalance'),
            assetTotal: document.getElementById('assetTotal'),
            ledgerBalance: document.getElementById('ledgerBalance'),
            body: document.getElementById('assetBody'),
            dialog: document.getElementById(ED),
            heading: document.getElementById(ED + '-heading'),
            subtitle: document.getElementById(ED + '-subtitle'),
            name: document.getElementById(ED + '-name'),
            amount: document.getElementById(ED + '-amount'),
            del: document.getElementById(ED + '-delete'),
            save: document.getElementById(ED + '-save')
        },
        settings = { submitting: false, editing: null, ledgerBalance: null, assetTotal: null },
        url = {
            summary: contextPath + '/ledger/dashboard/summary',
            list: contextPath + '/ledger/asset/list',
            save: contextPath + '/ledger/asset/save',
            del: contextPath + '/ledger/asset/delete'
        },

        num = function(n){ return Number(n).toLocaleString('ko-KR'); },
        signed = function(n){ return (n < 0 ? '−' : '') + num(Math.abs(n)); },

        init = function(){
            m$.newButton.addEventListener('click', function(){ open(null); });
            App.bindAmountInput(m$.amount);
            m$.dialog.querySelectorAll('[data-close]').forEach(function(b){
                b.addEventListener('click', function(){ m$.dialog.close(); });
            });
            m$.save.addEventListener('click', save);
            m$.del.addEventListener('click', remove);
            reload();
        },

        reload = function(){
            App.post(url.summary)
                .then(function(res){
                    App.result(res, {ok: function(){
                        settings.ledgerBalance = res.data.cycleBalance;
                        m$.ledgerBalance.textContent = signed(res.data.cycleBalance);
                        renderTotal();
                    }});
                })
                .catch(function(){});
            App.post(url.list)
                .then(function(res){
                    App.result(res, {ok: function(){ render(res.data); }});
                })
                .catch(function(){});
        },

        // 두 요청이 모두 도착해야 총 잔액을 그린다
        renderTotal = function(){
            if (settings.ledgerBalance === null || settings.assetTotal === null) return;
            m$.totalBalance.textContent = signed(settings.ledgerBalance + settings.assetTotal);
        },

        render = function(data){
            settings.assetTotal = data.total;
            m$.assetTotal.textContent = num(data.total);
            renderTotal();
            if (!data.assets.length) {
                m$.body.replaceChildren(App.h('tr', null, [App.h('td', {attrs: {colspan: 4}, text: '기록한 자산이 없어요. 통장·적금 잔액을 추가해 보세요.'})]));
                return;
            }
            m$.body.replaceChildren.apply(m$.body, data.assets.map(function(a){
                return App.h('tr', null, [
                    App.h('th', {attrs: {scope: 'row'}, text: a.name}),
                    App.h('td', {attrs: {'data-label': '현재 잔액'}, text: App.money(a.amount)}),
                    App.h('td', {attrs: {'data-label': '마지막 갱신일'}, text: a.updatedAt.replace(/-/g, '.')}),
                    App.h('td', {attrs: {'data-label': '관리'}}, [
                        App.h('button', {type: 'button', className: 'button small edit-button', text: '수정', attrs: {'aria-label': a.name + ' 수정'}, on: {click: function(){ open(a); }}})
                    ])
                ]);
            }));
        },

        open = function(a){
            settings.editing = a;
            m$.heading.textContent = a ? '자산 수정' : '자산 추가';
            m$.subtitle.textContent = a ? a.name : '필요한 내용을 입력해 주세요.';
            m$.name.value = a ? a.name : '';
            m$.amount.value = a ? num(a.amount) : '';
            m$.del.hidden = !a;
            m$.dialog.showModal();
            m$.name.focus();
        },

        save = function(){
            if (settings.submitting) return;
            var digits = m$.amount.value.replace(/[,\s]/g, '');
            if (!m$.name.value.trim()) {
                _error('알림', '자산 이름을 입력해주세요.');
                return;
            }
            if (!/^\d{1,11}$/.test(digits)) {
                _error('알림', '잔액을 0원 이상 숫자로 입력해주세요.');
                return;
            }
            settings.submitting = true;
            App.post(url.save, {id: settings.editing ? settings.editing.id : '', name: m$.name.value.trim(), amount: digits})
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
            var a = settings.editing;
            if (!a) return;
            _confirm('삭제할까요?', '‘' + a.name + '’ 자산을 삭제해요.', function(){
                if (settings.submitting) return;
                settings.submitting = true;
                App.post(url.del, {id: a.id})
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
    App.asset.init();
});
