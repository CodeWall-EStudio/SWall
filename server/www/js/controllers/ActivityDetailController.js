angular.module('ts.controllers.activityDetail', [
        'ts.utils.constants',
        'ts.services.activity',
        'ts.controllers.main'
    ])
    .controller('ActivityDetailController', [
        '$rootScope', '$scope', 'ActivityService', 'EVENT_MODE_CHANGE', 'CMD_SHOW_ACTIVITY_PANEL',
        function($rootScope, $scope, ActivityService, EVENT_MODE_CHANGE, CMD_SHOW_ACTIVITY_PANEL){
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

            $scope.editActivity = function(){
                $rootScope.$emit(CMD_SHOW_ACTIVITY_PANEL, {
                    panelTitle: '编辑活动',
                    confirmBtnTitle: '保存',
                    activity: $.extend(true, {}, $rootScope.selectedActivity)
                });
            };

            $scope.closeActivity = function(){
                if(confirm('确定要关闭活动？')){
                    ActivityService.closeActivity($rootScope.selectedActivity._id);
                    //TODO how to refresh after successfully close the activity
                }
            };

            $scope.deleteActivity = function(){

            };

            $scope.handlePlayBtnClick = function(){
                var url = 'activity_play.html#?aid=' + $rootScope.selectedActivity._id;
                window.open(url, '_blank');
            };

            $rootScope.$on(EVENT_MODE_CHANGE, function(event, mode){
                ActivityService.selectActivity();
            });
        }
    ]);