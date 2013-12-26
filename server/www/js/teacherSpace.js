(function(){

    //var BACKEND_SERVER = 'http://localhost:8080';
    var BACKEND_SERVER = '';


    angular.module('TeacherSpace', [])
        .controller('MainController', [
            '$rootScope', '$scope', '$http',
            function($rootScope, $scope, $http){
                $rootScope.uid = 'oscar'; //TODO 替换成当前用户uid

                $rootScope.activityID = '';
                $rootScope.activity = null;

                $rootScope.fetchActivities = function(params){
                    //TODO 显示一个modal菊花禁掉所有操作

                    //取消选择了的活动
                    $rootScope.activityID = '';
                    $rootScope.activity = null;

                    //构造搜索请求
                    params = params || {};
                    params['uid'] = $rootScope.uid;
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
                                        var date = new Date(activity.info.date),
                                            y = date.getFullYear(),
                                            m = date.getMonth()+1,
                                            d = date.getDate();
                                        return [y, m, d].join('_');
                                    });

                                //转换为数组 => [{date:date1, activities:[...]}, ...]
                                $rootScope.activityList = _.map(groupedActivities, function(arr, date){
                                    return {date:date, activities:arr};
                                });

                                //另外存一个id与活动的map => {id1:Activity, id2:..., ...}
                                $rootScope.activityMap = _.reduce(activities, function(result, activity){
                                    result[activity._id] = activity;
                                    return result;
                                }, {});
                            }
                            else {
                                //TODO 拉取失败
                            }
                        })
                        .error(function(data, status){
                            //TODO
                        });
                }

                $rootScope.fetchActivities();
            }
        ])
        .controller('NavigatorController', [
            '$rootScope', '$scope',
            function($rootScope, $scope){

            }
        ])
        .controller('ToolbarController', [
            '$rootScope', '$scope',
            function($rootScope, $scope){
                $scope.authorizationTypes = [
                    {label:'授权我参与的', value:'invited'},
                    {label:'公开的', value:'public'},
                    {label:'全部', value:null}
                ];
                $scope.statuses = [
                    {label:'开放的', value:'active'},
                    {label:'关闭的', value:'closed'},
                    {label:'全部', value:null}
                ];

                $scope.aTypeIndex = 2;
                $scope.statusIndex = 2;
                $scope.searchKeyword = '';

                $scope.updateQueryCondition = function(field, index){
                    if($scope[field] !== index){
                        $scope[field] = index;
                        var params = {},
                            selectedAType = $scope.authorizationTypes[$scope.aTypeIndex].value,
                            selectedStatus = $scope.statuses[$scope.statusIndex].value;

                        if(selectedAType)           params['authorize'] = selectedAType;
                        if(selectedStatus)          params['status'] = selectedStatus;
                        if($scope.searchKeyword)    params['kw'] = $scope.searchKeyword;

                        $rootScope.fetchActivities(params);
                    }
                };
            }
        ])
        .controller('ActivityListController', [
            '$rootScope', '$scope',
            function($rootScope, $scope){
                $scope.selectActivity = function(id){
                    $rootScope.activityID = id;
                    $rootScope.activity = $rootScope.activityMap[id];
                };
            }
        ])
        .controller('ActivityDetailController', [
            '$rootScope', '$scope',
            function($rootScope, $scope){
                $scope.userCount = function(){
                    if($rootScope.activity){
                        if($rootScope.activity.active){
                            return $rootScope.activity.users.participators.length;
                        }
                        else{
                            var users = _.countBy($rootScope.activity.resources, function(resource){
                                return resource.user;
                            });
                            return _.keys(users).length;
                        }
                    }
                    return 0;
                }
            }
        ])
        .controller('ActivityFieldsController', [
            '$rootScope', '$scope', '$http',
            function($rootScope, $scope, $http){
                $scope.type = 1;
                $scope.createActivity = function(){
                    var params = {
                        uid:        $rootScope.uid,
                        uids:       $scope.invitedUsers ? $scope.invitedUsers : '',
                        title:      $scope.title,
                        type:       $scope.type|0,
                        desc:       $scope.desc,
                        date:       $scope.date ? $scope.date.getTime() : 0,
                        teacher:    $scope.teacher,
                        grade:      $scope.grade,
                        'class':    $scope['class'],
                        subject:    $scope.subject,
                        domain:     $scope.domain
                    };
                    //TODO Angular將參數變成json放到body里了！要改成form-urlencoded
                    $http.post(BACKEND_SERVER + '/activities', params, {responseType:'json'})
                        .success(function(data, status){
                            console.log(data, status);
                        })
                        .error(function(data, status){

                        });
                }
            }
        ])
        .directive('activityDate', function(){
            function link(scope, element, attrs){
                var ts = scope.item.date.split('_');
                element.text(ts[0] + '年' + ts[1] + '月' + ts[2] + '日');
            }
            return {link:link};
        })
        .directive('activityDatePicker', function(){
            function link(scope, element, attrs){
                var now = new Date();
                var datePicker = $(element).datepicker({
                        onRender: function(date){
                            return date.getTime() < now.getTime() ? 'disabled' : '';
                        }
                    })
                    .on('changeDate', function(e){
                        datePicker.hide();

                        //update model value
                        var modelName = attrs['dateModel'];
                        if(modelName) scope[modelName] = e.date;
                    })
                    .data('datepicker');
            }
            return {link:link};
        });

})();