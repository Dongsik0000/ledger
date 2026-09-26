App.settings = (function(){
    var m$ = {
            payDay: document.getElementById('payDay'),
            payDayAdjust: document.getElementById('payDayAdjust'),
            cycleSave: document.getElementById('cycleSave'),
            cycleExample: document.getElementById('cycleExample'),
            expenseBody: document.getElementById('expenseCategoryBody'),
            incomeBody: document.getElementById('incomeCategoryBody'),
            categoryAdd: document.getElementById('categoryAdd'),
            newCategoryName: document.getElementById('newCategoryName'),
            newCategoryGroup: document.getElementById('newCategoryGroup'),
            newCategoryOrder: document.getElementById('newCategoryOrder'),
            categoryAddSave: document.getElementById('categoryAddSave'),
            categoryAddCancel: document.getElementById('categoryAddCancel'),
            paymentBody: document.getElementById('paymentBody'),
            paymentAdd: document.getElementById('paymentAdd'),
            newPaymentName: document.getElementById('newPaymentName'),
            paymentAddSave: document.getElementById('paymentAddSave'),
            paymentAddCancel: document.getElementById('paymentAddCancel'),
            exportFrom: document.getElementById('exportFrom'),
            exportTo: document.getElementById('exportTo'),
            exportButton: document.getElementById('exportButton')
        },
        // drafts: 아직 저장하지 않은 행 입력("c12"·"p3" → 값). 한 행을 저장하면 표 전체를 다시 그리므로 다른 행의 입력을 되살린다
        settings = { submitting: false, drafts: {}, cycleDirty: false },
        url = {
            load: contextPath + '/ledger/settings/load',
            summary: contextPath + '/ledger/dashboard/summary',
            cycleSave: contextPath + '/ledger/settings/cycle/save',
            categorySave: contextPath + '/ledger/settings/category/save',
            categoryDelete: contextPath + '/ledger/settings/category/delete',
            categoryMove: contextPath + '/ledger/settings/category/move',
            paymentSave: contextPath + '/ledger/settings/payment/save',
            paymentDelete: contextPath + '/ledger/settings/payment/delete',
            paymentMove: contextPath + '/ledger/settings/payment/move',
            exportCsv: contextPath + '/ledger/settings/export'
        },

        init = function(){
            m$.cycleSave.addEventListener('click', saveCycle);
            [m$.payDay, m$.payDayAdjust].forEach(function(el){
                el.addEventListener('change', function(){ settings.cycleDirty = true; });
            });
            m$.categoryAddSave.addEventListener('click', addCategory);
            m$.categoryAddCancel.addEventListener('click', function(){
                resetCategoryAdd();
                m$.categoryAdd.open = false;
            });
            m$.paymentAddSave.addEventListener('click', addPayment);
            m$.paymentAddCancel.addEventListener('click', function(){
                m$.newPaymentName.value = '';
                m$.paymentAdd.open = false;
            });
            initExport();
            load();
        },

        // 기본 기간: 이번 달 1일 ~ 오늘
        initExport = function(){
            var d = new Date(),
                ym = d.getFullYear() + '-' + (d.getMonth() < 9 ? '0' : '') + (d.getMonth() + 1);
            m$.exportFrom.value = ym + '-01';
            m$.exportTo.value = ym + '-' + (d.getDate() < 10 ? '0' : '') + d.getDate();
            m$.exportButton.addEventListener('click', exportCsv);
        },

        // 파일 다운로드라 페이지 이동으로 받는다(서버가 attachment 로 응답해 화면은 그대로)
        exportCsv = function(){
            var from = m$.exportFrom.value, to = m$.exportTo.value;
            if (!from || !to) {
                _error('알림', '시작일과 종료일을 입력해주세요.');
                return;
            }
            if (from > to) {
                _error('알림', '시작일이 종료일보다 늦어요.');
                return;
            }
            location.href = url.exportCsv + '?' + new URLSearchParams({from: from, to: to}).toString();
        },

        load = function(){
            App.post(url.load)
                .then(function(res){
                    App.result(res, { ok: function(){ render(res.data); } });
                })
                .catch(function(){});
            loadCycle();
        },

        // "2026-09-26" -> "9월 26일"
        dayLabel = function(d){
            var p = d.split('-');
            return Number(p[1]) + '월 ' + Number(p[2]) + '일';
        },

        // 저장된 설정으로 계산한 현재 주기 "9월 26일~10월 25일"
        loadCycle = function(){
            App.post(url.summary)
                .then(function(res){
                    App.result(res, { ok: function(){
                        m$.cycleExample.textContent = dayLabel(res.data.cycleStart) + '~' + dayLabel(res.data.cycleEnd);
                    } });
                })
                .catch(function(){});
        },

        render = function(data){
            if (!settings.cycleDirty) {
                m$.payDay.value = String(data.payDay);
                m$.payDayAdjust.checked = !!data.payDayAdjust;
            }
            renderRows(m$.expenseBody, data.categories.filter(function(c){ return c.type === 'EXPENSE'; }), categoryRow, 5, '지출 카테고리가 없어요.');
            renderRows(m$.incomeBody, data.categories.filter(function(c){ return c.type === 'INCOME'; }), categoryRow, 5, '수입 카테고리가 없어요.');
            renderRows(m$.paymentBody, data.paymentMethods, paymentRow, 4, '결제수단이 없어요.');
        },

        renderRows = function(tbody, list, rowFn, cols, emptyText){
            var rows = list.map(rowFn);
            if (!rows.length) {
                rows = [App.h('tr', null, [App.h('td', {attrs: {colspan: cols}, text: emptyText})])];
            }
            tbody.replaceChildren.apply(tbody, rows);
        },

        // 요청 공통: 중복 전송을 막고, 성공하면 짧게 알린 뒤 다시 불러온다
        //   opts.title 성공 문구, opts.done 성공 후 추가 동작, opts.inUse 95(사용 중) 처리
        send = function(u, param, opts){
            opts = opts || {};
            if (settings.submitting) return;
            settings.submitting = true;
            var handlers = {
                ok: function(){
                    App.saved(opts.title);
                    if (opts.done) opts.done();
                    load();
                }
            };
            handlers[App.CODE.NOT_FOUND] = function(){
                _error('찾을 수 없어요', '이미 삭제되었거나 권한이 없는 항목이에요.');
                load();
            };
            if (opts.inUse) handlers[App.CODE.IN_USE] = opts.inUse;

            App.post(u, param)
                .then(function(res){ App.result(res, handlers); })
                .catch(function(){})
                .then(function(){ settings.submitting = false; });
        },

        // 삭제 확인 → 사용 중(95)이면 숨기기를 제안
        confirmDelete = function(name, u, id, hide, key){
            _confirm('삭제할까요?', '‘' + name + '’ 항목을 삭제해요.', function(){
                send(u, {id: id}, {
                    title: '삭제했어요',
                    done: clearDraft(key),
                    inUse: function(res){
                        _confirm('사용 중인 항목이에요', res.message + '\n대신 숨길까요?', hide);
                    }
                });
            });
        },

        saveCycle = function(){
            send(url.cycleSave, {payDay: m$.payDay.value, payDayAdjust: m$.payDayAdjust.checked},
                {done: function(){ settings.cycleDirty = false; }});
        },

        // 행 입력칸들을 저장 전 입력(drafts)과 연결한다. inputs: {이름: input}, original: 저장된 값
        bindDraft = function(key, inputs, original){
            var value = function(el){ return el.type === 'checkbox' ? el.checked : el.value; },
                draft = settings.drafts[key];
            Object.keys(inputs).forEach(function(k){
                var el = inputs[k];
                if (draft && k in draft) {
                    if (el.type === 'checkbox') el.checked = draft[k]; else el.value = draft[k];
                }
                el.addEventListener(el.type === 'checkbox' ? 'change' : 'input', function(){
                    var now = {}, changed = false;
                    Object.keys(inputs).forEach(function(n){
                        now[n] = value(inputs[n]);
                        if (String(now[n]) !== String(original[n])) changed = true;
                    });
                    if (changed) settings.drafts[key] = now; else delete settings.drafts[key];
                });
            });
        },

        clearDraft = function(key){
            return function(){ delete settings.drafts[key]; };
        },

        field = function(label, input){
            return App.h('label', {className: 'field'}, [App.h('span', {text: label}), input]);
        },

        switchField = function(input){
            return App.h('label', {className: 'switch'}, [input, App.h('span', {attrs: {'aria-hidden': 'true'}})]);
        },

        smallButton = function(text, onClick, extraClass, label){
            return App.h('button', {
                type: 'button',
                className: 'button small' + (extraClass ? ' ' + extraClass : ''),
                text: text,
                attrs: label ? {'aria-label': label} : null,
                on: {click: onClick}
            });
        },

        categoryRow = function(c){
            var name = App.h('input', {type: 'text', value: c.name, maxLength: 50}),
                group = App.h('input', {type: 'text', value: c.groupName || '', maxLength: 50, placeholder: '없음'}),
                order = App.h('input', {type: 'number', value: c.sortOrder, min: 0, max: 9999, inputMode: 'numeric'}),
                active = App.h('input', {type: 'checkbox', checked: !!c.active, attrs: {'aria-label': c.name + ' 표시'}}),
                key = 'c' + c.id,
                save = function(){
                    send(url.categorySave, {id: c.id, type: c.type, name: name.value, groupName: group.value, sortOrder: order.value, active: active.checked},
                        {done: clearDraft(key)});
                },
                move = function(direction){
                    return function(){ send(url.categoryMove, {id: c.id, direction: direction}, {title: '순서를 바꿨어요'}); };
                };

            bindDraft(key, {name: name, group: group, order: order, active: active},
                {name: c.name, group: c.groupName || '', order: c.sortOrder, active: !!c.active});

            return App.h('tr', null, [
                App.h('th', {attrs: {scope: 'row'}}, [field('이름', name)]),
                App.h('td', {attrs: {'data-label': '그룹'}}, [field('그룹명', group)]),
                App.h('td', {attrs: {'data-label': '표시 순서'}}, [
                    App.h('div', {className: 'order-control'}, [
                        field('순서', order),
                        smallButton('↑', move('UP'), null, c.name + ' 위로 이동'),
                        smallButton('↓', move('DOWN'), null, c.name + ' 아래로 이동')
                    ])
                ]),
                App.h('td', {attrs: {'data-label': '표시'}}, [switchField(active)]),
                App.h('td', {attrs: {'data-label': '관리'}}, [
                    smallButton('저장', function(){ save(); }),
                    ' ',
                    smallButton('삭제', function(){
                        // 숨기기는 저장된 원래 값으로(입력칸에서 고치다 만 값까지 저장하지 않게)
                        confirmDelete(c.name, url.categoryDelete, c.id, function(){
                            send(url.categorySave, {id: c.id, type: c.type, name: c.name, groupName: c.groupName || '', sortOrder: c.sortOrder, active: false},
                                {title: '숨겼어요', done: clearDraft(key)});
                        }, key);
                    }, 'danger')
                ])
            ]);
        },

        paymentRow = function(p){
            var name = App.h('input', {type: 'text', value: p.name, maxLength: 50}),
                active = App.h('input', {type: 'checkbox', checked: !!p.active, attrs: {'aria-label': p.name + ' 표시'}}),
                key = 'p' + p.id,
                save = function(){
                    send(url.paymentSave, {id: p.id, name: name.value, active: active.checked}, {done: clearDraft(key)});
                },
                move = function(direction){
                    return function(){ send(url.paymentMove, {id: p.id, direction: direction}, {title: '순서를 바꿨어요'}); };
                };

            bindDraft(key, {name: name, active: active}, {name: p.name, active: !!p.active});

            return App.h('tr', null, [
                App.h('th', {attrs: {scope: 'row'}}, [field('결제수단 이름', name)]),
                App.h('td', {attrs: {'data-label': '순서'}}, [
                    App.h('div', {className: 'order-control'}, [
                        smallButton('↑', move('UP'), null, p.name + ' 위로 이동'),
                        smallButton('↓', move('DOWN'), null, p.name + ' 아래로 이동')
                    ])
                ]),
                App.h('td', {attrs: {'data-label': '표시'}}, [switchField(active)]),
                App.h('td', {attrs: {'data-label': '관리'}}, [
                    smallButton('저장', function(){ save(); }),
                    ' ',
                    smallButton('삭제', function(){
                        confirmDelete(p.name, url.paymentDelete, p.id, function(){
                            send(url.paymentSave, {id: p.id, name: p.name, active: false}, {title: '숨겼어요', done: clearDraft(key)});
                        }, key);
                    }, 'danger')
                ])
            ]);
        },

        resetCategoryAdd = function(){
            m$.newCategoryName.value = '';
            m$.newCategoryGroup.value = '';
            m$.newCategoryOrder.value = '';
        },

        addCategory = function(){
            var checked = document.querySelector('input[name="newCategoryType"]:checked');
            if (App.isEmpty(m$.newCategoryName.value.trim())) {
                _error('알림', '카테고리 이름을 입력해주세요.');
                return;
            }
            send(url.categorySave, {
                type: checked ? checked.value : 'EXPENSE',
                name: m$.newCategoryName.value,
                groupName: m$.newCategoryGroup.value,
                sortOrder: m$.newCategoryOrder.value
            }, {title: '추가했어요', done: resetCategoryAdd});
        },

        addPayment = function(){
            if (App.isEmpty(m$.newPaymentName.value.trim())) {
                _error('알림', '결제수단 이름을 입력해주세요.');
                return;
            }
            send(url.paymentSave, {name: m$.newPaymentName.value}, {
                title: '추가했어요',
                done: function(){ m$.newPaymentName.value = ''; }
            });
        };

    return { init: init };
}());

document.addEventListener('DOMContentLoaded', function(){
    App.settings.init();
});
