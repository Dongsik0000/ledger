// 공통 안내 창: _alert(완료) · _error(경고) · _confirm(확인·취소).
// 처음 부를 때 한 번 만들어 두고 다시 쓴다. 제목·설명은 textContent 로 넣는다(HTML 해석 없음, 줄바꿈 \n 유지).

// 편집 창(<dialog>.showModal())은 브라우저 최상위 레이어라 body 에 붙은 모달이 z-index 와 상관없이 그 뒤에 가려진다.
// 열린 dialog 가 있으면 모달을 그 안으로 옮겨 같은 레이어에서 보이게 한다
function _modalHost(modal){
    var host = document.querySelector('dialog[open]') || document.body;
    if (modal.parentNode !== host) host.appendChild(modal);
}

// id 의 모달을 돌려준다. 없으면 html 로 만들어 body 에 붙이고 setup(modal) 로 이벤트를 단다
function _modalGet(id, html, setup){
    var modal = document.getElementById(id);
    if (modal) return modal;
    modal = document.createElement('div');
    modal.id = id;
    modal.className = 'manage-modal';
    modal.setAttribute('aria-hidden', 'true');
    modal.setAttribute('tabindex', '-1');
    modal.innerHTML = html;
    document.body.appendChild(modal);
    setup(modal);
    return modal;
}

// 제목·설명·확인 콜백을 넣고 연다
function _modalOpen(modal, title, desc, callback){
    modal.querySelector('[data-modal-title]').textContent = title || '';
    modal.querySelector('[data-modal-desc]').textContent = desc || '';
    modal._cb = callback || null;
    _modalHost(modal);
    modal.classList.add('is-open');
    modal.setAttribute('aria-hidden', 'false');
}

// 닫는다. runCallback 이면 열 때 받은 콜백을 한 번 실행한다
function _modalClose(modal, runCallback){
    clearTimeout(modal._autoCloseTimer);
    modal.classList.remove('is-open');
    modal.setAttribute('aria-hidden', 'true');
    var cb = modal._cb;
    modal._cb = null;
    if (runCallback && typeof cb === 'function') cb();
}

// 버튼 하나짜리 창(_alert·_error): [data-modal-close]·Escape·Enter 모두 닫고 콜백 실행
function _modalBindSingle(modal){
    var close = function(){ _modalClose(modal, true); };
    modal.querySelectorAll('[data-modal-close]').forEach(function(el){
        el.addEventListener('click', function(){
            el.blur();
            close();
        });
    });
    modal.addEventListener('keydown', function(e){
        if (e.key === 'Escape' || e.key === 'Enter') {
            e.preventDefault();
            e.stopPropagation();
            close();
        }
    });
}

// 완료 안내. options.autoClose 가 있으면 확인 버튼 없이 그 시간(ms, true 면 1000) 뒤 저절로 닫힌다(콜백 없음).
// 생략 형태: _alert(title, callback, options) · _alert(title, options) · _alert(title, desc, options)
function _alert(title, desc, callback, options){
    if (typeof desc === 'function') {
        options = callback;
        callback = desc;
        desc = '';
    } else if (desc !== null && typeof desc === 'object') {
        options = desc;
        callback = null;
        desc = '';
    }
    if (callback !== null && typeof callback === 'object') {
        options = callback;
        callback = null;
    }
    options = options || {};

    var modal = _modalGet('common-alert-modal',
        '<div class="manage-modal-backdrop" data-modal-close></div>' +
        '<section class="manage-modal-dialog" role="dialog" aria-modal="true" aria-label="처리 완료 안내">' +
            '<div class="manage-complete-body">' +
                '<h3 class="manage-complete-title" data-modal-title></h3>' +
                '<p class="manage-complete-desc" data-modal-desc></p>' +
                '<button class="manage-complete-button" type="button" data-modal-close>확인</button>' +
            '</div>' +
        '</section>',
        _modalBindSingle);

    clearTimeout(modal._autoCloseTimer);
    modal.querySelector('.manage-complete-button').hidden = !!options.autoClose;
    if (document.activeElement) document.activeElement.blur();
    _modalOpen(modal, title, desc, callback);

    if (options.autoClose) {
        modal._autoCloseTimer = setTimeout(function(){
            _modalClose(modal, false);
        }, typeof options.autoClose === 'number' ? options.autoClose : 1000);
    } else {
        modal.focus();
    }
}

// 경고. 확인을 누르면 callback. 생략 형태: _error(title, callback)
function _error(title, desc, callback){
    if (typeof desc === 'function') {
        callback = desc;
        desc = '';
    }

    var modal = _modalGet('common-warn-modal',
        '<div class="manage-modal-backdrop" data-modal-close></div>' +
        '<section class="manage-modal-dialog" role="dialog" aria-modal="true" aria-labelledby="commonWarnTitle">' +
            '<div class="warn-body">' +
                '<div class="warn-icon" aria-hidden="true"><span></span><span></span></div>' +
                '<h3 id="commonWarnTitle" class="common-warn-title" data-modal-title></h3>' +
                '<p class="common-warn-desc" data-modal-desc></p>' +
            '</div>' +
            '<div class="manage-modal-actions warn-actions">' +
                '<button class="manage-modal-btn primary" type="button" data-modal-close>확인</button>' +
            '</div>' +
        '</section>',
        _modalBindSingle);

    if (document.activeElement) document.activeElement.blur();
    _modalOpen(modal, title, desc, callback);
    modal.focus();
}

// 확인·취소. 확인을 누르면 callback, 취소하면 열기 전 초점 위치로 돌아간다. 생략 형태: _confirm(title, callback)
function _confirm(title, desc, callback){
    if (typeof desc === 'function') {
        callback = desc;
        desc = '';
    }

    var modal = _modalGet('common-confirm-modal',
        '<div class="manage-modal-backdrop" data-modal-close></div>' +
        '<section class="manage-modal-dialog" role="dialog" aria-modal="true" aria-labelledby="commonConfirmTitle">' +
            '<div class="manage-modal-head">' +
                '<div>' +
                    '<h3 id="commonConfirmTitle" class="common-confirm-title" data-modal-title></h3>' +
                    '<p class="common-confirm-desc" data-modal-desc></p>' +
                '</div>' +
                '<button class="manage-modal-close" type="button" aria-label="팝업 닫기" data-modal-close></button>' +
            '</div>' +
            '<div class="manage-modal-actions">' +
                '<button class="manage-modal-btn" type="button" data-modal-close>취소</button>' +
                '<button class="manage-modal-btn primary" type="button" data-modal-ok>확인</button>' +
            '</div>' +
        '</section>',
        function(m){
            var ok = m.querySelector('[data-modal-ok]');
            var cancel = function(){
                _modalClose(m, false);
                var back = m._returnFocus;
                m._returnFocus = null;
                if (back && back.isConnected && typeof back.focus === 'function') back.focus();
            };
            m.querySelectorAll('[data-modal-close]').forEach(function(el){
                el.addEventListener('click', function(){
                    el.blur();
                    cancel();
                });
            });
            ok.addEventListener('click', function(){
                ok.blur();
                _modalClose(m, true);
            });
            m.addEventListener('keydown', function(e){
                if (e.key === 'Escape') {
                    e.preventDefault();
                    e.stopPropagation();
                    cancel();
                } else if (e.key === 'Enter') {
                    // 버튼에 초점이 있으면 그 버튼의 기본 동작(취소면 취소)을 따르고, 창 자체에 초점이 있을 때만 확인
                    e.stopPropagation();
                    if (e.target === m) {
                        e.preventDefault();
                        ok.click();
                    }
                }
            });
        });

    modal._returnFocus = document.activeElement && document.activeElement !== document.body ? document.activeElement : null;
    if (document.activeElement) document.activeElement.blur();
    _modalOpen(modal, title, desc, callback);
    modal.querySelector('[data-modal-ok]').focus();
}
