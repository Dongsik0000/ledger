App.signup = (function(){
    var m$ = {
            form: document.getElementById('signupForm')
        },
        settings = { submitting: false },
        url = { signup: contextPath + '/auth/signup' },

        init = function(){
            m$.form.addEventListener('submit', function(e){
                e.preventDefault();
                submit();
            });
        },

        submit = function(){
            if(settings.submitting) return;

            var param = App.formToObject(m$.form);
            if(App.isEmpty(param.username) || App.isEmpty(param.password) || App.isEmpty(param.signupCode)){
                _error('알림', '모든 항목을 입력해주세요.');
                return;
            }
            if(param.password.length < 8){
                _error('알림', '비밀번호는 8자 이상이어야 합니다.');
                return;
            }

            settings.submitting = true;
            App.post(url.signup, param)
                .then(function(res){
                    switch(res.code){
                        case '00':
                            _alert('가입 완료', '로그인 화면으로 이동합니다.', function(){
                                location.href = contextPath + '/login';
                            });
                            break;
                        case '11': _error('가입 실패', '가입 코드가 올바르지 않습니다.'); break;
                        case '12': _error('가입 실패', '이미 사용 중인 아이디입니다.'); break;
                        default:   _error('오류', res.message || '가입 처리 중 문제가 발생했습니다.');
                    }
                })
                .catch(function(){})
                .then(function(){ settings.submitting = false; });
        };

    return { init: init };
}());

document.addEventListener('DOMContentLoaded', function(){
    App.signup.init();
});
