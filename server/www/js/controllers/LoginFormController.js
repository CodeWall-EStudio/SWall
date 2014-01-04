angular.module('ts.controllers.loginForm', [
        'ts.services.user'
    ])
    .controller('LoginFormController', [
        '$rootScope', '$scope', 'UserService',
        function($rootScope, $scope, UserService){
            $scope.uid = '';
            $scope.pwd = '';
            $scope.errMsg = '';

            $scope.login = function(){
                if($scope.uid){
                    if($scope.pwd){
                        UserService.login(
                            $scope.uid, $scope.pwd,
                            function(data, status){
                                if(status == 200){
                                    $('#loginModal').modal('hide');
                                }
                                else {
                                    //TODO 登录失败
                                }
                                //reset form
                                $scope.uid = $scope.pwd = $scope.errMsg = '';
                            }
                        );
                    }
                    else $scope.errMsg = '请输入密码'; }
                else $scope.errMsg = '请输入帐号名';
            };
        }
    ]);