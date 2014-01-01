(function(){

    angular.module('teacherSpace', [
        'ts.services.user',
        'ts.services.activity',
        'ts.controllers.main',
        'ts.controllers.navigator',
        'ts.controllers.toolbar',
        'ts.controllers.activityList',
        'ts.controllers.activityDetail',
        'ts.controllers.activityPanel',
        'ts.controllers.loginForm',
        'ts.directives.ngEnter',
        'ts.directives.activityDate',
        'ts.directives.activityDatePicker'
    ]);
})();