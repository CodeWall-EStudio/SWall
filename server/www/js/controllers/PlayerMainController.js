angular.module('ap.controllers.main', [
        'ts.services.activity',
        'ts.services.user'
    ])
    .controller('PlayerMainController', [
        '$rootScope', '$scope', '$location', 'ActivityService', 'UserService',
        function($rootScope, $scope, $location, ActivityService, UserService){
            $rootScope.username = UserService.nick();
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

                            /**
                             * 处理资源，将同一分钟内的活动归到一组
                             * $rootScope.resources = {
                             *      t1: [Resource1, Resource2, ...],
                             *      t2: ...
                             *      ...
                             * }
                             */
                            $rootScope.resources = _.groupBy($rootScope.activity.resources, function(resource){
                                var date = new Date(resource.date);
                                date.setSeconds(0);
                                date.setMilliseconds(0);
                                return date.getTime();
                            });

                            $rootScope.presources = _.filter($rootScope.activity.resources, function(resource){
                                return resource.type == 1 || resource.type == 2;
                            });
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