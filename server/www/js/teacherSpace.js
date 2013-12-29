(function(){

    //var BACKEND_SERVER = 'http://localhost:8080';
    var BACKEND_SERVER = '';


    angular.module('TeacherSpace', [])
        /**
         * Main Controller
         * @rootScope {String} uid
         * @rootScope {String} activityID selected activity id
         * @rootScope {Object} activity selected activity
         * @rootScope {Array} activityList activities grouped by date
         * @rootScope {Object} activityMap {id:activity}
         * @rootScope {Function} fetchActivities(params:Object)
         */
        .controller('MainController', [
            '$rootScope', '$scope', '$http', '$location',
            function($rootScope, $scope, $http, $location){
                $rootScope.uid = 'oscar'; //TODO 替换成当前用户uid

                $rootScope.activityID = '';
                $rootScope.activity = null;

                $rootScope.fetchActivities = function(params){
                    //TODO 显示一个modal菊花禁掉所有操作

                    //取消选择了的活动
                    $rootScope.activityID = '';
                    $rootScope.activity = null;

                    //构造搜索请求
                    $http.get(BACKEND_SERVER + '/activities?uid=' + $rootScope.uid, {responseType:'json', params:params})
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
                };

                $rootScope.mode = function(){
                    return $location.search()['mode'];
                };

                $rootScope.gotoMode = function(mode){
                    $location.search('mode', mode);
                    $rootScope.$emit('event.mode.change', mode);
                };

                $rootScope.fetchActivities();
            }
        ])
        /**
         * Navigator Controller
         */
        .controller('NavigatorController', [
            '$rootScope', '$scope', '$location',
            function($rootScope, $scope, $location){

            }
        ])
        /**
         * Toolbar Controller
         * @scope {Array} authorizationTypes
         * @scope {Array} statuses
         * @scope {int} aTypeIndex index of the selected authorization type
         * @scope {int} statusIndex index of the selected status
         * @scope {String} searchKeyword
         * @scope {Function} updateQueryAndSearch(field:String, value:*)
         */
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
                $scope.searchKeyword = ''; //TODO press enter on search field to search

                $scope.showCreateActivityModal = function(event){
                    if(!event.currentTarget.classList.contains('disabled')){
                        $('#createActivityModal').modal('show');
                    }
                };

                $scope.updateQueryAndSearch = function(field, value){
                    if(field){
                        $scope[field] = value;
                    }

                    var query = {},
                        selectedAType = $scope.authorizationTypes[$scope.aTypeIndex].value,
                        selectedStatus = $scope.statuses[$scope.statusIndex].value;

                    if(selectedAType)           query['authorize'] = selectedAType;
                    if(selectedStatus)          query['status'] = selectedStatus;
                    if($scope.searchKeyword)    query['kw'] = $scope.searchKeyword;

                    $rootScope.fetchActivities(query);
                };
            }
        ])
        /**
         * Activity List Controller
         * @scope {Function} selectActivity(id:String)
         */
        .controller('ActivityListController', [
            '$rootScope', '$scope',
            function($rootScope, $scope){
                $scope.selectActivity = function(id){
                    $rootScope.activityID = id;
                    $rootScope.activity = $rootScope.activityMap[id];
                };
            }
        ])
        /**
         * Activity Detail Controller
         * @scope {Function} userCount()
         */
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
        /**
         * Activity Form Controller
         * @scope {String} title
         * @scope {String} desc
         * @scope {Date} date
         * @scope {int} type
         * @scope {String} teacher
         * @scope {int} grade
         * @scope {int} cls
         * @scope {int} subject
         * @scope {String} domain
         * @scope {String} invitedUsers "uid1,uid2,uid3,..."
         * @scope {String} errMsg error message responsed from backend server
         * @scope {Object} config activity configuration responsed from backend server
         * @scope {Function} createActivity()
         */
        .controller('ActivityFormController', [
            '$rootScope', '$scope', '$http',
            function($rootScope, $scope, $http){
                var modal = $('#createActivityModal'),
                    fieldset = document.querySelector('#createActivityForm > fieldset');

                modal.on('hidden.bs.modal', function () {
                    $scope.$apply(function(){
                        resetFields();
                    });
                });

                $scope.type = 1;

                $scope.$watch('grade', function(newValue){
                    $scope.cls = 0;
                });

                $scope.createActivity = function(){
                    $scope.errMsg = '';
                    disableFields();

                    var config = $scope.config ? $scope.config.activityConfig : {};
                    var params = {
                        uids:       $scope.invitedUsers ? $scope.invitedUsers : '',
                        title:      $scope.title || '',
                        type:       $scope.type|1,
                        desc:       $scope.desc || '',
                        date:       $scope.date ? $scope.date.getTime() : 0,
                        teacher:    $scope.teacher || '',
                        grade:      config.classes[$scope.grade].grade,
                        'class':    config.classes[$scope.grade].cls[$scope.cls],
                        subject:    config.subjects[$scope.subject],
                        domain:     $scope.domain || ''
                    };
                    var body = _.map(params, function(value, key){
                        return encodeURIComponent(key) + '=' + encodeURIComponent(value);
                    }).join('&');
                    $http.post(
                            BACKEND_SERVER + '/activities?uid=' + $rootScope.uid,
                            body,
                            {
                                responseType: 'json',
                                headers: {
                                    'Content-Type': 'application/x-www-form-urlencoded'
                                }
                            }
                        )
                        .success(function(data, status){
                            console.log(data, status);
                            enableFields();
                            hideModal();
                        })
                        .error(function(data, status){
                            data = data || {c:-1, msg:'UNKNOWN'};
                            $scope.errMsg = status + ' - [' + data.c + '] ' + data.m;
                            enableFields();
                        });
                }

                function fetchActivityConfig(){
                    $http.get(BACKEND_SERVER + '/activities/config', null, {responseType:'json'})
                        .success(function(data, status){
                            console.log('[ActivityFormController] activity config =', data);
                            $scope.config = data;
                            $scope.grade = 0;
                            $scope.cls = 0;
                            $scope.subject = 0;
                        })
                        .error(function(data, status){
                            console.error('[ActivityFormController] FAIL TO FETCH ACTIVITY CONFIGURATIONS');
                        });
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
                    $scope.invitedUsers = '';
                    $scope.title        = '';
                    $scope.type         = 1;
                    $scope.desc         = '';
                    $scope.teacher      = '';
                    $scope.grade        = '';
                    $scope['class']     = '';
                    $scope.subject      = '';
                    $scope.domain       = '';
                    $scope.errMsg       = '';
                    console.log('reset');
                }

                fetchActivityConfig();
            }
        ])
        .directive('ngEnter', function(){
            function link(scope, element, attrs){
                element.on('keydown', function(event){
                    if(event.keyCode == 13){
                        var callback = attrs['ngEnter'];
                        if(callback){
                            _.bind(function(scope){
                                eval('this.' + callback);
                            }, scope)();
                        }
                    }
                });
            }
            return {link:link};
        })
        /**
         * Activity Date Directive
         * scope.item.date:String => "x年x月x日"
         */
        .directive('activityDate', function(){
            function link(scope, element, attrs){
                var ts = scope.item.date.split('_');
                element.text(ts[0] + '年' + ts[1] + '月' + ts[2] + '日');
            }
            return {link:link};
        })
        /**
         * Activity Date Picker Directive
         * 1.禁止选择今天以前的日期
         * 2.选择日期后更新scope[ngModel]=date
         */
        .directive('activityDatePicker', function(){
            function link(scope, element, attrs){
                var now = new Date();
                now.setHours(0);
                now.setMinutes(0);
                now.setSeconds(0);
                now.setMilliseconds(0);
                var datePicker = $(element).datepicker({
                        onRender: function(date){
                            return date.getTime() < now.getTime() ? 'disabled' : '';
                        }
                    })
                    .on('changeDate', function(e){
                        datePicker.hide();

                        //update model value
                        var modelName = attrs['ngDateModel'];
                        if(modelName) scope[modelName] = e.date;
                    })
                    .data('datepicker');
            }
            return {link:link};
        });

})();