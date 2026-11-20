App.dashboard = (function(){
	var LAST_PAYMENT_KEY = 'ledger.lastPaymentMethodId',
		DOW = '일월화수목금토',
		m$ = {
			cycleRange: document.getElementById('cycleRange'),  /* 이번 주기 기간 */
			cycleBar: document.getElementById('cycleBar'),  /* 주기 진행 막대 */
			daysLeft: document.getElementById('daysLeft'),  /* 남은 일수 */
			dailyBudget: document.getElementById('dailyBudget'),  /* 오늘 쓸 수 있는 돈 */
			pendingFixed: document.getElementById('pendingFixed'),  /* 아직 나가지 않은 고정지출 */
			todayLabel: document.getElementById('todayLabel'),  /* 기준일 */
			carryOver: document.getElementById('carryOver'),  /* 이월 */
			cycleIncome: document.getElementById('cycleIncome'),  /* 주기 수입 */
			cycleExpense: document.getElementById('cycleExpense'),  /* 주기 지출 */
			cycleBalance: document.getElementById('cycleBalance'),  /* 주기 잔액 */
			totalBalance: document.getElementById('totalBalance'),  /* 총 잔액(가계부 + 자산) */
			totalDetail: document.getElementById('totalDetail'),  /* 총 잔액 내역 */
			formula: document.getElementById('formula'),  /* 오늘 쓸 수 있는 돈 계산식 */
			mascot: document.getElementById('mascot'),  /* 잎새 캐릭터 */
			mascotLine: document.getElementById('mascotLine'),  /* 캐릭터 말풍선 */
			week: document.getElementById('weekStrip'),  /* 이번 주 7일 */
			weekTotal: document.getElementById('weekTotal'),  /* 이번 주 쓴 돈 */
			upcoming: document.getElementById('upcomingList'),  /* 다가오는 고정 항목 */
			dialog: document.getElementById('quickDialog'),  /* 빠른 기록 창 */
			amount: document.getElementById('quickAmount'),  /* 빠른 입력: 금액 */
			categories: document.getElementById('quickCategories'),  /* 카테고리 라디오 묶음 */
			budget: document.getElementById('quickBudget'),  /* 카테고리 예산 안내 */
			limitList: document.getElementById('limitList'),  /* 예산 막대 목록 */
			title: document.getElementById('quickTitle'),  /* 내용 */
			payments: document.getElementById('quickPayments'),  /* 결제수단 라디오 묶음 */
			account: document.getElementById('quickAccount'),  /* 통장 선택 */
			balance: document.getElementById('quickBalance'),  /* 통장 잔액 안내 */
			optional: document.getElementById('quickOptional'),  /* 날짜·메모 펼침 */
			date: document.getElementById('quickDate'),  /* 날짜 */
			memo: document.getElementById('quickMemo'),  /* 메모 */
			save: document.getElementById('quickSave'),  /* 기록하기 버튼 */
			form: document.getElementById('quickForm'),  /* 빠른 입력 폼 */
			jump: document.getElementById('quickJump'),  /* 머리의 "기록하기" 버튼 */
			recent: document.getElementById('recentList')  /* 최근 기록 목록 */
		},

		settings = {
			submitting: false,
			master: {categories: [], paymentMethods: []},
			balances: null,
			budgets: null,
			summary: null,     /* /ledger/dashboard/summary 응답(하루 예산 기준) */
			weekRows: null,    /* 이번 주 거래(이체 제외) */
			hop: false         /* 저장 직후 캐릭터가 한 번 뛴다 */
		},

		url = {
			summary: contextPath + '/ledger/dashboard/summary',
			recent: contextPath + '/ledger/dashboard/recent',
			master: contextPath + '/ledger/settings/master',
			balances: contextPath + '/ledger/account/balances',
			budgets: contextPath + '/ledger/dashboard/budgets',
			entryList: contextPath + '/ledger/entry/list',
			recurringList: contextPath + '/ledger/recurring/list',
			save: contextPath + '/ledger/entry/save'
		},

		/* 날짜 칸에 오늘을 넣고 선택지·잔액·요약·이번 주·고정 항목·최근 기록을 불러온다 */
		init = function(){
			bindEvent();
			m$.date.value = App.entryForm.today();
			m$.mascot.replaceChildren(App.leaf('happy'));

			loadMaster();
			loadBalances();
			loadBudgets();
			loadSummary();
			loadWeek();
			loadUpcoming();
			loadRecent();
		},

		/* 화면 이벤트 연결 */
		bindEvent = function(){
			App.bindAmountInput(m$.amount);
			document.querySelectorAll('input[name="quickType"]').forEach(function(r){
				r.addEventListener('change', function(){
					App.entryForm.renderCategories(m$.categories, 'quickCategory', settings.master.categories, r.value, null);
					showBalance();
					showBudget();
				});
			});
			m$.amount.addEventListener('input', showBalance);
			m$.amount.addEventListener('input', showBudget);
			m$.categories.addEventListener('change', showBudget);
			m$.account.addEventListener('change', showBalance);
			m$.date.addEventListener('change', showBalance);
			m$.date.addEventListener('change', showBudget);
			/* Enter·휴대폰 키보드의 완료로도 저장된다 */
			m$.form.addEventListener('submit', function(e){
				e.preventDefault();
				save();
			});
			m$.jump.addEventListener('click', openQuick);
			/* 휴대폰 아래 + : 이 화면에서는 기록 화면으로 가지 않고 빠른 기록 창을 연다 */
			document.addEventListener('click', function(e){
				var add = e.target.closest ? e.target.closest('[data-quick-add]') : null;
				if(!add) return;
				e.preventDefault();
				openQuick();
			});
			/* 닫기(×·닫기·Escape·뒤로 가기): 작성 중이면 버릴지 먼저 묻는다 */
			App.bindDialog(m$.dialog, isDirty, resetQuick);
		},

		/* 빠른 기록 창 열기 */
		openQuick = function(){
			if(m$.dialog.open) return;
			m$.dialog.showModal();
			m$.amount.focus();
		},

		/* 작성 중인지(금액·내용·메모 중 하나라도 적었는지) */
		isDirty = function(){
			return !!(m$.amount.value.trim() || m$.title.value.trim() || m$.memo.value.trim());
		},

		/* 입력 칸 비우기(구분·카테고리·결제수단·통장 선택은 다음 기록을 위해 남긴다) */
		resetQuick = function(){
			m$.amount.value = '';
			m$.amount.removeAttribute('aria-invalid');
			m$.title.value = '';
			m$.memo.value = '';
			m$.date.value = App.entryForm.today();
			m$.optional.open = false;
			showBalance();
			showBudget();
		},

		/* 마지막으로 쓴 결제수단(이 브라우저에만 저장). 저장소를 못 쓰는 환경이면 기억하지 않는다 */
		lastPayment = function(){
			try {
				var v = localStorage.getItem(LAST_PAYMENT_KEY);
				return v ? Number(v) : null;
			} catch(e){
				return null;
			}
		},
		/* 마지막 결제수단 저장(이 브라우저에만, 빈 값이면 지운다) */
		rememberPayment = function(id){
			try {
				if(id) localStorage.setItem(LAST_PAYMENT_KEY, String(id));
				else localStorage.removeItem(LAST_PAYMENT_KEY);
			} catch(e){ /* 저장 불가 환경: 무시 */ }
		},

		/* 카테고리·결제수단·통장 선택지 받기 */
		loadMaster = function(){
			App.post(url.master)
				.then(handleMasterResult)
				.catch(function(){});
		},

		/* 빠른 입력의 카테고리·결제수단(마지막 것)·통장 그리기 */
		handleMasterResult = function(res){
			App.result(res, {ok: function(){
				settings.master = res.data;
				App.entryForm.renderCategories(m$.categories, 'quickCategory', settings.master.categories, App.entryForm.checked('quickType') || 'EXPENSE', null);
				var last = lastPayment(),
					known = settings.master.paymentMethods.some(function(p){ return p.active && p.id === last; });
				App.entryForm.renderPayments(m$.payments, 'quickPayment', settings.master.paymentMethods, known ? last : null);
				App.entryForm.renderAccounts(m$.account, settings.master.accounts || [], App.entryForm.lastAccount());
				showBalance();
				showBudget();
			}});
		},

		/* 통장 잔액 안내 갱신 */
		showBalance = function(){
			App.entryForm.balanceHint(m$.balance, settings.balances, m$.account.value, App.entryForm.checked('quickType'),
				App.parseAmount(m$.amount.value), m$.date.value || App.entryForm.today(), false);
		},

		/* 카테고리 예산 안내 갱신 */
		showBudget = function(){
			App.entryForm.budgetHint(m$.budget, settings.budgets, App.entryForm.checked('quickCategory'), App.entryForm.checked('quickType'),
				App.parseAmount(m$.amount.value), m$.date.value || App.entryForm.today(), false, false);
		},

		/* 이번 주기 예산 받기: 막대를 그리고 입력 안내를 갱신. 예산이 없으면 정하는 방법을 안내한다 */
		loadBudgets = function(){
			App.post(url.budgets)
				.then(function(res){
					App.result(res, {ok: function(){
						settings.budgets = res.data;
						if(res.data.items.length){
							m$.limitList.replaceChildren.apply(m$.limitList, App.entryForm.budgetBars(res.data));
						} else {
							m$.limitList.replaceChildren(App.h('li', {className: 'form-note',
								text: '설정에서 지출 카테고리마다 이번 주기 예산을 정하면, 여기서 얼마나 썼는지 막대로 보여 줘요.'}));
						}
						showBudget();
					}});
				})
				.catch(function(){});
		},

		/* 잔액 안내는 보조 정보라 실패해도 입력을 막지 않고 안내 칸에 적는다 */
		loadBalances = function(){
			App.post(url.balances, null, {onError: function(){ m$.balance.textContent = '통장 잔액을 불러오지 못했어요.'; }})
				.then(function(res){
					App.result(res, {ok: function(){
						settings.balances = res.data;
						showBalance();
					}});
				})
				.catch(function(){});
		},

		/* "2026-10-12" → Date(그날 0시) */
		toDate = function(iso){
			var p = iso.split('-');
			return new Date(Number(p[0]), Number(p[1]) - 1, Number(p[2]));
		},

		/* Date → "2026-10-12" */
		toIso = function(d){
			return d.getFullYear() + '-' + String(d.getMonth() + 1).padStart(2, '0') + '-' + String(d.getDate()).padStart(2, '0');
		},

		/* a 에서 b 까지 며칠(같은 날 0) */
		dayDiff = function(a, b){
			return Math.round((toDate(b) - toDate(a)) / 86400000);
		},

		/* "2026-10-12" → "10.12(월)" */
		shortDate = function(iso){
			var d = toDate(iso);
			return (d.getMonth() + 1) + '.' + d.getDate() + '(' + DOW.charAt(d.getDay()) + ')';
		},

		/* 이번 주기 요약 받기 */
		loadSummary = function(){
			App.post(url.summary)
				.then(function(res){
					App.result(res, {ok: function(){ renderSummary(res.data); }});
				})
				.catch(function(){});
		},

		/* 요약 숫자·주기 막대와 오늘 쓸 수 있는 돈 계산식 그리기 */
		renderSummary = function(s){
			var t = toDate(s.today),
				total = dayDiff(s.cycleStart, s.cycleEnd) + 1,
				passed = Math.min(total, Math.max(0, dayDiff(s.cycleStart, s.today) + 1));
			settings.summary = s;
			m$.todayLabel.textContent = (t.getMonth() + 1) + '월 ' + t.getDate() + '일 ' + DOW.charAt(t.getDay()) + '요일';
			m$.cycleRange.textContent = App.dotted(s.cycleStart).slice(5) + ' — ' + App.dotted(s.cycleEnd).slice(5) + ' · ' + passed + '일째 / ' + total + '일';
			m$.cycleBar.style.width = (passed / total * 100) + '%';
			m$.daysLeft.textContent = s.daysLeft + '일 남음';
			m$.dailyBudget.textContent = (s.dailyBudget < 0 ? '−' : '') + Math.abs(s.dailyBudget).toLocaleString('ko-KR');
			m$.dailyBudget.parentNode.classList.toggle('expense', s.dailyBudget < 0);
			m$.pendingFixed.textContent = App.money(s.pendingFixed);
			m$.carryOver.textContent = App.money(s.carryOver);
			/* 0원에는 부호를 붙이지 않는다("−0원" 방지) */
			m$.cycleIncome.textContent = (s.cycleIncome ? '+' : '') + App.money(s.cycleIncome);
			m$.cycleExpense.textContent = (s.cycleExpense ? '−' : '') + App.money(s.cycleExpense);
			m$.cycleBalance.textContent = App.money(s.cycleBalance);
			m$.totalBalance.textContent = App.money(s.totalBalance);
			m$.totalDetail.textContent = '가계부 ' + App.money(s.cycleBalance) + ' + 자산 ' + App.money(s.assetTotal)
				+ (s.marketUpdated ? '' : ' · 주식 시세 갱신 실패: 마지막 평가액');
			m$.formula.textContent = '(이월 ' + App.money(s.carryOver) + ' + 수입 ' + App.money(s.cycleIncome)
				+ ' − 지출 ' + App.money(s.cycleExpense) + ' − 예정 고정지출 ' + App.money(s.pendingFixed)
				+ ') ÷ 남은 ' + s.daysLeft + '일 = ' + App.money(s.dailyBudget);
			renderWeek();
			renderMood();
		},

		/* 이번 주(일요일~토요일) 날짜들 */
		weekDays = function(){
			var t = toDate(App.entryForm.today()), days = [], i, d;
			for(i = 0; i < 7; i++){
				d = new Date(t.getFullYear(), t.getMonth(), t.getDate() - t.getDay() + i);
				days.push(toIso(d));
			}
			return days;
		},

		/* 그날 쓴 돈: 지출 합계(환불은 뺀다). 이체는 목록에서 이미 뺐다 */
		daySpend = function(iso){
			return (settings.weekRows || []).reduce(function(sum, r){
				return r.entryDate === iso && r.type === 'EXPENSE' ? sum + (r.refund ? -r.amount : r.amount) : sum;
			}, 0);
		},

		/* 이번 주 거래 받기. 주가 두 달에 걸치면 두 달을 받는다 */
		loadWeek = function(){
			var days = weekDays(),
				months = days.map(function(d){ return d.slice(0, 7); }).filter(function(m, i, a){ return a.indexOf(m) === i; });
			Promise.all(months.map(function(m){ return App.post(url.entryList, {period: 'MONTH', month: m}); }))
				.then(function(results){
					var rows = [], ok = results.every(function(res){
						if(!res || res.code !== App.CODE.SUCCESS) return false;
						rows = rows.concat(res.data.rows);
						return true;
					});
					if(!ok){
						App.fail(results.filter(function(res){ return !res || res.code !== App.CODE.SUCCESS; })[0]);
						return;
					}
					settings.weekRows = rows.filter(function(r){ return r.kind !== 'TRANSFER' && days.indexOf(r.entryDate) >= 0; });
					App.guard(function(){ renderWeek(); renderMood(); });
				})
				.catch(function(){});
		},

		/* 칸 색 단계: 0 없음 · 1~3 많을수록 진하게 · 4 하루 예산을 넘음. 기준은 오늘 쓸 수 있는 돈(없으면 이번 주 평균) */
		heat = function(spend, ref){
			if(spend <= 0) return 0;
			if(spend > ref) return 4;
			if(spend > ref * 0.7) return 3;
			return spend > ref * 0.35 ? 2 : 1;
		},

		/* 이번 주 7칸 그리기. 칸을 누르면 기록 달력에서 그날을 연다 */
		renderWeek = function(){
			if(!settings.weekRows) return;
			var today = App.entryForm.today(), days = weekDays(),
				spends = days.map(daySpend),
				past = spends.filter(function(v, i){ return days[i] <= today && v > 0; }),
				avg = past.length ? past.reduce(function(a, b){ return a + b; }, 0) / past.length : 1,
				ref = settings.summary && settings.summary.dailyBudget > 0 ? settings.summary.dailyBudget : avg,
				total = 0;
			m$.week.replaceChildren.apply(m$.week, days.map(function(d, i){
				var sp = spends[i], future = d > today, level = future ? 0 : heat(sp, ref),
					date = toDate(d),
					attrs = {'aria-label': (date.getMonth() + 1) + '월 ' + date.getDate() + '일 ' + DOW.charAt(date.getDay()) + '요일, '
						+ (future ? '다가오는 날' : sp > 0 ? App.money(sp) + ' 씀' : '쓴 돈 없음') + (level === 4 ? ', 하루 예산보다 많이 씀' : '')};
				if(!future) total += Math.max(0, sp);
				if(level) attrs['data-h'] = String(level);
				if(d === today) attrs['aria-current'] = 'date';
				return App.h('a', {
					className: 'wk' + (future ? ' future' : '') + (d === today ? ' today' : '') + (date.getDay() === 0 ? ' sun' : ''),
					href: contextPath + '/ledger/entry?' + new URLSearchParams({month: d.slice(0, 7), day: d}).toString(),
					attrs: attrs
				}, [
					App.h('span', {className: 'wk-dow', text: DOW.charAt(date.getDay()), attrs: {'aria-hidden': 'true'}}),
					App.h('span', {className: 'wk-d', text: String(date.getDate()), attrs: {'aria-hidden': 'true'}}),
					App.h('span', {className: 'wk-a' + (sp > 0 ? '' : ' none'), attrs: {'aria-hidden': 'true'},
						text: future ? '·' : sp > 0 ? shortMoney(sp) : '0원'})
				]);
			}));
			m$.weekTotal.textContent = '이번 주 쓴 돈 ' + App.money(total) + ' · 칸을 누르면 기록 달력에서 그날을 열어요';
		},

		/* 칸에 들어가는 짧은 금액: 1만 이상은 "8.9만" */
		shortMoney = function(n){
			return n >= 10000 ? String(Math.round(n / 1000) / 10) + '만' : n.toLocaleString('ko-KR');
		},

		/* 잎새 표정과 말풍선: 오늘 쓴 돈 ÷ 오늘 쓸 수 있는 돈 */
		renderMood = function(){
			var s = settings.summary;
			if(!s || !settings.weekRows) return;
			var spent = Math.max(0, daySpend(App.entryForm.today())), daily = Number(s.dailyBudget), mood, line;
			if(daily <= 0){
				mood = 'worried';
				line = '이번 주기 잔액이 빠듯해요. 꼭 필요한 것만 같이 골라 봐요.';
			} else if(spent >= daily){
				mood = 'worried';
				line = '오늘 하루 예산보다 ' + App.money(spent - daily) + ' 더 썼어요. 내일 같이 아껴 봐요.';
			} else if(spent >= daily * 0.6){
				mood = 'soso';
				line = '오늘 ' + App.money(spent) + ' 썼어요. 하루 예산의 ' + Math.round(spent / daily * 100) + '%예요. 천천히 가요.';
			} else {
				mood = 'happy';
				line = spent ? '오늘 ' + App.money(spent) + ' 썼어요. 이 정도면 여유로워요!' : '오늘은 아직 쓴 돈이 없어요. 좋은 출발이에요!';
			}
			m$.mascot.classList.remove('pop');   /* 처음 등장은 init 에서 이미 했다 */
			m$.mascot.replaceChildren(App.leaf(mood));
			m$.mascotLine.textContent = line;
			if(settings.hop){
				settings.hop = false;
				m$.mascot.classList.remove('pop', 'hop');
				void m$.mascot.offsetWidth;   /* 같은 애니메이션을 다시 돌리려면 한 번 그려야 한다 */
				m$.mascot.classList.add('hop');
			}
		},

		/* 다가오는 고정 항목 받기 */
		loadUpcoming = function(){
			App.post(url.recurringList)
				.then(function(res){
					App.result(res, {ok: function(){ renderUpcoming(res.data.items || []); }});
				})
				.catch(function(){});
		},

		/* 활성 항목 중 다음 결제일이 가까운 4건. 할부는 그 회차 금액 */
		renderUpcoming = function(items){
			var today = App.entryForm.today(),
				list = items.filter(function(i){ return i.active && i.nextDue && i.nextDue >= today; })
					.sort(function(a, b){ return a.nextDue < b.nextDue ? -1 : a.nextDue > b.nextDue ? 1 : 0; })
					.slice(0, 4);
			if(!list.length){
				m$.upcoming.replaceChildren(App.entryForm.empty('다가오는 고정 항목이 없어요', '월세·구독료·월급을 고정 항목으로 등록하면 결제일에 알아서 기록돼요.'));
				return;
			}
			m$.upcoming.replaceChildren.apply(m$.upcoming, list.map(function(i){
				var d = dayDiff(today, i.nextDue),
					amount = i.nextAmount || (i.installmentMonths && i.dueAmount ? i.dueAmount : i.amount);
				return App.h('div', {className: 'up-row'}, [
					App.h('span', {className: 'dday' + (d <= 7 ? ' soon' : ''), text: d === 0 ? '오늘' : 'D-' + d}),
					App.h('span', {attrs: {style: 'min-width:0'}}, [
						App.h('span', {className: 'up-t', text: i.name}),
						App.h('span', {className: 'up-m', text: shortDate(i.nextDue) + ' · ' + i.categoryName})
					]),
					App.h('span', {className: 'up-a ' + (i.type === 'INCOME' ? 'income' : 'expense'),
						text: (i.type === 'INCOME' ? '+' : '−') + App.money(amount)})
				]);
			}));
		},

		/* 최근 기록 수정: 거래 내역 화면에서 그 거래의 수정 창을 연다 */
		editInEntries = function(e){
			location.href = contextPath + '/ledger/entry?' + new URLSearchParams({month: e.entryDate.slice(0, 7), view: 'list', edit: e.id}).toString();
		},

		/* 최근 기록 받기 */
		loadRecent = function(){
			App.post(url.recent)
				.then(handleRecentResult)
				.catch(function(){});
		},

		/* 최근 기록 5건 그리기(카드 높이를 이웃 카드와 맞춘다). 수정은 기록 화면에서 */
		handleRecentResult = function(res){
			App.result(res, {ok: function(){
				if(!res.data.length){
					m$.recent.replaceChildren(App.entryForm.empty('오늘의 첫 기록을 남겨볼까요?', '작은 지출부터 하나씩 기록하면 나의 소비가 보이기 시작해요.'));
					return;
				}
				m$.recent.replaceChildren.apply(m$.recent, App.entryForm.grouped(res.data.slice(0, 5), editInEntries));
			}});
		},

		/* 입력 확인 후 기록. 성공하면 창을 닫고 잔액·예산·요약·이번 주·최근 기록 새로고침 */
		save = function(){
			if(settings.submitting) return;
			var amount = App.parseAmount(m$.amount.value),
				categoryId = App.entryForm.checked('quickCategory'),
				paymentMethodId = App.entryForm.checked('quickPayment'),
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

			var type = App.entryForm.checked('quickType'),
				entryDate = m$.date.value || App.entryForm.today();
			settings.submitting = true;
			App.post(url.save, {
				entryDate: entryDate,
				type: type,
				categoryId: categoryId,
				title: title,
				amount: amount,
				paymentMethodId: paymentMethodId,
				accountId: m$.account.value,
				memo: m$.memo.value.trim()
			})
				.then(function(res){
					App.result(res, {ok: function(){
						/* 새 지출이 예산 80%·100%를 넘기면 저장 안내 대신 경고 */
						var warn = App.entryForm.budgetAlert(settings.budgets, categoryId, type, amount, entryDate, false);
						rememberPayment(paymentMethodId);
						App.entryForm.rememberAccount(m$.account.value);
						resetQuick();
						m$.dialog.close();
						settings.hop = true;
						if(warn) _alert(warn.title, warn.desc); else App.saved('기록했어요');
						loadBalances();
						loadBudgets();
						loadSummary();
						loadWeek();
						loadRecent();
					}});
				})
				.catch(function(){})
				.then(function(){ settings.submitting = false; });
		};

	return{
		init: init
	};
}());

document.addEventListener('DOMContentLoaded', function(){
	App.dashboard.init();
});
