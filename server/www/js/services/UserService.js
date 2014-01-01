angular.module('ts.services.user', [])
    .constant('EVENT_LOGIN', 'event.login')
    .service('UserService', [
        '$rootScope', '$location', 'EVENT_LOGIN',
        function($rootScope, $location, EVENT_LOGIN){
            function uid(){
                return $location.search()['uid'];
            }

            function hasLoggedIn(){
                return uid() !== undefined;
            }

            function login(username, password, callback){
                $location.search('uid', username);
                callback(0);
                $rootScope.$emit(EVENT_LOGIN, 0);
            }

            return {
                uid: uid,
                hasLoggedIn: hasLoggedIn,
                login: login
            };
        }
    ]);