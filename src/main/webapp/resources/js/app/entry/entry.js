App.entry = (function(){
	var ED = 'entryEditor',
		m$ = {
			newButton: document.getElementById('entryNew'),  /* 거래 추가 버튼 */
			batchButton: document.getElementById('entryBatchOpen'),  /* 여러 건 입력 버튼 */
			monthTitle: document.getElementById('monthTitle'),  /* 목록 제목(월·주기) */
			monthRange: document.getElementById('monthRange'),  /* 조회 기간 설명 */
			prev: document.getElementById('prevMonth'),  /* 이전 달·주기 버튼 */
			next: document.getElementById('nextMonth'),  /* 다음 달·주기 버튼 */
			month: document.getElementById('month'),  /* 조회 월 */
			keyword: document.getElementById('filterKeyword'),  /* 검색어 */
			type: document.getElementById('filterType'),  /* 구분 필터 */
			category: document.getElementById('filterCategory'),  /* 카테고리 필터 */
			payment: document.getElementById('filterPayment'),  /* 결제수단 필터 */
			reset: document.getElementById('filterReset'),  /* 필터 초기화 버튼 */
			search: document.getElementById('filterSearch'),  /* 조회 버튼 */
			count: document.getElementById('listCount'),  /* 건수 */
			totals: document.getElementById('listTotals'),  /* 수입·지출·차이 합계 */
			truncated: document.getElementById('listTruncated'),  /* 500건까지만 보인다는 안내 */
			period: document.getElementById('period'),  /* 기간 선택(달력 월·주기·최근 12개월·전체) */
			monthField: document.getElementById('monthField'),  /* 조회 월 칸(달력 월일 때만) */
			list: document.getElementById('entryList'),  /* 거래 목록 */
			dialog: document.getElementById(ED),  /* 거래 추가·수정 창 */
			heading: document.getElementById(ED + '-heading'),  /* 창 제목 */
			subtitle: document.getElementById(ED + '-subtitle'),  /* 창 부제목 */
			amount: document.getElementById(ED + '-amount'),  /* 금액 */
			categories: document.getElementById(ED + '-categories'),  /* 카테고리 라디오 묶음 */
			title: document.getElementById(ED + '-title'),  /* 내용 */
			payments: document.getElementById(ED + '-payments'),  /* 결제수단 라디오 묶음 */
			account: document.getElementById(ED + '-account'),  /* 통장 선택 */
			balance: document.getElementById(ED + '-balance'),  /* 통장 잔액 안내 */
			transferField: document.getElementById(ED + '-transfer-field'),  /* 적금·예금 이체 칸 */
			transferAsset: document.getElementById(ED + '-transfer-asset'),  /* 이체할 자산 선택 */
			date: document.getElementById(ED + '-date'),  /* 날짜 */
			memoBox: document.getElementById(ED + '-memoBox'),  /* 메모 펼침 */
			memo: document.getElementById(ED + '-memo'),  /* 메모 */
			del: document.getElementById(ED + '-delete'),  /* 삭제 버튼 */
			save: document.getElementById(ED + '-save')  /* 저장 버튼 */
		},

		settings = {
			submitting: false,
			month: '',                                        /* yyyy-MM */
			master: {categories: [], paymentMethods: []},
			editing: null,                                    /* 수정 중인 거래(추가면 null) */
			assetLoadToken: 0,
			balances: null                                    /* 통장 잔액(창을 열 때마다 새로 받는다) */
		},

		url = {
			master: contextPath + '/ledger/settings/master',
			cashAssets: contextPath + '/ledger/asset/cash-list',
			balances: contextPath + '/ledger/account/balances',
			list: contextPath + '/ledger/entry/list',
			save: contextPath + '/ledger/entry/save',
			del: contextPath + '/ledger/entry/delete'
		},

		/* 조회 조건을 주소에서 읽고, 선택지를 받은 뒤 목록을 그린다 */
		init = function(){
			/* 조회 조건은 주소(?period=&month=&q=&type=&category=&payment=)에 남겨 새로고침·뒤로 가기에도 유지한다 */
			var q = new URLSearchParams(location.search);
			bindEvent();
			settings.month = /^\d{4}-(0[1-9]|1[0-2])$/.test(q.get('month') || '') ? q.get('month') : App.entryForm.today().slice(0, 7);
			m$.month.value = settings.month;
			m$.period.value = ['CYCLE', 'RECENT12', 'ALL'].indexOf(q.get('period')) >= 0 ? q.get('period') : 'MONTH';
			/* 주기 보기: 이 날짜가 속한 주기(없으면 오늘) */
			settings.cycleDate = /^\d{4}-\d{2}-\d{2}$/.test(q.get('date') || '') ? q.get('date') : '';
			showPeriod();
			App.entryForm.loadTitles();
			m$.keyword.value = q.get('q') || '';
			m$.type.value = ['INCOME', 'EXPENSE'].indexOf(q.get('type')) >= 0 ? q.get('type') : '';
			settings.editId = q.get('edit');

			loadMaster().then(function(){
				/* 선택지에 없는 값(지운 항목 등)이면 select 가 빈 값으로 남는다 */
				m$.category.value = q.get('category') || '';
				m$.payment.value = q.get('payment') || '';
				list();
			});
		},

		/* 화면 이벤트 연결 */
		bindEvent = function(){
			m$.period.addEventListener('change', function(){
				showPeriod();
				list();
			});
			m$.prev.addEventListener('click', function(){ moveMonth(-1); });
			m$.next.addEventListener('click', function(){ moveMonth(1); });
			m$.month.addEventListener('change', function(){
				if(m$.month.value){
					settings.month = m$.month.value;
					list();
				}
			});
			m$.search.addEventListener('click', list);
			m$.keyword.addEventListener('keydown', function(e){
				if(e.key === 'Enter') list();
			});
			m$.reset.addEventListener('click', function(){
				m$.keyword.value = '';
				m$.type.value = '';
				m$.category.value = '';
				m$.payment.value = '';
				list();
			});

			m$.newButton.addEventListener('click', openNew);
			m$.batchButton.addEventListener('click', function(){ App.entryBatch.open(settings.master, list); });
			App.bindAmountInput(m$.amount);
			m$.dialog.querySelectorAll('input[name="' + ED + '-type"]').forEach(function(r){
				r.addEventListener('change', function(){
					App.entryForm.renderCategories(m$.categories, ED + '-category', settings.master.categories, r.value, null);
					showTransfer();
					showBalance();
				});
			});
			m$.amount.addEventListener('input', showBalance);
			m$.account.addEventListener('change', showBalance);
			/* 새 거래의 내용이 예전 거래와 같으면 그 구분·카테고리·결제수단·통장(금액은 비었을 때만)을 채운다. 수정 중에는 건드리지 않는다 */
			m$.title.addEventListener('change', function(){
				var s = !settings.editing && App.entryForm.suggestionFor(m$.title.value);
				if(s) App.entryForm.fillFromSuggestion(s, m$.dialog,
					{type: ED + '-type', category: ED + '-category', payment: ED + '-payment'}, m$.account, m$.amount);
			});
			m$.date.addEventListener('change', showBalance);
			m$.dialog.querySelectorAll('[data-close]').forEach(function(b){
				b.addEventListener('click', requestClose);
			});
			/* Escape 로 닫을 때도 작성 중인 내용이 있으면 먼저 묻는다. cancel 이벤트를 막는 것만으로는 */
			/* Chrome 이 연속된 Escape 에서 창을 그냥 닫으므로(close watcher), 키 입력 단계에서 닫기 요청 자체를 막는다. */
			/* (확인창 안의 Escape 는 확인창이 전파를 끊으므로 여기로 오지 않는다) */
			m$.dialog.addEventListener('keydown', function(e){
				if(e.key === 'Escape'){
					e.preventDefault();
					requestClose();
				}
			});
			m$.dialog.addEventListener('cancel', function(e){   /* 휴대폰 뒤로 가기 등 키 입력이 아닌 닫기 요청 */
				e.preventDefault();
				requestClose();
			});
			/* 금액·내용·메모에서 Enter(휴대폰 키보드의 완료)로 저장. 한글 조합 중 Enter 는 조합 완료로 둔다 */
			[m$.amount, m$.title, m$.memo].forEach(function(el){
				el.setAttribute('enterkeyhint', 'done');
				el.addEventListener('keydown', function(e){
					if(e.key === 'Enter' && !e.isComposing){
						e.preventDefault();
						save();
					}
				});
			});
			m$.save.addEventListener('click', save);
			m$.del.addEventListener('click', remove);
		},

		/* 달력 월일 때만 이전·다음 달과 조회 월을 보인다 */
		/* 이전·다음은 달력 월(한 달씩)·주기(한 주기씩)에서, 조회 월 칸은 달력 월에서만 보인다 */
		showPeriod = function(){
			var period = m$.period.value;
			m$.prev.hidden = m$.next.hidden = period !== 'MONTH' && period !== 'CYCLE';
			m$.monthField.hidden = period !== 'MONTH';
			m$.prev.setAttribute('aria-label', period === 'CYCLE' ? '이전 주기' : '이전 달');
			m$.next.setAttribute('aria-label', period === 'CYCLE' ? '다음 주기' : '다음 달');
		},

		/* "2026-09-23" 에서 n일 옮긴 날짜 */
		shiftDate = function(iso, n){
			var d = new Date(iso + 'T00:00:00');
			d.setDate(d.getDate() + n);
			return d.getFullYear() + '-' + String(d.getMonth() + 1).padStart(2, '0') + '-' + String(d.getDate()).padStart(2, '0');
		},

		/* 조회 조건을 주소에 남긴다(새로고침·뒤로 가기에도 유지) */
		saveQuery = function(){
			var q = new URLSearchParams({month: settings.month});
			if(m$.period.value !== 'MONTH') q.set('period', m$.period.value);
			if(m$.period.value === 'CYCLE' && settings.cycleDate) q.set('date', settings.cycleDate);
			if(m$.keyword.value.trim()) q.set('q', m$.keyword.value.trim());
			if(m$.type.value) q.set('type', m$.type.value);
			if(m$.category.value) q.set('category', m$.category.value);
			if(m$.payment.value) q.set('payment', m$.payment.value);
			history.replaceState(null, '', location.pathname + '?' + q.toString());
		},

		/* 카테고리·결제수단·통장 선택지 받기 */
		loadMaster = function(){
			return App.post(url.master)
				.then(handleMasterResult)
				.catch(function(){});
		},

		/* 선택지를 저장하고 필터를 채운다 */
		handleMasterResult = function(res){
			App.result(res, {ok: function(){
				settings.master = res.data;
				fillFilters();
			}});
		},

		/* 카테고리·결제수단 필터 선택지 */
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

		/* 이전·다음 달(주기 보기면 이전·다음 주기) */
		moveMonth = function(delta){
			if(m$.period.value === 'CYCLE'){
				/* 이전 주기는 시작일 전날, 다음 주기는 종료일 다음 날이 속한 주기 */
				if(!settings.cycle) return;
				settings.cycleDate = delta < 0 ? shiftDate(settings.cycle.from, -1) : shiftDate(settings.cycle.to, 1);
				list();
				return;
			}
			var p = settings.month.split('-'),
				d = new Date(Number(p[0]), Number(p[1]) - 1 + delta, 1);
			settings.month = d.getFullYear() + '-' + (d.getMonth() < 9 ? '0' : '') + (d.getMonth() + 1);
			m$.month.value = settings.month;
			list();
		},

		/* 검색어나 필터를 하나라도 썼는지 */
		filtered = function(){
			return m$.keyword.value.trim() || m$.type.value || m$.category.value || m$.payment.value;
		},

		/* 조회 조건으로 목록 요청 */
		list = function(){
			var p = settings.month.split('-'),
				lastDay = new Date(Number(p[0]), Number(p[1]), 0).getDate();
			if(m$.period.value === 'MONTH'){
				m$.monthTitle.textContent = p[0] + '년 ' + Number(p[1]) + '월';
				m$.monthRange.textContent = '달력 월 기준 · ' + Number(p[1]) + '월 1일~' + lastDay + '일';
			} else if(m$.period.value === 'CYCLE'){
				m$.monthTitle.textContent = '주기';        /* 응답을 받으면 "2026년 9월 주기"와 기간으로 바꾼다 */
				m$.monthRange.textContent = '';
			} else {
				m$.monthTitle.textContent = m$.period.value === 'ALL' ? '전체 기간' : '최근 12개월';
				m$.monthRange.textContent = '';
			}
			saveQuery();

			App.post(url.list, {
				period: m$.period.value,
				month: settings.month,
				date: m$.period.value === 'CYCLE' ? settings.cycleDate : '',
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

		/* 합계·목록 그리기. ?edit= 로 들어왔으면 그 거래의 수정 창을 연다 */
		render = function(data){
			var rows = data.rows, t = data.totals, net = t.income - t.expense;
			if(m$.period.value === 'RECENT12' && data.from){
				m$.monthRange.textContent = data.from.split('-').join('.') + '부터';
			}
			if(m$.period.value === 'CYCLE'){
				var cm = data.cycleMonth.split('-');
				settings.cycle = {from: data.from, to: data.to};
				m$.monthTitle.textContent = cm[0] + '년 ' + Number(cm[1]) + '월 주기';
				m$.monthRange.textContent = '주기 기준 · ' + data.from.split('-').join('.') + ' ~ ' + data.to.slice(5).split('-').join('.')
					+ (data.holidayMissing ? ' · 이 해의 공휴일이 없어 주말만 반영' : '');
			}
			/* 합계: 목록이 잘려도 조건에 맞는 전체 거래 기준 */
			m$.totals.replaceChildren(
				'수입 ', App.h('b', {className: 'income', text: (t.income ? '+' : '') + App.money(t.income)}),
				' · 지출 ', App.h('b', {className: 'expense', text: (t.expense ? '−' : '') + App.money(t.expense)}),
				' · 차이 ', App.h('b', {text: (net > 0 ? '+' : '') + App.money(net)}));
			m$.truncated.hidden = !data.truncated;
			m$.count.textContent = data.truncated ? t.count + '건 중 ' + rows.length + '건' : rows.length + '건';
			if(!rows.length){
				m$.list.replaceChildren(filtered()
					? App.entryForm.empty('조건에 맞는 거래가 없어요', '검색어나 필터를 바꿔 보세요.')
					: App.entryForm.empty((m$.period.value === 'MONTH' ? '이 달' : '이 기간') + '의 첫 기록을 남겨볼까요?', '작은 지출부터 하나씩 기록하면 나의 소비가 보이기 시작해요.'));
				return;
			}
			m$.list.replaceChildren.apply(m$.list, App.entryForm.grouped(rows, openEdit));
			/* 대시보드 최근 기록에서 "수정"으로 들어온 경우(?edit=id) 그 거래의 수정 창을 한 번 연다 */
			if(settings.editId){
				var target = rows.filter(function(r){ return String(r.id) === settings.editId; })[0];
				settings.editId = null;
				if(target) openEdit(target);
			}
		},

		/* 구분 라디오 고르기 */
		setType = function(type){
			m$.dialog.querySelector('input[name="' + ED + '-type"][value="' + type + '"]').checked = true;
		},

		/* 지출일 때만 적금·예금 이체 칸을 보인다 */
		showTransfer = function(){
			var expense = App.entryForm.checked(ED + '-type') === 'EXPENSE';
			m$.transferField.hidden = !expense;
			if(!expense) m$.transferAsset.value = '';
		},

		/* 통장 잔액 안내 갱신 */
		showBalance = function(){
			App.entryForm.balanceHint(m$.balance, settings.balances, m$.account.value, App.entryForm.checked(ED + '-type'),
				App.parseAmount(m$.amount.value), m$.date.value, !!settings.editing);
		},

		/* 잔액 안내는 보조 정보라 실패해도 창을 막지 않고 안내 칸에 적는다 */
		loadBalances = function(){
			settings.balances = null;
			m$.balance.textContent = '';
			App.post(url.balances, null, {onError: function(){ m$.balance.textContent = '통장 잔액을 불러오지 못했어요.'; }})
				.then(function(res){
					App.result(res, {ok: function(){
						settings.balances = res.data;
						showBalance();
					}});
				})
				.catch(function(){});
		},

		/* 이체할 자산 선택지 받기. 늦게 도착한 이전 응답은 버린다 */
		loadTransferAssets = function(selectedId){
			var token = ++settings.assetLoadToken;
			m$.save.disabled = true;
			m$.transferAsset.disabled = true;
			m$.transferAsset.replaceChildren(App.h('option', {value: '', text: '자산 불러오는 중…'}));
			App.post(url.cashAssets)
				.then(function(res){
					if(token !== settings.assetLoadToken) return;
					App.result(res, {ok: function(){
						var assets = res.data || [],
							options = [App.h('option', {value: '', text: assets.length ? '연결 안 함' : '연결할 자산이 없어요 · 자산 화면에서 추가해 주세요'})];
						assets.forEach(function(a){ options.push(App.h('option', {value: String(a.id), text: a.name})); });
						m$.transferAsset.replaceChildren.apply(m$.transferAsset, options);
						m$.transferAsset.value = selectedId ? String(selectedId) : '';
						m$.transferAsset.disabled = false;
						m$.save.disabled = false;
						/* 비동기 목록 표시만으로 작성 중 변경 경고가 생기지 않도록 초기 선택값만 갱신 */
						var baseline = JSON.parse(settings.snapshot);
						baseline[baseline.length - 1] = m$.transferAsset.value;
						settings.snapshot = JSON.stringify(baseline);
					}});
				})
				.catch(function(){
					if(token === settings.assetLoadToken){
						m$.transferAsset.replaceChildren(App.h('option', {value: '', text: '자산 목록을 불러오지 못했어요'}));
					}
				});
		},

		/* 거래 창 열기. e 가 있으면 수정, 없으면 추가 */
		open = function(e){
			settings.editing = e;
			m$.heading.textContent = e ? '거래 수정' : '거래 추가';
			m$.subtitle.textContent = e ? e.title : '필요한 내용을 입력해 주세요.';
			m$.amount.value = e ? Number(e.amount).toLocaleString('ko-KR') : '';
			setType(e ? e.type : 'EXPENSE');
			App.entryForm.renderCategories(m$.categories, ED + '-category', settings.master.categories, e ? e.type : 'EXPENSE', e ? e.categoryId : null);
			m$.title.value = e ? e.title : '';
			App.entryForm.renderPayments(m$.payments, ED + '-payment', settings.master.paymentMethods, e ? e.paymentMethodId : null);
			App.entryForm.renderAccounts(m$.account, settings.master.accounts || [], e ? e.accountId : App.entryForm.lastAccount());
			m$.date.value = e ? e.entryDate
				: (settings.month === App.entryForm.today().slice(0, 7) ? App.entryForm.today() : settings.month + '-01');
			m$.memo.value = e && e.memo ? e.memo : '';
			m$.memoBox.open = !!(e && e.memo);
			showTransfer();
			m$.del.hidden = !e;
			settings.snapshot = snapshot();
			loadTransferAssets(e ? e.transferAssetId : null);
			loadBalances();
			m$.dialog.showModal();
			m$.amount.focus();
		},

		/* 창에 입력된 값 전체(열었을 때와 비교해 작성 중인지 판단) */
		snapshot = function(){
			return JSON.stringify([m$.amount.value, App.entryForm.checked(ED + '-type'), App.entryForm.checked(ED + '-category'),
				m$.title.value, App.entryForm.checked(ED + '-payment'), m$.account.value, m$.date.value, m$.memo.value,
				m$.transferAsset.value]);
		},

		/* 작성 중이면 버릴지 물은 뒤 닫는다 */
		requestClose = function(){
			if(snapshot() === settings.snapshot){
				m$.dialog.close();
				return;
			}
			_confirm('작성 중인 내용을 버릴까요?', '저장하지 않은 입력은 사라져요.', function(){ m$.dialog.close(); });
		},

		/* 새 거래 */
		openNew = function(){ open(null); },

		/* 거래 수정 */
		openEdit = function(e){ open(e); },

		/* 입력 확인 후 저장 요청 */
		save = function(){
			if(settings.submitting) return;
			var amount = App.parseAmount(m$.amount.value),
				categoryId = App.entryForm.checked(ED + '-category'),
				title = m$.title.value.trim();
			if(!amount){
				_error('알림', App.amountError(m$.amount.value));
				return;
			}
			if(!categoryId){
				_error('알림', '카테고리를 골라주세요.');
				return;
			}
			if(!title){
				_error('알림', '내용을 입력해주세요.');
				return;
			}
			if(!m$.date.value){
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
				transferAssetId: m$.transferField.hidden ? '' : m$.transferAsset.value,
				accountId: m$.account.value,
				memo: m$.memo.value.trim()
			})
				.then(handleSaveResult)
				.catch(function(){})
				.then(function(){ settings.submitting = false; });
		},

		/* 저장 성공: 통장 기억, 창 닫기, 목록 새로고침 */
		handleSaveResult = function(res){
			App.result(res, {ok: function(){
				App.entryForm.rememberAccount(m$.account.value);
				m$.dialog.close();
				App.saved();
				App.entryForm.loadTitles();
				list();
			}});
		},

		/* 삭제 확인 후 요청 */
		remove = function(){
			var e = settings.editing;
			if(!e) return;
			_confirm('삭제할까요?', '‘' + e.title + '’ 거래를 삭제해요.', function(){
				if(settings.submitting) return;
				settings.submitting = true;
				App.post(url.del, {id: e.id})
					.then(handleRemoveResult)
					.catch(function(){})
					.then(function(){ settings.submitting = false; });
			});
		},

		/* 삭제 성공: 창 닫기, 목록 새로고침 */
		handleRemoveResult = function(res){
			App.result(res, {ok: function(){
				m$.dialog.close();
				App.saved('삭제했어요');
				list();
			}});
		};

	return{
		init: init
	};
}());

document.addEventListener('DOMContentLoaded', function(){
	App.entry.init();
});
