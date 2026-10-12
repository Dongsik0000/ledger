/* 거래 입력·표시 공용 도우미. 거래 내역(entry.js·entryBatch.js)과 대시보드(dashboard.js)가 같이 쓴다. */
App.entryForm = (function(){
	var LAST_ACCOUNT_KEY = 'ledger.lastAccountId',
		TITLE_LIST_ID = 'entryTitles',                    /* 내용 칸의 list 속성이 가리키는 datalist */
		titles = [],                                      /* 내용 자동 채우기 값(/ledger/entry/titles) */
		/* 카테고리 아이콘: 이름에 든 낱말로 고른다(사용자가 추가한 카테고리도). 앞에 있는 규칙이 우선, 없으면 book */
		ICON_RULES = [
			[/급여|월급|상여|성과급|수입|용돈/, 'wallet'],
			[/저축|적금|예금|투자|주식/, 'leaf'],
			[/카페|커피|간식/, 'coffee'],
			[/교통|택시|주차|주유|차량/, 'train'],
			[/장보기|생필품|쇼핑|의류|미용/, 'bag'],
			[/식비|배달|외식|식사/, 'food'],
			[/주거|월세|관리비|공과금|고정/, 'home'],
			[/문화|여가|데이트|여행|취미/, 'sun']
		],
		WARM = {'카페/간식': true},

		/* 카테고리 이름 → 아이콘 이름 */
		iconOf = function(name){
			for(var i = 0; i < ICON_RULES.length; i++){
				if(ICON_RULES[i][0].test(name || '')) return ICON_RULES[i][1];
			}
			return 'book';
		},

		/* 한 자리 수 앞에 0 */
		pad = function(n){ return (n < 10 ? '0' : '') + n; },

		/* 선택지: 활성 항목 + 이미 고른 항목(비활성이어도 기존 거래 수정이 되도록) */
		visible = function(list, selectedId){
			return list.filter(function(x){ return x.active || x.id === selectedId; });
		},

		/* label 로 감싼 라디오 하나 */
		radio = function(name, value, label, checked){
			return App.h('label', null, [
				App.h('input', {type: 'radio', name: name, value: String(value), checked: !!checked}),
				App.h('span', {text: label})
			]);
		},

		/* 구분에 맞는 카테고리 라디오. 고른 것이 없으면 첫 항목 */
		renderCategories = function(container, name, categories, type, selectedId){
			var list = visible(categories.filter(function(c){ return c.type === type; }), selectedId),
				sel = list.some(function(c){ return c.id === selectedId; }) ? selectedId : (list[0] && list[0].id);
			if(!list.length){
				container.replaceChildren(App.h('span', {className: 'form-note', text: '설정에서 카테고리를 추가해 주세요.'}));
				return;
			}
			container.replaceChildren.apply(container, list.map(function(c){
				return radio(name, c.id, c.name + (c.active ? '' : ' (숨김)'), c.id === sel);
			}));
		},

		/* 결제수단 라디오. "선택 안 함" 포함 */
		renderPayments = function(container, name, payments, selectedId){
			var items = [radio(name, '', '선택 안 함', selectedId === null || selectedId === undefined)];
			visible(payments, selectedId).forEach(function(p){
				items.push(radio(name, p.id, p.name + (p.active ? '' : ' (숨김)'), p.id === selectedId));
			});
			container.replaceChildren.apply(container, items);
		},

		/* 통장 선택지. 빈 값 = 기본 통장(없으면 "선택 안 함")으로 저장된다. 통장이 없으면 칸을 숨긴다(.field 안에 있을 때) */
		renderAccounts = function(select, accounts, selectedId){
			var def = accounts.filter(function(a){ return a.isDefault; })[0],
				options = [App.h('option', {value: '', text: def ? def.name + ' (기본 통장)' : '선택 안 함'})],
				field = select.closest('.field');
			accounts.forEach(function(a){
				if(!a.isDefault) options.push(App.h('option', {value: String(a.id), text: a.name}));
			});
			select.replaceChildren.apply(select, options);
			select.value = accounts.some(function(a){ return !a.isDefault && a.id === selectedId; }) ? String(selectedId) : '';
			if(field) field.hidden = !accounts.length;
		},

		/* 통장 선택 값의 통장(/ledger/account/balances 응답에서). 빈 값 = 기본 통장 */
		accountOf = function(balances, accountId){
			return balances.accounts.filter(function(a){
				return accountId ? a.id === Number(accountId) : a.isDefault;
			})[0] || null;
		},

		/* 새 거래 한 건이 통장 잔액에 주는 변화 {통장 id: 금액}. 통장 화면과 같은 규칙이다.
		   고른 통장(기본 통장 외)은 날짜가 그 통장의 countFrom ~ to 이면 바뀐다.
		   기본 통장 = 가계부 잔액 − 다른 통장이라, 가계부 잔액(시작 잔액 기준일 ~ to)이 바뀐 만큼에서 다른 통장 변화를 뺀 만큼 바뀐다 */
		balanceEffect = function(balances, accountId, type, amount, date){
			var signed = (type === 'INCOME' ? 1 : -1) * amount,
				def = accountOf(balances, ''),
				own = accountOf(balances, accountId),
				ledger = def && date <= balances.to && (!def.countFrom || date >= def.countFrom) ? signed : 0,
				mine = own && !own.isDefault && date <= balances.to && date >= own.countFrom ? signed : 0,
				effect = {};
			if(mine) effect[own.id] = mine;
			if(def && ledger !== mine) effect[def.id] = ledger - mine;
			return effect;
		},

		/* 통장 칸 아래 안내: "생활비 통장 현재 잔액 X원 → 저장 후 Y원". balances 가 없으면(불러오는 중) 비운다.
		   수정 중이면 그 거래가 이미 잔액에 들어 있으므로 현재 잔액만 보여 준다 */
		balanceHint = function(el, balances, accountId, type, amount, date, editing){
			var own = balances && accountOf(balances, accountId), now, delta;
			if(!own){
				el.textContent = '';
				return;
			}
			now = Number(own.balance);
			delta = !editing && amount && date ? balanceEffect(balances, accountId, type, amount, date)[own.id] || 0 : 0;
			el.textContent = own.name + ' 현재 잔액 ' + App.money(now)
				+ (delta ? ' → 저장 후 ' + App.money(now + delta)
					: !editing && amount && date ? ' · 이 날짜는 통장 잔액 계산 기간(기준일 ~ 이번 주기 끝) 밖이에요' : '');
		},

		/* 마지막으로 고른 통장(이 브라우저에만 저장). 저장소를 못 쓰는 환경이면 기억하지 않는다 */
		lastAccount = function(){
			try {
				var v = localStorage.getItem(LAST_ACCOUNT_KEY);
				return v ? Number(v) : null;
			} catch(e){
				return null;
			}
		},

		/* 마지막 통장 저장(빈 값이면 지운다) */
		rememberAccount = function(id){
			try {
				if(id) localStorage.setItem(LAST_ACCOUNT_KEY, String(id));
				else localStorage.removeItem(LAST_ACCOUNT_KEY);
			} catch(e){ /* 저장 불가 환경: 무시 */ }
		},

		/* 최근 내용 목록을 받아 datalist 로 그린다. 저장 뒤 다시 불러 방금 쓴 내용도 나오게 한다.
		   자동 채우기는 보조 기능이라 실패해도 알리지 않고 목록 없이 둔다 */
		loadTitles = function(){
			App.post(contextPath + '/ledger/entry/titles', null, {onError: function(){}})
				.then(function(res){
					if(!res || res.code !== App.CODE.SUCCESS) return;
					var list = document.getElementById(TITLE_LIST_ID) || document.body.appendChild(App.h('datalist', {id: TITLE_LIST_ID}));
					titles = res.data;
					list.replaceChildren.apply(list, titles.map(function(t){ return App.h('option', {value: t.title}); }));
				})
				.catch(function(){});
		},

		/* 내용이 정확히 같은 최근 거래 값. 없으면 null */
		suggestionFor = function(title){
			var t = String(title || '').trim();
			return t ? titles.filter(function(s){ return s.title === t; })[0] || null : null;
		},

		/* 라디오로 고르는 입력 창(거래 추가·빠른 입력)에 예전 값을 채운다. names: 구분·카테고리·결제수단 라디오 name.
		   구분 라디오에 change 를 보내 화면이 카테고리를 다시 그리게 한 뒤 고른다. 숨김·삭제된 항목은 선택지에 없어 건너뛴다.
		   금액은 비어 있을 때만 채운다(먼저 적은 금액을 덮어쓰지 않게) */
		fillFromSuggestion = function(s, root, names, account, amount){
			var pick = function(name, value){
				var el = root.querySelector('input[name="' + name + '"][value="' + value + '"]');
				if(el) el.checked = true;
				return el;
			};
			var type = pick(names.type, s.type);
			if(type) type.dispatchEvent(new Event('change', {bubbles: true}));
			pick(names.category, s.categoryId);
			pick(names.payment, s.paymentMethodId === null || s.paymentMethodId === undefined ? '' : s.paymentMethodId);
			setAccount(account, s.accountId);
			if(!amount.value.trim()){
				amount.value = Number(s.amount).toLocaleString('ko-KR');
				amount.dispatchEvent(new Event('input', {bubbles: true}));
			}
		},

		/* 통장 선택지에 그 통장이 있으면 고르고, 없으면(기본 통장·NULL) 빈 값 = 기본 통장 */
		setAccount = function(select, accountId){
			var v = accountId === null || accountId === undefined ? '' : String(accountId);
			select.value = Array.prototype.some.call(select.options, function(o){ return o.value === v; }) ? v : '';
			select.dispatchEvent(new Event('change', {bubbles: true}));
		},

		/* 그 name 에서 고른 라디오 값. 없으면 빈 문자열 */
		checked = function(name){
			var el = document.querySelector('input[name="' + name + '"]:checked');
			return el ? el.value : '';
		},

		/* 오늘 "YYYY-MM-DD"(브라우저 시간) */
		today = function(){
			var d = new Date();
			return d.getFullYear() + '-' + pad(d.getMonth() + 1) + '-' + pad(d.getDate());
		},

		/* "2026-10-08" -> "10월 8일" */
		dateLabel = function(d){
			var p = d.split('-');
			return Number(p[1]) + '월 ' + Number(p[2]) + '일';
		},

		/* 수입은 +, 지출은 − 를 붙인 금액 */
		signedMoney = function(type, amount){
			return (type === 'INCOME' ? '+' : '−') + App.money(amount);
		},

		/* 거래 한 줄. onEdit 이 있으면 수정 버튼 */
		row = function(e, onEdit){
			var info = [
				App.h('strong', {text: e.title}),
				App.h('small', {text: e.categoryName + (e.paymentMethodName ? ' · ' + e.paymentMethodName : '')
					+ (e.transferAssetName ? ' · → ' + e.transferAssetName : '')})
			];
			if(e.memo){
				info.push(App.h('span', {className: 'memo-tag', attrs: {title: e.memo}}, [App.icon('edit'), App.h('span', {text: e.memo})]));
			}
			var children = [
				App.h('span', {className: 'category-icon' + (WARM[e.categoryName] ? ' warm' : '')}, [App.icon(iconOf(e.categoryName))]),
				App.h('div', {className: 'transaction-info'}, info),
				App.h('strong', {className: 'amount ' + (e.type === 'INCOME' ? 'income' : 'expense'), text: signedMoney(e.type, e.amount)})
			];
			if(onEdit){
				children.push(App.h('button', {
					type: 'button', className: 'button small edit-button', text: '수정',
					attrs: {'aria-label': e.title + ' 수정'},
					on: {click: function(){ onEdit(e); }}
				}));
			}
			return App.h('div', {className: 'transaction'}, children);
		},

		/* 날짜별로 묶은 목록(날짜 줄 + 일 합계 + 거래들) */
		grouped = function(entries, onEdit){
			var groups = [], nodes = [];
			entries.forEach(function(e){
				var g = groups[groups.length - 1];
				if(!g || g.date !== e.entryDate){
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

		/* 빈 목록 안내 */
		empty = function(title, text){
			return App.h('div', {className: 'empty-state compact'}, [
				App.h('span', {className: 'empty-icon'}, [App.icon('book')]),
				App.h('strong', {text: title}),
				App.h('p', {text: text})
			]);
		};

	return{
		renderCategories: renderCategories,
		renderPayments: renderPayments,
		renderAccounts: renderAccounts,
		accountOf: accountOf,
		balanceEffect: balanceEffect,
		balanceHint: balanceHint,
		lastAccount: lastAccount,
		loadTitles: loadTitles,
		suggestionFor: suggestionFor,
		fillFromSuggestion: fillFromSuggestion,
		setAccount: setAccount,
		rememberAccount: rememberAccount,
		checked: checked,
		today: today,
		dateLabel: dateLabel,
		signedMoney: signedMoney,
		row: row,
		grouped: grouped,
		empty: empty
	};
}());
