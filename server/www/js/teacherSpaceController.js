var app = angular.module('TeacherSpace', []);

app.controller('MainController', [
    '$rootScope', '$scope', '$http',
    function($rootScope, $scope, $http){
        function fetchActivities(){
            $http.get(
                    '/activities',
                    {
                        responseType: 'json',
                        params: {
                            uid: 'oscar',
                            after: new Date().getTime()
                        }
                    }
                )
                .success(function(data, status){
                    if(status === 200 && !data.c){
                        var activities = data.r.activities;
                        $rootScope.activities = _.groupBy(activities, function(activity){
                            var date = new Date(activity.info.date);
                            return [date.getFullYear(), date.getMonth(), date.getDate()].join('/');
                        });
                        console.log($rootScope.activities);
                    }
                });
            console.log('fetching activities ...');
        }

        fetchActivities();
    }
]);