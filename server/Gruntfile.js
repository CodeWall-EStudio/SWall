module.exports = function(grunt) {

    // Project configuration.
    grunt.initConfig({
        pkg: grunt.file.readJSON('package.json'),

        //合并压缩JavaScript
        uglify: {
            options: {
                banner: '/*! <%= pkg.name %> <%= grunt.template.today("dd-mm-yyyy") %> */\n'
            },
            dist: {
                files: {
                    'www/dist/js/teacherSpace.min.js': [
                        'www/js/utils/Constants/js',
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
                    'www/dist/js/activityPlay.min.js': [
                        'www/js/services/UtilsService.js',
                        'www/js/services/UserService.js',
                        'www/js/services/ActivityService.js',
                        'www/js/controllers/PlayerMainController.js',
                        'www/js/controllers/MainVideoUploaderController.js',
                        'www/js/activityPlay.js'
                    ]
                }
            }
        },

        //合并压缩css
        cssmin: {
            minify: {
                files: {
                    'www/dist/css/teacherSpace.css': [
                        'www/lib/bootstrap_tagsinput/bootstrap-tagsinput.css',
                        'www/lib/bootstrap_datetimepicker/bootstrap-datetimepicker.css',
                        'www/css/common.css',
                        'www/css/teacherSpace.css'
                    ],
                    'www/dist/css/activityPlay.min.css': [
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
                    dest: 'www/dist/'
                }]
            }
        },

        //替换html文件的script引用等
        replace: {
            teacherSpace: {
                src: ['www/dist/*.html'],
                dest: 'www/dist/',
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
                    'www/dist/teacher_space.html': 'www/dist/teacher_space.html',
                    'www/dist/activity_play.html': 'www/dist/activity_play.html'
                }
            }
        },

        clean: {
            build: ["www/dist"]
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

    // Default task(s).
    grunt.registerTask('default', [
        'uglify', //js
        'cssmin', //css
        'copy', //statis resources
        'replace', 'htmlmin' //html
    ]);

};