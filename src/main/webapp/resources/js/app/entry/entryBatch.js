/* 여러 건 입력: 줄마다 그 통장의 거래 후 잔액을 이어서 보여 줘 은행 앱 거래내역과 한 줄씩 맞춰 볼 수 있다.
   거래 내역 화면(entry.js)이 open(master, onSaved) 로 연다. 저장은 한 번에, 하나라도 틀리면 아무것도 저장하지 않는다(서버도 같다) */
App.entryBatch = (function(){
	var ID = 'entryBatch', MAX = 50,
		m$ = {
			dialog: document.getElementById(ID),
			body: document.getElementById(ID + '-body'),
			add: document.getElementById(ID + '-add'),
			summary: document.getElementById(ID + '-summary'),
			save: document.getElementById(ID + '-save')
		},

		settings = {
			submitting: false,
			master: {categories: [], paymentMethods: [], accounts: []},
			balances: null,                                   /* 통장 잔액(창을 열 때마다 새로 받는다) */
			balanceError: false,
			rows: [],                                         /* 줄마다 입력 요소 묶음 */
			onSaved: null
		},

		url = {
			balances: contextPath + '/ledger/account/balances',
			save: contextPath + '/ledger/entry/save-batch'
		},

		init = function(){
			bindEvent();
		},

		bindEvent = function(){
			m$.add.addEventListener('click', function(){
				var r = addRow();
				if(r) r.title.focus();
			});
			m$.save.addEventListener('click', save);
			/* 줄 안의 어떤 입력이 바뀌어도 잔액을 처음부터 다시 센다 */
			m$.body.addEventListener('input', recompute);
			m$.body.addEventListener('change', recompute);
			App.bindDialog(m$.dialog, isDirty, function(){});
		},

		open = function(master, onSaved){
			settings.master = master;
			settings.onSaved = onSaved;
			settings.rows = [];
			m$.body.replaceChildren();
			addRow();
			loadBalances();
			m$.dialog.showModal();
			settings.rows[0].title.focus();
		},

		/* 잔액은 보조 정보라 실패해도 입력·저장은 막지 않고 요약 칸에 알린다 */
		loadBalances = function(){
			settings.balances = null;
			settings.balanceError = false;
			recompute();
			App.post(url.balances, null, {onError: function(){
				settings.balanceError = true;
				recompute();
			}})
				.then(function(res){
					App.result(res, {ok: function(){
						settings.balances = res.data;
						recompute();
					}});
				})
				.catch(function(){});
		},

		option = function(value, text, selected){
			return App.h('option', {value: String(value), text: text, selected: !!selected});
		},

		/* 구분에 맞는 활성 카테고리. 고른 것이 없으면 첫 항목 */
		fillCategories = function(r, selectedId){
			var list = settings.master.categories.filter(function(c){ return c.active && c.type === r.type.value; });
			r.category.replaceChildren.apply(r.category, list.length
				? list.map(function(c){ return option(c.id, c.name, String(c.id) === String(selectedId)); })
				: [option('', '설정에서 카테고리를 추가해 주세요')]);
		},

		/* 한 줄. prev(윗줄)가 있으면 날짜·구분·카테고리·결제수단·통장을 이어받는다 */
		newRow = function(prev){
			var r = {
				no: App.h('span'),
				date: App.h('input', {type: 'date', value: prev ? prev.date.value : App.entryForm.today(), attrs: {'aria-label': '날짜'}}),
				type: App.h('select', {attrs: {'aria-label': '구분'}}, [
					option('EXPENSE', '지출', !prev || prev.type.value === 'EXPENSE'),
					option('INCOME', '수입', prev && prev.type.value === 'INCOME')
				]),
				category: App.h('select', {attrs: {'aria-label': '카테고리'}}),
				title: App.h('input', {type: 'text', maxLength: 100, placeholder: '예: 점심', attrs: {'aria-label': '내용', enterkeyhint: 'next'}}),
				amount: App.h('input', {type: 'text', inputMode: 'numeric', autocomplete: 'off', placeholder: '0', attrs: {'aria-label': '금액 (원)', enterkeyhint: 'next'}}),
				payment: App.h('select', {attrs: {'aria-label': '결제수단'}}),
				account: App.h('select', {attrs: {'aria-label': '통장'}}),
				balance: App.h('span', {className: 'batch-balance'})
			};
			fillCategories(r, prev ? prev.category.value : null);
			r.payment.replaceChildren.apply(r.payment, [option('', '선택 안 함')].concat(
				settings.master.paymentMethods.filter(function(p){ return p.active; }).map(function(p){
					return option(p.id, p.name, prev && prev.payment.value === String(p.id));
				})));
			App.entryForm.renderAccounts(r.account, settings.master.accounts || [],
				prev ? Number(prev.account.value) || null : App.entryForm.lastAccount());
			App.bindAmountInput(r.amount);
			r.type.addEventListener('change', function(){ fillCategories(r, null); });
			/* 마지막 줄의 내용·금액에서 Enter 면 줄을 더하고, 아니면 다음 줄로. 한글 조합 중 Enter 는 조합 완료로 둔다 */
			[r.title, r.amount].forEach(function(el){
				el.addEventListener('keydown', function(e){
					if(e.key !== 'Enter' || e.isComposing) return;
					e.preventDefault();
					var i = settings.rows.indexOf(r),
						next = i === settings.rows.length - 1 ? addRow() : settings.rows[i + 1];
					if(next) next.title.focus();
				});
			});
			r.tr = App.h('tr', null, [
				App.h('th', {attrs: {scope: 'row'}}, [r.no]),
				App.h('td', {attrs: {'data-label': '날짜', 'data-cell': 'date'}}, [r.date]),
				App.h('td', {attrs: {'data-label': '구분', 'data-cell': 'type'}}, [r.type]),
				App.h('td', {attrs: {'data-label': '카테고리', 'data-cell': 'category'}}, [r.category]),
				App.h('td', {attrs: {'data-label': '내용', 'data-cell': 'title'}}, [r.title]),
				App.h('td', {attrs: {'data-label': '금액 (원)', 'data-cell': 'amount'}}, [r.amount]),
				App.h('td', {attrs: {'data-label': '결제수단', 'data-cell': 'payment'}}, [r.payment]),
				App.h('td', {attrs: {'data-label': '통장', 'data-cell': 'account'}}, [r.account]),
				App.h('td', {attrs: {'data-label': '거래 후 잔액'}}, [r.balance]),
				App.h('td', {attrs: {'data-label': '관리'}}, [App.h('button', {
					type: 'button', className: 'button small', text: '빼기',
					on: {click: function(){ removeRow(r); }}
				})])
			]);
			return r;
		},

		addRow = function(){
			if(settings.rows.length >= MAX){
				_error('알림', '한 번에 ' + MAX + '건까지 입력할 수 있어요.');
				return null;
			}
			var r = newRow(settings.rows[settings.rows.length - 1]);
			settings.rows.push(r);
			m$.body.appendChild(r.tr);
			recompute();
			return r;
		},

		/* 마지막 한 줄은 지우지 않고 비운다 */
		removeRow = function(r){
			if(settings.rows.length === 1){
				r.title.value = '';
				r.amount.value = '';
				recompute();
				return;
			}
			settings.rows.splice(settings.rows.indexOf(r), 1);
			r.tr.remove();
			recompute();
		},

		blank = function(r){
			return !r.title.value.trim() && !r.amount.value.trim();
		},

		isDirty = function(){
			return settings.rows.some(function(r){ return !blank(r); });
		},

		/* 통장별 잔액을 위에서부터 이어서 센다. 날짜가 잔액 계산 기간 밖인 줄은 그 통장 잔액을 바꾸지 않는다 */
		recompute = function(){
			var b = settings.balances, running = {}, count = 0, income = 0, expense = 0, lines = [];
			if(b) b.accounts.forEach(function(a){ running[a.id] = Number(a.balance); });
			settings.rows.forEach(function(r, i){
				var amount = App.parseAmount(r.amount.value),
					own = b && App.entryForm.accountOf(b, r.account.value),
					effect;
				r.no.textContent = (i + 1) + '번째';
				r.balance.classList.remove('expense');
				if(!amount || !r.date.value){
					r.balance.textContent = '—';
					return;
				}
				count++;
				if(r.type.value === 'INCOME') income += amount; else expense += amount;
				if(!b){
					r.balance.textContent = settings.balanceError ? '—' : '불러오는 중';
					return;
				}
				effect = App.entryForm.balanceEffect(b, r.account.value, r.type.value, amount, r.date.value);
				Object.keys(effect).forEach(function(id){ running[id] += effect[id]; });
				if(!own){
					r.balance.textContent = '—';
				} else if(effect[own.id]){
					r.balance.textContent = App.money(running[own.id]);
					r.balance.classList.toggle('expense', running[own.id] < 0);
				} else {
					r.balance.textContent = '기간 밖 · 반영 안 됨';
				}
			});

			lines.push(App.h('strong', {text: count + '건 · 지출 ' + App.money(expense) + ' · 수입 ' + App.money(income)}));
			if(settings.balanceError){
				lines.push(App.h('span', {className: 'expense', text: '통장 잔액을 불러오지 못했어요. 저장은 할 수 있어요.'}));
			} else if(b){
				b.accounts.forEach(function(a){
					var before = Number(a.balance), after = running[a.id];
					if(after !== before){
						lines.push(App.h('span', {text: a.name + ' ' + App.money(before) + ' → ' + App.money(after)
							+ ' (' + (after > before ? '+' : '') + App.money(after - before) + ')'}));
					}
				});
			}
			m$.summary.replaceChildren.apply(m$.summary, lines);
		},

		save = function(){
			if(settings.submitting) return;
			/* 비운 줄을 먼저 빼서 화면 번호와 서버가 알려 주는 "N번째 줄"이 같게 한다 */
			settings.rows.filter(blank).forEach(function(r){
				if(settings.rows.length > 1) removeRow(r);
			});
			if(settings.rows.length === 1 && blank(settings.rows[0])){
				_error('알림', '저장할 거래를 입력해주세요.');
				return;
			}
			var rows = [], error = null;
			settings.rows.forEach(function(r, i){
				var title = r.title.value.trim(),
					amount = App.parseAmount(r.amount.value),
					message = !amount ? App.amountError(r.amount.value)
						: !r.category.value ? '카테고리를 골라주세요.'
						: !title ? '내용을 입력해주세요.'
						: !r.date.value ? '날짜를 입력해주세요.'
						: null;
				if(message && !error) error = (i + 1) + '번째 줄: ' + message;
				rows.push({
					entryDate: r.date.value,
					type: r.type.value,
					categoryId: r.category.value,
					title: title,
					amount: amount,
					paymentMethodId: r.payment.value,
					accountId: r.account.value
				});
			});
			if(error){
				_error('알림', error);
				return;
			}

			settings.submitting = true;
			App.post(url.save, {rows: rows})
				.then(handleSaveResult)
				.catch(function(){})
				.then(function(){ settings.submitting = false; });
		},

		handleSaveResult = function(res){
			App.result(res, {ok: function(){
				App.entryForm.rememberAccount(settings.rows[settings.rows.length - 1].account.value);
				m$.dialog.close();
				App.saved(res.data.count + '건 저장했어요');
				if(settings.onSaved) settings.onSaved();
			}});
		};

	return{
		init: init,
		open: open
	};
}());

document.addEventListener('DOMContentLoaded', function(){
	App.entryBatch.init();
});
