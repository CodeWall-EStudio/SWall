angular.module('ts.controllers.activityPanel', [
        'ts.services.activity'
    ])
    .constant('CMD_SHOW_ACTIVITY_PANEL', 'cmd.activity.panel.show')
    .controller('ActivityPanelController', [
        '$rootScope', '$scope', '$http', 'ActivityService', 'CMD_SHOW_ACTIVITY_PANEL',
        function($rootScope, $scope, $http, ActivityService, CMD_SHOW_ACTIVITY_PANEL){
            var modal                       = $('#createActivityModal'),
                datePicker                  = $('input[activity-date-picker]'),
                usersInput                  = $('input[data-role="tagsinput"]'),
                fieldset                    = document.getElementById('activityFormAllFields'),
                fieldsetForOpenActivities   = document.getElementById('activityFormFieldsForOpenActivities'),
                defaults = {
                    panelTitle: '创建活动',
                    confirmBtnTitle: '创建活动',
                    cancelBtnTitle: '取消',
                    activityContent: {
                        info: {
                            type: 1
                        },
                        users: {
                            invitedUsers: []
                        }
                    }
                };

            $rootScope.$on(CMD_SHOW_ACTIVITY_PANEL, function(event, data){
                //initialize $scope
                data = data || {};
                $scope.panelTitle       = data.panelTitle || defaults.panelTitle;
                $scope.confirmBtnTitle  = data.confirmBtnTitle || defaults.confirmBtnTitle;
                $scope.cancelBtnTitle   = data.cancelBtnTitle || defaults.cancelBtnTitle;
                $scope.activity         = data.activity ||cloneDefaulActivityContent();
                $scope.invitedUsers     = $scope.activity.users.invitedUsers.join(',');

                //update date picker and users input
                var date = $scope.activity.info.date;
                if(date) datePicker.datepicker('setValue', new Date(date));
                _.each($scope.activity.users.invitedUsers, function(item){
                    usersInput.tagsinput('add', item);
                });

                //show the panel's dom
                $scope.showActivityPanel();
            });

            modal.on('hidden.bs.modal', function () {
                //$scope.$apply(function(){
                resetFields();
                //});
            });

            $scope.$watch('grade', function(newValue){
                $scope.cls = 0;
            });

            $scope.showActivityPanel = function(){
                $('#createActivityModal').modal('show');
                if(!$scope.activity._id || $scope.activity.active){
                    fieldsetForOpenActivities.removeAttribute('disabled');
                }
                else{
                    fieldsetForOpenActivities.setAttribute('disabled', 'disabled');
                }
            };

            $scope.createActivity = function(){
                $scope.errMsg = '';
                disableFields();

                var config = $scope.config ? $scope.config.activityConfig : {};
                var params = {
                    uids:       $scope.invitedUsers || '',
                    title:      $scope.activity.info.title || '',
                    type:       $scope.activity.info.type|1,
                    desc:       $scope.activity.info.desc || '',
                    date:       $scope.activity.info.date ? $scope.activity.info.date.getTime() : 0,
                    teacher:    $scope.activity.info.teacher || '',
                    grade:      config.classes[$scope.grade].grade,
                    'class':    config.classes[$scope.grade].cls[$scope.cls],
                    subject:    config.subjects[$scope.subject],
                    domain:     $scope.activity.info.domain || ''
                };
                ActivityService.createActivity(
                    params,
                    handleActivityCreatedOrUpdatedSuccess,
                    handleActivityCreatedOrUpdatedFail
                );
            };

            $scope.updateActivity = function(){
                $scope.errMsg = '';
                disableFields();

                var config = $scope.config ? $scope.config.activityConfig : {};
                var params = {
                    uids: $scope.invitedUsers || ''
                };
                if($scope.activity.active){
                    params['title']     = $scope.activity.info.title || '';
                    params['type']      = $scope.activity.info.type || '';
                    params['desc']      = $scope.activity.info.desc || '';
                    params['date']      = $scope.activity.info.date || 0;
                    params['teacher']   = $scope.activity.info.teacher || '';
                    params['grade']     = config.classes[$scope.grade].grade || '';
                    params['class']     = config.classes[$scope.grade].cls[$scope.cls] || '';
                    params['subject']   = config.subjects[$scope.subject] || '';
                    params['domain']    = $scope.activity.info.domain || '';
                }
                ActivityService.updateActivity(
                    $scope.activity._id,
                    params,
                    handleActivityCreatedOrUpdatedSuccess,
                    handleActivityCreatedOrUpdatedFail
                );
            };

            $scope.handleConfirmBtnClick = function(){
                if($scope.activity._id) $scope.updateActivity();
                else                    $scope.createActivity();
            };

            function cloneDefaulActivityContent(){
                return $.extend(true, {}, defaults.activityContent);
            }

            function fetchActivityConfig(){
                ActivityService.fetchActivityConfig(
                    function(data, status){
                        $scope.config = data;
                        $scope.grade = 0;
                        $scope.cls = 0;
                        $scope.subject = 0;
                    }
                )
            }

            function handleActivityCreatedOrUpdatedSuccess(data, status){
                console.log(data, status);
                enableFields();
                hideModal();
            }
            function handleActivityCreatedOrUpdatedFail(data, status){
                data = data || {c:-1, msg:'UNKNOWN'};
                $scope.errMsg = status + ' - [' + data.c + '] ' + data.m;
                enableFields();
            }

            function enableFields(){
                fieldset.removeAttribute('disabled');
            }
            function disableFields(){
                fieldset.setAttribute('disabled', 'disabled');
            }

            function hideModal(){
                modal.modal('hide');
            }

            function resetFields(){
                $scope.activity = cloneDefaulActivityContent();
                $scope.grade = $scope.cls = $scope.subject = 0;
                $scope.errMsg = '';
                usersInput.tagsinput('removeAll');
            }

            fetchActivityConfig();
        }
    ]);