module.exports = function(grunt) {

    // Project configuration.
    grunt.initConfig({
        pkg: grunt.file.readJSON('package.json'),
        CONSTS: {
            DIST: 'www-dist/',
            REDIS: {
                LOCAL: '/Users/oscartong/Dropbox/programming/libraries/redis-2.8.12/src/redis-server'
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

        //替换html文件的script引用等
        replace: {
            teacherSpace: {
                src: ['<%= CONSTS.DIST %>*.html'],
                dest: '<%= CONSTS.DIST %>',
                replacements: [{
                    //<!-- grunt:teacherSpace.min.js --> ... <!-- end -->
                    from: /<!-- grunt:((\w+).min.js) -->(.|\n)*?<!-- end -->/g,
                    to: function(matchedWord, pos, fullContent, matches){
                        var script = '<script src="js/' + matches[0] + '?' + (new Date()).getTime() + '"></script>'
                        console.log('Updated ' + script);
                        return script;
                    }
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

        shell: {
            //启动/关闭数据库等的命令
            runRedis: {     command: '<%= CONSTS.REDIS.LOCAL %> app/configs/redis-local.conf'   },
            runMongoDB: {   command: 'mongod --fork --logpath log/mongodb.log'                  },
            killRedis: {    command: 'pkill -9 "redis-server"'                                  },
            killMongoDB: {  command: 'pkill -9 "mongod"'                                        },

            //后台
            server: {       command: 'node app/server.js'   }
        },

        //清掉输出目录
        clean: {
            build: ["<%= CONSTS.DIST %>"]
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

    //默认任务：重新编译整个前段项目
    grunt.registerTask('default', ['clean', 'uglify', 'cssmin', 'copy', 'replace', 'htmlmin']);

    //后台相关的任务
    grunt.registerTask('rundb', ['shell:runRedis', 'shell:runMongoDB']);
    grunt.registerTask('killdb', ['shell:killRedis', 'shell:killMongoDB']);
    grunt.registerTask('server', ['shell:server']);

};