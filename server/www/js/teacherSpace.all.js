angular.module('ts.utils.constants', [])
    //env
    .constant('BACKEND_SERVER', location.hostname == 'localhost' ? 'http://localhost:8090' : '')

    //event
    .constant('EVENT_LOGIN', 'event.login') //登录结果
    .constant('EVENT_MODE_CHANGE', 'event.mode.change') //导航栏上切换了模式（查看/管理）

    //cmd
    .constant('CMD_SHOW_LOGIN_PANEL', 'cmd.login.panel.show') //展示登錄界面
    .constant('CMD_SHOW_ACTIVITY_PANEL', 'cmd.activity.panel.show'); //展示活动创建/编辑面板
    angular.module('ts.services.utils', [])
    .service('UtilsService', [
        function(){
            var api = {
                object: {
                    toUrlencodedString: function(object){
                        return _.map(object, function(value, key){
                            return encodeURIComponent(key) + '=' + encodeURIComponent(value);
                        }).join('&');
                    }
                },
                cookie: {
                    /**
                     * get cookie
                     * @param {String} name
                     * @returns {String} cookie value
                     */
                    get: function(name){
                        var items = document.cookie.split('; '),
                            cookies = _.reduce(items, function(result, item){
                                var kv = item.split('=');
                                result[kv[0]] = kv[1];
                                return result;
                            }, {});
                        //console.log('[UtilsService] cookies=', cookies);
                        return cookies[name];
                    }
                },
                time: {
                    timezoneOffset: (new Date()).getTimezoneOffset(),

                    /**
                     * convert date into datetime-local input value
                     * @param {Date} [date]
                     * @param {Boolean} [includeSeconds]
                     * @returns {String} 2014-02-22T01:39:00
                     */
                    dateToDatetimePickerValue: function(date, includeSeconds){
                        if(!date) date = new Date();
                        //toISOString 會得出一個中央時區既時間（timezone=0），我地呢度算上時區，最後得出一個本地的時間
                        date.setMinutes(date.getMinutes() - api.time.timezoneOffset);
                        if(!includeSeconds){
                            date.setSeconds(0);
                            date.setMilliseconds(0);
                        }
                        return date.toISOString().split('.')[0];
                    },

                    /**
                     * convert datetime-local input value to timestamp
                     * @param {String} value
                     * @returns {number}
                     */
                    datetimePickerValueToTs: function (value){
                        if(value){
                            var tail = (value.split(':').length < 3) ? ':00.000Z' : '.000Z';
                            var result = Date.parse(value + tail) + api.time.timezoneOffset * 60 * 1000;
                            console.log(value, result, new Date(result));
                            return result;
                        }
                        return 0;
                    }
                }
            };

            return api;
        }
    ]);angular.module('ts.services.user', [
        'ts.utils.constants',
        'ts.services.utils'
    ])
    .service('UserService', [
        '$rootScope', '$http', 'UtilsService', 'BACKEND_SERVER', 'EVENT_LOGIN',
        function($rootScope, $http, UtilsService, BACKEND_SERVER, EVENT_LOGIN){
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
                var s = localStorage['login_result'];
                var r = s ? JSON.parse(s) : {};
                return r ? r.nick : '';
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
                document.cookie = 'uin=; path=/; expires=' + d;
                document.cookie = 'skey=; path=/; expires=' + d;
                localStorage.removeItem('login_result');
            }

            function fetchProfile(uid){

            }

            return {
                uid:            uid,
                hasLoggedIn:    hasLoggedIn,
                nick:           nick,
                login:          login,
                logout:         logout,
                fetchProfile:   fetchProfile,
                activity: {
                    isCreatorOfActivity: isCreatorOfActivity
                }
            };
        }
    ]);angular.module('ts.services.activity', [
        'ts.utils.constants',
        'ts.services.utils',
        'ts.services.user'
    ])
    .service('ActivityService', [
        '$rootScope', '$http', 'UtilsService', 'UserService', 'BACKEND_SERVER',
        function($rootScope, $http, UtilsService, UserService, BACKEND_SERVER)
        {
            var fieldset = document.getElementById('activityDetailButtons');

            $rootScope.profiles = {};
            $rootScope.activityList = [];
            $rootScope.activityMap = {};
            $rootScope.selectedActivity = null;
            $rootScope.selectedActivityID = '';
            $rootScope.hints = '';

            function selectActivity(activity){
                if(activity){
                    $rootScope.selectedActivity = activity;
                    $rootScope.selectedActivityID = activity._id;
                }
                else {
                    $rootScope.selectedActivity = null;
                    $rootScope.selectedActivityID = '';
                }
                console.log('[ActivityService] selected activity', $rootScope.selectedActivity);
            }
            function selectActivityByID(id){
                var activity = $rootScope.activityMap[id];
                selectActivity(activity);
            }

            function fetchActivityConfig(success, error){
                var ts = new Date().getTime();
                $http.get(BACKEND_SERVER + '/activities/config?_=' + ts, null, {responseType:'json'})
                    .success(function(data, status){
                        console.log('[ActivityService] activity config =', data);
                        if(success) success(data, status);
                    })
                    .error(function(data, status){
                        console.error('[ActivityService] FAIL TO FETCH ACTIVITY CONFIGURATIONS');
                        if(error) error(data, status);
                    });
            }

            function fetchActivities(params, success, error){
                //cleanup first
                $rootScope.activityList = [];
                $rootScope.activityMap = {};
                showWaitHints();

                params = params || {};
                if($rootScope.mode() == 'manager'){
                    params['creator'] = UserService.uid();
                }

                params['_'] = new Date().getTime();

                console.log('[ActivityService] fetching activities with', params);
                //构造搜索请求
                $http.get(BACKEND_SERVER + '/activities', {responseType:'json', params:params})
                    .success(function(data, status){
                        if(status === 200 && !data.c){
                            $rootScope.profiles = data.r.profiles;

                            //处理活动列表
                            //CGI返回的活动列表 => [Activity, ...]
                            //按日期分组活动 => {date1:[Activity, ...], date2:[...]}
                            var activities = data.r.activities,
                                groupedActivities = _.groupBy(activities, function(activity){
                                    //TODO 分组的时间可能有几个维度，比如距今一星期内的活动会以每天为一组，一星期至一个月内的以一周为一组
                                    //TODO 一个月到半年内的以每月为一组，半年到一年的为一组，更旧的则全归为同一组
                                    return getDateKeyFromActivity(activity);
                                });

                            //转换为数组 => [{date:date1, activities:[...]}, ...]
                            //TODO 按时间逆序排序？注意如果要排序，activityList要根据N.date排序、N.activities里既每个活动也要排
                            $rootScope.activityList = _.map(groupedActivities, function(arr, date){
                                return {date:date, activities:arr};
                            });

                            //另外存一个id与活动的map => {id1:Activity, id2:..., ...}
                            $rootScope.activityMap = _.reduce(activities, function(result, activity){
                                result[activity._id] = activity;
                                return result;
                            }, {});

                            if(!$rootScope.activityList.length) showEmptyActivitiesHints();
                            else clearHints();

                            console.log('[ActivityService] activities result', $rootScope.activityList, $rootScope.activityMap);
                        }
                        if(success) success(data, status);
                    })
                    .error(function(data, status){
                        showFetchActivitiesErrorHints();
                        if(error) error(data, status);
                    });
            }

            function getActivity(aid, success, error){
                var ts = new Date().getTime();
                $http.get(BACKEND_SERVER + '/activities/' + aid + '?_=' + ts, {responseType:'json'})
                    .success(success)
                    .error(error);
            }

            function createActivity(params, success, error){
                params = params || {};
                params['_'] = new Date().getTime();
                var body = UtilsService.object.toUrlencodedString(params);
                $http.post(
                        BACKEND_SERVER + '/activities',
                        body,
                        {
                            responseType: 'json',
                            headers: {'Content-Type': 'application/x-www-form-urlencoded'}
                        }
                    )
                    .success(function(data, status){
                        if(data && !data.c && data.r){
                            clearHints();
                            insertActivityToRootScope(data.r[0]);
                        }
                        clearHints();
                        if(success) success(data, status);
                    })
                    .error(error);
            }

            function updateActivity(activityID, params, success, error){
                disableButtons();
                params = params || {};
                params['_'] = new Date().getTime();
                var body = UtilsService.object.toUrlencodedString(params);
                $http.put(
                        BACKEND_SERVER + '/activities/' + activityID,
                        body,
                        {
                            responseType: 'json',
                            headers: {'Content-Type': 'application/x-www-form-urlencoded'}
                        }
                    )
                    .success(function(data, status){
                        if(data && !data.c && data.r){
                            updateActivityInRootScope(data.r);
                        }
                        enableButtons();
                        if(success) success(data, status);
                    })
                    .error(function(data, status){
                        enableButtons();
                        if(error) error(data, status);
                    });
            }

            //关闭活动
            function closeActivity(activityID, success, error){
                updateActivity(activityID, {status:'closed'}, success, error);
            }

            function deleteActivity(activityID, success, error){
                disableButtons();
                $http({method:'DELETE', url:BACKEND_SERVER+'/activities/'+activityID+'?_='+new Date().getTime()})
                    .success(function(data, status){
                        if(data && !data.c){
                            selectActivity();
                            removeActivityFromRootScope(activityID);
                        }
                        enableButtons();
                        if(!$rootScope.activityList.length) showEmptyActivitiesHints();
                        if(success) success(data, status);
                    })
                    .error(function(data, status){
                        enableButtons();
                        if(error) error(data, status);
                    });
            }

            ////////////////////////////////////////////////////////////////////////////////////////////////////////////
            // Statistics

            /**
             * 获取上传资源最多的用户排行
             * @param {Object} activity
             * @param {int} [count=10]
             * @return {Array}
             */
            function getUploadedResourceUsersRanking(activity, count){
                var counts  = _.countBy(_.pluck(activity.resources, 'user')),//{uid1:count1, uid2:count2}
                    arr     = _.map(counts, function(count, uid){ return {uid:uid, count:count}; }),//[{uid:uid1, count:count1}, {...}, ...]
                    result  = _.sortBy(arr, function(item){ return -item.count; });
                return result;
            }

            /**
             * 获取按时间分布的资源上传统计
             * @param {Object} activity
             * @param {int} [delta=300] 默认300秒内的上传资源会合并成一项
             * @return {Object}
             */
            function getResourcesGroupByTime(activity, delta){
                var firstResource = _.first(activity.resources);
                if(firstResource){
                    var beginning = firstResource.date,
                        results = {};
                    delta = (delta || 300) * 1000;
                    _.each(activity.resources, function(resource){
                        var index = Math.floor((resource.date - beginning)/delta),
                            key = index * delta;
                        if(!results[key]) results[key] = [];
                        results[key].push(resource);
                    });
                    return {
                        beginning: beginning,
                        items: results
                    };
                }
                return {};
            }

            ////////////////////////////////////////////////////////////////////////////////////////////////////////////
            // Helper methods

            function insertActivityToRootScope(activity){
                $rootScope.activityMap[activity._id] = activity;
                var dateKey = getDateKeyFromActivity(activity);
                for(var i=0; i<$rootScope.activityList.length; ++i){
                    var item = $rootScope.activityList[i];
                    if(item.date == dateKey){
                        insertActivityToExistedDateGroup(item, activity);
                        return;
                    }
                    else if(item.date > dateKey){
                        insertActivityToNewDateGroup(i, activity);
                        return;
                    }
                }
                insertActivityToNewDateGroup(i, activity);
            }
            function insertActivityToExistedDateGroup(group, activity){
                group.activities.unshift(activity);
            }
            function insertActivityToNewDateGroup(index, activity){
                $rootScope.activityList.splice(index, 0, {
                    date: getDateKeyFromActivity(activity),
                    activities: [activity]
                });
            }

            function updateActivityInRootScope(activity){
                $rootScope.activityMap[activity._id] = activity;
                for(var i=0; i<$rootScope.activityList.length; ++i){
                    var activities = $rootScope.activityList[i].activities;
                    for(var j=0; j<activities.length; ++j){
                        if(activities[j]._id == activity._id){
                            if($rootScope.selectedActivity == activities[j]){
                                $rootScope.selectedActivity = activity;
                                $rootScope.selectedActivityID = activity._id;
                            }
                            activities[j] = activity;
                            return;
                        }
                    }
                }
            }

            function removeActivityFromRootScope(activityID){
                //delete activity from activityMap
                delete $rootScope.activityMap[activityID];
                //delete activity from activityList
                for(var i=0; i<$rootScope.activityList.length; ++i){
                    for(var j=0; j<$rootScope.activityList[i].activities.length; ++j){
                        var activity = $rootScope.activityList[i].activities[j];
                        if(activity._id == activityID){
                            $rootScope.activityList[i].activities.splice(j, 1);
                            if(!$rootScope.activityList[i].activities.length){
                                $rootScope.activityList.splice(i, 1);
                            }
                            return;
                        }
                    }
                }
            }

            function getDateKeyFromActivity(activity){
                var date = new Date(activity.info.date);
                date.setHours(0);
                date.setMinutes(0);
                date.setSeconds(0);
                date.setMilliseconds(0);
                return date.getTime().toString();
            }

            function disableButtons(){
                fieldset.setAttribute('disabled', 'disabled');
            }
            function enableButtons(){
                fieldset.removeAttribute('disabled');
            }

            function clearHints(){
                $rootScope.hints = '';
            }
            function showWaitHints(){
                $rootScope.hints = '请稍候...';
            }
            function showEmptyActivitiesHints(){
                $rootScope.hints = '没找到任何活动';
            }
            function showFetchActivitiesErrorHints(){
                $rootScope.hints = '拉取活动失败';
            }

            ////////////////////////////////////////////////////////////////////////////////////////////////////////////

            return {
                selectActivity:         selectActivity,
                selectActivityByID:     selectActivityByID,
                fetchActivities:        fetchActivities,
                getActivity:            getActivity,
                createActivity:         createActivity,
                updateActivity:         updateActivity,
                closeActivity:          closeActivity,
                deleteActivity:         deleteActivity,
                fetchActivityConfig:    fetchActivityConfig,

                statistics: {
                    byUser: getUploadedResourceUsersRanking,
                    byTime: getResourcesGroupByTime
                }
            };
        }
    ]);angular.module('ts.controllers.activityDetail', [
        'ts.utils.constants',
        'ts.services.activity'
    ])
    .controller('ActivityDetailController', [
        '$rootScope', '$scope', 'ActivityService', 'EVENT_MODE_CHANGE', 'CMD_SHOW_ACTIVITY_PANEL',
        function($rootScope, $scope, ActivityService, EVENT_MODE_CHANGE, CMD_SHOW_ACTIVITY_PANEL){
            $scope.userCount = function(){
                if($rootScope.selectedActivity){
                    if($rootScope.selectedActivity.active){
                        //如果是开放的活动，则返回当前正在参与活动的用户数量
                        return $scope.participatorCount();
                    }
                    else{
                        //否则返回上传过资源的用户总数
                        return $scope.uploadedUserCount();
                    }
                }
                return 0;
            };

            $scope.participatorCount = function(){
                var a = $rootScope.selectedActivity;
                return (a && a.active) ? a.users.participators.length : 0;
            };

            $scope.uploadedUserCount = function(){
                if($rootScope.selectedActivity){
                    var users = _.countBy($rootScope.selectedActivity.resources, function(resource){
                        return resource.user;
                    });
                    return _.keys(users).length;
                }
                return 0;
            };

            $scope.resourceCount = function(type){
                if($rootScope.selectedActivity && $rootScope.selectedActivity.resources){
                    return _.reduce($rootScope.selectedActivity.resources, function(count, resource){
                        if(resource.type == type) count += 1;
                        return count;
                    }, 0);
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
                if(confirm('确定要删除活动？')){
                    ActivityService.deleteActivity($rootScope.selectedActivity._id, null, function(){
                        alert('删除活动失败，请注意：有资源或有用户加入的活动不能删除');
                    });
                }
            };

            $scope.handlePlayBtnClick = function(){
                var url = 'activity_play.html#?aid=' + $rootScope.selectedActivity._id;
                window.open(url, '_blank');
            };

            $rootScope.$on(EVENT_MODE_CHANGE, function(event, mode){
                ActivityService.selectActivity();
            });
        }
    ]);angular.module('ts.controllers.activityList', [
        'ts.services.activity'
    ])
    .controller('ActivityListController', [
        '$rootScope', '$scope', 'ActivityService',
        function($rootScope, $scope, ActivityService){
            $scope.selectActivity = function(id){
                ActivityService.selectActivityByID(id);
            };
        }
    ]);angular.module('ts.controllers.activityPanel', [
        'ts.utils.constants',
        'ts.services.activity',
        'ts.services.utils'
    ])
    .controller('ActivityPanelController', [
        '$rootScope', '$scope', '$http', 'ActivityService', 'UtilsService', 'CMD_SHOW_ACTIVITY_PANEL',
        function($rootScope, $scope, $http, ActivityService, UtilService, CMD_SHOW_ACTIVITY_PANEL){
            var modal                       = $('#createActivityModal'),
                usersInput                  = $('input[data-role="tagsinput"]'),
                datetimePicker              = document.getElementById('na_date'),
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

            if(datetimePicker.type != 'datetime-local'){
                $('#na_data').datetimePicker('setEndDate',null);
            }

            $rootScope.$on(CMD_SHOW_ACTIVITY_PANEL, function(event, data){
                //initialize $scope
                data = data || {};
                $scope.panelTitle       = data.panelTitle || defaults.panelTitle;
                $scope.confirmBtnTitle  = data.confirmBtnTitle || defaults.confirmBtnTitle;
                $scope.cancelBtnTitle   = data.cancelBtnTitle || defaults.cancelBtnTitle;
                $scope.activity         = data.activity || cloneDefaulActivityContent();
                $scope.invitedUsers     = $scope.activity.users.invitedUsers.join(',');

                //get config
                var config = $scope.config ? $scope.config.activityConfig : {},
                    info = $scope.activity.info;
                //find grade index
                for(var i=0; i<config.classes.length; ++i){
                    var grade = config.classes[i];
                    if(grade.grade == info['grade']){
                        $scope.grade = i;
                        $scope.cls = grade.cls.indexOf(info['class']);
                        console.log($scope.grade, $scope.cls);
                        break;
                    }
                }

                //初始化日期時間
                var ts = $scope.activity.info.date,
                    now = new Date(),
                    date = (ts ? new Date(ts) : now);
                date.setSeconds(0);
                date.setMilliseconds(0);
                $scope.activity.info.date = UtilService.time.dateToDatetimePickerValue(date);
                datetimePicker.setAttribute('min', UtilService.time.dateToDatetimePickerValue(now));

                //初始化授權用戶
                _.each($scope.activity.users.invitedUsers, function(item){
                    usersInput.tagsinput('add', item);
                });

                //show the panel's dom
                $scope.showActivityPanel();
            });

            function getNickForUid(uid){
                var profile = $rootScope.profiles[uid];
                return profile ? profile.nick + '(' + uid + ')' : uid;
            }

            modal.on('hidden.bs.modal', function () {
                //$scope.$apply(function(){
                resetFields();
                //});
            });

            $scope.$watch('grade', function(newValue){
                //$scope.cls = 0;
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

                var config = $scope.config ? $scope.config.activityConfig : {},
                    gradeConfig = config.classes[$scope.grade],
                    subjectConfig = config.subjects[$scope.subject],
                    params = {
                    uids:       $scope.invitedUsers || '',
                    title:      $scope.activity.info.title || '',
                    type:       $scope.activity.info.type|1,
                    desc:       $scope.activity.info.desc || '',
                    date:       UtilService.time.datetimePickerValueToTs($scope.activity.info.date),
                    teacher:    $scope.activity.info.teacher || '',
                    grade:      gradeConfig ? gradeConfig.grade : '',
                    'class':    gradeConfig ? gradeConfig.cls[$scope.cls] : '',
                    subject:    subjectConfig || '',
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
                    params['date']      = UtilService.time.datetimePickerValueToTs($scope.activity.info.date);
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
                        /*$scope.grade = 0;
                        $scope.cls = 0;
                        $scope.subject = 0;*/
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
                var message = '其他未知错误';
                switch(data.c){
                    case 1:     message = '后台其他错误'; break;
                    case 10:    message = '用户尚未登录'; break;
                    case 10000: message = '用户权限填写不正确'; break;
                    case 10010: message = '活动标题填写不正确'; break;
                    case 10020: message = '活动类型填写不正确'; break;
                    case 10040: message = '活动日期填写不正确，必须晚于当前时间！'; break;
                    case 10050: message = '出课教师填写不正确'; break;
                    case 10060: message = '年级填写不正确'; break;
                    case 10070: message = '班级填写不正确'; break;
                    case 10080: message = '学科填写不正确'; break;
                    case 10090: message = '课题填写不正确'; break;
                }
                //$scope.errMsg = status + ' - [' + data.c + '] ' + data.m;
                $scope.errMsg = '[' + status + '|' + data.c + ']' + message;
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

            fetchActivityConfig();
        }
    ]);angular.module('ts.controllers.loginForm', [
        'ts.utils.constants',
        'ts.services.user'
    ])
    .controller('LoginFormController', [
        '$rootScope', '$scope', 'UserService', 'CMD_SHOW_LOGIN_PANEL',
        function($rootScope, $scope, UserService, CMD_SHOW_LOGIN_PANEL){
            $scope.uid = '';
            $scope.pwd = '';
            $scope.errMsg = '';

            $scope.login = function(){
                function handleResult(data, status){
                    if(status == 200){
                        $('#loginModal').modal('hide');
                    }
                    else {
                        $scope.errMsg = '验证失败';
                        $scope.$digest();
                    }
                    //reset form
                    $scope.uid = $scope.pwd = $scope.errMsg = '';
                }

                if($scope.uid){
                    if($scope.pwd){
                        UserService.login(
                            $scope.uid, $scope.pwd,
                            handleResult, handleResult
                        );
                    }
                    else $scope.errMsg = '请输入密码'; }
                else $scope.errMsg = '请输入帐号名';
            };

            $rootScope.$on(CMD_SHOW_LOGIN_PANEL, function(){
                if(UserService.hasLoggedIn()){
                    if(confirm('确定要退出登录吗？')){
                        UserService.logout();
                    }
                    else return;
                }
                $('#loginModal').modal('show');
            });

            //NOTE 20140302 QQ登錄改造，改用QQ互聯OAUTH2登錄
            /*if(!UserService.hasLoggedIn()){
                $('#loginModal').modal('show');
            }*/
        }
    ]);angular.module('ts.controllers.main', [
        'ts.utils.constants',
        'ts.services.user',
        'ts.services.activity'
    ])
    .controller('MainController', [
        '$rootScope', '$scope', '$http', '$location', 'UserService', 'ActivityService',
        'EVENT_LOGIN', 'CMD_SHOW_LOGIN_PANEL', 'EVENT_MODE_CHANGE',
        function($rootScope, $scope, $http, $location, UserService, ActivityService,
            EVENT_LOGIN, CMD_SHOW_LOGIN_PANEL, EVENT_MODE_CHANGE){

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
                $rootScope.$emit(CMD_SHOW_LOGIN_PANEL);
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
                if(mode !== $rootScope.mode()){
                    $location.search('mode', mode);
                    $rootScope.$emit(EVENT_MODE_CHANGE, mode);
                }
            };

            //登錄成功後拉取活動列表
            $rootScope.$on(EVENT_LOGIN, function(event, status){
                if(status == 200){
                    $rootScope.fetchActivities();
                }
            });

            function main(){
                if(UserService.hasLoggedIn()){
                    $rootScope.fetchActivities();
                }
            }
            main();
        }
    ]);angular.module('ts.controllers.navigator', [
        'ts.services.user'
    ])
    .controller('NavigatorController', [
        '$rootScope', '$scope', 'UserService', 'EVENT_LOGIN', 'CMD_SHOW_LOGIN_PANEL',
        function($rootScope, $scope, UserService, EVENT_LOGIN, CMD_SHOW_LOGIN_PANEL){
            function getNickname(){
                $scope.nickname = UserService.nick();
            }

            $rootScope.$on(EVENT_LOGIN, function(event, status){
                if(status == 200){
                    getNickname();
                }
            });

            getNickname();
        }
    ]);angular.module('ts.controllers.toolbar', [
        'ts.utils.constants'
    ])
    .controller('ToolbarController', [
        '$rootScope', '$scope', 'EVENT_MODE_CHANGE', 'CMD_SHOW_ACTIVITY_PANEL',
        function($rootScope, $scope, EVENT_MODE_CHANGE, CMD_SHOW_ACTIVITY_PANEL){
            $scope.authorizationTypes = [
                {label:'授权我参与的', value:'invited'},
                {label:'公开的', value:'public'},
                {label:'全部权限', value:null}
            ];
            $scope.statuses = [
                {label:'开放的', value:'active'},
                {label:'关闭的', value:'closed'},
                {label:'全部状态', value:null}
            ];

            $scope.aTypeIndex = 2;
            $scope.statusIndex = 2;
            $scope.searchKeyword = '';

            $scope.showCreateActivityModal = function(event){
                if(!event.currentTarget.classList.contains('disabled')){
                    $rootScope.$emit(CMD_SHOW_ACTIVITY_PANEL);
                }
            };

            $scope.updateQueryAndSearch = function(field, value){
                if(field){
                    $scope[field] = value;
                }

                var query = {},
                    selectedAType = $scope.authorizationTypes[$scope.aTypeIndex].value,
                    selectedStatus = $scope.statuses[$scope.statusIndex].value;

                if(selectedAType)                   query['authorize'] = selectedAType;
                if(selectedStatus)                  query['status'] = selectedStatus;
                if($scope.searchKeyword)            query['kw'] = $scope.searchKeyword;

                //TODO 改成抛事件
                $rootScope.fetchActivities(query);
            };

            $rootScope.$on(EVENT_MODE_CHANGE, function(event, mode){
                $scope.updateQueryAndSearch();
            });
        }
    ]);angular.module('ts.directives.ngEnter', [])
    .directive('ngEnter', function(){
        function link(scope, element, attrs){
            element.on('keydown', function(event){
                if(event.keyCode == 13){
                    var callback = attrs['ngEnter'];
                    if(callback){
                        _.bind(function(){
                            eval('this.' + callback);
                        }, scope)();
                    }
                }
            });
        }
        return {link:link};
    });(function(){

    angular.module('teacherSpace', [
        'ts.controllers.main',
        'ts.controllers.navigator',
        'ts.controllers.toolbar',
        'ts.controllers.activityList',
        'ts.controllers.activityDetail',
        'ts.controllers.activityPanel',
        'ts.controllers.loginForm',
        'ts.directives.ngEnter'
    ]);
})();