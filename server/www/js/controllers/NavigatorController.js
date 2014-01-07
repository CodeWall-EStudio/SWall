angular.module('ts.controllers.navigator', [
        'ts.services.user'
    ])
    .controller('NavigatorController', [
        '$rootScope', '$scope', 'UserService', 'EVENT_LOGIN',
        function($rootScope, $scope, UserService, EVENT_LOGIN){
            function getNickname(){
                $scope.nickname = UserService.nick();
            }

            $rootScope.$on(EVENT_LOGIN, function(event, status){
                if(status == 200){
                    getNickname();
                }
            });

            getNickname();
        }
    ]);