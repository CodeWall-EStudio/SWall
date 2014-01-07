angular.module('ap.controllers.main', [
        'ts.services.activity',
        'ts.services.user'
    ])
    .controller('PlayerMainController', [
        '$rootScope', '$scope', '$location', 'ActivityService', 'UserService',
        function($rootScope, $scope, $location, ActivityService, UserService){
            $rootScope.username = UserService.nick();

            function getActivity(){
                var aid = $location.search()['aid'];
                ActivityService.getActivity(
                    aid,
                    function(data, status){
                        if(status == 200 && !data.c){
                            $rootScope.activity = data.r;

                            //计算活动年月和时间
                            var date = new Date(data.r.info.date);
                            $rootScope.activityDate = date.getFullYear() + '年' + (date.getMonth()+1) + '月' + date.getDate() + '日';
                            $rootScope.activityTime = date.getHours() + ':' + date.getMinutes();

                            //处理资源，将同一分钟内的活动归到一组
                            $rootScope.resources = _.groupBy($rootScope.activity.resources, function(resource){
                                var date = new Date(resource.date);
                                date.setSeconds(0);
                                date.setMilliseconds(0);
                                return date.getTime();
                            });
                        }
                        else{
                            //TODO handle error
                        }
                    },
                    function(data, status){
                        //TODO handle server exception
                    }
                );
                console.log('[PlayerMainController] getting info of activity ' + aid);
            }

            getActivity();
        }
    ]);