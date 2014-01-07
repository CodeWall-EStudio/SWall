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
        }
    ]);