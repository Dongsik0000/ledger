App.asset = (function(){
    var ED = 'assetEditor',
        m$ = {
            newButton: document.getElementById('assetNew'),
            totalBalance: document.getElementById('totalBalance'),
            assetTotal: document.getElementById('assetTotal'),
            ledgerBalance: document.getElementById('ledgerBalance'),
            body: document.getElementById('assetBody'),
            marketStatus: document.getElementById('assetMarketStatus'),
            dialog: document.getElementById(ED),
            heading: document.getElementById(ED + '-heading'),
            subtitle: document.getElementById(ED + '-subtitle'),
            name: document.getElementById(ED + '-name'),
            amount: document.getElementById(ED + '-amount'),
            stock: document.getElementById(ED + '-stock'),
            symbol: document.getElementById(ED + '-symbol'),
            stockSearch: document.getElementById(ED + '-stock-search'),
            searchButton: document.getElementById(ED + '-stock-search-button'),
            searchStatus: document.getElementById(ED + '-stock-search-status'),
            searchResults: document.getElementById(ED + '-stock-search-results'),
            quantity: document.getElementById(ED + '-quantity'),
            del: document.getElementById(ED + '-delete'),
            save: document.getElementById(ED + '-save')
        },
        settings = { submitting: false, editing: null, ledgerBalance: null, assetTotal: null, searchToken: 0 },
        url = {
            summary: contextPath + '/ledger/dashboard/summary',
            list: contextPath + '/ledger/asset/list',
            search: contextPath + '/ledger/asset/search',
            save: contextPath + '/ledger/asset/save',
            del: contextPath + '/ledger/asset/delete'
        },

        num = function(n){ return Number(n).toLocaleString('ko-KR'); },
        signed = function(n){ return (n < 0 ? '−' : '') + num(Math.abs(n)); },

        init = function(){
            m$.newButton.addEventListener('click', function(){ open(null); });
            App.bindAmountInput(m$.amount);
            m$.stock.addEventListener('change', showAssetMode);
            m$.searchButton.addEventListener('click', searchStocks);
            m$.stockSearch.addEventListener('keydown', function(e){
                if (e.key === 'Enter') { e.preventDefault(); searchStocks(); }
            });
            m$.stockSearch.addEventListener('input', function(){
                settings.searchToken++;
                m$.searchButton.disabled = false;
                m$.symbol.value = '';
                m$.searchResults.replaceChildren();
                m$.searchStatus.textContent = '검색 결과에서 미국 상장 종목을 선택해 주세요.';
            });
            m$.symbol.addEventListener('input', function(){
                m$.searchStatus.textContent = '직접 입력한 종목 코드로 저장해요.';
                m$.searchResults.replaceChildren();
            });
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
            var stocks = data.assets.filter(function(a){ return !!a.stockSymbol; });
            m$.marketStatus.textContent = !stocks.length ? '' : !data.marketUpdated
                ? '시세를 갱신하지 못해 마지막 평가액을 표시해요. 기준일을 확인해 주세요.'
                : '미국 주식은 마지막 제공 시세 기준 평가액이에요. API 요금제에 따라 장중 가격과 다를 수 있어요.';
            settings.assetTotal = data.total;
            m$.assetTotal.textContent = num(data.total);
            renderTotal();
            if (!data.assets.length) {
                m$.body.replaceChildren(App.h('tr', null, [App.h('td', {attrs: {colspan: 4}, text: '기록한 자산이 없어요. 통장·적금 잔액을 추가해 보세요.'})]));
                return;
            }
            m$.body.replaceChildren.apply(m$.body, data.assets.map(function(a){
                return App.h('tr', null, [
                    App.h('th', {attrs: {scope: 'row'}}, a.stockSymbol
                        ? [a.name, App.h('small', {text: ' · ' + a.stockSymbol + ' ' + Number(a.stockQuantity).toLocaleString('ko-KR') + '주'})]
                        : [a.name]),
                    App.h('td', {attrs: {'data-label': '현재 잔액'}}, a.stockSymbol
                        ? [App.money(a.amount), App.h('small', {text: ' · $' + Number(a.stockPriceUsd).toFixed(2) + ' × ' + Number(a.stockQuantity).toLocaleString('ko-KR') + '주'})]
                        : [App.money(a.amount)]),
                    App.h('td', {attrs: {'data-label': '마지막 갱신일'}, text: a.stockSymbol
                        ? '주가 ' + (a.stockQuoteDate || '—').replace(/-/g, '.') + ' · 평가 ' + (a.stockValuedAt || '—').replace(/-/g, '.')
                        : a.updatedAt.replace(/-/g, '.')}),
                    App.h('td', {attrs: {'data-label': '관리'}}, [
                        App.h('button', {type: 'button', className: 'button small edit-button', text: '수정', attrs: {'aria-label': a.name + ' 수정'}, on: {click: function(){ open(a); }}})
                    ])
                ]);
            }));
        },

        showAssetMode = function(){
            var stock = m$.stock.checked;
            m$.dialog.querySelectorAll('[data-stock]').forEach(function(el){ el.hidden = !stock; });
            m$.dialog.querySelectorAll('[data-cash]').forEach(function(el){ el.hidden = stock; });
        },

        searchStocks = function(){
            if (m$.searchButton.disabled) return;
            var query = m$.stockSearch.value.trim();
            if (query.length < 2 || query.length > 60) {
                m$.searchStatus.textContent = '회사·ETF 영문명이나 종목 코드를 2~60자로 입력해 주세요.';
                return;
            }
            var token = ++settings.searchToken;
            m$.searchButton.disabled = true;
            m$.searchResults.replaceChildren();
            m$.searchStatus.textContent = '종목을 찾고 있어요…';
            App.post(url.search, {query: query})
                .then(function(res){
                    if (token !== settings.searchToken) return;
                    if (res.code !== App.CODE.SUCCESS) {
                        m$.searchStatus.textContent = '검색에 실패했어요. 종목 코드를 직접 입력할 수도 있어요.';
                        App.result(res);
                        return;
                    }
                    var matches = res.data || [];
                    m$.searchStatus.textContent = matches.length
                        ? '미국 상장 종목을 확인하고 하나를 선택해 주세요.'
                        : '미국 상장 종목을 찾지 못했어요. 영문명이나 종목 코드로 다시 검색해 주세요.';
                    m$.searchResults.replaceChildren.apply(m$.searchResults, matches.map(function(match){
                        return App.h('button', {type: 'button', className: 'button small',
                            on: {click: function(){
                                m$.symbol.value = match.symbol;
                                if (!m$.name.value.trim()) m$.name.value = match.name.slice(0, 50);
                                m$.searchResults.replaceChildren();
                                m$.searchStatus.textContent = match.name + ' (' + match.symbol + ') 선택됨 · 미국 상장, USD';
                                m$.quantity.focus();
                            }}}, [match.name + ' · ' + match.symbol,
                                App.h('small', {text: match.type === 'ETF' ? 'ETF · 미국 · USD' : '주식 · 미국 · USD'})]);
                    }));
                })
                .catch(function(){
                    if (token === settings.searchToken) m$.searchStatus.textContent = '검색에 실패했어요. 잠시 후 다시 시도해 주세요.';
                })
                .then(function(){ if (token === settings.searchToken) m$.searchButton.disabled = false; });
        },

        open = function(a){
            settings.editing = a;
            m$.heading.textContent = a ? '자산 수정' : '자산 추가';
            m$.subtitle.textContent = a ? a.name : '필요한 내용을 입력해 주세요.';
            m$.name.value = a ? a.name : '';
            m$.amount.value = a && !a.stockSymbol ? num(a.amount) : '';
            m$.stock.checked = !!(a && a.stockSymbol);
            m$.symbol.value = a && a.stockSymbol ? a.stockSymbol : '';
            m$.stockSearch.value = '';
            m$.searchResults.replaceChildren();
            m$.searchStatus.textContent = a && a.stockSymbol
                ? '현재 종목: ' + a.stockSymbol + ' · 다른 종목은 이름으로 검색해 선택해 주세요.'
                : '검색 결과에서 미국 상장 종목을 선택해 주세요.';
            settings.searchToken++;
            m$.searchButton.disabled = false;
            m$.quantity.value = a && a.stockQuantity ? Number(a.stockQuantity) : '';
            showAssetMode();
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
            if (m$.stock.checked) {
                if (!/^[A-Z.]{1,12}$/.test(m$.symbol.value.trim().toUpperCase())
                        || !/^[0-9]{1,12}(\.[0-9]{1,6})?$/.test(m$.quantity.value)
                        || Number(m$.quantity.value) <= 0) {
                    _error('알림', '종목 코드와 보유 수량을 확인해주세요.');
                    return;
                }
            } else if (!/^\d{1,11}$/.test(digits)) {
                _error('알림', '잔액을 0원 이상 숫자로 입력해주세요.');
                return;
            }
            settings.submitting = true;
            App.post(url.save, {id: settings.editing ? settings.editing.id : '', name: m$.name.value.trim(),
                stock: m$.stock.checked, amount: m$.stock.checked ? '' : digits,
                symbol: m$.stock.checked ? m$.symbol.value.trim().toUpperCase() : '',
                quantity: m$.stock.checked ? m$.quantity.value : ''})
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
