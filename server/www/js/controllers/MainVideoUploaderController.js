angular.module('ap.controllers.videoUploader', [
        'ts.services.activity',
        'ts.services.user'
    ])
    .controller('MainVideoUploaderController', [
        '$rootScope', '$scope', '$location', 'ActivityService', 'UserService',
        function($rootScope, $scope, $location, ActivityService, UserService)
        {
            var mainVideoInput = document.getElementById('mainVideoFile');

            reset();

            $scope.onSelectedFile = function(){
                var files = mainVideoInput.files,
                    file = files ? files[0] : null;
                console.log('[uploader] selected file', file);

                if(file/* && TODO 判斷文件類型？*/){
                    //load the file and get duration info
                    $scope.videoFile = file;
                    var video = document.createElement('video'),
                        url = window.URL.createObjectURL(file);

                    video.addEventListener('loadedmetadata', function(){
                        $scope.videoDuration = video.duration;
                        console.log('[uploader] video duration = ' + video.duration);
                    });
                    video.src = url;
                    console.log('[uploader] reading video file ...');
                }
            };

            $scope.startUpload = function(){
                uploadVideoFile();
            };

            function uploadVideoFile(){
                if($scope.videoFile && $scope.videoDuration && $scope.videoName){
                    //create form and xml http request to post the file
                    var form = new FormData(),
                        xhr = new XMLHttpRequest();
                    form.append('file', $scope.videoFile);

                    xhr.upload.addEventListener('progress', function(e){
                        $scope.$apply(function(){
                            $scope.videoUploadProgress = Math.floor(e.loaded / e.total * 100);
                        });
                    }, false);

                    xhr.addEventListener('load', function(e){
                        $scope.videoIsUploading = false;
                        if(xhr.status == 200){
                            var json = JSON.parse(xhr.responseText),
                                url = (json && json[0]) ? '/resources/' + json[0].filename : null;
                            console.log('[uploader] upload video success', json, url);
                            $scope.videoURL = url;
                            addVideoToActivity();
                        }
                        else {
                            //TODO handle other status code
                        }
                        $scope.$digest();
                    }, false);

                    xhr.addEventListener('error', function(e){
                        console.log('[uploader] upload video error', e);
                        //TODO alert
                        $scope.$apply(function(){
                            reset();
                        });
                    }, false);

                    //start uploading the file
                    $scope.videoIsUploading = true;
                    $scope.videoUploadProgress = 0;
                    xhr.open('POST', '/resources');
                    xhr.send(form);
                    console.log('[uploader] uploading video ...');
                }
            }

            function addVideoToActivity(){
                var aid = $location.search()['aid'],
                    form = new FormData(),
                    xhr = new XMLHttpRequest();

                form.append('src', $scope.videoURL);
                form.append('name', $scope.videoName);
                form.append('duration', $scope.videoDuration);

                xhr.addEventListener('load', function(e){
                    $scope.videoIsAdding = false;
                    if(xhr.status == 201){
                        var json = JSON.parse(xhr.responseText),
                            retCode = json ? json['c'] : -1,
                            activity = json ? json['r'] : null;
                        console.log('[uploader] add success', json);
                        if(!retCode && activity){
                            $('#uploadMainVideoModal').modal('hide')
                            $rootScope.mainVideos = activity.videos;
                        }
                    }
                    else {
                        //TODO handle other status code
                    }
                    $rootScope.$digest();
                });
                xhr.addEventListener('error', function(e){
                    console.log('[uploader] add video error', e);
                    //TODO alert
                    $scope.$apply(function(){
                        reset();
                    });
                });
                $scope.videoIsAdding = true;
                xhr.open('POST', '/activities/' + aid + '/videos');
                xhr.send(form);
                console.log('[uploader] adding video to activity', aid);
            }

            function reset(){
                //local file info
                $scope.videoFile = null;
                $scope.videoName = null;
                $scope.videoDuration = 0;

                //upload status & result
                $scope.videoIsUploading = false;
                $scope.videoIsAdding = false;
                $scope.videoUploadProgress = 0;
                $scope.videoURL = null;
            }
        }
    ]);