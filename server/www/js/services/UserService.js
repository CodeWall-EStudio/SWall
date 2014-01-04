angular.module('ts.services.user', ['ts.utils.constants'])
    .service('UserService', [
        '$rootScope', '$location', '$http', 'EVENT_LOGIN',
        function($rootScope, $location, $http, EVENT_LOGIN){
            function uid(){
                return $location.search()['uid'];
            }

            function hasLoggedIn(){
                return uid() !== undefined;
            }

            function login(username, password, callback){
                $http.post('')
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