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
            if(App.isEmpty(param.username) || App.isEmpty(param.password) || App.isEmpty(param.passwordConfirm) || App.isEmpty(param.signupCode)){
                _error('알림', '모든 항목을 입력해주세요.');
                return;
            }
            if(param.username.length > 50){
                _error('알림', '아이디는 50자 이하로 입력해주세요.');
                return;
            }
            if(param.password.length < 8 || new TextEncoder().encode(param.password).length > 72){
                _error('알림', '비밀번호는 8자 이상, 72바이트 이하로 입력해주세요.\n(영문 72자, 한글 24자까지)');
                return;
            }
            if(param.password !== param.passwordConfirm){
                _error('알림', '비밀번호 확인이 일치하지 않아요.');
                return;
            }

            settings.submitting = true;
            App.post(url.signup, param)
                .then(function(res){
                    switch(res.code){
                        case App.CODE.SUCCESS:
                            _alert('가입 완료', '로그인 화면으로 이동합니다.', function(){
                                location.href = contextPath + '/login';
                            });
                            break;
                        case App.CODE.SIGNUP_FAIL_CODE:
                            _error('가입 실패', '가입 코드가 올바르지 않습니다.');
                            break;
                        case App.CODE.SIGNUP_FAIL_EXISTS:
                            _error('가입 실패', '이미 사용 중인 아이디입니다.');
                            break;
                        case App.CODE.SIGNUP_BLOCKED:
                            _error('가입 제한', '가입 코드를 여러 번 잘못 입력했습니다.\n5분 뒤에 다시 시도해주세요.');
                            break;
                        default:
                            App.fail(res, '가입 처리 중 문제가 발생했습니다.');
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
