module.exports = function(grunt) {

    // Project configuration.
    grunt.initConfig({
        pkg: grunt.file.readJSON('package.json'),
        CONSTS: {
            //目录
            DIST: 'www-dist/',
            SERVER: 'app/',

            //Redis和MongoDB的配置
            REDIS: {
                //TODO 这个应该写到我本地的PATH里吧
                LOCAL: '/Users/oscartong/Dropbox/programming/libraries/redis-2.8.12/src/redis-server'
            },
            MONGODB: {
                LOCAL: {
                    HOST: 'localhost',
                    PORT: 27017,
                    USERNAME: '',
                    PASSWORD: ''
                },
                CODEWALLE: {
                    HOST: 'localhost',
                    PORT: 27017,
                    USERNAME: 'swall',
                    PASSWORD: 'DfvszXKePFtfB9KM'
                }
            }
        },

        //目录和文件发生变化时自动重新编译
        watch: {
            www: {
                files: ['Gruntfile.js', 'www/**'],
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
                    '<%= CONSTS.DIST %>js/teacherSpace.min.js': [
                        'www/js/utils/Constants.js',
                        'www/js/services/UtilsService.js',
                        'www/js/services/UserService.js',
                        'www/js/services/ActivityService.js',
                        'www/js/controllers/ActivityDetailController.js',
                        'www/js/controllers/ActivityListController.js',
                        'www/js/controllers/ActivityPanelController.js',
                        'www/js/controllers/LoginFormController.js',
                        'www/js/controllers/MainController.js',
                        'www/js/controllers/NavigatorController.js',
                        'www/js/controllers/ToolbarController.js',
                        'www/js/directives/ngEnter.js',
                        'www/js/teacherSpace.js'
                    ],
                    '<%= CONSTS.DIST %>js/activityPlay.min.js': [
                        'www/js/utils/Constants.js',
                        'www/js/services/UtilsService.js',
					    'www/js/services/UserService.js',
					    'www/js/services/ActivityService.js',
					    'www/js/controllers/PlayerMainController.js',
					    'www/js/controllers/MainVideoUploaderController.js',
                        //'www/js/directives/activityDate.js',
					    'www/js/activityPlay.js'
                    ]
                }
            }
        },

        //合并压缩css
        cssmin: {
            minify: {
                files: {
                    '<%= CONSTS.DIST %>css/teacherSpace.min.css': [
                        'www/lib/bootstrap_tagsinput/bootstrap-tagsinput.css',
                        'www/lib/bootstrap_datetimepicker/bootstrap-datetimepicker.css',
                        'www/css/common.css',
                        'www/css/teacherSpace.css'
                    ],
                    '<%= CONSTS.DIST %>css/activityPlay.min.css': [
                        'www/css/common.css',
                        'www/css/activityPlay.css'
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
                    dest: '<%= CONSTS.DIST %>'
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
                    '<%= CONSTS.DIST %>teacher_space.html': '<%= CONSTS.DIST %>teacher_space.html',
                    '<%= CONSTS.DIST %>activity_play.html': '<%= CONSTS.DIST %>activity_play.html'
                }
            }
        },

        //替换模版内容
        replace: {
            //替换页面的script引用: <!-- grunt:teacherSpace.min.js --> ... <!-- end -->
            teacherSpace: {
                src: ['<%= CONSTS.DIST %>*.html'],
                dest: '<%= CONSTS.DIST %>',
                replacements: [{
                    from: /<!-- grunt:((\w+).min.js) -->(.|\n)*?<!-- end -->/g,
                    to: function(matchedWord, pos, fullContent, matches){
                        var script = '<script src="js/' + matches[0] + '?' + (new Date()).getTime() + '"></script>'
                        console.log('Updated ' + script);
                        return script;
                    }
                }]
            },

            //替换mongodb的运行环境
            mongoDBLocal: mongoDBReplacement('LOCAL'),
            mongoDBCodeWallE: mongoDBReplacement('CODEWALLE')
        },

        //shell命令
        shell: {
            //启动/关闭数据库等的命令
            runRedis: {     command: '<%= CONSTS.REDIS.LOCAL %> app/configs/redis-local.conf' },
            runMongoDB: {   command: 'mongod --fork --logpath log/mongodb.log' },
            killRedis: {    command: 'pkill -9 "redis-server"' },
            killMongoDB: {  command: 'pkill -9 "mongod"' },

            //后台
            runServer: {    command: 'sudo node app/server.js' },
            killServer: {   command: 'pkill -9 "node"' },

            //打包发布包
            zipDist: {      command: 'rm -f deploy.zip && zip -q -r deploy app www-dist' }
        },

        //ssh
        sshconfig: {
            //TODO 支持不同环境
            codeWallE: {
                host: 'codewalle.com',
                username: 'root',
                password: 'uswveYrvGr93wjvA'
            }
        },
        sftp: {
            deploy: {
                files: {
                    "./": "deploy.zip"
                },
                options: {
                    path: '/home/swall/SWall',
                    config: 'codeWallE',
                    showProgress: true
                }
            }
        },
        sshexec: {
            deploy: {
                command: [
                    'cd /home/swall/SWall',
                    'cp -r server backups/server.<%= grunt.template.today("yyyymmddhhMMss") %>', //backup
                    'forever stop /home/swall/SWall/server/app/server.js', //stop server
                    'unzip -q -o deploy.zip -d server', //unzip new version
                    'forever -a -m 10 start /home/swall/SWall/server/app/server.js' //restart server
                ].join(' && '),
                options: {
                    config: 'codeWallE'
                }
            }
        },

        //清掉输出目录
        clean: {
            build: ["<%= CONSTS.DIST %>"],
            deploy: ['deploy.zip']
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

    //默认任务：重新编译整个前段项目
    grunt.registerTask('default', ['clean:build', 'uglify', 'cssmin', 'copy', 'replace:teacherSpace', 'htmlmin']);

    //后台相关的任务
    grunt.registerTask('rundb', ['shell:runRedis', 'shell:runMongoDB']);
    grunt.registerTask('killdb', ['shell:killRedis', 'shell:killMongoDB']);
    grunt.registerTask('runserver', ['replace:mongoDBLocal', 'shell:runServer']);
    grunt.registerTask('killserver', ['shell:killServer']);

    //发布
    grunt.registerTask('deploy', [
        'default',                  //编译前端
        'replace:mongoDBCodeWallE', //更新数据库连接配置
        'shell:zipDist',            //打包发布包
        'sftp:deploy',              //上传发布包
        'sshexec:deploy',           //更新文件，重启后台服务
        'clean:deploy'
    ])

};


//HELPER FUNCTIONS HERE ================================================================================================


/**
 * 根据传入的环境，返回替换mongoDB运行环境数据的模版
 * @param {String} env 'LOCAL', 'CODEWALLE', ...
 * @returns {{src: string[], overwrite: boolean, replacements: {from: RegExp, to: to}[]}}
 */
function mongoDBReplacement(env){
    return {
        src: ['<%= CONSTS.SERVER %>app_modules/db.js'],
        overwrite: true,
        replacements: [{
            from: /(\/\* grunt:MONGODB\.(\w+?) \*\/)(.*?)(\/\* end \*\/)/g,
            to: function(matchedWord, pos, fullContent, matches){
                var value = '<%= CONSTS.MONGODB.' + env + '.' + matches[1] + ' %>',
                    bracket = (typeof value == 'string') ? '"' : '';
                return matches[0] + bracket + value + bracket + matches[3];
            }
        }]
    }
}

function log(err, stdout, stderr, cb) {
    console.log(stdout);
    cb();
}