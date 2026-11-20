App.settings = (function(){
	var m$ = {
			payDay: document.getElementById('payDay'),  /* 주기 시작일 선택 */
			payDayAdjust: document.getElementById('payDayAdjust'),  /* 주말·공휴일 보정 체크 */
			cycleSave: document.getElementById('cycleSave'),  /* 주기 저장 버튼 */
			cycleRuler: document.getElementById('cycleRuler'),  /* 주기 막대 */
			cycleRange: document.getElementById('cycleRange'),  /* 주기 기간 글 */
			cycleFill: document.getElementById('cycleFill'),  /* 막대에서 오늘까지 채운 부분 */
			cycleCaption: document.getElementById('cycleCaption'),  /* 막대 설명 */
			openingBalance: document.getElementById('openingBalance'),  /* 시작 잔액 */
			openingDate: document.getElementById('openingDate'),  /* 시작 잔액 기준일 */
			openingSave: document.getElementById('openingSave'),  /* 시작 잔액 저장 버튼 */
			passwordForm: document.getElementById('passwordForm'),  /* 비밀번호 변경 폼 */
			currentPassword: document.getElementById('currentPassword'),  /* 현재 비밀번호 */
			newPassword: document.getElementById('newPassword'),  /* 새 비밀번호 */
			newPasswordConfirm: document.getElementById('newPasswordConfirm'),  /* 새 비밀번호 확인 */
			dangerForm: document.getElementById('dangerForm'),  /* 기록 초기화·탈퇴 폼 */
			dangerPassword: document.getElementById('dangerPassword'),  /* 초기화·탈퇴용 현재 비밀번호 */
			resetData: document.getElementById('resetData'),  /* 기록 초기화 버튼 */
			withdraw: document.getElementById('withdraw'),  /* 계정 탈퇴 버튼 */
			expenseList: document.getElementById('expenseCategoryList'),  /* 지출 카테고리 목록 */
			incomeList: document.getElementById('incomeCategoryList'),  /* 수입 카테고리 목록 */
			categoryOrderStatus: document.getElementById('categoryOrderStatus'),  /* 카테고리 순서 변경 안내(화면 낭독기) */
			categoryOpen: document.getElementById('categoryOpen'),  /* 카테고리 관리 창 열기 버튼 */
			categoryDialog: document.getElementById('categoryDialog'),  /* 카테고리 관리 창 */
			categoryAdd: document.getElementById('categoryAdd'),  /* 카테고리 추가 칸 */
			categoryAddOpen: document.getElementById('categoryAddOpen'),  /* 카테고리 추가 열기 버튼 */
			newCategoryName: document.getElementById('newCategoryName'),  /* 새 카테고리 이름 */
			newCategoryGroup: document.getElementById('newCategoryGroup'),  /* 새 카테고리 그룹 */
			categoryAddSave: document.getElementById('categoryAddSave'),  /* 카테고리 추가 버튼 */
			categoryAddCancel: document.getElementById('categoryAddCancel'),  /* 카테고리 추가 취소 버튼 */
			paymentList: document.getElementById('paymentList'),  /* 결제수단 목록 */
			paymentOrderStatus: document.getElementById('paymentOrderStatus'),  /* 결제수단 순서 변경 안내(화면 낭독기) */
			paymentOpen: document.getElementById('paymentOpen'),  /* 결제수단 관리 창 열기 버튼 */
			paymentDialog: document.getElementById('paymentDialog'),  /* 결제수단 관리 창 */
			paymentAdd: document.getElementById('paymentAdd'),  /* 결제수단 추가 칸 */
			paymentAddOpen: document.getElementById('paymentAddOpen'),  /* 결제수단 추가 열기 버튼 */
			newPaymentName: document.getElementById('newPaymentName'),  /* 새 결제수단 이름 */
			paymentAddSave: document.getElementById('paymentAddSave'),  /* 결제수단 추가 버튼 */
			paymentAddCancel: document.getElementById('paymentAddCancel'),  /* 결제수단 추가 취소 버튼 */
			exportPresets: document.getElementById('exportPresets'),  /* CSV 내보내기 기간 빠른 선택 */
			exportFrom: document.getElementById('exportFrom'),  /* 내보내기 시작일 */
			exportTo: document.getElementById('exportTo'),  /* 내보내기 종료일 */
			exportButton: document.getElementById('exportButton'),  /* 내보내기 버튼 */
			cycleDialog: document.getElementById('cycleDialog'),  /* 기록 주기 창 */
			openingDialog: document.getElementById('openingDialog'),  /* 시작 잔액 창 */
			exportDialog: document.getElementById('exportDialog'),  /* 내보내기 창 */
			passwordDialog: document.getElementById('passwordDialog'),  /* 비밀번호 변경 창 */
			dangerDialog: document.getElementById('dangerDialog'),  /* 초기화·탈퇴 창 */
			/* 목록 선택 막대(c: 카테고리, p: 결제수단) */
			bulk: {
				c: {all: document.getElementById('categorySelectAll'), count: document.getElementById('categorySelectCount'),
					save: document.getElementById('categoryBulkSave'), del: document.getElementById('categoryBulkDelete')},
				p: {all: document.getElementById('paymentSelectAll'), count: document.getElementById('paymentSelectCount'),
					save: document.getElementById('paymentBulkSave'), del: document.getElementById('paymentBulkDelete')}
			}
		},

		/* 프로필 메뉴의 ?open= 키 → 창 id */
		OPEN_KEYS = {cycle: 'cycleDialog', opening: 'openingDialog', 'export': 'exportDialog', category: 'categoryDialog',
			payment: 'paymentDialog', holiday: 'holidayDialog', password: 'passwordDialog', data: 'dangerDialog'},

		settings = {
			submitting: false,
			/* 아직 저장하지 않은 행 입력("c12"·"p3" → 값). 한 행을 저장하면 목록 전체를 다시 그리므로 다른 행의 입력을 되살린다 */
			drafts: {},
			/* 마지막으로 받은 목록(창을 닫으며 입력을 버릴 때 요청 없이 목록을 원래 값으로 다시 그린다) */
			data: null,
			cycleDirty: false,
			openingDirty: false,
			/* 선택한 목록 줄("c12"·"p3" → true). 다시 그려도 선택을 유지한다 */
			selected: {},
			/* 저장된 설정으로 계산한 이번 주기 {start, end}(내보내기 '이번 주기'에도 쓴다) */
			cycle: null
		},

		url = {
			load: contextPath + '/ledger/settings/load',
			summary: contextPath + '/ledger/dashboard/summary',
			cycleSave: contextPath + '/ledger/settings/cycle/save',
			openingSave: contextPath + '/ledger/settings/opening/save',
			password: contextPath + '/ledger/account/password',
			reset: contextPath + '/ledger/account/reset',
			withdraw: contextPath + '/ledger/account/withdraw',
			categorySave: contextPath + '/ledger/settings/category/save',
			categoryDelete: contextPath + '/ledger/settings/category/delete',
			categoryOrder: contextPath + '/ledger/settings/category/order',
			paymentSave: contextPath + '/ledger/settings/payment/save',
			paymentDelete: contextPath + '/ledger/settings/payment/delete',
			paymentOrder: contextPath + '/ledger/settings/payment/order',
			exportCsv: contextPath + '/ledger/settings/export'
		},

		/* 이벤트 연결, 내보내기 기간을 이번 달로 채우고 설정 불러오기 */
		init = function(){
			var key = new URLSearchParams(location.search).get('open');
			bindEvent();
			applyExportRange('month');
			load();
			if(key && OPEN_KEYS[key]){
				history.replaceState(null, '', location.pathname);   /* 새로고침해도 다시 열리지 않게 */
				openDialog(document.getElementById(OPEN_KEYS[key]));
			}
		},

		/* 창 열기(이미 다른 창이 열려 있으면 겹쳐 열지 않는다) */
		openDialog = function(dialog){
			if(dialog && !dialog.open && !document.querySelector('dialog[open]')) dialog.showModal();
		},

		/* 화면 이벤트 연결 */
		bindEvent = function(){
			m$.cycleSave.addEventListener('click', saveCycle);
			m$.openingSave.addEventListener('click', saveOpening);
			m$.passwordForm.addEventListener('submit', function(e){
				e.preventDefault();
				changePassword();
			});
			/* 비밀번호 칸의 Enter 로는 아무것도 하지 않는다(되돌릴 수 없는 작업은 버튼으로만) */
			m$.dangerForm.addEventListener('submit', function(e){ e.preventDefault(); });
			m$.resetData.addEventListener('click', function(){ dangerAction(false); });
			m$.withdraw.addEventListener('click', function(){ dangerAction(true); });
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
			/* 창을 닫으면 선택도 푼다(다음에 열 때 깨끗하게) */
			m$.categoryDialog.addEventListener('close', function(){ clearSelection('c'); });
			m$.paymentDialog.addEventListener('close', function(){ clearSelection('p'); });
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

			/* 항목 카드 → 그 항목의 창 */
			document.querySelectorAll('[data-open-dialog]').forEach(function(b){
				b.addEventListener('click', function(){ openDialog(document.getElementById(b.getAttribute('data-open-dialog'))); });
			});
			/* 이 화면에서 프로필 메뉴를 누르면 새로 열지 않고 창만 연다 */
			document.addEventListener('click', function(e){
				var link = e.target.closest ? e.target.closest('[data-settings-open]') : null,
					dialog = link && document.getElementById(OPEN_KEYS[link.getAttribute('data-settings-open')]);
				if(!dialog) return;
				e.preventDefault();
				var menu = link.closest('details');
				if(menu) menu.removeAttribute('open');
				openDialog(dialog);
			});
			/* 닫기: 저장하지 않은 입력이 있으면 버릴지 먼저 묻고, 버리면 저장된 값으로 되돌린다 */
			App.bindDialog(m$.cycleDialog, function(){ return settings.cycleDirty; }, function(){
				settings.cycleDirty = false;
				if(settings.data) render(settings.data);
				renderCycle();
			});
			App.bindDialog(m$.openingDialog, function(){ return settings.openingDirty; }, function(){
				settings.openingDirty = false;
				m$.openingBalance.removeAttribute('aria-invalid');
				if(settings.data) render(settings.data);
			});
			App.bindDialog(m$.exportDialog, function(){ return false; }, function(){});
			App.bindDialog(m$.passwordDialog, function(){
				return !!(m$.currentPassword.value || m$.newPassword.value || m$.newPasswordConfirm.value);
			}, function(){ m$.passwordForm.reset(); });
			App.bindDialog(m$.dangerDialog, function(){ return !!m$.dangerPassword.value; }, function(){ m$.dangerPassword.value = ''; });

			['c', 'p'].forEach(function(prefix){
				var b = m$.bulk[prefix];
				b.all.addEventListener('change', function(){
					rowsOf(prefix).forEach(function(li){ setSelected(li, b.all.checked); });
					updateBulk(prefix);
				});
				b.save.addEventListener('click', function(){ bulkSave(prefix); });
				b.del.addEventListener('click', function(){ bulkDelete(prefix); });
			});
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
			/* 이번 주기 */
			cycle: function(){ return settings.cycle; },
			/* 이번 달 */
			month: function(t){ return {start: new Date(t.getFullYear(), t.getMonth(), 1), end: new Date(t.getFullYear(), t.getMonth() + 1, 0)}; },
			/* 지난달 */
			lastMonth: function(t){ return {start: new Date(t.getFullYear(), t.getMonth() - 1, 1), end: new Date(t.getFullYear(), t.getMonth(), 0)}; },
			/* 올해 */
			year: function(t){ return {start: new Date(t.getFullYear(), 0, 1), end: new Date(t.getFullYear(), 11, 31)}; }
		},

		/* 빠른 선택 기간을 내보내기 날짜 칸에 채운다 */
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

		/* 설정 값 받기 */
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

		/* 이번 주기를 받아 주기 막대를 그리고, 내보내기 기간이 주기면 맞춘다 */
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

		/* 설정 값 그리기. 저장하지 않은 입력은 남긴다 */
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
			/* 없어진 줄의 선택은 버리고 선택 막대를 맞춘다 */
			Object.keys(settings.selected).forEach(function(k){
				if(!document.querySelector('[data-handle="' + k + '"]')) delete settings.selected[k];
			});
			updateBulk('c');
			updateBulk('p');
			/* 키보드로 순서를 옮기던 손잡이에 초점을 되돌린다(다시 그리면 요소가 바뀐다) */
			if(focused){
				var handle = document.querySelector('[data-handle="' + focused + '"]');
				if(handle) handle.focus();
			}
		},

		/* 정렬 목록 그리기(비면 안내) */
		renderList = function(list, items, itemFn, emptyText){
			var rows = items.map(itemFn);
			if(!rows.length) rows = [App.h('li', {className: 'sort-empty', text: emptyText})];
			list.replaceChildren.apply(list, rows);
		},

		/* 요청 공통: 중복 전송을 막고, 성공하면 짧게 알린 뒤 다시 불러온다 */
		/* opts.title 성공 문구, opts.done 성공 후 추가 동작, opts.close 성공하면 먼저 닫을 창, opts.inUse 95(사용 중) 처리 */
		send = function(u, param, opts){
			opts = opts || {};
			if(settings.submitting) return;
			settings.submitting = true;
			var handlers = {
				/* 성공: 알리고 추가 동작 뒤 다시 불러오기 */
				ok: function(){
					if(opts.close) opts.close.close();
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

		/* 목록의 항목 요소들 */
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

		/* 시작 잔액 칸: 앞의 -(음수)와 숫자·쉼표만 서식 정리, 그 밖의 입력은 값을 두고 오류 표시 */
		signedNumber = function(n){ return (n < 0 ? '-' : '') + Math.abs(n).toLocaleString('ko-KR'); },

		/* "-1,000" 같은 입력 → 숫자. 형식이 틀리면 null */
		parseSigned = function(s){
			var t = String(s).replace(/[,\s]/g, '').replace('−', '-');
			return /^-?\d{1,11}$/.test(t) ? Number(t) : null;
		},

		/* 입력하는 동안 서식 정리, 틀리면 aria-invalid */
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

		/* 입력 확인 후 시작 잔액 저장(기준일을 비우면 해제) */
		saveOpening = function(){
			var date = m$.openingDate.value,
				amount = m$.openingBalance.value.trim() ? parseSigned(m$.openingBalance.value) : 0;
			if(date && amount === null){
				_error('알림', '시작 잔액은 숫자만 입력해주세요. (음수는 앞에 -, 소수점 불가, 최대 11자리)');
				return;
			}
			send(url.openingSave, {openingBalance: date ? amount : 0, openingDate: date},
				{title: date ? '저장했어요' : '시작 잔액을 해제했어요', close: m$.openingDialog, done: function(){ settings.openingDirty = false; }});
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
				m$.passwordDialog.close();
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

		/* 기록 초기화(withdraw=false)·계정 탈퇴(true): 비밀번호를 받고 한 번 더 물은 뒤 보낸다.
		   성공하면 초기화는 화면을 새로 열고, 탈퇴는 로그인 화면으로 */
		dangerAction = function(withdraw){
			if(!m$.dangerPassword.value){
				_error('알림', '현재 비밀번호를 입력해주세요.');
				return;
			}
			_confirm(withdraw ? '정말 탈퇴할까요?' : '기록을 모두 지울까요?',
				withdraw ? '계정과 모든 기록이 지워지고 되돌릴 수 없어요.'
					: '거래 · 이체 · 고정 항목 · 통장 · 자산 · 시작 잔액이 지워지고 되돌릴 수 없어요.',
				function(){
					if(settings.submitting) return;
					settings.submitting = true;
					var handlers = {ok: function(){
						m$.dangerPassword.value = '';
						if(withdraw) _alert('탈퇴했어요', '그동안 써 주셔서 고마워요.', function(){ location.href = contextPath + '/login'; });
						else _alert('기록을 초기화했어요', '카테고리 · 결제수단 · 예산 · 주기 설정은 그대로예요.', function(){ location.reload(); });
					}};
					handlers[App.CODE.LOGIN_BLOCKED] = function(){
						_error('잠시 후 다시 시도해 주세요', '현재 비밀번호를 여러 번 틀렸어요. 5분 뒤에 다시 시도해 주세요.');
					};
					App.post(withdraw ? url.withdraw : url.reset, {password: m$.dangerPassword.value})
						.then(function(res){ App.result(res, handlers); })
						.catch(function(){})
						.then(function(){ settings.submitting = false; });
				});
		},

		/* 주기 시작일·보정 저장 */
		saveCycle = function(){
			send(url.cycleSave, {payDay: m$.payDay.value, payDayAdjust: m$.payDayAdjust.checked},
				{close: m$.cycleDialog, done: function(){ settings.cycleDirty = false; }});
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

		/* 켜고 끄는 스위치 모양 체크박스 */
		switchField = function(input){
			return App.h('label', {className: 'switch'}, [input, App.h('span', {attrs: {'aria-hidden': 'true'}})]);
		},

		/* 목록 한 줄: 선택 / 손잡이 / 입력칸들 / 표시 스위치. key 는 drafts·선택·초점 복원에 쓰는 "c12"·"p3" */
		sortItem = function(key, id, name, active, fields, actions){
			var select = App.h('input', {type: 'checkbox', checked: !!settings.selected[key], attrs: {'aria-label': '‘' + name + '’ 선택'}});
			select.addEventListener('change', function(){
				if(select.checked) settings.selected[key] = true; else delete settings.selected[key];
				updateBulk(key.charAt(0));
			});
			return App.h('li', {className: 'sort-item' + (active ? '' : ' is-inactive'), attrs: {'data-id': id, 'data-name': name}}, [
				App.h('label', {className: 'row-select'}, [select]),
				App.h('button', {
					type: 'button',
					className: 'drag-handle',
					attrs: {'data-handle': key, 'aria-label': '‘' + name + '’ 순서 옮기기'}
				}, [App.icon('grip')]),
				App.h('div', {className: 'sort-fields'}, fields),
				App.h('div', {className: 'sort-actions'}, actions)
			]);
		},

		/* 카테고리 한 줄(이름·그룹·주기 예산(지출만)·사용 여부·저장·삭제·손잡이) */
		categoryItem = function(c){
			var name = App.h('input', {type: 'text', value: c.name, maxLength: 50}),
				group = App.h('input', {type: 'text', value: c.groupName || '', maxLength: 50, placeholder: '그룹 없음'}),
				expense = c.type === 'EXPENSE',
				budget = App.h('input', {type: 'text', inputMode: 'numeric', autocomplete: 'off', placeholder: '예산 없음',
					value: c.budget ? Number(c.budget).toLocaleString('ko-KR') : '', attrs: {'aria-label': c.name + ' 주기 예산(원)'}}),
				active = App.h('input', {type: 'checkbox', checked: !!c.active, attrs: {'aria-label': c.name + ' 거래에서 보이기'}}),
				key = 'c' + c.id,
				inputs = {name: name, group: group, active: active},
				original = {name: c.name, group: c.groupName || '', active: !!c.active},
				fields = [field('이름', name, 'sort-name'), field('그룹', group, 'sort-group', true)];

			if(expense){
				App.bindAmountInput(budget);
				inputs.budget = budget;
				original.budget = budget.value;
				fields.push(field('예산', budget, 'sort-group', true));
			}
			bindDraft(key, inputs, original);

			return rowOf(sortItem(key, c.id, c.name, c.active, fields, [switchField(active)]), inputs, {
				key: key, id: c.id, name: c.name, saveUrl: url.categorySave, deleteUrl: url.categoryDelete,
				/* 예산은 지출 카테고리만 보낸다(빈 값 = 예산 없음). 보내지 않으면 서버가 저장된 예산을 그대로 둔다 */
				body: function(){
					var b = {id: c.id, type: c.type, name: name.value, groupName: group.value, active: active.checked};
					if(expense) b.budget = budget.value;
					return b;
				},
				/* 사용 중이라 지울 수 없으면 저장된 원래 값으로 숨긴다(고치다 만 값까지 저장하지 않게) */
				hide: {id: c.id, type: c.type, name: c.name, groupName: c.groupName || '', active: false}
			});
		},

		/* 결제수단 한 줄(이름·사용 여부·저장·삭제·손잡이) */
		paymentItem = function(p){
			var name = App.h('input', {type: 'text', value: p.name, maxLength: 50}),
				active = App.h('input', {type: 'checkbox', checked: !!p.active, attrs: {'aria-label': p.name + ' 거래에서 보이기'}}),
				key = 'p' + p.id;

			bindDraft(key, {name: name, active: active}, {name: p.name, active: !!p.active});

			return rowOf(sortItem(key, p.id, p.name, p.active, [field('결제수단 이름', name, 'sort-name')], [switchField(active)]),
				{name: name, active: active}, {
					key: key, id: p.id, name: p.name, saveUrl: url.paymentSave, deleteUrl: url.paymentDelete,
					body: function(){ return {id: p.id, name: name.value, active: active.checked}; },
					hide: {id: p.id, name: p.name, active: false}
				});
		},

		/* 줄에 저장·삭제 정보를 달고, 고치면 그 줄을 저절로 선택한다(drafts 갱신 뒤에 돌도록 bindDraft 다음에 연결) */
		rowOf = function(li, inputs, row){
			li._row = row;
			Object.keys(inputs).forEach(function(k){
				var el = inputs[k];
				el.addEventListener(el.type === 'checkbox' ? 'change' : 'input', function(){
					if(settings.drafts[row.key] && !settings.selected[row.key]){
						setSelected(li, true);
						updateBulk(row.key.charAt(0));
					}
				});
			});
			return li;
		},

		/* 그 목록의 줄들(c: 지출+수입 카테고리, p: 결제수단) */
		rowsOf = function(prefix){
			return prefix === 'c' ? itemsOf(m$.expenseList).concat(itemsOf(m$.incomeList)) : itemsOf(m$.paymentList);
		},

		/* 줄 선택 켜고 끄기 */
		setSelected = function(li, on){
			var box = li.querySelector('.row-select input');
			if(box) box.checked = on;
			if(on) settings.selected[li._row.key] = true; else delete settings.selected[li._row.key];
		},

		/* 그 목록의 선택을 모두 푼다 */
		clearSelection = function(prefix){
			rowsOf(prefix).forEach(function(li){ if(li._row) setSelected(li, false); });
			updateBulk(prefix);
		},

		/* 선택한 줄들 */
		selectedRows = function(prefix){
			return rowsOf(prefix).filter(function(li){ return li._row && settings.selected[li._row.key]; });
		},

		/* 선택 막대: 개수 · 전체 선택 상태 · 버튼 사용 가능 여부 */
		updateBulk = function(prefix){
			var b = m$.bulk[prefix], n = selectedRows(prefix).length, total = rowsOf(prefix).length;
			b.count.textContent = '선택 ' + n + '개';
			b.all.checked = total > 0 && n === total;
			b.all.indeterminate = n > 0 && n < total;
			b.save.disabled = b.del.disabled = n === 0 || settings.submitting;
		},

		/* 여러 줄을 하나씩 차례로 보낸다(한꺼번에 저장하는 API가 없다). step(li) 는 그 줄의 결과를 기록하는 Promise */
		runRows = function(prefix, rows, step, title){
			settings.submitting = true;
			updateBulk(prefix);
			var result = {fails: []};
			rows.reduce(function(p, li){
				return p.then(function(){ return step(li, result); });
			}, Promise.resolve()).then(function(){
				settings.submitting = false;
				if(result.fails.length) _error('일부를 처리하지 못했어요', result.fails.join(' / '));
				else App.saved(title(result));
				load();
			});
		},

		/* 한 요청. 성공이면 결과(res), 실패면 fails 에 이름과 까닭을 남기고 null */
		post = function(li, u, body, result){
			var name = '‘' + li._row.name + '’';
			return App.post(u, body, {onError: function(){}}).then(function(res){
				return res;
			}, function(){
				result.fails.push(name + ' 연결 오류');
				return null;
			});
		},

		/* 그 줄을 처리했으면 작성 중 표시와 선택을 지운다 */
		settle = function(li){
			delete settings.drafts[li._row.key];
			delete settings.selected[li._row.key];
		},

		/* 선택 저장 */
		bulkSave = function(prefix){
			var rows = selectedRows(prefix);
			if(!rows.length || settings.submitting) return;
			runRows(prefix, rows, function(li, result){
				return post(li, li._row.saveUrl, li._row.body(), result).then(function(res){
					if(!res) return;
					if(res.code === App.CODE.SUCCESS){ settle(li); result.saved = (result.saved || 0) + 1; }
					else result.fails.push('‘' + li._row.name + '’ ' + (res.message || '저장하지 못했어요'));
				});
			}, function(r){ return r.saved + '개 저장했어요'; });
		},

		/* 선택 삭제: 거래·고정 항목에 쓰여 지울 수 없는 항목(95)은 대신 숨긴다 */
		bulkDelete = function(prefix){
			var rows = selectedRows(prefix);
			if(!rows.length || settings.submitting) return;
			_confirm('선택한 ' + rows.length + '개를 삭제할까요?', '거래나 고정 항목에 쓰인 항목은 지우는 대신 숨겨요.', function(){
				runRows(prefix, rows, function(li, result){
					var r = li._row;
					return post(li, r.deleteUrl, {id: r.id}, result).then(function(res){
						if(!res) return;
						if(res.code === App.CODE.SUCCESS){ settle(li); result.deleted = (result.deleted || 0) + 1; return; }
						if(res.code !== App.CODE.IN_USE){ result.fails.push('‘' + r.name + '’ ' + (res.message || '삭제하지 못했어요')); return; }
						return post(li, r.saveUrl, r.hide, result).then(function(res2){
							if(!res2) return;
							if(res2.code === App.CODE.SUCCESS){ settle(li); result.hidden = (result.hidden || 0) + 1; }
							else result.fails.push('‘' + r.name + '’ ' + (res2.message || '숨기지 못했어요'));
						});
					});
				}, function(r){
					return [r.deleted ? '삭제 ' + r.deleted + '개' : '', r.hidden ? '숨김 ' + r.hidden + '개' : ''].filter(Boolean).join(' · ');
				});
			});
		},

		/* 카테고리 추가 칸 비우기 */
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

		/* 카테고리 추가 칸 비우고 접기 */
		closeCategoryAdd = function(){
			resetCategoryAdd();
			toggleAdd(m$.categoryAdd, m$.categoryAddOpen, m$.newCategoryName, false);
		},

		/* 결제수단 추가 칸 비우고 접기 */
		closePaymentAdd = function(){
			m$.newPaymentName.value = '';
			toggleAdd(m$.paymentAdd, m$.paymentAddOpen, m$.newPaymentName, false);
		},

		/* 입력 확인 후 카테고리 추가 */
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

		/* 입력 확인 후 결제수단 추가 */
		addPayment = function(){
			if(App.isEmpty(m$.newPaymentName.value.trim())){
				_error('알림', '결제수단 이름을 입력해주세요.');
				return;
			}
			send(url.paymentSave, {name: m$.newPaymentName.value}, {
				title: '추가했어요',
				/* 추가 성공: 이름 칸 비우기 */
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
