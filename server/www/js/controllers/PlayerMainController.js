angular.module('ap.controllers.main', [
        'ts.services.activity',
        'ts.services.user'
    ])
    .controller('PlayerMainController', [
        '$rootScope', '$scope', '$location', 'ActivityService', 'UserService',
        function($rootScope, $scope, $location, ActivityService, UserService){
            var autoRefreshTimeout = 0;

            $rootScope.username = UserService.nick();
            $rootScope.userCount = 0;
            $rootScope.uploadedUsers = []; //上傳過資源的用戶
            $rootScope.rawResources = [];
            $rootScope.resources = [];
            $rootScope.presources = []; //previewable resources
            $rootScope.profiles = {};

            $rootScope.selectedMainVideo = null; //已选择的主视频
            $rootScope.editingOrder = false; //编辑顺序模式
            $rootScope.editingTime = false; //编辑时间模式
            $rootScope.mainVideos = [ //主视频 TODO：按order排序
                {name:'主视频1', src:'...', duration:100, order:1, startTime:100},
                {name:'主视频2', src:'...', duration:100, order:2, startTime:100},
                {name:'主视频3', src:'...', duration:100, order:3, startTime:100}
            ];

            $scope.selectedUser = null;
            $scope.selectedResource = null;
            $scope.selectedRIndex = -1;
            $scope.autoRefresh = true;

            $scope.$watch('selectedResource', function(newValue){
                if(newValue && newValue.type == 2){
                    setTimeout(function(){
                        var el = document.getElementById('videoPlayer');
                        el.src = newValue.content;
                    }, 100);
                }
            });

            $rootScope.uid2nick = function(uid){
                if($rootScope.profiles[uid]){
                    return $rootScope.profiles[uid].nick;
                }
                return uid;
            };

            $scope.uploadMainVideo = function(){
                //TODO show modal to upload main video
            };

            $scope.selectMainVideo = function(video){
                $scope.selectedMainVideo = video;
            };

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
            };

            $scope.filterByUser = function(uid){
                $scope.selectedUser = uid;

                $('.resourceGroup').css('display', 'none');
                setTimeout(function(){
                    //刷新webkit box的高度
                    $('.resourceGroup').css('display', '-webkit-box');
                    $scope.$digest();
                    refreshAllNextItemLineHeight();
                }, 0);
            };

            $scope.nextLineHeight = function(gindex, rindex){
                if(rindex){
                    var el = $('.resourceItem[data-gindex="' + gindex + '"][data-rindex="' + rindex + '"]'),
                        height = el.outerHeight();
                    return height ? height - 3 : 0;
                }
                return 0;
            };

            $scope.isFirstResourceInGroup = function(gindex, rindex){
                if(rindex == 0) return true;
                else {
                    //找出当前的资源节点
                    var selector = '.resourceItem[data-gindex="' + gindex + '"][data-rindex="' + rindex + '"]',
                        resourceEl = document.querySelector(selector);
                    if(resourceEl){
                        //找出其父节点和第一个可见的子节点
                        var groupEl = resourceEl.parentElement,
                            firstResourceEl = groupEl.querySelectorAll('.resourceItem:not(.ng-hide)')[0];
                        return resourceEl == firstResourceEl;
                    }
                    return false;
                }
            };

            $scope.resourceShouldBeVisible = function(resource){
                return (!$scope.selectedUser || $scope.selectedUser == resource.user);
            };

            $scope.groupContainsResourceOfSelectedUser = function(group){
                if(!$scope.selectedUser) return true;
                return _.some(group.resources, function(resource){
                    return resource.user == $scope.selectedUser;
                });
            };

            $scope.hideResourceDetail = function(){
                clearSelection();
            };

            $scope.toggleAuthRefresh = function(){
                $scope.autoRefresh = !$scope.autoRefresh;
                if($scope.autoRefresh){
                    getActivity();
                }
                else{
                    if(autoRefreshTimeout) {
                        clearTimeout(autoRefreshTimeout);
                        autoRefreshTimeout = 0;
                    }
                }
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
                            $rootScope.profiles = data.profiles;
                            $rootScope.activity = data.r;
                            //processFetchedResources(data.r);
                            addFetchedResourcesToRootScope($rootScope.activity.resources);

                            if($rootScope.activity.active){
                                //计算用户数
                                $rootScope.userCount = $rootScope.activity.users.participators.length;

                                //如果是正在展示的活動，則固定每5s刷一次資源列表
                                if($scope.autoRefresh){
                                    autoRefreshTimeout = setTimeout(function(){
                                        getActivity();
                                    }, 5000);
                                }
                            }
                            else{
                                //计算用户数
                                var users = _.countBy($rootScope.activity.resources, function(resource){
                                    return resource.user;
                                });
                                $rootScope.userCount = _.keys(users).length;
                            }
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

            function addFetchedResourcesToRootScope(resources){
                //过滤出新的资源
                var localLatestGroup = $rootScope.resources[0],
                    localLatestTs = localLatestGroup ? localLatestGroup.resources[0].date : 0,
                    newResources = _.filter(resources, function(resource){ return resource.date > localLatestTs; });
                console.log('new resources', newResources);
                for(var i=0; i<newResources.length; ++i)
                {
                    //计算新资源的时间戳（以每分钟为单位），把新资源插入到按分钟分组的资源分组里
                    var r = newResources[i],
                        d = new Date(r.date);
                    d.setSeconds(0);
                    d.setMilliseconds(0);
                    var ts = d.getTime();

                    if($rootScope.resources[0] && $rootScope.resources[0].ts == ts){
                        //如果新资源的时间戳和本地最新的资源分钟时间戳一样，则插入到该分组中
                        $rootScope.resources[0].resources.splice(0, 0, r);
                    }
                    else{
                        //否则插入一个新的分组
                        $rootScope.resources.splice(0, 0, {ts:ts, resources:[r]});
                    }

                    //单独过滤出图片和视频这些可以预览大图的资源，用来做上下翻页
                    if(r.type == 1 || r.type == 2){
                        $rootScope.presources.splice(0, 0, r);
                    }

                    //取出上傳資源的用戶
                    if($rootScope.uploadedUsers.indexOf(r.user) == -1){
                        $rootScope.uploadedUsers.push(r.user);
                        //TODO 排序？
                    }
                }


                console.log('resources', $rootScope.resources);
                console.log('presources', $rootScope.presources);
            }

            window.rs = $rootScope;

            getActivity();
        }
    ]);


//圖片加載完成後，更新折線高度用的 //////////////////////////////////////////////////////////////////////////////////////////

function onImgLoad(e){
    var img = e.target,
        item = $(img).parents('.resourceItem')[0],
        gindex = parseInt(item.getAttribute('data-gindex')),
        rindex = parseInt(item.getAttribute('data-rindex'));
    updateNextItemLineHeight(gindex, rindex);
}

function updateNextItemLineHeight(gindex, rindex){
    var thisResourceItem = $('.resourceItem[data-gindex="' + gindex + '"][data-rindex="' + rindex + '"]'),
        nextLine = $('.resourceItem[data-gindex="' + gindex + '"][data-rindex="' + (rindex+1) + '"] .nextLine'),
        thisHeight = thisResourceItem.outerHeight();
    if(nextLine.length){
        nextLine.height(thisHeight - 3);
    }
}

function refreshAllNextItemLineHeight(){
    _.each(rs.resources, function(group, g){
        _.each(group.resources, function(item, r){
            if(item.type !== 0) updateNextItemLineHeight(g, r);
        });
    })
}