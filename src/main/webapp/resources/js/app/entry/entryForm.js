/* 거래 입력·표시 공용 도우미. 거래 내역(entry.js·entryBatch.js)과 대시보드(dashboard.js)가 같이 쓴다. */
App.entryForm = (function(){
	var LAST_ACCOUNT_KEY = 'ledger.lastAccountId',
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

		/* 새 거래 한 건이 통장 잔액에 주는 변화 {통장 id: 금액}. 통장 화면과 같은 규칙이다. refund 면 반대 방향.
		   고른 통장(기본 통장 외)은 날짜가 그 통장의 countFrom ~ to 이면 바뀐다.
		   기본 통장 = 가계부 잔액 − 다른 통장이라, 가계부 잔액(시작 잔액 기준일 ~ to)이 바뀐 만큼에서 다른 통장 변화를 뺀 만큼 바뀐다 */
		balanceEffect = function(balances, accountId, type, amount, date, refund){
			var signed = signOf(type, refund) * amount,
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
		balanceHint = function(el, balances, accountId, type, amount, date, editing, refund){
			var own = balances && accountOf(balances, accountId), now, delta;
			if(!own){
				el.textContent = '';
				return;
			}
			now = Number(own.balance);
			delta = !editing && amount && date ? balanceEffect(balances, accountId, type, amount, date, refund)[own.id] || 0 : 0;
			el.textContent = own.name + ' 현재 잔액 ' + App.money(now)
				+ (delta ? ' → 저장 후 ' + App.money(now + delta)
					: !editing && amount && date ? ' · 이 날짜는 통장 잔액 계산 기간(기준일 ~ 이번 주기 끝) 밖이에요' : '');
		},

		/* 카테고리의 예산 항목(/ledger/dashboard/budgets 응답에서). 예산이 없거나 아직 못 받았으면 null */
		budgetOf = function(budgets, categoryId){
			return budgets && budgets.items.filter(function(b){ return b.id === Number(categoryId); })[0] || null;
		},

		/* 새 거래가 예산 사용액에 더하는 금액: 이번 주기(from~to) 지출만, 환불이면 뺀다 */
		budgetDelta = function(budgets, type, amount, date, refund){
			return type === 'EXPENSE' && amount && date >= budgets.from && date <= budgets.to ? (refund ? -amount : amount) : 0;
		},

		/* 사용률(%, 반올림) */
		percent = function(spent, budget){ return Math.round(spent / budget * 100); },

		/* 카테고리 아래 예산 안내: "식비 예산 300,000원 중 240,000원 (80%) → 저장 후 252,000원 (84%)". 80% 이상이면 경고 색.
		   수정 중이면 그 거래가 이미 사용액에 들어 있어 현재 사용액만 보여 준다. 예산이 없는 카테고리면 숨긴다 */
		budgetHint = function(el, budgets, categoryId, type, amount, date, editing, refund){
			var b = type === 'EXPENSE' ? budgetOf(budgets, categoryId) : null, now, after;
			el.hidden = !b;
			if(!b) return;
			now = Number(b.spent);
			after = now + (editing ? 0 : budgetDelta(budgets, type, amount, date, refund));
			el.textContent = b.name + ' 예산 ' + App.money(b.budget) + ' 중 ' + App.money(now) + ' (' + percent(now, b.budget) + '%)'
				+ (after !== now ? ' → 저장 후 ' + App.money(after) + ' (' + percent(after, b.budget) + '%)'
					: !editing && amount && date ? ' · 이 날짜는 이번 주기 밖이라 예산에 들어가지 않아요' : '');
			el.classList.toggle('expense', after >= b.budget * 0.8);
		},

		/* 저장한 새 지출이 예산의 80%를 처음 넘기거나, 예산을 다 쓰거나 넘으면 알릴 문구 {title, desc}. 없으면 null */
		budgetAlert = function(budgets, categoryId, type, amount, date, refund){
			var b = type === 'EXPENSE' ? budgetOf(budgets, categoryId) : null, budget, before, after;
			if(!b) return null;
			budget = Number(b.budget);
			before = Number(b.spent);
			after = before + budgetDelta(budgets, type, amount, date, refund);
			if(after <= before) return null;   /* 환불·주기 밖 */
			if(after > budget) return {title: b.name + ' 예산을 넘었어요', desc: '이번 주기 ' + App.money(after) + ' 사용 · ' + App.money(after - budget) + ' 초과'};
			if(after === budget) return {title: b.name + ' 예산을 다 썼어요', desc: '이번 주기 예산 ' + App.money(budget) + '을 모두 썼어요.'};
			if(before < budget * 0.8 && after >= budget * 0.8) return {title: b.name + ' 예산의 ' + percent(after, budget) + '%를 썼어요', desc: '이번 주기에 ' + App.money(budget - after) + ' 남았어요.'};
			return null;
		},

		/* 대시보드 예산 막대: 이름 · 사용액/예산 · 막대(80% 이상 주의, 100% 이상 초과) · 남은(넘은) 금액 */
		budgetBars = function(budgets){
			return budgets.items.map(function(b){
				var spent = Number(b.spent), budget = Number(b.budget), rate = spent / budget,
					fill = App.h('span');
				fill.style.width = Math.max(0, Math.min(100, rate * 100)) + '%';
				return App.h('li', {className: 'limit-item' + (rate >= 1 ? ' is-over' : rate >= 0.8 ? ' is-warn' : '')}, [
					App.h('div', {className: 'limit-head'}, [
						App.h('strong', {text: b.name}),
						App.h('span', {text: App.money(spent) + ' / ' + App.money(budget) + ' (' + percent(spent, budget) + '%)'})
					]),
					App.h('div', {className: 'limit-bar', attrs: {'aria-hidden': 'true'}}, [fill]),
					App.h('small', {text: spent > budget ? App.money(spent - budget) + ' 초과' : App.money(budget - spent) + ' 남음'})
				]);
			});
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

		/* 그 name 에서 고른 라디오 값. 없으면 빈 문자열 */
		checked = function(name){
			var el = document.querySelector('input[name="' + name + '"]:checked');
			return el ? el.value : '';
		},

		/* 오늘 "YYYY-MM-DD"(브라우저 시간) */
		today = function(){
			var d = new Date();
			return d.getFullYear() + '-' + App.pad(d.getMonth() + 1) + '-' + App.pad(d.getDate());
		},

		/* "2026-10-08" -> "10월 8일" */
		dateLabel = function(d){
			var p = d.split('-');
			return Number(p[1]) + '월 ' + Number(p[2]) + '일';
		},

		/* 잔액에 주는 방향: 수입 +1, 지출 −1. 환불·반납이면 반대(지출 환불은 들어온 돈, 수입 반납은 나간 돈) */
		signOf = function(type, refund){
			return (type === 'INCOME') !== !!refund ? 1 : -1;
		},

		/* 들어온 돈은 +, 나간 돈은 − 를 붙인 금액 */
		signedMoney = function(type, amount, refund){
			return (signOf(type, refund) > 0 ? '+' : '−') + App.money(amount);
		},

		/* 금액 칸. 통장 필터로 조회해 balance 가 있으면(값이 null 이어도) 그 아래에 거래 후 잔액 */
		amountCell = function(e, income, text){
			var children = [text];
			if('balance' in e){
				children.push(App.h('small', {
					text: e.balance === null ? '잔액 —' : '잔액 ' + App.money(e.balance),
					attrs: e.balance === null ? {title: '통장 기준일 전이거나 이번 주기 뒤라 잔액을 알 수 없어요'} : null
				}));
			}
			return App.h('strong', {className: 'amount ' + (income ? 'income' : 'expense')}, children);
		},

		/* 통장 간 이체 한 줄(통장 필터 조회에서만 온다). 수정은 통장 화면에서 */
		transferRow = function(t){
			return App.h('div', {className: 'transaction'}, [
				App.h('span', {className: 'category-icon'}, [App.icon('bank')]),
				App.h('div', {className: 'transaction-info'}, [
					App.h('strong', {text: t.fromAccountName + ' → ' + t.toAccountName}),
					App.h('small', {text: '이체' + (t.memo ? ' · ' + t.memo : '')})
				]),
				amountCell(t, t.amount > 0, (t.amount > 0 ? '+' : '−') + App.money(Math.abs(t.amount))),
				App.h('a', {className: 'button small edit-button', text: '통장', href: contextPath + '/ledger/account',
					attrs: {'aria-label': '이체는 통장 화면에서 수정'}})
			]);
		},

		/* 거래 한 줄. onEdit 이 있으면 수정 버튼 */
		row = function(e, onEdit){
			var info = [
				App.h('strong', {text: e.title}),
				App.h('small', {text: e.categoryName + (e.refund ? (e.type === 'INCOME' ? ' · 반납' : ' · 환불') : '')
					+ (e.paymentMethodName ? ' · ' + e.paymentMethodName : '')
					+ (e.transferAssetName ? ' · → ' + e.transferAssetName : '')})
			];
			if(e.memo){
				info.push(App.h('span', {className: 'memo-tag', attrs: {title: e.memo}}, [App.icon('edit'), App.h('span', {text: e.memo})]));
			}
			var children = [
				App.h('span', {className: 'category-icon' + (WARM[e.categoryName] ? ' warm' : '')}, [App.icon(iconOf(e.categoryName))]),
				App.h('div', {className: 'transaction-info'}, info),
				amountCell(e, signOf(e.type, e.refund) > 0, signedMoney(e.type, e.amount, e.refund))
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

		/* 날짜별로 묶은 목록(날짜 줄 + 일 합계 + 거래들). 이체는 수입·지출이 아니라 일 합계에 넣지 않는다 */
		grouped = function(entries, onEdit){
			var groups = [], nodes = [];
			entries.forEach(function(e){
				var g = groups[groups.length - 1];
				if(!g || g.date !== e.entryDate){
					g = {date: e.entryDate, items: [], total: 0};
					groups.push(g);
				}
				g.items.push(e);
				if(e.kind !== 'TRANSFER') g.total += signOf(e.type, e.refund) * e.amount;
			});
			groups.forEach(function(g){
				nodes.push(App.h('div', {className: 'date-label'}, [
					App.h('time', {attrs: {datetime: g.date}, text: dateLabel(g.date)}),
					App.h('span', null, ['일 합계 ', App.h('strong', {
						className: g.total >= 0 ? 'income' : 'expense',
						text: (g.total >= 0 ? '+' : '−') + App.money(Math.abs(g.total))
					})])
				]));
				g.items.forEach(function(e){ nodes.push(e.kind === 'TRANSFER' ? transferRow(e) : row(e, onEdit)); });
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
		budgetHint: budgetHint,
		budgetAlert: budgetAlert,
		budgetBars: budgetBars,
		lastAccount: lastAccount,
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
