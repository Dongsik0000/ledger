App.dashboard = (function(){
    var LAST_PAYMENT_KEY = 'ledger.lastPaymentMethodId',
        m$ = {
            cycleRange: document.getElementById('cycleRange'),
            daysLeft: document.getElementById('daysLeft'),
            dailyBudget: document.getElementById('dailyBudget'),
            pendingFixed: document.getElementById('pendingFixed'),
            todayLabel: document.getElementById('todayLabel'),
            carryOver: document.getElementById('carryOver'),
            cycleIncome: document.getElementById('cycleIncome'),
            cycleExpense: document.getElementById('cycleExpense'),
            cycleBalance: document.getElementById('cycleBalance'),
            totalBalance: document.getElementById('totalBalance'),
            totalDetail: document.getElementById('totalDetail'),
            formula: document.getElementById('formula'),
            amount: document.getElementById('quickAmount'),
            categories: document.getElementById('quickCategories'),
            title: document.getElementById('quickTitle'),
            payments: document.getElementById('quickPayments'),
            optional: document.getElementById('quickOptional'),
            date: document.getElementById('quickDate'),
            memo: document.getElementById('quickMemo'),
            save: document.getElementById('quickSave'),
            form: document.getElementById('quickForm'),
            jump: document.getElementById('quickJump'),
            recent: document.getElementById('recentList')
        },
        settings = { submitting: false, master: {categories: [], paymentMethods: []} },
        url = {
            summary: contextPath + '/ledger/dashboard/summary',
            recent: contextPath + '/ledger/dashboard/recent',
            master: contextPath + '/ledger/settings/master',
            save: contextPath + '/ledger/entry/save'
        },

        init = function(){
            App.bindAmountInput(m$.amount);
            document.querySelectorAll('input[name="quickType"]').forEach(function(r){
                r.addEventListener('change', function(){
                    App.entryForm.renderCategories(m$.categories, 'quickCategory', settings.master.categories, r.value, null);
                });
            });
            // Enter·휴대폰 키보드의 완료로도 저장된다
            m$.form.addEventListener('submit', function(e){
                e.preventDefault();
                save();
            });
            // "바로 기록": 입력 칸으로 이동하면서 금액 칸에 커서를 둔다
            m$.jump.addEventListener('click', function(e){
                e.preventDefault();
                m$.amount.scrollIntoView({behavior: 'smooth', block: 'center'});
                m$.amount.focus({preventScroll: true});
            });
            m$.date.value = App.entryForm.today();

            loadMaster();
            loadSummary();
            loadRecent();
        },

        // 마지막으로 쓴 결제수단(이 브라우저에만 저장). 저장소를 못 쓰는 환경이면 기억하지 않는다
        lastPayment = function(){
            try {
                var v = localStorage.getItem(LAST_PAYMENT_KEY);
                return v ? Number(v) : null;
            } catch (e) {
                return null;
            }
        },
        rememberPayment = function(id){
            try {
                if (id) localStorage.setItem(LAST_PAYMENT_KEY, String(id));
                else localStorage.removeItem(LAST_PAYMENT_KEY);
            } catch (e) { /* 저장 불가 환경: 무시 */ }
        },

        loadMaster = function(){
            App.post(url.master)
                .then(function(res){
                    App.result(res, {ok: function(){
                        settings.master = res.data;
                        App.entryForm.renderCategories(m$.categories, 'quickCategory', settings.master.categories, App.entryForm.checked('quickType') || 'EXPENSE', null);
                        var last = lastPayment(),
                            known = settings.master.paymentMethods.some(function(p){ return p.active && p.id === last; });
                        App.entryForm.renderPayments(m$.payments, 'quickPayment', settings.master.paymentMethods, known ? last : null);
                    }});
                })
                .catch(function(){});
        },

        dotted = function(d){ return d.replace(/-/g, '.'); },

        loadSummary = function(){
            App.post(url.summary)
                .then(function(res){
                    App.result(res, {ok: function(){ renderSummary(res.data); }});
                })
                .catch(function(){});
        },

        renderSummary = function(s){
            m$.cycleRange.textContent = dotted(s.cycleStart) + ' — ' + dotted(s.cycleEnd).slice(5);
            m$.daysLeft.textContent = s.daysLeft + '일 남음';
            m$.dailyBudget.textContent = (s.dailyBudget < 0 ? '−' : '') + Math.abs(s.dailyBudget).toLocaleString('ko-KR');
            m$.dailyBudget.parentNode.classList.toggle('expense', s.dailyBudget < 0);
            m$.pendingFixed.textContent = App.money(s.pendingFixed);
            m$.todayLabel.textContent = '기준일 ' + dotted(s.today);
            m$.carryOver.textContent = App.money(s.carryOver);
            // 0원에는 부호를 붙이지 않는다("−0원" 방지)
            m$.cycleIncome.textContent = (s.cycleIncome ? '+' : '') + App.money(s.cycleIncome);
            m$.cycleExpense.textContent = (s.cycleExpense ? '−' : '') + App.money(s.cycleExpense);
            m$.cycleBalance.textContent = App.money(s.cycleBalance);
            m$.totalBalance.textContent = App.money(s.totalBalance);
            m$.totalDetail.textContent = '가계부 ' + App.money(s.cycleBalance) + ' + 자산 ' + App.money(s.assetTotal);
            m$.formula.textContent = '(이월 ' + App.money(s.carryOver) + ' + 수입 ' + App.money(s.cycleIncome)
                + ' − 지출 ' + App.money(s.cycleExpense) + ' − 예정 고정지출 ' + App.money(s.pendingFixed)
                + ') ÷ 남은 ' + s.daysLeft + '일 = ' + App.money(s.dailyBudget);
        },

        // 최근 기록 수정: 거래 내역 화면에서 그 거래의 수정 창을 연다
        editInEntries = function(e){
            location.href = contextPath + '/ledger/entry?' + new URLSearchParams({month: e.entryDate.slice(0, 7), edit: e.id}).toString();
        },

        loadRecent = function(){
            App.post(url.recent)
                .then(function(res){
                    App.result(res, {ok: function(){
                        if (!res.data.length) {
                            m$.recent.replaceChildren(App.entryForm.empty('오늘의 첫 기록을 남겨볼까요?', '작은 지출부터 하나씩 기록하면 나의 소비가 보이기 시작해요.'));
                            return;
                        }
                        m$.recent.replaceChildren.apply(m$.recent, App.entryForm.grouped(res.data, editInEntries));
                    }});
                })
                .catch(function(){});
        },

        save = function(){
            if (settings.submitting) return;
            var amount = App.parseAmount(m$.amount.value),
                categoryId = App.entryForm.checked('quickCategory'),
                paymentMethodId = App.entryForm.checked('quickPayment'),
                title = m$.title.value.trim();
            if (!amount) {
                _error('알림', App.amountError(m$.amount.value));
                return;
            }
            if (!categoryId) {
                _error('알림', '카테고리를 골라주세요.');
                return;
            }
            if (!title) {
                _error('알림', '내용을 입력해주세요.');
                return;
            }

            settings.submitting = true;
            App.post(url.save, {
                entryDate: m$.date.value || App.entryForm.today(),
                type: App.entryForm.checked('quickType'),
                categoryId: categoryId,
                title: title,
                amount: amount,
                paymentMethodId: paymentMethodId,
                memo: m$.memo.value.trim()
            })
                .then(function(res){
                    App.result(res, {ok: function(){
                        rememberPayment(paymentMethodId);
                        m$.amount.value = '';
                        m$.title.value = '';
                        m$.memo.value = '';
                        m$.date.value = App.entryForm.today();
                        m$.optional.open = false;
                        App.saved('기록했어요');
                        loadSummary();
                        loadRecent();
                    }});
                })
                .catch(function(){})
                .then(function(){ settings.submitting = false; });
        };

    return { init: init };
}());

document.addEventListener('DOMContentLoaded', function(){
    App.dashboard.init();
});
