App.settings = (function(){
	var m$ = {
			payDay: document.getElementById('payDay'),
			payDayAdjust: document.getElementById('payDayAdjust'),
			cycleSave: document.getElementById('cycleSave'),
			cycleRuler: document.getElementById('cycleRuler'),
			cycleRange: document.getElementById('cycleRange'),
			cycleFill: document.getElementById('cycleFill'),
			cycleCaption: document.getElementById('cycleCaption'),
			openingBalance: document.getElementById('openingBalance'),
			openingDate: document.getElementById('openingDate'),
			openingSave: document.getElementById('openingSave'),
			passwordForm: document.getElementById('passwordForm'),
			currentPassword: document.getElementById('currentPassword'),
			newPassword: document.getElementById('newPassword'),
			newPasswordConfirm: document.getElementById('newPasswordConfirm'),
			expenseList: document.getElementById('expenseCategoryList'),
			incomeList: document.getElementById('incomeCategoryList'),
			categoryOrderStatus: document.getElementById('categoryOrderStatus'),
			categoryOpen: document.getElementById('categoryOpen'),
			categoryDialog: document.getElementById('categoryDialog'),
			categoryAdd: document.getElementById('categoryAdd'),
			categoryAddOpen: document.getElementById('categoryAddOpen'),
			newCategoryName: document.getElementById('newCategoryName'),
			newCategoryGroup: document.getElementById('newCategoryGroup'),
			categoryAddSave: document.getElementById('categoryAddSave'),
			categoryAddCancel: document.getElementById('categoryAddCancel'),
			paymentList: document.getElementById('paymentList'),
			paymentOrderStatus: document.getElementById('paymentOrderStatus'),
			paymentOpen: document.getElementById('paymentOpen'),
			paymentDialog: document.getElementById('paymentDialog'),
			paymentAdd: document.getElementById('paymentAdd'),
			paymentAddOpen: document.getElementById('paymentAddOpen'),
			newPaymentName: document.getElementById('newPaymentName'),
			paymentAddSave: document.getElementById('paymentAddSave'),
			paymentAddCancel: document.getElementById('paymentAddCancel'),
			exportPresets: document.getElementById('exportPresets'),
			exportFrom: document.getElementById('exportFrom'),
			exportTo: document.getElementById('exportTo'),
			exportButton: document.getElementById('exportButton')
		},

		settings = {
			submitting: false,
			/* 아직 저장하지 않은 행 입력("c12"·"p3" → 값). 한 행을 저장하면 목록 전체를 다시 그리므로 다른 행의 입력을 되살린다 */
			drafts: {},
			/* 마지막으로 받은 목록(창을 닫으며 입력을 버릴 때 요청 없이 목록을 원래 값으로 다시 그린다) */
			data: null,
			cycleDirty: false,
			openingDirty: false,
			/* 저장된 설정으로 계산한 이번 주기 {start, end}(내보내기 '이번 주기'에도 쓴다) */
			cycle: null
		},

		url = {
			load: contextPath + '/ledger/settings/load',
			summary: contextPath + '/ledger/dashboard/summary',
			cycleSave: contextPath + '/ledger/settings/cycle/save',
			openingSave: contextPath + '/ledger/settings/opening/save',
			password: contextPath + '/ledger/account/password',
			categorySave: contextPath + '/ledger/settings/category/save',
			categoryDelete: contextPath + '/ledger/settings/category/delete',
			categoryOrder: contextPath + '/ledger/settings/category/order',
			paymentSave: contextPath + '/ledger/settings/payment/save',
			paymentDelete: contextPath + '/ledger/settings/payment/delete',
			paymentOrder: contextPath + '/ledger/settings/payment/order',
			exportCsv: contextPath + '/ledger/settings/export'
		},

		init = function(){
			bindEvent();
			applyExportRange('month');
			load();
		},

		bindEvent = function(){
			m$.cycleSave.addEventListener('click', saveCycle);
			m$.openingSave.addEventListener('click', saveOpening);
			m$.passwordForm.addEventListener('submit', function(e){
				e.preventDefault();
				changePassword();
			});
			m$.openingBalance.addEventListener('input', function(){
				settings.openingDirty = true;
				formatSigned(m$.openingBalance);
			});
			m$.openingDate.addEventListener('change', function(){ settings.openingDirty = true; });
			[m$.payDay, m$.payDayAdjust].forEach(function(el){
				el.addEventListener('change', function(){
					settings.cycleDirty = true;
					renderCycle();
				});
			});
			m$.categoryAddOpen.addEventListener('click', function(){ toggleAdd(m$.categoryAdd, m$.categoryAddOpen, m$.newCategoryName); });
			m$.paymentAddOpen.addEventListener('click', function(){ toggleAdd(m$.paymentAdd, m$.paymentAddOpen, m$.newPaymentName); });
			m$.categoryAddSave.addEventListener('click', addCategory);
			m$.categoryAddCancel.addEventListener('click', closeCategoryAdd);
			m$.paymentAddSave.addEventListener('click', addPayment);
			m$.paymentAddCancel.addEventListener('click', closePaymentAdd);
			m$.categoryOpen.addEventListener('click', function(){ m$.categoryDialog.showModal(); });
			m$.paymentOpen.addEventListener('click', function(){ m$.paymentDialog.showModal(); });
			/* 창을 닫으면 추가 칸도 접는다(다음에 열 때 목록부터 보이게) */
			m$.categoryDialog.addEventListener('close', closeCategoryAdd);
			m$.paymentDialog.addEventListener('close', closePaymentAdd);
			App.bindDialog(m$.categoryDialog, function(){
				return hasDraft('c') || !!(m$.newCategoryName.value.trim() || m$.newCategoryGroup.value.trim());
			}, function(){
				discardDrafts('c');
				closeCategoryAdd();
			});
			App.bindDialog(m$.paymentDialog, function(){
				return hasDraft('p') || !!m$.newPaymentName.value.trim();
			}, function(){
				discardDrafts('p');
				closePaymentAdd();
			});
			bindSort(m$.expenseList, m$.categoryOrderStatus);
			bindSort(m$.incomeList, m$.categoryOrderStatus);
			bindSort(m$.paymentList, m$.paymentOrderStatus);
			m$.exportPresets.addEventListener('change', function(e){ applyExportRange(e.target.value); });
			[m$.exportFrom, m$.exportTo].forEach(function(el){
				el.addEventListener('input', function(){
					m$.exportPresets.querySelectorAll('input').forEach(function(r){ r.checked = false; });
				});
			});
			m$.exportButton.addEventListener('click', exportCsv);
		},

		/* Date → "2026-09-28"(로컬 날짜) */
		iso = function(d){
			return d.getFullYear() + '-' + String(d.getMonth() + 1).padStart(2, '0') + '-' + String(d.getDate()).padStart(2, '0');
		},

		/* "2026-09-28" → 로컬 자정 Date */
		parseDay = function(s){
			var p = s.split('-');
			return new Date(Number(p[0]), Number(p[1]) - 1, Number(p[2]));
		},

		/* 기간 빠른 선택. 날짜를 직접 바꾸면 선택을 푼다. 기본은 이번 달(주기를 불러오면 이번 주기) */
		exportRanges = {
			cycle: function(){ return settings.cycle; },
			month: function(t){ return {start: new Date(t.getFullYear(), t.getMonth(), 1), end: new Date(t.getFullYear(), t.getMonth() + 1, 0)}; },
			lastMonth: function(t){ return {start: new Date(t.getFullYear(), t.getMonth() - 1, 1), end: new Date(t.getFullYear(), t.getMonth(), 0)}; },
			year: function(t){ return {start: new Date(t.getFullYear(), 0, 1), end: new Date(t.getFullYear(), 11, 31)}; }
		},

		applyExportRange = function(key){
			var r = exportRanges[key](new Date());
			if(!r) return;
			m$.exportFrom.value = iso(r.start);
			m$.exportTo.value = iso(r.end);
			m$.exportPresets.querySelector('input[value="' + key + '"]').checked = true;
		},

		/* 파일 다운로드라 페이지 이동으로 받는다(서버가 attachment 로 응답해 화면은 그대로) */
		exportCsv = function(){
			var from = m$.exportFrom.value, to = m$.exportTo.value;
			if(!from || !to){
				_error('알림', '시작일과 종료일을 입력해주세요.');
				return;
			}
			if(from > to){
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

		/* Date → "9월 26일" */
		dayLabel = function(d){ return (d.getMonth() + 1) + '월 ' + d.getDate() + '일'; },

		/* 저장된 설정으로 계산한 이번 주기 */
		loadCycle = function(){
			App.post(url.summary)
				.then(handleCycleResult)
				.catch(function(){});
		},

		handleCycleResult = function(res){
			App.result(res, { ok: function(){
				var first = !settings.cycle;
				settings.cycle = {start: parseDay(res.data.cycleStart), end: parseDay(res.data.cycleEnd)};
				renderCycle();
				var preset = m$.exportPresets.querySelector('input:checked');
				if(first ? preset && preset.value === 'month' : preset && preset.value === 'cycle') applyExportRange('cycle');
			} });
		},

		/* 주기 막대: 시작~끝 사이에서 오늘의 위치. 저장 전 변경이 있으면 흐리게 두고 저장하면 다시 계산됨을 알린다 */
		renderCycle = function(){
			var c = settings.cycle;
			if(!c) return;
			var day = 86400000,
				total = Math.round((c.end - c.start) / day) + 1,
				today = parseDay(iso(new Date())),
				n = Math.min(Math.max(Math.round((today - c.start) / day) + 1, 0), total);
			m$.cycleRange.textContent = dayLabel(c.start) + ' ~ ' + dayLabel(c.end);
			m$.cycleFill.style.width = (n / total * 100) + '%';
			m$.cycleRuler.classList.toggle('is-stale', settings.cycleDirty);
			m$.cycleCaption.textContent = settings.cycleDirty
				? '저장하면 새 시작일로 주기를 다시 계산해요.'
				: '오늘은 ' + total + '일 중 ' + n + '일째, ' + (total - n) + '일 남았어요.';
		},

		render = function(data){
			var focused = document.activeElement && document.activeElement.getAttribute('data-handle');
			settings.data = data;
			if(!settings.cycleDirty){
				m$.payDay.value = String(data.payDay);
				m$.payDayAdjust.checked = !!data.payDayAdjust;
			}
			if(!settings.openingDirty){
				m$.openingBalance.value = data.openingDate ? signedNumber(data.openingBalance) : '';
				m$.openingDate.value = data.openingDate || '';
			}
			renderList(m$.expenseList, data.categories.filter(function(c){ return c.type === 'EXPENSE'; }), categoryItem, '지출 카테고리가 없어요.');
			renderList(m$.incomeList, data.categories.filter(function(c){ return c.type === 'INCOME'; }), categoryItem, '수입 카테고리가 없어요.');
			renderList(m$.paymentList, data.paymentMethods, paymentItem, '결제수단이 없어요.');
			/* 키보드로 순서를 옮기던 손잡이에 초점을 되돌린다(다시 그리면 요소가 바뀐다) */
			if(focused){
				var handle = document.querySelector('[data-handle="' + focused + '"]');
				if(handle) handle.focus();
			}
		},

		renderList = function(list, items, itemFn, emptyText){
			var rows = items.map(itemFn);
			if(!rows.length) rows = [App.h('li', {className: 'sort-empty', text: emptyText})];
			list.replaceChildren.apply(list, rows);
		},

		/* 요청 공통: 중복 전송을 막고, 성공하면 짧게 알린 뒤 다시 불러온다 */
		/* opts.title 성공 문구, opts.done 성공 후 추가 동작, opts.inUse 95(사용 중) 처리 */
		send = function(u, param, opts){
			opts = opts || {};
			if(settings.submitting) return;
			settings.submitting = true;
			var handlers = {
				ok: function(){
					App.saved(opts.title);
					if(opts.done) opts.done();
					load();
				}
			};
			handlers[App.CODE.NOT_FOUND] = function(){
				_error('찾을 수 없어요', '이미 삭제되었거나 권한이 없는 항목이에요.');
				load();
			};
			if(opts.inUse) handlers[App.CODE.IN_USE] = opts.inUse;

			App.post(u, param)
				.then(function(res){ App.result(res, handlers); })
				.catch(function(){})
				.then(function(){ settings.submitting = false; });
		},

		/* 목록 순서 저장: 화면의 순서 그대로 전체 id 를 보낸다. 결과와 상관없이 다시 불러와 서버 순서로 맞춘다. */
		/* 다른 창에서 항목이 추가·삭제돼 목록이 달라졌으면 서버가 91 로 거절한다 */
		saveOrder = function(list, status, name){
			var ids = Array.prototype.map.call(list.querySelectorAll('.sort-item'), function(li){ return Number(li.dataset.id); }),
				param = list.dataset.type ? {type: list.dataset.type, ids: ids} : {ids: ids},
				handlers = {ok: function(){
					var pos = ids.indexOf(Number(list.dataset.moved)) + 1;
					status.textContent = '‘' + name + '’ 항목을 ' + pos + '번째로 옮겼어요.';
				}};
			handlers[App.CODE.NOT_FOUND] = function(){
				_error('목록이 바뀌었어요', '다른 곳에서 항목이 추가되거나 삭제됐어요. 최신 목록으로 다시 보여 드릴게요.');
			};
			App.post(list.dataset.type ? url.categoryOrder : url.paymentOrder, param)
				.then(function(res){ App.result(res, handlers); })
				.catch(function(){})
				.then(load);
		},

		itemsOf = function(list){ return Array.prototype.slice.call(list.querySelectorAll('.sort-item')); },

		/* 손잡이로 순서 바꾸기. 마우스·터치는 Pointer Events(터치에서는 HTML 드래그앤드롭이 동작하지 않는다), 키보드는 ↑·↓. */
		/* 끄는 동안에는 항목을 바로 옮겨 보여 주고, 놓았을 때 순서가 달라졌으면 한 번 저장한다 */
		bindSort = function(list, status){
			var box = list.closest('dialog'),
				keySave = App.debounce(function(item){ saveOrder(list, status, item.dataset.name); }, 400);

			list.addEventListener('pointerdown', function(e){
				var handle = e.target.closest('.drag-handle');
				if(!handle || e.button !== 0) return;
				var item = handle.closest('.sort-item'),
					before = itemsOf(list).indexOf(item);
				e.preventDefault();
				item.classList.add('is-dragging');
				list.classList.add('is-sorting');

				var move = function(ev){
					var r = box.getBoundingClientRect();
					if(ev.clientY < r.top + 70) box.scrollBy(0, -14);
					else if(ev.clientY > r.bottom - 70) box.scrollBy(0, 14);
					itemsOf(list).some(function(li){
						if(li === item) return false;
						var b = li.getBoundingClientRect();
						if(ev.clientY < b.top || ev.clientY > b.bottom) return false;
						list.insertBefore(item, ev.clientY < b.top + b.height / 2 ? li : li.nextSibling);
						return true;
					});
				};
				/* 항목을 옮기면(insertBefore) 손잡이의 포인터 캡처가 풀리므로 window 에서 받는다 */
				var end = function(){
					window.removeEventListener('pointermove', move);
					window.removeEventListener('pointerup', end);
					window.removeEventListener('pointercancel', end);
					item.classList.remove('is-dragging');
					list.classList.remove('is-sorting');
					if(itemsOf(list).indexOf(item) !== before){
						list.dataset.moved = item.dataset.id;
						saveOrder(list, status, item.dataset.name);
					}
				};
				window.addEventListener('pointermove', move);
				window.addEventListener('pointerup', end);
				window.addEventListener('pointercancel', end);
			});

			list.addEventListener('keydown', function(e){
				var handle = e.target.closest('.drag-handle');
				if(!handle || (e.key !== 'ArrowUp' && e.key !== 'ArrowDown')) return;
				var item = handle.closest('.sort-item'),
					target = e.key === 'ArrowUp' ? item.previousElementSibling : item.nextElementSibling;
				e.preventDefault();
				if(!target || !target.classList.contains('sort-item')) return;
				list.insertBefore(item, e.key === 'ArrowUp' ? target : target.nextSibling);
				handle.focus();
				list.dataset.moved = item.dataset.id;
				status.textContent = '‘' + item.dataset.name + '’ ' + (itemsOf(list).indexOf(item) + 1) + '번째';
				keySave(item);
			});
		},

		/* 삭제 확인 → 사용 중(95)이면 숨기기를 제안 */
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

		/* 시작 잔액 칸: 앞의 -(음수)와 숫자·쉼표만 서식 정리, 그 밖의 입력은 값을 두고 오류 표시 */
		signedNumber = function(n){ return (n < 0 ? '-' : '') + Math.abs(n).toLocaleString('ko-KR'); },

		parseSigned = function(s){
			var t = String(s).replace(/[,\s]/g, '').replace('−', '-');
			return /^-?\d{1,11}$/.test(t) ? Number(t) : null;
		},

		formatSigned = function(input){
			var t = input.value.replace(/[,\s]/g, '').replace('−', '-');
			if(t === '' || t === '-'){
				input.removeAttribute('aria-invalid');
			} else if(/^-?\d{1,11}$/.test(t)){
				input.value = signedNumber(Number(t));
				input.removeAttribute('aria-invalid');
			} else {
				input.setAttribute('aria-invalid', 'true');
			}
		},

		saveOpening = function(){
			var date = m$.openingDate.value,
				amount = m$.openingBalance.value.trim() ? parseSigned(m$.openingBalance.value) : 0;
			if(date && amount === null){
				_error('알림', '시작 잔액은 숫자만 입력해주세요. (음수는 앞에 -, 소수점 불가, 최대 11자리)');
				return;
			}
			send(url.openingSave, {openingBalance: date ? amount : 0, openingDate: date},
				{title: date ? '저장했어요' : '시작 잔액을 해제했어요', done: function(){ settings.openingDirty = false; }});
		},

		/* 비밀번호 변경은 목록을 다시 그릴 필요가 없어 send() 대신 직접 보낸다 */
		changePassword = function(){
			var next = m$.newPassword.value;
			if(!m$.currentPassword.value || !next || !m$.newPasswordConfirm.value){
				_error('알림', '현재 비밀번호와 새 비밀번호를 모두 입력해주세요.');
				return;
			}
			if(next.length < 8 || new TextEncoder().encode(next).length > 72){
				_error('알림', '새 비밀번호는 8자 이상, 72바이트 이하로 입력해주세요.');
				return;
			}
			if(next !== m$.newPasswordConfirm.value){
				_error('알림', '새 비밀번호 확인이 일치하지 않아요.');
				return;
			}
			if(settings.submitting) return;
			settings.submitting = true;
			var handlers = {ok: function(){
				m$.passwordForm.reset();
				_alert('비밀번호를 바꿨어요', '다음 로그인부터 새 비밀번호를 써 주세요.');
			}};
			handlers[App.CODE.LOGIN_BLOCKED] = function(){
				_error('잠시 후 다시 시도해 주세요', '현재 비밀번호를 여러 번 틀렸어요. 5분 뒤에 다시 시도해 주세요.');
			};
			App.post(url.password, {currentPassword: m$.currentPassword.value, newPassword: next, newPasswordConfirm: m$.newPasswordConfirm.value})
				.then(function(res){ App.result(res, handlers); })
				.catch(function(){})
				.then(function(){ settings.submitting = false; });
		},

		saveCycle = function(){
			send(url.cycleSave, {payDay: m$.payDay.value, payDayAdjust: m$.payDayAdjust.checked},
				{done: function(){ settings.cycleDirty = false; }});
		},

		/* 행 입력칸들을 저장 전 입력(drafts)과 연결한다. inputs: {이름: input}, original: 저장된 값 */
		bindDraft = function(key, inputs, original){
			var value = function(el){ return el.type === 'checkbox' ? el.checked : el.value; },
				draft = settings.drafts[key];
			Object.keys(inputs).forEach(function(k){
				var el = inputs[k];
				if(draft && k in draft){
					if(el.type === 'checkbox') el.checked = draft[k]; else el.value = draft[k];
				}
				el.addEventListener(el.type === 'checkbox' ? 'change' : 'input', function(){
					var now = {}, changed = false;
					Object.keys(inputs).forEach(function(n){
						now[n] = value(inputs[n]);
						if(String(now[n]) !== String(original[n])) changed = true;
					});
					if(changed) settings.drafts[key] = now; else delete settings.drafts[key];
				});
			});
		},

		clearDraft = function(key){
			return function(){ delete settings.drafts[key]; };
		},

		/* prefix: 'c' 카테고리, 'p' 결제수단 */
		hasDraft = function(prefix){
			return Object.keys(settings.drafts).some(function(k){ return k.charAt(0) === prefix; });
		},

		/* 해당 목록의 저장 전 입력을 버리고 저장된 값으로 다시 그린다(다른 목록의 입력은 drafts 로 되살아난다) */
		discardDrafts = function(prefix){
			Object.keys(settings.drafts).forEach(function(k){
				if(k.charAt(0) === prefix) delete settings.drafts[k];
			});
			if(settings.data) render(settings.data);
		},

		/* 목록 입력칸. shown 이면 이름표를 칸 앞에 보이고, 아니면 스크린리더에만 읽힌다 */
		field = function(label, input, extraClass, shown){
			return App.h('label', {className: 'field' + (extraClass ? ' ' + extraClass : '')}, [App.h('span', {className: shown ? null : 'sr-only', text: label}), input]);
		},

		switchField = function(input){
			return App.h('label', {className: 'switch'}, [input, App.h('span', {attrs: {'aria-hidden': 'true'}})]);
		},

		smallButton = function(text, onClick, extraClass){
			return App.h('button', {
				type: 'button',
				className: 'button small' + (extraClass ? ' ' + extraClass : ''),
				text: text,
				on: {click: onClick}
			});
		},

		/* 목록 한 줄: 손잡이 / 입력칸들 / 표시 스위치 / 저장·삭제. key 는 drafts·초점 복원에 쓰는 "c12"·"p3" */
		sortItem = function(key, id, name, active, fields, actions){
			return App.h('li', {className: 'sort-item' + (active ? '' : ' is-inactive'), attrs: {'data-id': id, 'data-name': name}}, [
				App.h('button', {
					type: 'button',
					className: 'drag-handle',
					attrs: {'data-handle': key, 'aria-label': '‘' + name + '’ 순서 옮기기'}
				}, [App.icon('grip')]),
				App.h('div', {className: 'sort-fields'}, fields),
				App.h('div', {className: 'sort-actions'}, actions)
			]);
		},

		categoryItem = function(c){
			var name = App.h('input', {type: 'text', value: c.name, maxLength: 50}),
				group = App.h('input', {type: 'text', value: c.groupName || '', maxLength: 50, placeholder: '그룹 없음'}),
				active = App.h('input', {type: 'checkbox', checked: !!c.active, attrs: {'aria-label': c.name + ' 거래에서 보이기'}}),
				key = 'c' + c.id;

			bindDraft(key, {name: name, group: group, active: active},
				{name: c.name, group: c.groupName || '', active: !!c.active});

			return sortItem(key, c.id, c.name, c.active, [
				field('이름', name, 'sort-name'),
				field('그룹', group, 'sort-group', true)
			], [
				switchField(active),
				smallButton('저장', function(){
					send(url.categorySave, {id: c.id, type: c.type, name: name.value, groupName: group.value, active: active.checked},
						{done: clearDraft(key)});
				}),
				smallButton('삭제', function(){
					/* 숨기기는 저장된 원래 값으로(입력칸에서 고치다 만 값까지 저장하지 않게) */
					confirmDelete(c.name, url.categoryDelete, c.id, function(){
						send(url.categorySave, {id: c.id, type: c.type, name: c.name, groupName: c.groupName || '', active: false},
							{title: '숨겼어요', done: clearDraft(key)});
					}, key);
				}, 'danger')
			]);
		},

		paymentItem = function(p){
			var name = App.h('input', {type: 'text', value: p.name, maxLength: 50}),
				active = App.h('input', {type: 'checkbox', checked: !!p.active, attrs: {'aria-label': p.name + ' 거래에서 보이기'}}),
				key = 'p' + p.id;

			bindDraft(key, {name: name, active: active}, {name: p.name, active: !!p.active});

			return sortItem(key, p.id, p.name, p.active, [
				field('결제수단 이름', name, 'sort-name')
			], [
				switchField(active),
				smallButton('저장', function(){
					send(url.paymentSave, {id: p.id, name: name.value, active: active.checked}, {done: clearDraft(key)});
				}),
				smallButton('삭제', function(){
					confirmDelete(p.name, url.paymentDelete, p.id, function(){
						send(url.paymentSave, {id: p.id, name: p.name, active: false}, {title: '숨겼어요', done: clearDraft(key)});
					}, key);
				}, 'danger')
			]);
		},

		resetCategoryAdd = function(){
			m$.newCategoryName.value = '';
			m$.newCategoryGroup.value = '';
		},

		/* 창 위쪽 '추가' 버튼: 목록 위의 추가 칸을 열고 닫는다. 열면 맨 위로 올려 이름 칸에 초점 */
		toggleAdd = function(panel, opener, input, show){
			if(show === undefined) show = panel.hidden;
			panel.hidden = !show;
			opener.setAttribute('aria-expanded', String(show));
			if(show){
				panel.closest('dialog').scrollTop = 0;
				input.focus();
			}
		},

		closeCategoryAdd = function(){
			resetCategoryAdd();
			toggleAdd(m$.categoryAdd, m$.categoryAddOpen, m$.newCategoryName, false);
		},

		closePaymentAdd = function(){
			m$.newPaymentName.value = '';
			toggleAdd(m$.paymentAdd, m$.paymentAddOpen, m$.newPaymentName, false);
		},

		addCategory = function(){
			var checked = document.querySelector('input[name="newCategoryType"]:checked');
			if(App.isEmpty(m$.newCategoryName.value.trim())){
				_error('알림', '카테고리 이름을 입력해주세요.');
				return;
			}
			send(url.categorySave, {
				type: checked ? checked.value : 'EXPENSE',
				name: m$.newCategoryName.value,
				groupName: m$.newCategoryGroup.value
			}, {title: '추가했어요', done: resetCategoryAdd});
		},

		addPayment = function(){
			if(App.isEmpty(m$.newPaymentName.value.trim())){
				_error('알림', '결제수단 이름을 입력해주세요.');
				return;
			}
			send(url.paymentSave, {name: m$.newPaymentName.value}, {
				title: '추가했어요',
				done: function(){ m$.newPaymentName.value = ''; }
			});
		};

	return{
		init: init
	};
}());

document.addEventListener('DOMContentLoaded', function(){
	App.settings.init();
});
