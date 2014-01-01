angular.module('ts.controllers.main', [
        'ts.services.user',
        'ts.services.activity'
    ])
    .controller('MainController', [
        '$rootScope', '$scope', '$http', '$location', 'UserService', 'ActivityService',
        'EVENT_LOGIN',
        function($rootScope, $scope, $http, $location, UserService, ActivityService,
            EVENT_LOGIN){

            $rootScope.$on(EVENT_LOGIN, function(event, ret){
                $rootScope.fetchActivities();
            });

            /**
             * 拉取活動列表
             * @param [params]
             */
            $rootScope.fetchActivities = function(params){
                //TODO 显示一个modal菊花禁掉所有操作
                //取消选择了的活动
                ActivityService.selectActivity();
                ActivityService.fetchActivities(params);
            };

            $rootScope.showLoginModal = function(){
                //TODO 改为发事件
                if(UserService.hasLoggedIn()){
                    //TODO logout
                }
                else {
                    $('#loginModal').modal('show');
                }
            };

            /**
             * 返回當前模式，可能是「瀏覽活動(viewer)」或是「管理活動(manager)」
             * @returns {String}
             */
            $rootScope.mode = function(){
                return $location.search()['mode'];
            };
            /**
             * 使用指定模式
             * @param {String} mode viewer or manager
             */
            $rootScope.gotoMode = function(mode){
                $location.search('mode', mode);
                $rootScope.$emit('event.mode.change', mode);
            };

            function main(){
                if(UserService.hasLoggedIn()){
                    $rootScope.fetchActivities();
                }
                else{
                    $rootScope.showLoginModal();
                }
            }
            main();
        }
    ]);