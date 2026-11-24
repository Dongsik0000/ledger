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
			filterSummary: document.querySelector('.filter-disclosure > summary'),  /* 검색·필터 펼침 제목 */
			chips: document.getElementById('filterChips'),  /* 적용한 조건 칩 */
			category: document.getElementById('filterCategory'),  /* 카테고리 필터 */
			payment: document.getElementById('filterPayment'),  /* 결제수단 필터 */
			accountFilter: document.getElementById('filterAccount'),  /* 통장 필터 */
			reset: document.getElementById('filterReset'),  /* 필터 초기화 버튼 */
			search: document.getElementById('filterSearch'),  /* 조회 버튼 */
			count: document.getElementById('listCount'),  /* 건수 */
			totals: document.getElementById('listTotals'),  /* 수입·지출·차이 합계 */
			balanceNote: document.getElementById('balanceNote'),  /* 거꾸로 계산한 잔액 설명 */
			truncated: document.getElementById('listTruncated'),  /* 500건까지만 보인다는 안내 */
			period: document.getElementById('period'),  /* 기간 선택(달력 월·주기·최근 12개월·전체) */
			monthField: document.getElementById('monthField'),  /* 조회 월 칸(달력 월일 때만) */
			list: document.getElementById('entryList'),  /* 거래 목록 */
			viewButtons: document.querySelectorAll('[data-view]'),  /* 달력 · 목록 보기 전환 */
			calWrap: document.getElementById('entryCalendarWrap'),  /* 달력 보기 영역 */
			cal: document.getElementById('entryCalendar'),  /* 달력 칸 */
			dayTitle: document.getElementById('entryDayTitle'),  /* 고른 날 제목 */
			daySum: document.getElementById('entryDaySum'),  /* 고른 날 합계 */
			dayList: document.getElementById('entryDayList'),  /* 고른 날 기록 */
			dayPanel: document.querySelector('.day-panel'),  /* 고른 날·조회 결과 패널 */
			dayAll: document.getElementById('entryDayAll'),  /* 고른 날에서 전체 조회 결과로 */
			dayAdd: document.getElementById('entryDayAdd'),  /* 고른 날에 기록 추가 */
			dialog: document.getElementById(ED),  /* 거래 추가·수정 창 */
			heading: document.getElementById(ED + '-heading'),  /* 창 제목 */
			subtitle: document.getElementById(ED + '-subtitle'),  /* 창 부제목 */
			amount: document.getElementById(ED + '-amount'),  /* 금액 */
			entryCategory: document.getElementById(ED + '-category'),  /* 입력 창 카테고리 선택(필터 m$.category 와 다름) */
			budget: document.getElementById(ED + '-budget'),  /* 카테고리 예산 안내 */
			title: document.getElementById(ED + '-title'),  /* 내용 */
			payments: document.getElementById(ED + '-payments'),  /* 결제수단 라디오 묶음 */
			account: document.getElementById(ED + '-account'),  /* 통장 선택 */
			balance: document.getElementById(ED + '-balance'),  /* 통장 잔액 안내 */
			refund: document.getElementById(ED + '-refund'),  /* 환불·반납 체크 */
			refundLabel: document.getElementById(ED + '-refund-label'),  /* 환불·반납 설명(구분에 따라 바뀜) */
			refundFrom: document.getElementById(ED + '-refund-from'),  /* 이 거래의 환불 기록 버튼(수정 창) */
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
			balances: null,                                   /* 통장 잔액(창을 열 때마다 새로 받는다) */
			budgets: null,                                    /* 이번 주기 카테고리 예산(창을 열 때마다 새로 받는다) */
			view: 'calendar',                                 /* calendar | list */
			rows: [],                                         /* 마지막으로 받은 목록(달력이 쓴다) */
			range: null,                                      /* 달력 기간 {from, to} */
			selDay: '',                                       /* 달력에서 고른 날 */
			filtered: false,                                  /* 지금 보이는 목록이 검색·필터 결과인지 */
			dayPicked: false,                                 /* 조회 결과에서 날짜를 골라 그날만 보는지 */
			reveal: false,                                    /* 조회를 누른 뒤 결과 패널이 화면 밖이면 그곳으로 이동 */
			calFocus: false,                                  /* 달력을 다시 그린 뒤 고른 칸에 포커스 */
			dailyRef: 0,                                      /* 지출 막대 기준: 오늘 쓸 수 있는 돈(못 받으면 평균) */
			newDate: null                                     /* 추가 창에 미리 넣을 날짜(달력에서 연 경우) */
		},

		url = {
			master: contextPath + '/ledger/settings/master',
			cashAssets: contextPath + '/ledger/asset/cash-list',
			balances: contextPath + '/ledger/account/balances',
			budgets: contextPath + '/ledger/dashboard/budgets',
			list: contextPath + '/ledger/entry/list',
			summary: contextPath + '/ledger/dashboard/summary',
			save: contextPath + '/ledger/entry/save',
			del: contextPath + '/ledger/entry/delete'
		},

		/* 조회 조건을 주소에서 읽고, 선택지를 받은 뒤 목록을 그린다 */
		init = function(){
			/* 조회 조건은 주소(?period=&month=&q=&type=&category=&payment=&account=)에 남겨 새로고침·뒤로 가기에도 유지한다 */
			var q = new URLSearchParams(location.search);
			bindEvent();
			settings.month = /^\d{4}-(0[1-9]|1[0-2])$/.test(q.get('month') || '') ? q.get('month') : App.entryForm.today().slice(0, 7);
			m$.month.value = settings.month;
			m$.period.value = ['CYCLE', 'RECENT12', 'ALL'].indexOf(q.get('period')) >= 0 ? q.get('period') : 'MONTH';
			/* 주기 보기: 이 날짜가 속한 주기(없으면 오늘) */
			settings.cycleDate = /^\d{4}-\d{2}-\d{2}$/.test(q.get('date') || '') ? q.get('date') : '';
			showPeriod();
			m$.keyword.value = q.get('q') || '';
			m$.type.value = ['INCOME', 'EXPENSE'].indexOf(q.get('type')) >= 0 ? q.get('type') : '';
			settings.editId = q.get('edit');
			/* 보기(?view=list)와 달력에서 고른 날(?day=, 오늘 화면의 이번 주 칸에서 온다) */
			settings.view = q.get('view') === 'list' ? 'list' : 'calendar';
			settings.selDay = /^\d{4}-\d{2}-\d{2}$/.test(q.get('day') || '') ? q.get('day') : '';
			loadDailyRef();

			loadMaster().then(function(){
				/* 선택지에 없는 값(지운 항목 등)이면 select 가 빈 값으로 남는다 */
				m$.category.value = q.get('category') || '';
				m$.payment.value = q.get('payment') || '';
				m$.accountFilter.value = q.get('account') || '';
				list();
				/* 다른 화면의 아래 + 로 들어오면(?new=1) 추가 창을 바로 연다 */
				if(q.get('new')) openNew(settings.selDay);
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
			m$.search.addEventListener('click', search);
			m$.keyword.addEventListener('keydown', function(e){
				if(e.key === 'Enter') search();
			});
			m$.dayAll.addEventListener('click', function(){
				settings.dayPicked = false;
				renderCalendar();
			});
			m$.reset.addEventListener('click', function(){
				m$.keyword.value = '';
				m$.type.value = '';
				m$.category.value = '';
				m$.payment.value = '';
				m$.accountFilter.value = '';
				list();
			});

			m$.newButton.addEventListener('click', function(){ openNew(calendarShown() ? settings.selDay : null); });
			/* 휴대폰 아래 + : 이 화면에서는 새로 열지 않고 추가 창을 바로 연다 */
			document.addEventListener('click', function(e){
				var add = e.target.closest ? e.target.closest('[data-quick-add]') : null;
				if(!add) return;
				e.preventDefault();
				if(!m$.dialog.open) openNew(calendarShown() ? settings.selDay : null);
			});
			m$.viewButtons.forEach(function(b){
				b.addEventListener('click', function(){
					settings.view = b.getAttribute('data-view');
					saveQuery();
					showView();
				});
			});
			m$.cal.addEventListener('click', function(e){
				var cell = e.target.closest('.cal-cell');
				if(cell && !cell.disabled) selectDay(cell.getAttribute('data-date'));
			});
			/* 방향키로 하루·한 주씩 옮긴다(기간 밖으로는 가지 않는다) */
			m$.cal.addEventListener('keydown', function(e){
				var step = {ArrowRight: 1, ArrowLeft: -1, ArrowDown: 7, ArrowUp: -7}[e.key], next;
				if(!step || !settings.range) return;
				e.preventDefault();
				next = shiftDate(settings.selDay, step);
				if(next >= settings.range.from && next <= settings.range.to) selectDay(next);
			});
			m$.dayAdd.addEventListener('click', function(){ openNew(settings.selDay); });
			m$.batchButton.addEventListener('click', function(){ App.entryBatch.open(settings.master, list); });
			App.bindAmountInput(m$.amount);
			m$.dialog.querySelectorAll('input[name="' + ED + '-type"]').forEach(function(r){
				r.addEventListener('change', function(){
					App.entryForm.renderCategories(m$.entryCategory, settings.master.categories, r.value, null);
					showRefund();
					showTransfer();
					showBalance();
					showBudget();
				});
			});
			m$.amount.addEventListener('input', showBalance);
			m$.amount.addEventListener('input', showBudget);
			m$.entryCategory.addEventListener('change', showBudget);
			m$.account.addEventListener('change', showBalance);
			m$.date.addEventListener('change', showBalance);
			m$.date.addEventListener('change', showBudget);
			m$.refund.addEventListener('change', function(){
				showTransfer();
				showBalance();
				showBudget();
			});
			m$.refundFrom.addEventListener('click', refundFrom);
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
			if(m$.accountFilter.value) q.set('account', m$.accountFilter.value);
			if(settings.view === 'list') q.set('view', 'list');
			else if(settings.selDay) q.set('day', settings.selDay);
			history.replaceState(null, '', location.pathname + '?' + q.toString());
		},

		/* 지출 막대 기준(오늘 쓸 수 있는 돈). 보조 정보라 실패하면 그 기간의 하루 평균 지출을 기준으로 쓴다 */
		loadDailyRef = function(){
			App.post(url.summary, null, {onError: function(){}})
				.then(function(res){
					if(!res || res.code !== App.CODE.SUCCESS) return;
					settings.dailyRef = Number(res.data.dailyBudget) || 0;
					if(calendarShown()) App.guard(renderCalendar);
				})
				.catch(function(){});
		},

		/* 달력은 달력 월·주기에서만 볼 수 있다(최근 12개월·전체는 목록) */
		calendarAllowed = function(){
			return m$.period.value === 'MONTH' || m$.period.value === 'CYCLE';
		},

		/* 지금 달력 보기인지 */
		calendarShown = function(){
			return calendarAllowed() && settings.view === 'calendar';
		},

		/* 달력 · 목록 전환 */
		showView = function(){
			var shown = calendarShown(), allowed = calendarAllowed();
			m$.viewButtons.forEach(function(b){
				var isCal = b.getAttribute('data-view') === 'calendar';
				b.setAttribute('aria-pressed', String(isCal === shown));
				b.disabled = isCal && !allowed;
				b.title = isCal && !allowed ? '달력은 달력 월 · 주기에서 볼 수 있어요' : '';
			});
			m$.calWrap.hidden = !shown;
			m$.list.hidden = shown;
			if(shown) renderCalendar();
		},

		/* "2026-09-23" 의 요일(0 = 일요일) */
		dowOf = function(iso){
			return new Date(iso + 'T00:00:00').getDay();
		},

		/* 휴대폰 달력 칸에 들어가는 짧은 금액(4~5글자): 800 · 3.4천 · 1.4만 · 125만 · 1.2억 */
		shortMoney = function(n){
			var thousand = Math.round(n / 100) / 10, man1 = Math.round(n / 1000) / 10, man = Math.round(n / 10000);
			if(n < 1000) return String(n);
			if(thousand < 10) return thousand + '천';   /* 단위는 반올림한 값으로 고른다(9,999 → 1만) */
			if(man1 < 10) return man1 + '만';
			if(man < 10000) return man + '만';
			return Math.round(n / 10000000) / 10 + '억';
		},

		/* 칸 아래 지출 막대: 기준(오늘 쓸 수 있는 돈) 대비 그날 지출. 70% 넘으면 주의, 기준을 넘으면 초과(예산 막대와 같은 규칙) */
		spendMeter = function(spend, ref){
			var fill = App.h('span');
			fill.style.width = Math.min(100, spend / ref * 100) + '%';
			return App.h('span', {className: 'cal-meter' + (spend > ref ? ' is-over' : spend > ref * 0.7 ? ' is-warn' : ''),
				attrs: {'aria-hidden': 'true'}}, [fill]);
		},

		/* 고른 날 바꾸기 */
		selectDay = function(iso){
			settings.selDay = iso;
			settings.dayPicked = true;
			settings.calFocus = true;
			saveQuery();
			renderCalendar();
		},

		/* 달력 그리기: 기간을 일요일~토요일 주로 채우고, 기간 밖 칸은 흐리게(고를 수 없음).
		   칸 오른쪽 아래에 그날 수입(+, 초록)·지출(−, 빨강) 합계(환불·반납은 뺀 값), 그 아래 지출 막대(오늘까지만).
		   검색·필터 결과면 맞는 거래가 없는 날을 가라앉힌다. 휴대폰 폭은 "1.4만"처럼 줄여 쓰고, 480px 이하는 CSS 가 기호를 숨겨 색과 줄 위치(수입 위 · 지출 아래)로 구분한다 */
		renderCalendar = function(){
			var r = settings.range;
			if(!r) return;
			var today = App.entryForm.today(), spend = {}, income = {}, hits = {}, cells = [], vals, ref, d,
				compact = window.matchMedia('(max-width: 768px)').matches,
				amt = function(n){ return compact ? shortMoney(n) : n.toLocaleString('ko-KR'); },
				start = shiftDate(r.from, -dowOf(r.from)), end = shiftDate(r.to, 6 - dowOf(r.to));
			settings.rows.forEach(function(x){
				hits[x.entryDate] = true;
				if(x.kind === 'TRANSFER') return;
				var sum = x.type === 'EXPENSE' ? spend : income;
				sum[x.entryDate] = (sum[x.entryDate] || 0) + (x.refund ? -x.amount : x.amount);
			});
			if(!settings.selDay || settings.selDay < r.from || settings.selDay > r.to){
				settings.selDay = today >= r.from && today <= r.to ? today : r.from;
			}
			vals = Object.keys(spend).map(function(k){ return spend[k]; }).filter(function(v){ return v > 0; });
			ref = settings.dailyRef > 0 ? settings.dailyRef : (vals.length ? vals.reduce(function(a, b){ return a + b; }, 0) / vals.length : 1);
			'일월화수목금토'.split('').forEach(function(c){
				cells.push(App.h('div', {className: 'cal-dow', text: c, attrs: {'aria-hidden': 'true'}}));
			});
			for(d = start; d <= end; d = shiftDate(d, 1)){
				var inRange = d >= r.from && d <= r.to, sp = spend[d] || 0, inc = income[d] || 0,
					metered = inRange && d <= today && sp > 0, over = metered && sp > ref,
					miss = inRange && settings.filtered && !hits[d],
					p = d.split('-'),
					attrs = {'data-date': d, 'aria-pressed': String(d === settings.selDay && (!settings.filtered || settings.dayPicked)),
						'aria-label': Number(p[1]) + '월 ' + Number(p[2]) + '일' + (sp > 0 ? ', 지출 ' + App.money(sp) : '')
							+ (inc > 0 ? ', 수입 ' + App.money(inc) : '') + (over ? ', 오늘 쓸 수 있는 돈보다 많이 씀' : '') + (miss ? ', 조건에 맞는 거래 없음' : '')};
				if(d === today) attrs['aria-current'] = 'date';
				cells.push(App.h('button', {
					type: 'button', className: 'cal-cell' + (inRange ? '' : ' out') + (miss ? ' no-match' : '') + (d === today ? ' today' : ''),
					tabIndex: d === settings.selDay ? 0 : -1, disabled: !inRange, attrs: attrs
				}, [
					App.h('span', {className: 'cal-top'}, [App.h('span', {className: 'cal-d', text: String(Number(p[2]))})]),
					sp > 0 || inc > 0 ? App.h('span', {className: 'cal-amts', attrs: {'aria-hidden': 'true'}}, [
						inc > 0 ? App.h('span', {className: 'cal-amt income'}, [App.h('span', {className: 'cal-sign', text: '+'}), amt(inc)]) : null,
						sp > 0 ? App.h('span', {className: 'cal-amt'}, [App.h('span', {className: 'cal-sign', text: '−'}), amt(sp)]) : null
					]) : null,
					metered ? spendMeter(sp, ref) : null
				]));
			}
			m$.cal.replaceChildren.apply(m$.cal, cells);
			renderDay();
			if(settings.calFocus){
				settings.calFocus = false;
				var sel = m$.cal.querySelector('[data-date="' + settings.selDay + '"]');
				if(sel) sel.focus();
			}
		},

		/* 옆 패널: 고른 날의 기록(수정은 목록과 같은 창). 검색·필터 결과면 날짜를 고르기 전까지 결과 전체 */
		renderDay = function(){
			var d = settings.selDay, p = d.split('-'), all = settings.filtered && !settings.dayPicked,
				rows = all ? settings.rows : settings.rows.filter(function(x){ return x.entryDate === d; }),
				out = 0, inc = 0, count = 0, empty;
			rows.forEach(function(x){
				if(x.kind === 'TRANSFER') return;
				count++;
				var v = x.refund ? -x.amount : x.amount;
				if(x.type === 'EXPENSE') out += v; else inc += v;
			});
			m$.dayPanel.classList.toggle('is-result', all);
			m$.dayAll.hidden = all || !settings.filtered;
			m$.dayAdd.hidden = all;
			empty = all ? ['조건에 맞는 거래가 없어요', '검색어나 필터를 바꾸거나, 위의 조건을 눌러 빼 보세요.']
				: settings.filtered ? ['이날은 조건에 맞는 거래가 없어요', '전체 결과를 보거나 다른 날을 골라 보세요.']
				: ['이날은 조용했어요', '아래 버튼으로 이날의 기록을 바로 적을 수 있어요.'];
			m$.dayTitle.textContent = all ? '조회 결과 ' + count + '건' : Number(p[1]) + '월 ' + Number(p[2]) + '일 ' + '일월화수목금토'.charAt(dowOf(d)) + '요일';
			m$.daySum.textContent = rows.length ? '지출 ' + App.money(out) + (inc ? ' · 수입 ' + App.money(inc) : '') : '기록 없음';
			m$.dayList.replaceChildren.apply(m$.dayList, rows.length
				? App.entryForm.grouped(rows, openEdit)
				: [App.entryForm.empty(empty[0], empty[1])]);
			m$.dayAdd.lastChild.textContent = Number(p[1]) + '월 ' + Number(p[2]) + '일에 기록 추가';
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

		/* 카테고리·결제수단·통장 필터 선택지. 통장이 없으면 통장 필터를 숨긴다 */
		fillFilters = function(){
			var categoryOptions = [App.h('option', {value: '', text: '전체'})],
				paymentOptions = [App.h('option', {value: '', text: '전체'})],
				accountOptions = [App.h('option', {value: '', text: '전체'})],
				accounts = settings.master.accounts || [];
			settings.master.categories.forEach(function(c){
				var label = (c.type === 'INCOME' ? '수입 · ' : '지출 · ') + c.name + (c.active ? '' : ' (숨김)');
				categoryOptions.push(App.h('option', {value: String(c.id), text: label}));
			});
			settings.master.paymentMethods.forEach(function(p){
				paymentOptions.push(App.h('option', {value: String(p.id), text: p.name + (p.active ? '' : ' (숨김)')}));
			});
			m$.category.replaceChildren.apply(m$.category, categoryOptions);
			accounts.forEach(function(a){
				accountOptions.push(App.h('option', {value: String(a.id), text: a.name + (a.isDefault ? ' (기본 통장)' : '')}));
			});
			m$.payment.replaceChildren.apply(m$.payment, paymentOptions);
			m$.accountFilter.replaceChildren.apply(m$.accountFilter, accountOptions);
			m$.accountFilter.closest('.field').hidden = !accounts.length;
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
			return m$.keyword.value.trim() || m$.type.value || m$.category.value || m$.payment.value || m$.accountFilter.value;
		},

		/* 적용한 조건 칩: 누르면 그 조건만 빼고 다시 조회. 칩에 있던 초점은 남은 칩(없으면 펼침 제목)으로 옮긴다 */
		renderChips = function(){
			var focused = m$.chips.contains(document.activeElement),
				picked = function(el){ return el.value ? el.options[el.selectedIndex].text : ''; },
				category = settings.master.categories.filter(function(c){ return String(c.id) === m$.category.value; })[0],
				keyword = m$.keyword.value.trim(),
				chip = function(kind, text, el){
					return text ? App.h('button', {type: 'button', className: 'filter-chip', attrs: {'aria-label': kind + ' ' + text + ' 조건 빼기'},
						on: {click: function(){ el.value = ''; list(); }}}, [text, App.h('span', {text: '×', attrs: {'aria-hidden': 'true'}})]) : null;
				},
				chips = [
					chip('검색어', keyword ? '‘' + keyword + '’' : '', m$.keyword),
					chip('구분', picked(m$.type), m$.type),
					chip('카테고리', category ? category.name : '', m$.category),
					chip('결제수단', picked(m$.payment), m$.payment),
					chip('통장', picked(m$.accountFilter), m$.accountFilter)
				].filter(Boolean);
			m$.chips.replaceChildren.apply(m$.chips, chips);
			m$.chips.hidden = !chips.length;
			if(focused) (chips[0] || m$.filterSummary).focus({preventScroll: true});
		},

		/* 조회 버튼·검색어 Enter: 결과 패널이 화면 아래에 있으면(좁은 폭) 그곳까지 내려간다 */
		search = function(){
			settings.reveal = true;
			list();
		},

		/* 결과 패널이 화면 아래로 벗어나 있으면 보이게 옮긴다 */
		revealResult = function(){
			if(!settings.reveal) return;
			settings.reveal = false;
			if(!settings.filtered || !calendarShown()) return;
			if(m$.dayPanel.getBoundingClientRect().top < window.innerHeight * 0.75) return;
			m$.dayPanel.scrollIntoView({block: 'start',
				behavior: window.matchMedia('(prefers-reduced-motion: reduce)').matches ? 'auto' : 'smooth'});
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
				accountId: m$.accountFilter.value,
				keyword: m$.keyword.value.trim()
			})
				.then(function(res){
					App.result(res, {ok: function(){
						settings.filtered = !!filtered();
						settings.dayPicked = false;
						renderChips();
						render(res.data);
						revealResult();
					}});
				})
				.catch(function(){})
				.then(function(){ settings.reveal = false; });
		},

		/* 합계·목록 그리기. ?edit= 로 들어왔으면 그 거래의 수정 창을 연다 */
		render = function(data){
			var rows = data.rows, t = data.totals, net = t.income - t.expense,
				entries = rows.filter(function(r){ return r.kind !== 'TRANSFER'; });   /* 통장 필터면 이체 줄이 섞여 온다 */
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
			m$.balanceNote.hidden = !rows.some(function(r){ return r.balanceState === 'ESTIMATED'; });
			m$.count.textContent = data.truncated ? t.count + '건 중 ' + entries.length + '건' : entries.length + '건';
			/* 달력 기간: 달력 월은 그 달, 주기는 서버가 알려 준 주기 */
			settings.rows = rows;
			if(m$.period.value === 'MONTH'){
				var mp = settings.month.split('-');
				settings.range = {from: settings.month + '-01', to: settings.month + '-' + String(new Date(Number(mp[0]), Number(mp[1]), 0).getDate()).padStart(2, '0')};
			} else {
				settings.range = m$.period.value === 'CYCLE' ? {from: data.from, to: data.to} : null;
			}
			showView();
			if(calendarShown()) saveQuery();   /* 고른 날이 기간에 맞게 바뀌었을 수 있다 */
			if(!rows.length){
				m$.list.replaceChildren(filtered()
					? App.entryForm.empty('조건에 맞는 거래가 없어요', '검색어나 필터를 바꿔 보세요.')
					: App.entryForm.empty((m$.period.value === 'MONTH' ? '이 달' : '이 기간') + '의 첫 기록을 남겨볼까요?', '작은 지출부터 하나씩 기록하면 나의 소비가 보이기 시작해요.'));
				return;
			}
			m$.list.replaceChildren.apply(m$.list, App.entryForm.grouped(rows, openEdit));
			/* 대시보드 최근 기록에서 "수정"으로 들어온 경우(?edit=id) 그 거래의 수정 창을 한 번 연다 */
			if(settings.editId){
				var target = entries.filter(function(r){ return String(r.id) === settings.editId; })[0];
				settings.editId = null;
				if(target) openEdit(target);
			}
		},

		/* 구분 라디오 고르기 */
		setType = function(type){
			m$.dialog.querySelector('input[name="' + ED + '-type"][value="' + type + '"]').checked = true;
		},

		/* 지출(환불 아님)일 때만 적금·예금 이체 칸을 보인다 */
		showTransfer = function(){
			var expense = App.entryForm.checked(ED + '-type') === 'EXPENSE' && !m$.refund.checked;
			m$.transferField.hidden = !expense;
			if(!expense) m$.transferAsset.value = '';
		},

		/* 환불·반납 설명: 지출이면 돌려받은 돈, 수입이면 돌려준 돈 */
		showRefund = function(){
			m$.refundLabel.textContent = App.entryForm.checked(ED + '-type') === 'INCOME'
				? '돌려준 돈 (수입에서 빠지고 잔액에서 나가요)' : '환불받은 돈 (지출에서 빠지고 잔액에 더해져요)';
		},

		/* 통장 잔액 안내 갱신 */
		showBalance = function(){
			App.entryForm.balanceHint(m$.balance, settings.balances, m$.account.value, App.entryForm.checked(ED + '-type'),
				App.parseAmount(m$.amount.value), m$.date.value, !!settings.editing, m$.refund.checked);
		},

		/* 카테고리 예산 안내 갱신 */
		showBudget = function(){
			App.entryForm.budgetHint(m$.budget, settings.budgets, m$.entryCategory.value, App.entryForm.checked(ED + '-type'),
				App.parseAmount(m$.amount.value), m$.date.value, !!settings.editing, m$.refund.checked);
		},

		/* 예산 안내도 보조 정보라 실패해도 창을 막지 않고 안내 칸에 적는다 */
		loadBudgets = function(){
			settings.budgets = null;
			m$.budget.hidden = true;
			App.post(url.budgets, null, {onError: function(){
				m$.budget.hidden = false;
				m$.budget.textContent = '예산을 불러오지 못했어요.';
			}})
				.then(function(res){
					App.result(res, {ok: function(){
						settings.budgets = res.data;
						showBudget();
					}});
				})
				.catch(function(){});
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

		/* 거래 창 열기. e 가 있으면 수정, 없으면 추가. preset 은 추가 창에 미리 채울 값(환불 기록) */
		open = function(e, preset){
			var src = e || preset;
			settings.editing = e;
			m$.heading.textContent = e ? '거래 수정' : preset ? '환불·반납 기록' : '거래 추가';
			m$.subtitle.textContent = e ? e.title
				: preset ? '‘' + preset.from + '’에서 돌려받거나 돌려준 돈을 적어요.' : '필요한 내용을 입력해 주세요.';
			m$.amount.value = src ? Number(src.amount).toLocaleString('ko-KR') : '';
			setType(src ? src.type : 'EXPENSE');
			App.entryForm.renderCategories(m$.entryCategory, settings.master.categories, src ? src.type : 'EXPENSE', src ? src.categoryId : null);
			m$.title.value = src ? src.title : '';
			App.entryForm.renderPayments(m$.payments, ED + '-payment', settings.master.paymentMethods, src ? src.paymentMethodId : null);
			App.entryForm.renderAccounts(m$.account, settings.master.accounts || [], src ? src.accountId : App.entryForm.lastAccount());
			m$.refund.checked = preset ? true : !!(e && e.refund);
			m$.date.value = e ? e.entryDate
				: !preset && settings.newDate ? settings.newDate
				: preset || settings.month === App.entryForm.today().slice(0, 7) ? App.entryForm.today() : settings.month + '-01';
			settings.newDate = null;
			m$.memo.value = e && e.memo ? e.memo : '';
			m$.memoBox.open = !!(e && e.memo);
			showRefund();
			showTransfer();
			m$.del.hidden = !e;
			m$.refundFrom.hidden = !e || !!e.refund;
			settings.snapshot = snapshot();
			loadTransferAssets(e ? e.transferAssetId : null);
			loadBalances();
			loadBudgets();
			m$.dialog.showModal();
			m$.amount.focus();
		},

		/* 창에 입력된 값 전체(열었을 때와 비교해 작성 중인지 판단) */
		snapshot = function(){
			return JSON.stringify([m$.amount.value, App.entryForm.checked(ED + '-type'), m$.entryCategory.value,
				m$.title.value, App.entryForm.checked(ED + '-payment'), m$.account.value, m$.date.value, m$.memo.value,
				m$.refund.checked, m$.transferAsset.value]);   /* 이체 자산은 마지막(loadTransferAssets 가 바꾼다) */
		},

		/* 작성 중이면 버릴지 물은 뒤 닫는다 */
		requestClose = function(){
			if(snapshot() === settings.snapshot){
				m$.dialog.close();
				return;
			}
			_confirm('작성 중인 내용을 버릴까요?', '저장하지 않은 입력은 사라져요.', function(){ m$.dialog.close(); });
		},

		/* 새 거래. date 를 주면(달력에서 고른 날) 그 날짜로 연다 */
		openNew = function(date){
			settings.newDate = typeof date === 'string' && /^\d{4}-\d{2}-\d{2}$/.test(date) ? date : null;
			open(null);
		},

		/* 거래 수정 */
		openEdit = function(e){ open(e); },

		/* 수정 중인 거래의 환불·반납 기록: 구분·카테고리·결제수단·통장·금액을 이어받은 새 거래 창. 금액은 부분 환불이면 고친다 */
		refundFrom = function(){
			var e = settings.editing,
				go = function(){
					m$.dialog.close();
					open(null, {type: e.type, categoryId: e.categoryId, paymentMethodId: e.paymentMethodId, accountId: e.accountId,
						amount: e.amount, title: e.title.slice(0, 97) + (e.type === 'INCOME' ? ' 반납' : ' 환불'), from: e.title});
				};
			if(snapshot() === settings.snapshot) go();
			else _confirm('수정 중인 내용을 버릴까요?', '저장하지 않은 수정은 사라지고 환불 기록 창이 열려요.', go);
		},

		/* 입력 확인 후 저장 요청 */
		save = function(){
			if(settings.submitting) return;
			var amount = App.parseAmount(m$.amount.value),
				categoryId = m$.entryCategory.value,
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
				refund: m$.refund.checked,
				memo: m$.memo.value.trim()
			})
				.then(handleSaveResult)
				.catch(function(){})
				.then(function(){ settings.submitting = false; });
		},

		/* 저장 성공: 통장 기억, 창 닫기, 목록 새로고침. 새 지출이 예산 80%·100%를 넘기면 저장 안내 대신 경고 */
		handleSaveResult = function(res){
			App.result(res, {ok: function(){
				var warn = settings.editing ? null : App.entryForm.budgetAlert(settings.budgets, m$.entryCategory.value,
					App.entryForm.checked(ED + '-type'), App.parseAmount(m$.amount.value), m$.date.value, m$.refund.checked);
				App.entryForm.rememberAccount(m$.account.value);
				settings.selDay = m$.date.value;   /* 달력은 방금 저장한 날을 보여 준다(기간 밖이면 기본값으로) */
				m$.dialog.close();
				if(warn) _alert(warn.title, warn.desc); else App.saved();
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
