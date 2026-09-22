// 전역 네임스페이스: 화면별 js는 App.xxx = (function(){ ... return {init:...}; })(); 형태로 등록한다.
// 사용 예) app/login/login.js -> App.login = (function(){ ... }()); jsp에서 App.login.init() 호출
var App = window.App || {};

// App.ajax(url, data, opts): 서버(jsonView) 공통 호출. fetch 기반, JSON 요청/응답 고정.
// opts.method 기본 POST, opts.onError 없으면 _error 모달로 공통 처리.
// jQuery 1.11.3의 $.ajax는 진짜 Promise가 아니라 .catch()를 못 씀 - fetch 유지.
//
// 사용 예)
//   App.ajax(contextPath + '/design/login', { user_id: 'abc', user_pw: '1234' })
//       .then(function(res){ console.log(res.code); })
//       .catch(function(){ /* 실패 시 처리, 안 써도 App.ajax가 알아서 _error 모달 띄움 */ });
//
//   App.get(contextPath + '/design/notice/list', { page: 1 }).then(function(res){ ... });
App.ajax = function (url, data, opts) {
    opts = opts || {};
    var method = (opts.method || 'POST').toUpperCase();
    var fetchOpt = {
        method: method,
        headers: {
            'Content-Type': 'application/json; charset=UTF-8',
            'X-Requested-With': 'XMLHttpRequest'   // 인터셉터가 Ajax로 인식하게
        },
        credentials: 'same-origin'
    };

    if (method === 'GET') {
        if (data) url += (url.indexOf('?') === -1 ? '?' : '&') + $.param(data);
    } else {
        fetchOpt.body = JSON.stringify(data || {});
    }

    return fetch(url, fetchOpt)
        .then(function (res) {
            if (!res.ok) throw new Error('HTTP ' + res.status);
            return res.json();
        })
        .then(function(data){
            if(data && data.sessionExpired) return App.sessionExpired();
            return data;
        })
        .catch(function (err) {
            if (typeof opts.onError === 'function') {
                opts.onError(err);
            } else if (typeof window._error === 'function') {
                window._error('오류', '요청 처리 중 문제가 발생했습니다.');
            }
            throw err;
        });
};

App.sessionExpired = function(){
    _error('로그인 세션시간이 초과되었습니다.', '다시 로그인 부탁드립니다.', function(){
        window.location.href = contextPath + '/login';
    });
    return new Promise(function(){});   // 체인 중단 - 뒤의 .then(render)를 막는다
};

// App.get(url, data, opts) / App.post(url, data, opts): App.ajax의 GET/POST 전용 별칭.
// 화면 코드에서는 method 옵션을 신경 쓰지 않도록 이 둘을 우선 사용.
App.get = function (url, data, opts) {
    return App.ajax(url, data, Object.assign({}, opts, {method: 'GET'}));
};
App.post = function (url, data, opts) {
    return App.ajax(url, data, Object.assign({}, opts, {method: 'POST'}));
};

// App.isEmpty(v): null/undefined/빈문자열/빈배열/빈객체 여부
//
// 사용 예)
//   if(App.isEmpty(param.user_id)){ _error('알림', '아이디를 입력해주세요.'); return; }
App.isEmpty = function (v) {
    if (v === null || v === undefined) return true;
    if (typeof v === 'string' || Array.isArray(v)) return v.length === 0;
    if (typeof v === 'object') return Object.keys(v).length === 0;
    return false;
};

// App.formToObject(form): <form> -> {name: value} 평면 객체. 폼 제출 데이터를 서버로 보낼 때 사용.
//
// 사용 예)
//   var param = App.formToObject(document.getElementById('searchForm'));
//   App.post(url, param).then(...);
App.formToObject = function (form) {
    var obj = {};
    new FormData(form).forEach(function (value, key) {
        if (obj.hasOwnProperty(key)) {
            if (!Array.isArray(obj[key])) obj[key] = [obj[key]];
            obj[key].push(value);
        } else {
            obj[key] = value;
        }
    });
    return obj;
};

// App.debounce(fn, wait): 마지막 호출 후 wait(ms) 지나야 실행. 검색어 입력, resize 등에 사용.
//
// 사용 예)
//   searchInput.addEventListener('input', App.debounce(function(){
//       App.get(url, { keyword: searchInput.value }).then(renderList);
//   }, 300));
App.debounce = function (fn, wait) {
    var timer = null;
    return function () {
        var ctx = this, args = arguments;
        clearTimeout(timer);
        timer = setTimeout(function () {
            fn.apply(ctx, args);
        }, wait);
    };
};

// show(container): 로딩화면을 뿌려줄 요소를 파라미터로 넘긴다.
// hide(container): container값이 없을 경우 모든 로딩화면 제거
//
// 사용 예)
//   _loading.show();               // 기본: .manage-section 영역에 표시
//   _loading.show('boardWrap');    // 특정 id 영역에만 표시
//   _loading.hide();               // 열려있는 로딩화면 전부 제거
var _loading = (function () {
    let OVERLAY_KEY = '_loadingOverlay';
    let _active = [];

    function resolve(container) {
        if (typeof container === 'string') return document.getElementById(container);
        return container || null;
    }

    function show(container) {
        let el = (container === undefined || container === null) ? document.querySelector('.manage-section') : resolve(container);
        if (!el) return;

        if (getComputedStyle(el).position === 'static') {
            el.style.position = 'relative';
        }

        let existing = el.querySelector('.line-manage-loading');
        if (existing) {
            el[OVERLAY_KEY] = existing;
            _active.push(el);
            return;
        }

        if (el[OVERLAY_KEY]) return;

        let overlay = document.createElement('div');
        overlay.className = 'line-manage-loading';
        overlay.setAttribute('role', 'status');
        overlay.setAttribute('aria-live', 'polite');
        overlay.innerHTML = '<div class="line-manage-loading-inner"><div class="spinner-wrap"><div class="krds-spinner" aria-hidden="true"></div></div><h1 class="loading-title">불러오는 중..</h1></div>';
        el.appendChild(overlay);
        el[OVERLAY_KEY] = overlay;
        _active.push(el);
    }

    function hide(container) {
        if (container === undefined) {
            _active.forEach(function (el) {
                if (el[OVERLAY_KEY]) {
                    el[OVERLAY_KEY].remove();
                    el[OVERLAY_KEY] = null;
                }
            });
            _active = [];

            let main = document.querySelector('.manage-section');
            if (main) {
                let preRendered = main.querySelector('.line-manage-loading');
                if (preRendered) preRendered.remove();
            }
            return;
        }

        let el = resolve(container);
        if (!el || !el[OVERLAY_KEY]) return;

        el[OVERLAY_KEY].remove();
        el[OVERLAY_KEY] = null;
        _active = _active.filter(function (a) {
            return a !== el;
        });
    }

    return {
        show: show,
        hide: hide
    };
}());