angular.module('ts.controllers.activityPanel', [
        'ts.utils.constants',
        'ts.services.activity'
    ])
    .controller('ActivityPanelController', [
        '$rootScope', '$scope', '$http', 'ActivityService', 'CMD_SHOW_ACTIVITY_PANEL',
        function($rootScope, $scope, $http, ActivityService, CMD_SHOW_ACTIVITY_PANEL){
            var modal                       = $('#createActivityModal'),
                usersInput                  = $('input[data-role="tagsinput"]'),
                datetimePicker              = document.getElementById('na_date'),
                fieldset                    = document.getElementById('activityFormAllFields'),
                fieldsetForOpenActivities   = document.getElementById('activityFormFieldsForOpenActivities'),
                timezoneOffset              = (new Date()).getTimezoneOffset(),
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

                //初始化日期時間
                var ts = $scope.activity.info.date,
                    now = new Date(),
                    date = (ts ? new Date(ts) : now);
                date.setSeconds(0);
                date.setMilliseconds(0);
                $scope.activity.info.date = dateToDatetimePickerValue(date);
                datetimePicker.setAttribute('min', dateToDatetimePickerValue(now));

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
                console.log(config, $scope.activity);
                var params = {
                    uids:       $scope.invitedUsers || '',
                    title:      $scope.activity.info.title || '',
                    type:       $scope.activity.info.type|1,
                    desc:       $scope.activity.info.desc || '',
                    date:       datetimePickerValueToTs($scope.activity.info.date),
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
                    params['date']      = datetimePickerValueToTs($scope.activity.info.date);
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

            function dateToDatetimePickerValue(date, includeSeconds){
                //toISOString 會得出一個中央時區既時間（timezone=0）
                //我地呢度算上時區，最後得出一個本地的時間
                date.setMinutes(date.getMinutes() - timezoneOffset);
                if(!includeSeconds){
                    date.setSeconds(0);
                    date.setMilliseconds(0);
                }
                return date.toISOString().split('.')[0];
            }

            function datetimePickerValueToTs(value){
                if(value){
                    var tail = (value.split(':').length < 3) ? ':00.000Z' : '.000Z';
                    var result = Date.parse(value + tail) + timezoneOffset * 60 * 1000;
                    console.log(value, result, new Date(result));
                    return result;
                }
                return 0;
            }

            fetchActivityConfig();
        }
    ]);