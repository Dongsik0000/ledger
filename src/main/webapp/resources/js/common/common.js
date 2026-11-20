// 공통 함수. 화면 js 는 App.xxx = (function(){ ... return {init: init}; })(); 로 등록하고 jsp 가 App.xxx.init() 을 부른다.
var App = window.App || {};

// App.post(url, data, opts): JSON POST 공통 호출(서버 jsonView 응답). 데이터를 바꾸는 요청은 Ajax + JSON 만 받는다(ApiRequestInterceptor).
// 세션이 끊겼으면 안내 후 로그인 화면으로 보내고, 통신 오류는 opts.onError 가 없으면 _error 창으로 알린 뒤 reject 한다.
//   App.post(url, param).then(function(res){ App.result(res, {ok: render}); }).catch(function(){});
App.post = function (url, data, opts) {
    opts = opts || {};
    return fetch(url, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json; charset=UTF-8',
            'X-Requested-With': 'XMLHttpRequest'
        },
        credentials: 'same-origin',
        body: JSON.stringify(data || {})
    })
        .then(function (res) {
            if (!res.ok) {
                var err = new Error('HTTP ' + res.status);
                err.status = res.status;
                throw err;
            }
            return res.json();
        })
        .then(function (data) {
            return data && data.sessionExpired ? App.sessionExpired() : data;
        })
        .catch(function (err) {
            if (typeof opts.onError === 'function') {
                opts.onError(err);
            } else if (err.status === 429) {
                // nginx 요청 속도 제한(로그인·가입 시도 등)
                _error('잠시 후 다시 시도해 주세요', '짧은 시간에 요청이 너무 많았어요. 1분쯤 뒤에 다시 시도해 주세요.');
            } else {
                _error('오류', '요청 처리 중 문제가 발생했습니다.');
            }
            throw err;
        });
};

// 세션 만료 안내 후 로그인 화면으로. 끝나지 않는 Promise 를 돌려 뒤의 then(화면 그리기)이 돌지 않게 한다
App.sessionExpired = function () {
    _error('로그인 세션시간이 초과되었습니다.', '다시 로그인 부탁드립니다.', function () {
        window.location.href = contextPath + '/login';
    });
    return new Promise(function () {});
};

// App.isEmpty(v): null/undefined/빈 문자열/빈 배열/빈 객체인지
App.isEmpty = function (v) {
    if (v === null || v === undefined) return true;
    if (typeof v === 'string' || Array.isArray(v)) return v.length === 0;
    if (typeof v === 'object') return Object.keys(v).length === 0;
    return false;
};

// 서버 결과 코드. Java ledger.cmmn.util.Constants 와 같은 값을 유지한다.
App.CODE = {
    SUCCESS: '00',
    FAIL: '99',
    LOGIN_FAIL: '01',
    LOGIN_BLOCKED: '03',
    SIGNUP_FAIL_CODE: '11',
    SIGNUP_FAIL_EXISTS: '12',
    SIGNUP_BLOCKED: '13',
    INVALID: '90',
    NOT_FOUND: '91',
    DUPLICATE: '92',
    HOLIDAY_KEY_MISSING: '93',
    HOLIDAY_API_FAIL: '94',
    IN_USE: '95'
};

// App.fail(res, fallback): 화면이 따로 처리하지 않은 결과 코드의 공통 안내(서버 message 우선)
App.fail = function (res, fallback) {
    _error('오류', (res && res.message) || fallback || '요청 처리 중 문제가 발생했습니다.');
};

// App.escape(v): 사용자 입력을 HTML 문자열에 넣기 전에 반드시 거친다. 가능하면 textContent·App.h 를 쓴다.
App.escape = function (v) {
    var map = {'&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;'};
    return String(v === null || v === undefined ? '' : v).replace(/[&<>"']/g, function (c) {
        return map[c];
    });
};

// App.num(n): 1930000 -> "1,930,000"
App.num = function (n) {
    return Number(n).toLocaleString('ko-KR');
};

// App.signed(n): 음수면 마이너스 기호(−)를 붙인 숫자. -30000 -> "−30,000"
App.signed = function (n) {
    var v = Number(n);
    return (v < 0 ? '−' : '') + App.num(Math.abs(v));
};

// App.money(n): 1930000 -> "1,930,000원", -30000 -> "−30,000원". 양수의 +는 화면에서 붙인다.
App.money = function (n) {
    return App.signed(n || 0) + '원';
};

// App.dotted(iso): "2026-10-12" -> "2026.10.12"
App.dotted = function (iso) {
    return iso.replace(/-/g, '.');
};

// App.pad(n): 한 자리 수 앞에 0. 3 -> "03"
App.pad = function (n) {
    return (n < 10 ? '0' : '') + n;
};

// App.h(tag, props, children): DOM 요소를 안전하게 만든다. 문자열 자식은 텍스트 노드(HTML 해석 없음).
//   props.text: textContent, props.attrs: setAttribute, props.on: 이벤트, 그 밖: 프로퍼티(value, checked, type, className …)
//   App.h('button', {type: 'button', className: 'button small', text: '저장', on: {click: save}})
App.h = function (tag, props, children) {
    var el = document.createElement(tag);
    props = props || {};
    Object.keys(props).forEach(function (k) {
        var v = props[k];
        if (v === undefined || v === null) return;
        if (k === 'attrs') {
            Object.keys(v).forEach(function (a) { el.setAttribute(a, v[a]); });
        } else if (k === 'on') {
            Object.keys(v).forEach(function (ev) { el.addEventListener(ev, v[ev]); });
        } else if (k === 'text') {
            el.textContent = v;
        } else {
            el[k] = v;
        }
    });
    (children || []).forEach(function (c) {
        if (c === null || c === undefined || c === false) return;
        el.appendChild(typeof c === 'string' || typeof c === 'number' ? document.createTextNode(String(c)) : c);
    });
    return el;
};

// App.icon(name): 아이콘 스프라이트(layout/icons.jsp)의 #i-name
App.icon = function (name) {
    var ns = 'http://www.w3.org/2000/svg';
    var svg = document.createElementNS(ns, 'svg');
    svg.setAttribute('class', 'icon');
    svg.setAttribute('aria-hidden', 'true');
    var use = document.createElementNS(ns, 'use');
    use.setAttribute('href', '#i-' + name);
    svg.appendChild(use);
    return svg;
};

// App.parseAmount(s): "1,930,000" -> 1930000. 숫자 1~11자리가 아니면 null
App.parseAmount = function (s) {
    var t = String(s === null || s === undefined ? '' : s).replace(/[,\s]/g, '');
    return /^\d{1,11}$/.test(t) ? Number(t) : null;
};

// App.bindAmountInput(input): 금액 칸에 입력하는 동안 쉼표 서식.
// 숫자·쉼표(11자리까지)만 서식을 정리하고, 소수점·음수·문자·자리 초과는 값을 바꾸지 않고 오류로 표시한다
App.bindAmountInput = function (input) {
    input.addEventListener('input', function () {
        var t = input.value.replace(/[,\s]/g, '');
        if (/^\d{0,11}$/.test(t)) {
            input.value = t ? Number(t).toLocaleString('ko-KR') : '';
            input.removeAttribute('aria-invalid');
        } else {
            input.setAttribute('aria-invalid', 'true');
        }
    });
};

// App.amountError(s, allowZero): 금액 칸 값의 오류 문구. 올바르면 null
App.amountError = function (s, allowZero) {
    var t = String(s === null || s === undefined ? '' : s).replace(/[,\s]/g, '');
    if (!/^\d*$/.test(t) || t.length > 11) {
        return '금액은 숫자만 입력해주세요. (소수점·음수 불가, 최대 11자리)';
    }
    if (!t || (!allowZero && Number(t) === 0)) {
        return allowZero ? '금액을 입력해주세요.' : '금액을 1원 이상 입력해주세요.';
    }
    return null;
};

// 음수를 받는 금액 칸(시작 잔액·통장 기준 잔액). 입력은 하이픈 -, 화면 표시는 App.signed 의 − 를 쓴다
// App.signedNumber(n): -1000 -> "-1,000" (입력 칸 값)
App.signedNumber = function (n) {
    return (n < 0 ? '-' : '') + Math.abs(n).toLocaleString('ko-KR');
};

// App.parseSigned(s): "-1,000"·"−1,000" -> -1000. 정수 1~11자리가 아니면 null
App.parseSigned = function (s) {
    var t = String(s).replace(/[,\s]/g, '').replace('−', '-');
    return /^-?\d{1,11}$/.test(t) ? Number(t) : null;
};

// App.formatSigned(input): 입력하는 동안 쉼표 서식. 틀린 형식은 값을 두고 aria-invalid 로 표시한다
App.formatSigned = function (input) {
    var t = input.value.replace(/[,\s]/g, '').replace('−', '-');
    if (t === '' || t === '-') {
        input.removeAttribute('aria-invalid');
    } else if (/^-?\d{1,11}$/.test(t)) {
        input.value = App.signedNumber(Number(t));
        input.removeAttribute('aria-invalid');
    } else {
        input.setAttribute('aria-invalid', 'true');
    }
};

// App.saved(title): 저장·삭제 성공을 짧게 알린다
App.saved = function (title) {
    _alert(title || '저장했어요', {autoClose: 900});
};

// App.bindDialog(dialog, isDirty, discard): 편집 창의 닫기 요청(data-close 버튼·Escape·뒤로 가기)을 한곳에서 받는다.
// isDirty() 가 true 면 버릴지 먼저 묻고, 버리기로 하면 discard() 뒤에 닫는다.
// Escape 는 cancel 을 막는 것만으로는 Chrome 이 연속 입력에서 창을 그냥 닫으므로 keydown 에서 막는다(entry.js 와 같은 방식)
App.bindDialog = function (dialog, isDirty, discard) {
    var requestClose = function () {
        if (!isDirty()) {
            dialog.close();
            return;
        }
        _confirm('작성 중인 내용을 버릴까요?', '저장하지 않은 입력은 사라져요.', function () {
            discard();
            dialog.close();
        });
    };
    dialog.querySelectorAll('[data-close]').forEach(function (b) {
        b.addEventListener('click', requestClose);
    });
    dialog.addEventListener('keydown', function (e) {
        if (e.key === 'Escape') {
            e.preventDefault();
            requestClose();
        }
    });
    dialog.addEventListener('cancel', function (e) {
        e.preventDefault();
        requestClose();
    });
};

// App.result(res, handlers): 결과 코드 분기 공통. handlers.ok 는 성공, handlers['95'] 처럼 코드별 처리.
// 처리하지 않은 90·92 는 서버 message, 91 은 "찾을 수 없어요", 나머지는 App.fail.
//   App.post(url, param).then(function(res){ App.result(res, {ok: reload}); });
App.result = function (res, handlers) {
    handlers = handlers || {};
    var code = res && res.code;
    if (code === App.CODE.SUCCESS) {
        if (handlers.ok) App.guard(handlers.ok, res);
        return;
    }
    if (handlers[code]) {
        App.guard(handlers[code], res);
        return;
    }
    if (code === App.CODE.INVALID || code === App.CODE.DUPLICATE) {
        _error('확인해 주세요', res.message || '입력한 내용을 확인해 주세요.');
    } else if (code === App.CODE.NOT_FOUND) {
        _error('찾을 수 없어요', '이미 삭제되었거나 권한이 없는 항목이에요.');
    } else {
        App.fail(res);
    }
};

// App.guard(fn, res): 결과 처리(화면 그리기) 중 예외를 조용히 삼키지 않고 알린다.
// 화면 코드의 .catch(function(){}) 는 통신 오류(이미 안내됨)용이라, 여기서 먼저 잡아야 빈 화면으로 멈추지 않는다
App.guard = function (fn, res) {
    try {
        fn(res);
    } catch (e) {
        if (window.console) console.error(e);
        _error('화면을 그리지 못했어요', '새로고침해 주세요. 계속되면 서버와 화면 버전이 다른지(배포 직후 등) 확인해 주세요.');
    }
};

// 로그아웃: data-logout 속성을 가진 요소를 누르면 POST /auth/logout 후 로그인 화면으로.
App.logout = function () {
    App.post(contextPath + '/auth/logout')
        .then(function () { location.href = contextPath + '/login'; })
        .catch(function () {});
};
document.addEventListener('click', function (e) {
    var el = e.target.closest ? e.target.closest('[data-logout]') : null;
    if (!el) return;
    e.preventDefault();
    App.logout();
});

// App.formToObject(form): <form> -> {name: value}
App.formToObject = function (form) {
    return Object.fromEntries(new FormData(form));
};

// App.debounce(fn, wait): 마지막 호출 후 wait(ms) 가 지나야 fn 을 실행한다
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
