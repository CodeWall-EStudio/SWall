(function(){

    angular.module('TeacherSpace', [])
        .controller('MainController', [
            '$rootScope', '$scope', '$http',
            function($rootScope, $scope, $http){
                $rootScope.uid = 'oscar'; //TODO 替换成当前用户uid

                $rootScope.activityID = '';
                $rootScope.activity = null;

                $rootScope.fetchActivities = function(params){
                    //TODO 显示一个modal菊花禁掉所有操作
                    params = params || {};
                    params['uid'] = $rootScope.uid;
                    $http.get('/activities', {responseType:'json', params:params})
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

                $scope.createActivity = function(){

                };

                $scope.updateQueryCondition = function(field, index){
                    if($scope[field] !== index){
                        $scope[field] = index;
                        var params = {},
                            selectedAType = $scope.authorizationTypes[$scope.aTypeIndex].value,
                            selectedStatus = $scope.statuses[$scope.statusIndex].value;
                        if(selectedAType) params['authorize'] = selectedAType;
                        if(selectedStatus) params['status'] = selectedStatus;
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
                    .on('changeDate', function(){
                        datePicker.hide();
                    })
                    .data('datepicker');
            }
            return {link:link};
        });

})();