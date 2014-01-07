angular.module('ts.utils.constants', [])
    //env
    .constant('BACKEND_SERVER', location.hostname == 'localhost' ? 'http://localhost:8080' : '')

    //event
    .constant('EVENT_LOGIN', 'event.login') //登录结果
    .constant('EVENT_LOGIN_CLICK', 'event.login.click') //导航栏上的登录/用户名按钮被点
    .constant('EVENT_MODE_CHANGE', 'event.mode.change') //导航栏上切换了模式（查看/管理）

    //cmd
    .constant('CMD_SHOW_ACTIVITY_PANEL', 'cmd.activity.panel.show'); //展示活动创建/编辑面板