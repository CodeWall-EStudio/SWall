angular.module('ts.services.user', [
        'ts.utils.constants',
        'ts.services.utils'
    ])
    .service('UserService', [
        '$rootScope', '$http', 'UtilsService', 'BACKEND_SERVER', 'EVENT_LOGIN',
        function($rootScope, $http, UtilsService, BACKEND_SERVER, EVENT_LOGIN){
            $rootScope.orgTree = {};
            $rootScope.orgUserIndex = {};
            $rootScope.processedOrgTree = [];

            function uid(){
                return UtilsService.cookie.get('uid');
            }

            function hasLoggedIn(){
                var uid = UtilsService.cookie.get('uid'),
                    skey = UtilsService.cookie.get('skey');
                return Boolean(uid && skey);
            }

            function isCreatorOfActivity(activity){
                var uid = UtilsService.cookie.get('uid'),
                    creator = activity.users.creator;
                return uid == creator;
            }

            function nick(){
                var uid = UtilsService.cookie.get('uid')
                if(uid){
                    //找一下localStorage里存储的登录结果，并对比uid确认是否当前用户
                    var s = localStorage['login_result'],
                        r = s ? JSON.parse(s) : {};
                    return r ? r.nick : uid;
                }
                else {
                    return '登录';
                }
            }

            function login(username, password, success, error){
                function handleResponse(data, status){
                    console.log('[UserService] login result:', data, status, document.cookie);
                    if(status == 200 && success){
                        localStorage['login_result'] = JSON.stringify(data);
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

            function logout(){
                var d = (new Date()).toGMTString();
                document.cookie = 'uid=; domain=.codewalle.com; path=/; expires=' + d;
                document.cookie = 'skey=; domain=.codewalle.com; path=/; expires=' + d;
                document.cookie = 'connect.sid=; domain=.codewalle.com; path=/; expires=' + d;
                localStorage.removeItem('login_result');
            }

            function fetchOrganizationTree(callback){
                var xhr = new XMLHttpRequest();
                xhr.addEventListener('load', function(){
                    var json = JSON.parse(xhr.responseText),
                        errorCode = json ? json['err'] : -1;
                    if(errorCode){
                        console.error('[UserService][API] fetch organization tree fail!', errorCode);
                        callback(errorCode);
                    }
                    else {
                        /**
                         * organizationTree = {
                         *      name: 'xxx',
                         *      users: [{nick, name, ...}, ...],
                         *      children: [childTree1, childTree2, ...],
                         *      ...
                         * }
                         */
                        var rawTree = json['result']['data']/*['children']*/,
                            userIndex = {},
                            processed = processOrganizationTree(rawTree, false, userIndex);

                        processed.nodes.unshift({text:'全员', icon:'glyphicon glyphicon-user', value:'*', id:'*'});
                        $rootScope.orgTree = rawTree;
                        $rootScope.orgUserIndex = userIndex;
                        $rootScope.processedOrgTree = processed.nodes || [];
                        console.log('orgTree',          $rootScope.orgTree);
                        console.log('orgUserIndex',     $rootScope.orgUserIndex);
                        console.log('processedOrgTree', $rootScope.processedOrgTree);
                        callback();
                    }
                });
                xhr.addEventListener('error', function(e){
                    console.error('[UserService][Request] fetch organization tree fail!', e);
                    callback(e);
                });

                xhr.withCredentials = true;
                xhr.open('GET', '/users/tree');
                xhr.send();
                console.log('[UserService] fetching organization tree ...')
            }

            function processOrganizationTree(tree, isUser, userIndex){
                /**
                 * full node specification:
                 * {
                 *      text: "Node 1",
                 *      icon: "glyphicon glyphicon-stop",
                 *      color: "#000000",
                 *      backColor: "#FFFFFF",
                 *      href: "#node-1",
                 *      tags: ['available'],
                 *      nodes: [
                 *          {},
                 *          ...
                 *      ]
                 * }
                 */
                if(isUser) {
                    userIndex[tree['_id']] = tree;
                    return {
                        text:   tree.nick,
                        icon:   'glyphicon glyphicon-user',
                        value:  tree.name,
                        id:     tree['_id']
                    };
                }
                else {
                    var userNodes = _.map(tree.users, function(user){
                            return processOrganizationTree(user, true, userIndex);
                        }),
                        childNodes = _.map(tree.children, function(child){
                            return processOrganizationTree(child, false, userIndex);
                        });
                    return {
                        text:       tree.name,
                        icon:       'glyphicon glyphicon-folder-open',
                        color:      '#999',
                        selectable: false,
                        nodes:      userNodes.concat(childNodes)
                    };
                }
            }

            function fetchProfile(uid){

            }

            return {
                uid:                    uid,
                hasLoggedIn:            hasLoggedIn,
                nick:                   nick,
                login:                  login,
                logout:                 logout,
                fetchProfile:           fetchProfile,
                fetchOrganizationTree:  fetchOrganizationTree,
                activity: {
                    isCreatorOfActivity: isCreatorOfActivity
                }
            };
        }
    ]);