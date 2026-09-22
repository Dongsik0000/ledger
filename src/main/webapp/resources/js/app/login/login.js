App.login = (function(){
    var m$ = {
            form: document.getElementById('loginForm')
        },
        settings = { submitting: false },
        url = { login: contextPath + '/auth/login' },

        init = function(){
            m$.form.addEventListener('submit', function(e){
                e.preventDefault();
                submit();
            });
        },

        submit = function(){
            if(settings.submitting) return;

            var param = App.formToObject(m$.form);
            if(App.isEmpty(param.username) || App.isEmpty(param.password)){
                _error('알림', '아이디와 비밀번호를 입력해주세요.');
                return;
            }

            settings.submitting = true;
            App.post(url.login, param)
                .then(function(res){
                    if(res.code === '00'){
                        location.href = contextPath + '/ledger/dashboard';
                    }else if(res.code === '01' || res.code === '02'){
                        _error('로그인 실패', '아이디 또는 비밀번호를 확인해주세요.');
                    }else{
                        _error('오류', res.message || '로그인 처리 중 문제가 발생했습니다.');
                    }
                })
                .catch(function(){})
                .then(function(){ settings.submitting = false; });
        };

    return { init: init };
}());

document.addEventListener('DOMContentLoaded', function(){
    App.login.init();
});
