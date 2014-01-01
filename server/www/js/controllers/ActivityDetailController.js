angular.module('ts.controllers.activityDetail', [
        'ts.services.activity',
        'ts.controllers.activityPanel'
    ])
    .controller('ActivityDetailController', [
        '$rootScope', '$scope', 'ActivityService', 'CMD_SHOW_ACTIVITY_PANEL',
        function($rootScope, $scope, ActivityService, CMD_SHOW_ACTIVITY_PANEL){
            $scope.userCount = function(){
                if($rootScope.selectedActivity){
                    if($rootScope.selectedActivity.active){
                        return $rootScope.selectedActivity.users.participators.length;
                    }
                    else{
                        var users = _.countBy($rootScope.selectedActivity.resources, function(resource){
                            return resource.user;
                        });
                        return _.keys(users).length;
                    }
                }
                return 0;
            };

            $scope.editActivity = function(activityDetail){
                $rootScope.$emit(CMD_SHOW_ACTIVITY_PANEL, {
                    panelTitle: '编辑活动',
                    confirmBtnTitle: '保存',
                    activity: $.extend(true, {}, activityDetail)
                });
            };

            $scope.closeActivity = function(activityDetail){
                if(confirm('确定要关闭活动？')){
                    ActivityService.closeActivity(activityDetail._id);
                    //TODO how to refresh after successfully close the activity
                }
            };

            $scope.deleteActivity = function(activityDetail){

            };
        }
    ]);