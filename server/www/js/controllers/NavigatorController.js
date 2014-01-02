angular.module('ts.controllers.navigator', [
        'ts.services.user'
    ])
    .controller('NavigatorController', [
        '$rootScope', '$scope', 'UserService', 'EVENT_LOGIN',
        function($rootScope, $scope, UserService, EVENT_LOGIN){
            $scope.username = UserService.uid();

            $rootScope.$on(EVENT_LOGIN, function(event, ret){
                $scope.username = UserService.uid();
            });
        }
    ]);