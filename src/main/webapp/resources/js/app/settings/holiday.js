// 설정 화면의 공휴일 관리(전 사용자 공용): 연도별 목록·가져오기·직접 추가·삭제
App.holiday = (function(){
    var m$ = {
            year: document.getElementById('holidayYear'),
            importButton: document.getElementById('holidayImport'),
            keyNote: document.getElementById('holidayKeyNote'),
            body: document.getElementById('holidayBody'),
            add: document.getElementById('holidayAdd'),
            date: document.getElementById('holidayDate'),
            name: document.getElementById('holidayName'),
            addCancel: document.getElementById('holidayAddCancel'),
            addSave: document.getElementById('holidayAddSave')
        },
        settings = { submitting: false, year: null },
        url = {
            list: contextPath + '/ledger/holiday/list',
            save: contextPath + '/ledger/holiday/save',
            del: contextPath + '/ledger/holiday/delete',
            importYear: contextPath + '/ledger/holiday/import'
        },

        init = function(){
            m$.year.addEventListener('change', function(){
                settings.year = Number(m$.year.value);
                load();
            });
            m$.importButton.addEventListener('click', importYear);
            m$.addSave.addEventListener('click', add);
            m$.addCancel.addEventListener('click', function(){
                m$.date.value = '';
                m$.name.value = '';
                m$.add.open = false;
            });
            load();
        },

        load = function(){
            App.post(url.list, settings.year ? {year: settings.year} : {})
                .then(function(res){
                    App.result(res, {ok: function(){ render(res.data); }});
                })
                .catch(function(){});
        },

        render = function(data){
            if (!settings.year) {
                settings.year = data.years[1];   // 올해
                m$.year.replaceChildren.apply(m$.year, data.years.map(function(y){
                    return App.h('option', {value: String(y), text: y + '년', selected: y === settings.year});
                }));
            }
            m$.importButton.disabled = !data.keyConfigured;
            m$.keyNote.hidden = data.keyConfigured;

            if (!data.holidays.length) {
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

        send = function(u, param, title, done){
            if (settings.submitting) return;
            settings.submitting = true;
            App.post(u, param)
                .then(function(res){
                    App.result(res, {ok: function(){
                        App.saved(typeof title === 'function' ? title(res) : title);
                        if (done) done();
                        load();
                    }});
                })
                .catch(function(){})
                .then(function(){ settings.submitting = false; });
        },

        importYear = function(){
            _confirm(settings.year + '년 공휴일을 가져올까요?', '공공데이터포털에서 받아 저장해요. 같은 날짜는 이름이 새로 바뀌어요.', function(){
                send(url.importYear, {year: settings.year}, function(res){ return res.data.count + '건을 가져왔어요'; });
            });
        },

        add = function(){
            if (!m$.date.value || !m$.name.value.trim()) {
                _error('알림', '날짜와 이름을 입력해주세요.');
                return;
            }
            send(url.save, {date: m$.date.value, name: m$.name.value.trim()}, '추가했어요', function(){
                m$.name.value = '';
            });
        },

        remove = function(h){
            _confirm('삭제할까요?', h.date + ' ‘' + h.name + '’ 공휴일을 삭제해요.\n모든 사용자의 주기·결제일 보정에 반영돼요.', function(){
                send(url.del, {date: h.date}, '삭제했어요');
            });
        };

    return { init: init };
}());

document.addEventListener('DOMContentLoaded', function(){
    App.holiday.init();
});
