angular.module('ap.controllers.main', [
        'ts.services.activity',
        'ts.services.user'
    ])
    .controller('PlayerMainController', [
        '$rootScope', '$scope', '$location', 'ActivityService', 'UserService',
        function($rootScope, $scope, $location, ActivityService, UserService){
            $rootScope.username = UserService.nick();
            $rootScope.userCount = 0;
            $rootScope.resources = {};
            $rootScope.presources = [];
            $scope.selectedResource = null;
            $scope.selectedRIndex = -1;

            $scope.showResourceDetail = function(resource){
                if(resource.type){
                    $scope.selectedResource = resource;
                    $scope.selectedRIndex = $rootScope.presources.indexOf(resource);
                }
            };

            $scope.hasPreviousPreviewableResource = function(){
                return $scope.selectedRIndex > 0;
            };
            $scope.hasNextPreviewableResource = function(){
                return $scope.selectedRIndex < $rootScope.presources.length-1;
            };

            $scope.selectPreviousPreviewableResource = function(){
                if($scope.selectedRIndex > 0){
                    -- $scope.selectedRIndex;
                    $scope.selectedResource = $rootScope.presources[$scope.selectedRIndex];
                }
            };
            $scope.selectNextPreviewableResource = function(){
                if($scope.selectedRIndex < $rootScope.presources.length-1){
                    ++ $scope.selectedRIndex;
                    $scope.selectedResource = $rootScope.presources[$scope.selectedRIndex];
                }
            }

            $scope.hideResourceDetail = function(){
                clearSelection();
            };

            function clearSelection(){
                $scope.selectedResource = null;
                $scope.selectedRIndex = -1;
            }

            function getActivity(){
                var aid = $location.search()['aid'];
                ActivityService.getActivity(
                    aid,
                    function(data, status){
                        console.log('[PlayerMainController] get activity success', status, data);
                        if(status == 200 && !data.c){
                            $rootScope.activity = data.r;
                            $rootScope.activity.resources.reverse();

                            /**
                             * 处理资源，将同一分钟内的活动归到一组
                             * $rootScope.resources = {
                             *      t1: [Resource1, Resource2, ...],
                             *      t2: ...
                             *      ...
                             * }
                             */
                             var groupedResources = _.map(
                                _.groupBy($rootScope.activity.resources, function(resource){
                                    var date = new Date(resource.date);
                                    date.setSeconds(0);
                                    date.setMilliseconds(0);
                                    return date.getTime();
                                }),
                                function(resources, ts){
                                    return {ts:ts, resources:resources};
                                }
                            );
                            $rootScope.resources = groupedResources;
                            //另外单独过滤过图片和视频这些可以预览大图的资源，用来做上下翻页用
                            $rootScope.presources = _.filter($rootScope.activity.resources, function(resource){
                                return resource.type == 1 || resource.type == 2;
                            });

                            //计算用户数
                            if($rootScope.activity.active){
                                $rootScope.userCount = $rootScope.activity.users.participators.length;
                            }
                            else{
                                var users = _.countBy($rootScope.selectedActivity.resources, function(resource){
                                    return resource.user;
                                });
                                $rootScope.userCount = _.keys(users).length;
                            }
                        }
                        else{
                            //TODO handle error
                        }
                    },
                    function(data, status){
                        //TODO handle server exception
                        console.error('[PlayerMainController] get activity fail', status, data);
                    }
                );
                console.log('[PlayerMainController] getting info of activity ' + aid);
            }

            getActivity();
        }
    ]);