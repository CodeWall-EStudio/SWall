angular.module('ts.services.user', [
        'ts.utils.constants',
        'ts.services.utils'
    ])
    .service('UserService', [
        '$rootScope', '$http', 'UtilsService', 'BACKEND_SERVER', 'EVENT_LOGIN',
        function($rootScope, $http, UtilsService, BACKEND_SERVER, EVENT_LOGIN){
            var nick;

            function uid(){
                return UtilsService.cookie.get('uid');
            }

            function hasLoggedIn(){
                var uid = UtilsService.cookie.get('uid'),
                    skey = UtilsService.cookie.get('skey');
                return uid && skey;
            }

            function nick(){
                return nick;
            }

            function login(username, password, success, error){
                function handleResponse(data, status){
                    console.log('[UserService] login result:', data, status, document.cookie);
                    if(status == 200 && success){
                        nick = data.nick;
                        success(data, status);
                    }
                    else if(error){
                        error(data, status);
                    }
                    $rootScope.$emit(EVENT_LOGIN, status, data);
                }
                $http.put(
                        BACKEND_SERVER + '/users/' + username + '/login',
                        'pwd=' + encodeURIComponent(password),
                        {
                            responseType: 'json',
                            headers: {'Content-Type': 'application/x-www-form-urlencoded'}
                        }
                    )
                    .success(handleResponse)
                    .error(handleResponse);
            }

            return {
                uid: uid,
                hasLoggedIn: hasLoggedIn,
                nick: nick,
                login: login
            };
        }
    ]);