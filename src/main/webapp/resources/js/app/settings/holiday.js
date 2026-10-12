/* 설정 화면의 공휴일 관리(전 사용자 공용): 연도별 목록·가져오기·직접 추가·삭제 */
App.holiday = (function(){
	var m$ = {
			year: document.getElementById('holidayYear'),  /* 연도 선택 */
			importButton: document.getElementById('holidayImport'),  /* 공휴일 가져오기 버튼 */
			keyNote: document.getElementById('holidayKeyNote'),  /* API 키 없음 안내 */
			body: document.getElementById('holidayBody'),  /* 공휴일 표 본문 */
			open: document.getElementById('holidayOpen'),  /* 공휴일 관리 창 열기 버튼 */
			dialog: document.getElementById('holidayDialog'),  /* 공휴일 관리 창 */
			add: document.getElementById('holidayAdd'),  /* 직접 추가 펼침 */
			date: document.getElementById('holidayDate'),  /* 추가할 날짜 */
			name: document.getElementById('holidayName'),  /* 추가할 이름 */
			addCancel: document.getElementById('holidayAddCancel'),  /* 추가 취소 버튼 */
			addSave: document.getElementById('holidayAddSave')  /* 추가 버튼 */
		},

		settings = {
			submitting: false,
			year: null
		},

		url = {
			list: contextPath + '/ledger/holiday/list',
			save: contextPath + '/ledger/holiday/save',
			del: contextPath + '/ledger/holiday/delete',
			importYear: contextPath + '/ledger/holiday/import'
		},

		/* 화면 이벤트 연결 후 불러오기 */
		init = function(){
			bindEvent();
			load();
		},

		/* 화면 이벤트 연결 */
		bindEvent = function(){
			m$.year.addEventListener('change', function(){
				settings.year = Number(m$.year.value);
				load();
			});
			m$.importButton.addEventListener('click', importYear);
			m$.addSave.addEventListener('click', add);
			m$.addCancel.addEventListener('click', clearAdd);
			m$.open.addEventListener('click', function(){ m$.dialog.showModal(); });
			/* 추가에 성공해도 날짜는 남겨 두므로(add) 이름이 있을 때만 작성 중으로 본다 */
			App.bindDialog(m$.dialog, function(){ return !!m$.name.value.trim(); }, clearAdd);
		},

		/* 직접 추가 칸 비우고 접기 */
		clearAdd = function(){
			m$.date.value = '';
			m$.name.value = '';
			m$.add.open = false;
		},

		/* 고른 해(처음엔 올해)의 공휴일 받기 */
		load = function(){
			App.post(url.list, settings.year ? {year: settings.year} : {})
				.then(function(res){
					App.result(res, {ok: function(){ render(res.data); }});
				})
				.catch(function(){});
		},

		/* 연도 선택지(처음 한 번)·API 키 안내·공휴일 표 */
		render = function(data){
			if(!settings.year){
				settings.year = data.years[1];   /* 올해 */
				m$.year.replaceChildren.apply(m$.year, data.years.map(function(y){
					return App.h('option', {value: String(y), text: y + '년', selected: y === settings.year});
				}));
			}
			m$.importButton.disabled = !data.keyConfigured;
			m$.keyNote.hidden = data.keyConfigured;

			if(!data.holidays.length){
				m$.body.replaceChildren(App.h('tr', null, [App.h('td', {attrs: {colspan: 3}, text: settings.year + '년 공휴일이 없어요. 가져오거나 직접 추가해 주세요.'})]));
				return;
			}
			m$.body.replaceChildren.apply(m$.body, data.holidays.map(function(h){
				return App.h('tr', null, [
					App.h('th', {attrs: {scope: 'row'}, text: h.date.replace(/-/g, '.')}),
					App.h('td', {attrs: {'data-label': '공휴일 이름'}, text: h.name}),
					App.h('td', {attrs: {'data-label': '관리'}}, [
						App.h('button', {type: 'button', className: 'button small danger', text: '삭제', on: {click: function(){ remove(h); }}})
					])
				]);
			}));
		},

		/* 요청 공통: 중복 전송을 막고 성공하면 알린 뒤 다시 불러온다 */
		send = function(u, param, title, done){
			if(settings.submitting) return;
			settings.submitting = true;
			App.post(u, param)
				.then(function(res){
					App.result(res, {ok: function(){
						App.saved(typeof title === 'function' ? title(res) : title);
						if(done) done();
						load();
					}});
				})
				.catch(function(){})
				.then(function(){ settings.submitting = false; });
		},

		/* 그 해 공휴일을 공공데이터포털에서 가져오기 */
		importYear = function(){
			_confirm(settings.year + '년 공휴일을 가져올까요?', '공공데이터포털에서 받아 저장해요. 같은 날짜는 이름이 새로 바뀌어요.', function(){
				send(url.importYear, {year: settings.year}, function(res){ return res.data.count + '건을 가져왔어요'; });
			});
		},

		/* 입력 확인 후 직접 추가 */
		add = function(){
			if(!m$.date.value || !m$.name.value.trim()){
				_error('알림', '날짜와 이름을 입력해주세요.');
				return;
			}
			send(url.save, {date: m$.date.value, name: m$.name.value.trim()}, '추가했어요', function(){
				m$.name.value = '';
			});
		},

		/* 삭제 확인 후 요청 */
		remove = function(h){
			_confirm('삭제할까요?', h.date + ' ‘' + h.name + '’ 공휴일을 삭제해요.\n모든 사용자의 주기·결제일 보정에 반영돼요.', function(){
				send(url.del, {date: h.date}, '삭제했어요');
			});
		};

	return{
		init: init
	};
}());

document.addEventListener('DOMContentLoaded', function(){
	App.holiday.init();
});
