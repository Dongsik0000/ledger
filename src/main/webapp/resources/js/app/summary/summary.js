App.summary = (function(){
	var INCOME_COLOR = '#547851',
		EXPENSE_COLOR = '#bc9856',
		DONUT_COLORS = ['#547851', '#bc9856', '#879975', '#a27560', '#6f8f9c', '#c9b98a', '#8a6f8f', '#9aa98a'],
		m$ = {
			basis: document.getElementById('basis'),  /* 기준 선택(달력 월·주기) */
			basisNote: document.getElementById('basisNote'),  /* 기준 설명 */
			from: document.getElementById('rangeFrom'),  /* 시작 월 */
			to: document.getElementById('rangeTo'),  /* 종료 월 */
			search: document.getElementById('rangeSearch'),  /* 조회 버튼 */
			cycleRange: document.getElementById('cycleRange'),  /* 이번 주기 기간 */
			daysLeft: document.getElementById('daysLeft'),  /* 남은 일수 */
			cycleBalance: document.getElementById('cycleBalance'),  /* 주기 잔액 */
			cycleBalanceFoot: document.getElementById('cycleBalanceFoot'),  /* 주기 잔액 설명 */
			cycleIncome: document.getElementById('cycleIncome'),  /* 주기 수입 */
			cycleIncomeFoot: document.getElementById('cycleIncomeFoot'),  /* 주기 수입 설명 */
			cycleExpense: document.getElementById('cycleExpense'),  /* 주기 지출 */
			cycleExpenseFoot: document.getElementById('cycleExpenseFoot'),  /* 주기 지출 설명 */
			monthlyBody: document.getElementById('monthlyBody'),  /* 월별 표 본문 */
			categoryHead: document.getElementById('categoryHead'),  /* 카테고리 표 머리(월) */
			categoryBody: document.getElementById('categoryBody'),  /* 카테고리 표 본문 */
			paymentHead: document.getElementById('paymentHead'),  /* 결제수단 표 머리(월) */
			paymentBody: document.getElementById('paymentBody'),  /* 결제수단 표 본문 */
			monthlyChart: document.getElementById('monthlyChart'),  /* 월별 막대 차트 */
			monthlyChartRange: document.getElementById('monthlyChartRange'),  /* 차트 기간 */
			categoryMonth: document.getElementById('categoryMonth'),  /* 도넛 월 선택 */
			categoryChart: document.getElementById('categoryChart'),  /* 도넛 차트 */
			categoryLegend: document.getElementById('categoryLegend')  /* 도넛 범례 */
		},

		settings = {
			charts: {}
		},

		url = {
			summary: contextPath + '/ledger/dashboard/summary',
			load: contextPath + '/ledger/summary/load',
			donut: contextPath + '/ledger/summary/donut'
		},

		/* 한 자리 수 앞에 0 */
		pad = function(n){ return (n < 10 ? '0' : '') + n; },

		/* 기준 월에서 delta 개월 옮긴 "YYYY-MM" */
		shiftMonth = function(ym, delta){
			var p = ym.split('-'), d = new Date(Number(p[0]), Number(p[1]) - 1 + delta, 1);
			return d.getFullYear() + '-' + pad(d.getMonth() + 1);
		},

		/* 천 단위 쉼표 */
		num = function(n){ return Number(n).toLocaleString('ko-KR'); },
		/* "2026-10-12" → "2026.10.12" */
		dotted = function(d){ return d.replace(/-/g, '.'); },
		/* "2026-10-12" → "10/12" */
		shortDate = function(d){ var p = d.split('-'); return Number(p[1]) + '/' + Number(p[2]); },

		/* 기간을 최근 3개월로 채우고 주기 요약·집계·도넛 불러오기 */
		init = function(){
			var now = new Date(),
				thisMonth = now.getFullYear() + '-' + pad(now.getMonth() + 1);
			bindEvent();
			m$.to.value = thisMonth;
			m$.from.value = shiftMonth(thisMonth, -2);
			m$.categoryMonth.value = thisMonth;

			loadCycle();
			load();
			loadDonut();
		},

		/* 화면 이벤트 연결 */
		bindEvent = function(){
			m$.search.addEventListener('click', function(){
				load();
				if(m$.to.value){
					m$.categoryMonth.value = m$.to.value;
					loadDonut();
				}
			});
			m$.categoryMonth.addEventListener('change', loadDonut);
			m$.basis.addEventListener('change', function(){
				load();
				loadDonut();
			});
		},

		/* 이번 주기 요약 받기 */
		loadCycle = function(){
			App.post(url.summary)
				.then(handleCycleResult)
				.catch(function(){});
		},

		/* 이번 주기 잔액·수입·지출 카드 */
		handleCycleResult = function(res){
			App.result(res, {ok: function(){
				var s = res.data, range = shortDate(s.cycleStart) + '~' + shortDate(s.cycleEnd);
				m$.cycleRange.textContent = dotted(s.cycleStart) + ' — ' + dotted(s.cycleEnd).slice(5);
				m$.daysLeft.textContent = s.daysLeft + '일 남음';
				m$.cycleBalance.textContent = num(s.cycleBalance);
				m$.cycleBalanceFoot.textContent = range + ' · 이월 포함';
				m$.cycleIncome.textContent = num(s.cycleIncome);
				m$.cycleIncomeFoot.textContent = range + ' 기준';
				m$.cycleExpense.textContent = num(s.cycleExpense);
				m$.cycleExpenseFoot.textContent = range + ' 기준';
			}});
		},

		/* 기간·기준으로 집계 받기 */
		load = function(){
			if(!m$.from.value || !m$.to.value){
				_error('알림', '시작 월과 종료 월을 골라주세요.');
				return;
			}
			App.post(url.load, {from: m$.from.value, to: m$.to.value, basis: m$.basis.value})
				.then(function(res){
					App.result(res, {ok: function(){ render(res.data); }});
				})
				.catch(function(){});
		},

		/* 표 머리글: 한 해 안이면 "8월", 해를 넘기면 "2026.08" */
		monthLabels = function(months){
			var sameYear = months.every(function(m){ return m.slice(0, 4) === months[0].slice(0, 4); });
			return months.map(function(m){ return sameYear ? Number(m.slice(5)) + '월' : dotted(m); });
		},

		/* 월별 표·카테고리 표·결제수단 표·막대 차트 */
		render = function(data){
			var labels = monthLabels(data.months),
				cycle = data.basis === 'CYCLE';

			/* 주기 기준: "9월" = 9월 급여일부터 10월 급여일 전날까지. 공휴일이 없는 해는 주말만 보정했다고 알린다 */
			m$.basisNote.textContent = cycle
				? '아래 표·차트는 주기 기준입니다. "9월"은 9월 급여일부터 다음 급여일 전날까지예요(최대 24개월).'
					+ (data.holidayMissingYears.length ? ' ' + data.holidayMissingYears.join('·') + '년은 공휴일이 등록되지 않아 주말만 반영했어요.' : '')
				: '아래 월별 표·차트는 달력 월 기준입니다(최대 24개월). 현재 주기 잔액은 별도로 표시합니다.';

			m$.monthlyBody.replaceChildren.apply(m$.monthlyBody, data.monthly.map(function(r){
				return App.h('tr', null, [
					App.h('th', {attrs: {scope: 'row'}, text: cycle
						? dotted(r.month) + ' 주기 · ' + shortDate(r.from) + '~' + shortDate(r.to) : dotted(r.month)}),
					App.h('td', {text: App.money(r.income)}),
					App.h('td', {text: App.money(r.expense)}),
					App.h('td', null, [App.h('span', {className: r.net < 0 ? 'expense' : '', text: (r.net > 0 ? '+' : '') + App.money(r.net)})]),
					/* 시작 잔액 기준일 전에 끝나는 달은 누적 잔액을 알 수 없어 null */
					App.h('td', {text: r.cumulative === null ? '—' : App.money(r.cumulative)})
				]);
			}));

			renderMatrix(m$.categoryHead, m$.categoryBody, '카테고리', labels, data.categoryRows);
			renderMatrix(m$.paymentHead, m$.paymentBody, '결제수단', labels, data.paymentRows);
			renderMonthlyChart(labels, data.monthly);
			m$.monthlyChartRange.textContent = dotted(data.months[0]) + ' ~ ' + dotted(data.months[data.months.length - 1]);
		},

		/* 행 × 월 표. kind 가 subtotal·total 이면 소계 줄 스타일 */
		renderMatrix = function(head, body, firstLabel, labels, rows){
			head.replaceChildren(App.h('tr', null, [App.h('th', {attrs: {scope: 'col'}, text: firstLabel})]
				.concat(labels.map(function(l){ return App.h('th', {attrs: {scope: 'col'}, text: l}); }))
				.concat([App.h('th', {attrs: {scope: 'col'}, text: '기간 합계'})])));
			body.replaceChildren.apply(body, rows.map(function(r){
				return App.h('tr', {className: r.kind === 'item' ? '' : 'subtotal'}, [App.h('th', {attrs: {scope: 'row'}, text: r.label})]
					.concat(r.values.map(function(v){ return App.h('td', {text: num(v)}); }))
					.concat([App.h('td', {text: num(r.total)})]));
			}));
		},

		/* 같은 캔버스에 다시 그리기 전에 이전 차트를 없앤다 */
		draw = function(key, canvas, config){
			if(settings.charts[key]) settings.charts[key].destroy();
			settings.charts[key] = new Chart(canvas, config);
		},

		/* 월별 수입·지출 막대 차트 */
		renderMonthlyChart = function(labels, monthly){
			draw('monthly', m$.monthlyChart, {
				type: 'bar',
				data: {
					labels: labels,
					datasets: [
						{label: '수입', data: monthly.map(function(r){ return r.income; }), backgroundColor: INCOME_COLOR, borderRadius: 5},
						{label: '지출', data: monthly.map(function(r){ return r.expense; }), backgroundColor: EXPENSE_COLOR, borderRadius: 5}
					]
				},
				options: {
					responsive: true,
					maintainAspectRatio: false,
					plugins: {
						legend: {display: false},
						tooltip: {callbacks: {label: function(ctx){ return ctx.dataset.label + ' ' + App.money(ctx.parsed.y); }}}
					},
					scales: {y: {beginAtZero: true, ticks: {callback: function(v){ return v >= 10000 ? (v / 10000).toLocaleString('ko-KR') + '만' : v; }}}}
				}
			});
		},

		/* 고른 월의 그룹별 지출 받기 */
		loadDonut = function(){
			if(!m$.categoryMonth.value) return;
			App.post(url.donut, {month: m$.categoryMonth.value, basis: m$.basis.value})
				.then(function(res){
					App.result(res, {ok: function(){ renderDonut(res.data); }});
				})
				.catch(function(){});
		},

		/* 그룹별 지출 도넛 차트와 범례 */
		renderDonut = function(rows){
			var total = rows.reduce(function(s, r){ return s + Number(r.amount); }, 0);
			draw('category', m$.categoryChart, {
				type: 'doughnut',
				data: {
					labels: rows.map(function(r){ return r.label; }),
					datasets: [{
						data: rows.map(function(r){ return r.amount; }),
						backgroundColor: rows.map(function(r, i){ return DONUT_COLORS[i % DONUT_COLORS.length]; }),
						borderWidth: 0
					}]
				},
				options: {
					responsive: true,
					maintainAspectRatio: false,
					cutout: '62%',
					plugins: {
						legend: {display: false},
						tooltip: {callbacks: {label: function(ctx){ return ctx.label + ' ' + App.money(ctx.parsed); }}}
					}
				}
			});
			if(!rows.length){
				m$.categoryLegend.replaceChildren(App.h('span', {className: 'form-note', text: '이 달에는 지출 기록이 없어요.'}));
				return;
			}
			m$.categoryLegend.replaceChildren.apply(m$.categoryLegend, rows.map(function(r, i){
				return App.h('div', null, [
					App.h('span', null, [
						App.h('i', {className: 'legend-dot', attrs: {style: 'background:' + DONUT_COLORS[i % DONUT_COLORS.length]}}),
						r.label
					]),
					App.h('b', {text: App.money(r.amount) + ' · ' + Math.round(r.amount * 100 / total) + '%'})
				]);
			}));
		};

	return{
		init: init
	};
}());

document.addEventListener('DOMContentLoaded', function(){
	App.summary.init();
});
