// 편집 창(<dialog>.showModal())은 브라우저 최상위 레이어에 올라가서, body 에 붙은 모달은 z-index 와 상관없이 그 뒤에 가려진다.
// 열린 dialog 가 있으면 모달을 그 안으로 옮겨 같은 레이어에서 보이게 한다.
function _modalHost(modal){
    var host = document.querySelector('dialog[open]') || document.body;
    if (modal.parentNode !== host) host.appendChild(modal);
}

function _alert(title, desc, callback, options){
    if(typeof desc === 'function'){
        options  = callback;
        callback = desc;
        desc     = '';
    }
    if(desc !== null && typeof desc === 'object'){
        options  = desc;
        desc     = '';
        callback = null;
    }
    if(callback !== null && typeof callback === 'object'){
        options  = callback;
        callback = null;
    }
    options = options || {};

    let modal = document.getElementById('common-alert-modal');

    if(!modal){
        modal = document.createElement('div');
        modal.id = 'common-alert-modal';
        modal.className = 'manage-modal manage-complete-modal';
        modal.setAttribute('aria-hidden', 'true');
        modal.setAttribute('tabindex', '-1');
        modal.innerHTML =
            '<div class="manage-modal-backdrop" data-alert-close="true"></div>' +
            '<section class="manage-modal-dialog manage-complete-dialog" role="dialog" aria-modal="true" aria-label="처리 완료 안내">' +
                '<div class="manage-complete-body">' +
                    '<div class="manage-complete-icon-wrap" aria-hidden="true">' +
                        '<div class="manage-complete-icon"></div>' +
                    '</div>' +
                    '<h3 class="manage-complete-title" style="white-space: pre-line; word-break: keep-all"></h3>' +
                    '<p class="manage-complete-desc" style="white-space: pre-line"></p>' +
                    '<button class="manage-complete-button" type="button" data-alert-close="true">확인</button>' +
                '</div>' +
            '</section>';
        document.body.appendChild(modal);

        modal.querySelectorAll('[data-alert-close="true"]').forEach(function(el){
            el.addEventListener('click', function(){
                if(modal._autoCloseTimer){
                    clearTimeout(modal._autoCloseTimer);
                    modal._autoCloseTimer = null;
                }
                modal.classList.remove('is-open');
                modal.setAttribute('aria-hidden', 'true');
                el.blur();
                let cb = modal._cb;
                modal._cb = null;
                if(typeof cb === 'function') cb();
            });
        });

        modal.addEventListener('keydown', function(e){
            if(e.key === 'Escape' || e.key === 'Enter'){
                e.preventDefault();
                e.stopPropagation();
                modal.querySelector('[data-alert-close="true"]').click();
            }
        });
    }

    if(modal._autoCloseTimer){
        clearTimeout(modal._autoCloseTimer);
        modal._autoCloseTimer = null;
    }

    let btn = modal.querySelector('.manage-complete-button');
    btn.style.display = options.autoClose ? 'none' : '';

    if(document.activeElement) document.activeElement.blur();

    modal.querySelector('.manage-complete-title').textContent = title || '';
    modal.querySelector('.manage-complete-desc').textContent  = desc  || '';
    modal._cb = callback || null;
    _modalHost(modal);
    modal.classList.add('is-open');
    modal.setAttribute('aria-hidden', 'false');
    if(!options.autoClose) modal.focus();

    if(options.autoClose){
        let delay = typeof options.autoClose === 'number' ? options.autoClose : 1000;
        modal._autoCloseTimer = setTimeout(function(){
            modal.classList.remove('is-open');
            modal.setAttribute('aria-hidden', 'true');
            modal._autoCloseTimer = null;
        }, delay);
    }
}

function _error(title, desc, callback){
	if(typeof desc === 'function'){
		callback = desc;
		desc = '';
	}

	let modal = document.getElementById('common-warn-modal');

	if(!modal){
		modal = document.createElement('div');
		modal.id = 'common-warn-modal';
		modal.className = 'manage-modal manage-modal-light bus-manage-modal route-error-demo-modal';
		modal.setAttribute('aria-hidden', 'true');
		modal.setAttribute('tabindex', '-1');
		modal.innerHTML =
			'<div class="manage-modal-backdrop" data-warn-close="true"></div>' +
			'<section class="manage-modal-dialog" role="dialog" aria-modal="true" aria-labelledby="commonWarnTitle">' +
				'<div class="route-error-demo-body">' +
					'<div class="route-error-demo-icon" aria-hidden="true">' +
						'<span></span>' +
						'<span></span>' +
					'</div>' +
					'<h3 id="commonWarnTitle" class="common-warn-title" style="white-space: pre-line"></h3>' +
					'<p class="common-warn-desc" style="white-space: pre-line"></p>' +
				'</div>' +
				'<div class="manage-modal-actions route-error-demo-actions">' +
					'<button class="manage-modal-btn primary route-error-demo-button" type="button" data-route-error-close="true">확인</button>' +
				'</div>' +
			'</section>';
		document.body.appendChild(modal);

		function closeWarnModal(){
			modal.classList.remove('is-open');
			modal.setAttribute('aria-hidden', 'true');
			let cb = modal._cb;
			modal._cb = null;
			if(typeof cb === 'function') cb();
		}

		modal.querySelectorAll('[data-route-error-close="true"]').forEach(function(el){
			el.addEventListener('click', function(){
				el.blur();
				closeWarnModal();
			});
		});

		modal.querySelector('.manage-modal-backdrop').addEventListener('click', function(){
			closeWarnModal();
		});

		modal.addEventListener('keydown', function(e){
			if(e.key === 'Escape' || e.key === 'Enter'){
				e.preventDefault();
				e.stopPropagation();
				modal.querySelector('[data-route-error-close="true"]').click();
			}
		});
	}

	if(document.activeElement) document.activeElement.blur();

	modal.querySelector('.common-warn-title').textContent = title || '';
	modal.querySelector('.common-warn-desc').textContent  = desc  || '';
	modal._cb = callback || null;
	_modalHost(modal);
	modal.classList.add('is-open');
	modal.setAttribute('aria-hidden', 'false');
	modal.focus();
}

function _confirm(title, desc, callback){
	if(typeof desc === 'function'){
		callback = desc;
		desc = '';
	}

	let modal = document.getElementById('common-confirm-modal');

	if(!modal){
		modal = document.createElement('div');
		modal.id = 'common-confirm-modal';
		modal.className = 'manage-modal manage-modal-light bus-manage-modal common-confirm-modal';
		modal.setAttribute('aria-hidden', 'true');
		modal.setAttribute('tabindex', '-1');
		modal.innerHTML =
			'<div class="manage-modal-backdrop" data-confirm-close="true"></div>' +
			'<section class="manage-modal-dialog" role="dialog" aria-modal="true" aria-labelledby="commonConfirmTitle">' +
				'<div class="manage-modal-head route-confirm-demo-head">' +
					'<div class="route-confirm-demo-copy">' +
						'<h3 id="commonConfirmTitle" class="common-confirm-title" style="white-space: pre-line"></h3>' +
						'<p class="common-confirm-desc" style="white-space: pre-line"></p>' +
					'</div>' +
					'<button class="manage-modal-close" type="button" aria-label="팝업 닫기" data-confirm-close="true"></button>' +
				'</div>' +
				'<div class="manage-modal-actions route-confirm-demo-actions">' +
					'<button class="manage-modal-btn ghost" type="button" data-confirm-close="true">취소</button>' +
					'<button class="manage-modal-btn primary common-confirm-ok" type="button">확인</button>' +
				'</div>' +
			'</section>';
		document.body.appendChild(modal);

		modal.querySelectorAll('[data-confirm-close="true"]').forEach(function(el){
			el.addEventListener('click', function(){
				modal.classList.remove('is-open');
				modal.setAttribute('aria-hidden', 'true');
				el.blur();
				modal._cb = null;
				// 취소하면 확인창을 열기 전에 초점이 있던 곳(입력 칸 등)으로 돌아간다
				let back = modal._returnFocus;
				modal._returnFocus = null;
				if(back && back.isConnected && typeof back.focus === 'function') back.focus();
			});
		});

		modal.querySelector('.common-confirm-ok').addEventListener('click', function(){
			modal.classList.remove('is-open');
			modal.setAttribute('aria-hidden', 'true');
			this.blur();
			let cb = modal._cb;
			modal._cb = null;
			if(typeof cb === 'function') cb();
		});

		modal.addEventListener('keydown', function(e){
			if(e.key === 'Escape'){
				e.preventDefault();
				e.stopPropagation();
				modal.querySelector('[data-confirm-close="true"]').click();
			}else if(e.key === 'Enter' && e.target === modal){
				// 버튼에 초점이 있으면 그 버튼의 기본 동작(취소면 취소)을 따른다. 창 자체에 초점이 있을 때만 확인
				e.preventDefault();
				e.stopPropagation();
				modal.querySelector('.common-confirm-ok').click();
			}else if(e.key === 'Enter'){
				e.stopPropagation();
			}
		});
	}

	modal._returnFocus = document.activeElement && document.activeElement !== document.body ? document.activeElement : null;
	if(document.activeElement) document.activeElement.blur();

	modal.querySelector('.common-confirm-title').textContent = title || '';
	modal.querySelector('.common-confirm-desc').textContent  = desc  || '';
	modal._cb = callback || null;
	_modalHost(modal);
	modal.classList.add('is-open');
	modal.setAttribute('aria-hidden', 'false');
	modal.querySelector('.common-confirm-ok').focus();
}