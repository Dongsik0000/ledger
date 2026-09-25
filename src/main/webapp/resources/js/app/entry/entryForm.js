// 거래 입력·표시 공용 도우미. 거래 내역(entry.js)과 대시보드(dashboard.js)가 같이 쓴다.
App.entryForm = (function(){
    var ICONS = {'식비': 'food', '배달': 'food', '카페/간식': 'coffee', '장보기': 'bag', '교통': 'train', '고정지출': 'home', '저축': 'leaf', '급여': 'wallet'},
        WARM = {'카페/간식': true},

        pad = function(n){ return (n < 10 ? '0' : '') + n; },

        // 선택지: 활성 항목 + 이미 고른 항목(비활성이어도 기존 거래 수정이 되도록)
        visible = function(list, selectedId){
            return list.filter(function(x){ return x.active || x.id === selectedId; });
        },

        radio = function(name, value, label, checked){
            return App.h('label', null, [
                App.h('input', {type: 'radio', name: name, value: String(value), checked: !!checked}),
                App.h('span', {text: label})
            ]);
        },

        // 구분에 맞는 카테고리 라디오. 고른 것이 없으면 첫 항목
        renderCategories = function(container, name, categories, type, selectedId){
            var list = visible(categories.filter(function(c){ return c.type === type; }), selectedId),
                sel = list.some(function(c){ return c.id === selectedId; }) ? selectedId : (list[0] && list[0].id);
            if (!list.length) {
                container.replaceChildren(App.h('span', {className: 'form-note', text: '설정에서 카테고리를 추가해 주세요.'}));
                return;
            }
            container.replaceChildren.apply(container, list.map(function(c){
                return radio(name, c.id, c.name + (c.active ? '' : ' (숨김)'), c.id === sel);
            }));
        },

        // 결제수단 라디오. "선택 안 함" 포함
        renderPayments = function(container, name, payments, selectedId){
            var items = [radio(name, '', '선택 안 함', selectedId === null || selectedId === undefined)];
            visible(payments, selectedId).forEach(function(p){
                items.push(radio(name, p.id, p.name + (p.active ? '' : ' (숨김)'), p.id === selectedId));
            });
            container.replaceChildren.apply(container, items);
        },

        checked = function(name){
            var el = document.querySelector('input[name="' + name + '"]:checked');
            return el ? el.value : '';
        },

        today = function(){
            var d = new Date();
            return d.getFullYear() + '-' + pad(d.getMonth() + 1) + '-' + pad(d.getDate());
        },

        // "2026-10-08" -> "10월 8일"
        dateLabel = function(d){
            var p = d.split('-');
            return Number(p[1]) + '월 ' + Number(p[2]) + '일';
        },

        signedMoney = function(type, amount){
            return (type === 'INCOME' ? '+' : '−') + App.money(amount);
        },

        // 거래 한 줄. onEdit 이 있으면 수정 버튼
        row = function(e, onEdit){
            var info = [
                App.h('strong', {text: e.title}),
                App.h('small', {text: e.categoryName + (e.paymentMethodName ? ' · ' + e.paymentMethodName : '')})
            ];
            if (e.memo) {
                info.push(App.h('span', {className: 'memo-tag', attrs: {title: e.memo}}, [App.icon('edit'), App.h('span', {text: e.memo})]));
            }
            var children = [
                App.h('span', {className: 'category-icon' + (WARM[e.categoryName] ? ' warm' : '')}, [App.icon(ICONS[e.categoryName] || 'book')]),
                App.h('div', {className: 'transaction-info'}, info),
                App.h('strong', {className: 'amount ' + (e.type === 'INCOME' ? 'income' : 'expense'), text: signedMoney(e.type, e.amount)})
            ];
            if (onEdit) {
                children.push(App.h('button', {
                    type: 'button', className: 'button small edit-button', text: '수정',
                    attrs: {'aria-label': e.title + ' 수정'},
                    on: {click: function(){ onEdit(e); }}
                }));
            }
            return App.h('div', {className: 'transaction'}, children);
        },

        // 날짜별로 묶은 목록(날짜 줄 + 일 합계 + 거래들)
        grouped = function(entries, onEdit){
            var groups = [], nodes = [];
            entries.forEach(function(e){
                var g = groups[groups.length - 1];
                if (!g || g.date !== e.entryDate) {
                    g = {date: e.entryDate, items: [], total: 0};
                    groups.push(g);
                }
                g.items.push(e);
                g.total += (e.type === 'INCOME' ? 1 : -1) * e.amount;
            });
            groups.forEach(function(g){
                nodes.push(App.h('div', {className: 'date-label'}, [
                    App.h('time', {attrs: {datetime: g.date}, text: dateLabel(g.date)}),
                    App.h('span', null, ['일 합계 ', App.h('strong', {
                        className: g.total >= 0 ? 'income' : 'expense',
                        text: (g.total >= 0 ? '+' : '−') + App.money(Math.abs(g.total))
                    })])
                ]));
                g.items.forEach(function(e){ nodes.push(row(e, onEdit)); });
            });
            return nodes;
        },

        empty = function(title, text){
            return App.h('div', {className: 'empty-state compact'}, [
                App.h('span', {className: 'empty-icon'}, [App.icon('book')]),
                App.h('strong', {text: title}),
                App.h('p', {text: text})
            ]);
        };

    return {
        renderCategories: renderCategories,
        renderPayments: renderPayments,
        checked: checked,
        today: today,
        dateLabel: dateLabel,
        signedMoney: signedMoney,
        row: row,
        grouped: grouped,
        empty: empty
    };
}());
