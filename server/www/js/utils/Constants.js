angular.module('ts.utils.constants', [])
    //env
    .constant('BACKEND_SERVER', location.hostname == 'localhost' ? 'http://localhost:8080' : '')

    //event
    .constant('EVENT_LOGIN', 'event.login')
    .constant('EVENT_MODE_CHANGE', 'event.mode.change')

    //cmd
    .constant('CMD_SHOW_ACTIVITY_PANEL', 'cmd.activity.panel.show');