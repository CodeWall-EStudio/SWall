angular.module('ts.services.activity', [
        'ts.utils.constants',
        'ts.services.utils',
        'ts.services.user'
    ])
    .service('ActivityService', [
        '$rootScope', '$http', 'UtilsService', 'UserService', 'BACKEND_SERVER',
        function($rootScope, $http, UtilsService, UserService, BACKEND_SERVER)
        {
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
                $rootScope.hints = '请稍候...';

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

                            if(!$rootScope.activityList.length) $rootScope.hints = '没找到任何活动';
                            else $rootScope.hints = '';

                            console.log('[ActivityService] activities result', $rootScope.activityList, $rootScope.activityMap);
                        }
                        if(success) success(data, status);
                    })
                    .error(function(data, status){
                        $rootScope.hints = '拉取活动失败';
                        if(error) error(data, status);
                    });
            }

            function getActivity(aid, success, error){
                $http.get(BACKEND_SERVER + '/activities/' + aid, {responseType:'json'})
                    .success(success)
                    .error(error);
            }

            function createActivity(params, success, error){
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
                            insertActivityToRootScope(data.r[0]);
                        }
                        if(success) success(data, status);
                    })
                    .error(error);
            }

            function updateActivity(activityID, params, success, error){
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
                        if(success) success(data, status);
                    })
                    .error(error);
            }

            function closeActivity(activityID, success, error){
                updateActivity(activityID, {status:'closed'}, success, error);
            }

            function deleteActivity(activityID, success, error){
                /*$http.delete(
                        BACKEND_SERVER + '/activities/' + activityID,
                        null,
                        {
                            responseType: 'json'
                        }
                    )
                    .success(function(data, status){
                        if(data && !data.c){
                            selectActivity();
                            //delete $rootScope.activityMap[activityID];
                            //TODO 從activityList里找出對應的活動並刪除之
                        }
                    })
                    .error(error);*/
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

            function getDateKeyFromActivity(activity){
                var date = new Date(activity.info.date);
                date.setHours(0);
                date.setMinutes(0);
                date.setSeconds(0);
                date.setMilliseconds(0);
                return date.getTime().toString();
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
                fetchActivityConfig:    fetchActivityConfig
            };
        }
    ]);