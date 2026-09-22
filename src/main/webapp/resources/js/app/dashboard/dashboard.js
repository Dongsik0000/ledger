App.dashboard = (function(){
    var init = function(){
        // 주기 요약: App.post(contextPath + '/ledger/dashboard/summary', {}).then(render)
    };

    return { init: init };
}());

document.addEventListener('DOMContentLoaded', function(){
    App.dashboard.init();
});
