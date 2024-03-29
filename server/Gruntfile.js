module.exports = function(grunt) {

    // Project configuration.
    grunt.initConfig({
        pkg: grunt.file.readJSON('package.json'),


        //本地常量
        CONSTS: {
            WWW_SOURCE: 'www/',
            WWW_DIST: 'www-dist/',
            SERVER: 'app/',
            DEPLOY: 'deploy'
        },


        //环境变量
        env: {
            //localhost
            localhost: {
                www: {
                    img: {
                        logo: '<img src="img/logo2.png">'
                    },
                    theme: {
                        nav: {
                            bg: ['rgb(36,74,134)', 'rgb(32,66,121)'],
                            text: ['rgb(180,201,221)', '#fff']
                        }
                    }
                },
                server: {
                    host: '"hylc-edu.cn"',
                    express: {
                        port: 80
                    },
                    mongodb: {
                        host: '"localhost"',
                        port: 27017,
                        username: '""',
                        password: '""'
                    },
                    api: {
                        host: '"szone.hylc-edu.cn"',
                        login: {
                            host: '"szone.hylc-edu.cn"'
                        }
                    }
                }
            },

            //swall.codewalle.com 測試環境
            'codewalle': {
                www: {
                    img: {
                        logo: '<img src="img/logo2.png">'
                    },
                    theme: {
                        nav: {
                            bg: ['rgb(36,74,134)', 'rgb(32,66,121)'],
                            text: ['rgb(180,201,221)', '#fff']
                        }
                    }
                },
                server: {
                    host: '"codewalle.com"',
                    express: {
                        port: 8090
                    },
                    mongodb: {
                        host: '"localhost"',
                        port: 27017,
                        username: '"swall"',
                        password: '"DfvszXKePFtfB9KM"'
                    },
                    api: {
                        host: '"qzone.codewalle.com"',
                        login: {
                            host: '"qzone.codewalle.com"'
                        }
                    },
                    //部署路径和服务器
                    home: '/home/swall/SWall',
                    ssh: {
                        host: 'codewalle.com',
                        username: 'root',
                        password: 'uswveYrvGr93wjvA'
                    }
                }
            },
            //阿里云 体验环境
            'aliyun': {
                www: {
                    img: {
                        logo: '<img src="img/logo3.png">'
                    },
                    theme: {
                        nav: {
                            bg: ['rgb(36,74,134)', 'rgb(32,66,121)'],
                            text: ['rgb(180,201,221)', '#fff']
                        }
                    }
                },
                server: {
                    host: '"hylc-edu.cn"',
                    express: {
                        port: 8090
                    },
                    mongodb: {
                        host: '"localhost"',
                        port: 27017,
                        username: '""',
                        password: '""'
                    },
                    api: {
                        host: '"szone.hylc-edu.cn"',
                        login: {
                            host: '"szone.hylc-edu.cn"'
                        }
                    },
                    //部署路径和服务器
                    home: '/data/project/Media',
                    ssh: {
                        host: '112.126.66.147',
                        username: 'root',
                        password: '2eefc9e3'
                    }
                }
            },

            //延慶二小
            'yqex47': {
                www: {
                    img: {
                        logo: '<img src="img/logo_yqex.png">'
                    },
                    theme: {
                        nav: {
                            bg: ['rgb(36,74,134)', 'rgb(32,66,121)'],
                            text: ['rgb(180,201,221)', '#fff']
                        }
                    }
                },
                server: {
                    host: '"71xiaoxue.com"',
                    express: {
                        port: 8090
                    },
                    mongodb: {
                        host: '"58.118.161.49"',
                        port: 27017,
                        username: '""',
                        password: '""'
                    },
                    api: {
                        host: '"szone.71xiaoxue.com"',
                        login: {
                            host: '"localhost:8091"'
                        }
                    },
                    //部署路径和服务器
                    home: '/data/project/Media',
                    ssh: {
                        host: '58.118.161.47',
                        username: 'root',
                        password: 'yqex123'
                    }
                }
            },
            'yqex49': {
                www: {
                    img: {
                        logo: '<img src="img/logo_yqex.png">'
                    },
                    theme: {
                        nav: {
                            bg: ['rgb(36,74,134)', 'rgb(32,66,121)'],
                            text: ['rgb(180,201,221)', '#fff']
                        }
                    }
                },
                server: {
                    host: '"71xiaoxue.com"',
                    express: {
                        port: 8090
                    },
                    mongodb: {
                        host: '"localhost"',
                        port: 27017,
                        username: '""',
                        password: '""'
                    },
                    api: {
                        host: '"szone.71xiaoxue.com"',
                        login: {
                            host: '"localhost:8091"'
                        }
                    },
                    //部署路径和服务器
                    home: '/data/project/Media',
                    ssh: {
                        host: '58.118.161.49',
                        username: 'root',
                        password: 'yqex123'
                    }
                }
            },

            //永定二小
            'ydex': {
                www: {
                    title: '北京第二实验小学永定分校',
                    img: {
                        logo: '<img src="img/logo_ydex.png">'
                    },
                    theme: {
                        nav: {
                            bg: ['#fff', 'rgb(32,66,121)'],
                            text: ['rgb(32,66,121)', '#fff']
                        }
                    }
                },
                server: {
                    host: '"hylc-edu.cn"',
                    express: {
                        port: 8090
                    },
                    mongodb: {
                        host: '"localhost"',
                        port: 27017,
                        username: '""',
                        password: '""'
                    },
                    api: {
                        host: '"ydszone.hylc-edu.cn"',
                        login: {
                            host: '"localhost:8091"'
                        }
                    },
                    //部署路径和服务器
                    home: '/data/project/Media',
                    ssh: {
                        host: '58.117.151.6',
                        username: 'root',
                        password: '71@xiaoxue'
                    }
                }
            },

            //怀柔二小
            'hrex': {
                www: {
                    img: {
                        logo: '<img src="img/logo_hrex.png">'
                    },
                    theme: {
                        nav: {
                            bg: ['#fff', 'rgb(32,66,121)'],
                            text: ['rgb(32,66,121)', '#fff']
                        }
                    }
                },
                server: {
                    host: '"hylc-edu.cn"',
                    express: {
                        port: 8090
                    },
                    mongodb: {
                        host: '"localhost"',
                        port: 27017,
                        username: '""',
                        password: '""'
                    },
                    api: {
                        host: '"hrmedia.hylc-edu.cn"',
                        login: {
                            host: '"localhost:8091"'
                        }
                    },
                    //部署路径和服务器
                    home: '/data/project/Media',
                    ssh: {
                        host: '58.133.96.3',
                        username: 'root',
                        password: '71@xiaoxue'
                    }
                }
            },

            //helpers
            //获取当前环境
            env: function(){
                return grunt.option('env') || 'localhost';
            },
            //获取当前环境下keypath所对应的值
            value: function(keypath) {
                if(keypath) {
                    var env = grunt.option('env') || 'localhost';
                    return '<%= env.' + env + '.' + keypath + ' %>';
                }
                else return null;
            }
        },


        //目录和文件发生变化时自动重新编译
        watch: {
            www: {
                files: ['Gruntfile.js', '<%= CONSTS.WWW_SOURCE %>**'],
                tasks: ['default']
            }
        },


        //合并压缩JavaScript
        uglify: {
            options: {
                banner: '/*! <%= pkg.name %> <%= grunt.template.today("dd-mm-yyyy") %> */\n'
            },
            dist: {
                files: {
                    '<%= CONSTS.WWW_DIST %>js/teacherSpace.min.js': [
                        '<%= CONSTS.WWW_SOURCE %>js/utils/Constants.js',
                        '<%= CONSTS.WWW_SOURCE %>js/services/UtilsService.js',
                        '<%= CONSTS.WWW_SOURCE %>js/services/UserService.js',
                        '<%= CONSTS.WWW_SOURCE %>js/services/ActivityService.js',
                        '<%= CONSTS.WWW_SOURCE %>js/controllers/ActivityDetailController.js',
                        '<%= CONSTS.WWW_SOURCE %>js/controllers/ActivityListController.js',
                        '<%= CONSTS.WWW_SOURCE %>js/controllers/ActivityPanelController.js',
                        '<%= CONSTS.WWW_SOURCE %>js/controllers/LoginFormController.js',
                        '<%= CONSTS.WWW_SOURCE %>js/controllers/MainController.js',
                        '<%= CONSTS.WWW_SOURCE %>js/controllers/NavigatorController.js',
                        '<%= CONSTS.WWW_SOURCE %>js/controllers/ToolbarController.js',
                        '<%= CONSTS.WWW_SOURCE %>js/directives/ngEnter.js',
                        '<%= CONSTS.WWW_SOURCE %>js/teacherSpace.js'
                    ],
                    '<%= CONSTS.WWW_DIST %>js/activityPlay.min.js': [
                        '<%= CONSTS.WWW_SOURCE %>js/utils/Constants.js',
                        '<%= CONSTS.WWW_SOURCE %>js/services/UtilsService.js',
					    '<%= CONSTS.WWW_SOURCE %>js/services/UserService.js',
					    '<%= CONSTS.WWW_SOURCE %>js/services/ActivityService.js',
					    '<%= CONSTS.WWW_SOURCE %>js/controllers/PlayerMainController.js',
					    '<%= CONSTS.WWW_SOURCE %>js/controllers/MainVideoUploaderController.js',
                        //'<%= CONSTS.WWW_SOURCE %>js/directives/activityDate.js',
					    '<%= CONSTS.WWW_SOURCE %>js/activityPlay.js'
                    ]
                }
            }
        },
        //合并压缩css
        cssmin: {
            minify: {
                files: {
                    '<%= CONSTS.WWW_DIST %>css/teacherSpace.min.css': [
                        '<%= CONSTS.WWW_SOURCE %>lib/bootstrap_tagsinput/bootstrap-tagsinput.css',
                        '<%= CONSTS.WWW_SOURCE %>lib/bootstrap_datetimepicker/bootstrap-datetimepicker.css',
                        '<%= CONSTS.WWW_SOURCE %>css/common.css',
                        '<%= CONSTS.WWW_SOURCE %>css/teacherSpace.css'
                    ],
                    '<%= CONSTS.WWW_DIST %>css/activityPlay.min.css': [
                        '<%= CONSTS.WWW_SOURCE %>css/common.css',
                        '<%= CONSTS.WWW_SOURCE %>css/activityPlay.css'
                    ]
                }
            }
        },
        //复制静态资源
        copy: {
            main: {
                files: [{
                    expand: true,
                    cwd: 'www',
                    src: [
                        '*.html',
                        'lib/**',
                        'img/**',
                        'player/**'
                    ],
                    dest: '<%= CONSTS.WWW_DIST %>'
                }]
            }
        },
        //压缩html
        htmlmin: {
            dist: {
                options: {
                    removeComments: true,
                    collapseWhitespace: true
                },
                files: {
                    '<%= CONSTS.WWW_DIST %>teacher_space.html': '<%= CONSTS.WWW_DIST %>teacher_space.html',
                    '<%= CONSTS.WWW_DIST %>activity_play.html': '<%= CONSTS.WWW_DIST %>activity_play.html'
                }
            }
        },


        //替换模版内容
        replace: {
            //替换环境变量，支持兩種格式：
            // /* grunt|env:xxx */ ... /* end */
            // <!-- grunt|env:xxx --> ... <!-- end -->
            env: {
                src: [
                    '<%= CONSTS.SERVER %>**/*.js',
                    '<%= CONSTS.WWW_SOURCE %>*.html',
                    '<%= CONSTS.WWW_SOURCE %>css/*.css',
                    '<%= CONSTS.WWW_SOURCE %>js/**/*.js'
                ],
                overwrite: true,
                filter: 'isFile',
                replacements: [{
                    from: /((\/\*|\<\!--) grunt\|env:(.+?) (\*\/|--\>))(.*?)((\/\*|\<\!--) end (\*\/|--\>))/g,
                    to: function(matchedWord, pos, fullContent, matches){
                        var env = grunt.option('env') || 'localhost';
                        return matches[0] + '<%= env.' + env + '.' + matches[2] + ' %>' + matches[5];
                    }
                }]
            },

            //替换页面的script引用: <!-- grunt|minjs:teacherSpace.min.js --> ... <!-- end -->
            minjs: {
                src: ['<%= CONSTS.WWW_DIST %>*.html'],
                overwrite: true,
                replacements: [{
                    from: /<!-- grunt\|minjs:((\w+).min.js) -->(.|\n)*?<!-- end -->/g,
                    to: function(matchedWord, pos, fullContent, matches){
                        var script = '<script src="js/' + matches[0] + '?' + (new Date()).getTime() + '"></script>'
                        console.log('Updated ' + script);
                        return script;
                    }
                }]
            }
        },


        //shell命令
        shell: {
            //启动/关闭数据库等的命令
            runRedis: {     command: 'redis-server app/configs/redis.conf' },
            runMongoDB: {   command: 'mongod --fork --logpath log/mongodb.log' },
            killRedis: {    command: 'pkill -9 "redis-server"' },
            killMongoDB: {  command: 'pkill -9 "mongod"' },

            //后台
            runServer: {    command: 'sudo node app/server.js' },
            killServer: {   command: 'sudo pkill -9 "node"' },

            //打包发布包
            zipDist: {      command: 'rm -f <%= CONSTS.DEPLOY %>.zip && zip -q -r <%= CONSTS.DEPLOY %> <%= CONSTS.SERVER %> <%= CONSTS.WWW_DIST %> -x <%= CONSTS.SERVER %>node_modules\\*' }
        },


        //ssh
        //上传需要部署的文件
        sftp: {
            deploy: {
                files: {
                    "./": [
                        "deploy.sh", //解壓和更新代碼的腳本
                        "<%= CONSTS.DEPLOY %>.zip" //發佈包
                    ]
                },
                options: {
                    path: '<%= env.value("server.home") %>',
                    host: '<%= env.value("server.ssh.host") %>',
                    username: '<%= env.value("server.ssh.username") %>',
                    password: '<%= env.value("server.ssh.password") %>',
                    showProgress: true
                }
            }
        },
        //执行部署更新操作
        sshexec: {
            deploy: {
                command: [
                    'cd <%= env.value("server.home") %>',
                    'chmod u+x deploy.sh',
                    './deploy.sh <%= env.value("server.home") %> <%= CONSTS.DEPLOY %>.zip'
                ].join(' && '),
                options: {
                    host: '<%= env.value("server.ssh.host") %>',
                    username: '<%= env.value("server.ssh.username") %>',
                    password: '<%= env.value("server.ssh.password") %>'
                }
            }
        },


        //清掉输出目录
        clean: {
            build: ["<%= CONSTS.WWW_DIST %>"],
            deploy: ['<%= CONSTS.DEPLOY %>.zip']
        }
    });


    // Load the plugins
    grunt.loadNpmTasks('grunt-contrib-uglify');
    grunt.loadNpmTasks('grunt-contrib-htmlmin');
    grunt.loadNpmTasks('grunt-contrib-cssmin');
    grunt.loadNpmTasks('grunt-contrib-copy');
    grunt.loadNpmTasks('grunt-contrib-qunit');
    grunt.loadNpmTasks('grunt-contrib-concat');
    grunt.loadNpmTasks('grunt-contrib-jshint');
    grunt.loadNpmTasks('grunt-contrib-watch');
    grunt.loadNpmTasks('grunt-contrib-clean');
    grunt.loadNpmTasks('grunt-text-replace');
    grunt.loadNpmTasks('grunt-shell');
    grunt.loadNpmTasks('grunt-ssh');

    //TODO https://github.com/gruntjs/grunt-contrib-imagemin


    //默认任务：重新编译整个前端项目
    grunt.registerTask('default', ['clean:build', 'uglify', 'cssmin', 'copy', 'replace:env', 'replace:minjs', 'htmlmin']);


    //本地的后台服务任务
    grunt.registerTask('rundb', ['shell:runRedis', 'shell:runMongoDB']);
    grunt.registerTask('killdb', ['shell:killRedis', 'shell:killMongoDB']);
    grunt.registerTask('runserver', ['replace:env', 'shell:runServer']);
    grunt.registerTask('killserver', ['shell:killServer']);


    //发布
    grunt.registerTask('deploy', 'Deploying project ...', function(){
        //判断一下环境对不对
        var env = grunt.option('env');
        if(!env || env == 'localhost'){
            grunt.log.error('Required options:');
            grunt.log.error('--env environment, for example: codewalle');
        }
        else {
            grunt.task.run([
                //编译前端，替換文件里的环境变量，打包发布包
                'default',
                'shell:zipDist',

                //部署发布包到对应的环境并重启服务
                'sftp:deploy',
                'sshexec:deploy',

                //清理本地
                'clean:deploy'
            ])
        }
    });

};