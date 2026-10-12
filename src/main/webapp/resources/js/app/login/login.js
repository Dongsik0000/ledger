App.login = (function(){
	var m$ = {
			form: document.getElementById('loginForm')  /* 로그인 폼 */
		},

		settings = {
			submitting: false
		},

		url = {
			login: contextPath + '/auth/login'
		},

		/* 화면 이벤트 연결 */
		init = function(){
			bindEvent();
		},

		/* 폼 제출을 Ajax 요청으로 바꾼다 */
		bindEvent = function(){
			m$.form.addEventListener('submit', function(e){
				e.preventDefault();
				submit();
			});
		},

		/* 입력 확인 후 로그인 요청 */
		submit = function(){
			if(settings.submitting) return;

			var param = App.formToObject(m$.form);
			if(App.isEmpty(param.username) || App.isEmpty(param.password)){
				_error('알림', '아이디와 비밀번호를 입력해주세요.');
				return;
			}

			settings.submitting = true;
			App.post(url.login, param)
				.then(handleLoginResult)
				.catch(function(){})
				.then(function(){ settings.submitting = false; });
		},

		/* 결과 코드별 이동·안내 */
		handleLoginResult = function(res){
			switch(res.code){
				case App.CODE.SUCCESS:
					location.href = contextPath + '/ledger/dashboard';
					break;
				case App.CODE.LOGIN_FAIL:
					_error('로그인 실패', '아이디 또는 비밀번호를 확인해주세요.');
					break;
				case App.CODE.LOGIN_BLOCKED:
					_error('로그인 제한', '로그인에 여러 번 실패했습니다.\n5분 뒤에 다시 시도해주세요.');
					break;
				default:
					App.fail(res, '로그인 처리 중 문제가 발생했습니다.');
			}
		};

	return{
		init: init
	};
}());

document.addEventListener('DOMContentLoaded', function(){
	App.login.init();
});
