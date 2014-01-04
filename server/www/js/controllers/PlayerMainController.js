angular.module('ap.controllers.main', [
        'ts.services.activity',
        'ts.services.user'
    ])
    .controller('PlayerMainController', [
        '$rootScope', '$scope', '$location', 'ActivityService', 'UserService',
        function($rootScope, $scope, $location, ActivityService, UserService){
            $rootScope.username = UserService.uid();

            function getActivity(){
                var aid = $location.search()['aid'];
                ActivityService.getActivity(
                    aid,
                    function(data, status){
                        if(status == 200 && !data.c){
                            $rootScope.activity = data.r;

                            var date = new Date(data.r.info.date);
                            $rootScope.activityDate = date.getFullYear() + '年' + (date.getMonth()+1) + '月' + date.getDate() + '日';
                            $rootScope.activityTime = date.getHours() + ':' + date.getMinutes();
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