angular.module('ts.controllers.navigator', [
        'ts.services.user'
    ])
    .controller('NavigatorController', [
        '$rootScope', '$scope', 'UserService', 'EVENT_LOGIN', 'EVENT_LOGIN_CLICK',
        function($rootScope, $scope, UserService, EVENT_LOGIN, EVENT_LOGIN_CLICK){
            function getNickname(){
                $scope.nickname = UserService.nick();
            }

            $rootScope.$on(EVENT_LOGIN, function(event, status){
                if(status == 200){
                    getNickname();
                }
            });

            $rootScope.$on(EVENT_LOGIN_CLICK, function(event){
                if(UserService.hasLoggedIn()){
                    if(confirm('确定要退出登录吗？')){
                        var d = (new Date()).toGMTString();
                        document.cookie = 'uin=; path=/; expires=' + d;
                        document.cookie = 'skey=; path=/; expires=' + d;
                    }
                    else return;
                }
                $('#loginModal').modal('show');
            });

            getNickname();
        }
    ]);