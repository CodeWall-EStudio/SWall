var mongodb = require('mongodb');


var HOST        = /* grunt|env:server.mongodb.host */"localhost"/* end */, //正式環境：'192.168.98.59'
    PORT        = /* grunt|env:server.mongodb.port */27017/* end */,
    USERNAME    = /* grunt|env:server.mongodb.username */"swall"/* end */,
    PASSWORD    = /* grunt|env:server.mongodb.password */"DfvszXKePFtfB9KM"/* end */,
    DB          = 'swall',
    CONFIG_VER  = 4; //TODO 修改了活动配置记得要改一下配置版本


//connect to mongodb
function connect(){
    var url = (USERNAME && PASSWORD) ? 'mongodb://' + USERNAME + ':' + PASSWORD + '@' + HOST + ':' + PORT + '/' + DB
                                     : 'mongodb://' + HOST + ':' + PORT + '/' + DB;
    mongodb.MongoClient.connect(url, null, function(err, db){
        if(err) throw err;

        //活动数据和配置
        var activityDataCollection      = db.collection('activity.data'),
            activityConfigCollection    = db.collection('activity.config'),
            activityUserCollection      = db.collection('activity.user');
        console.log('[mongodb] connected');

        //TODO 创建indexes来优化检索性能

        //初始化活动配置
        function queryActivityConfig(callback){
            activityConfigCollection.findOne({'activityConfig':{'$exists':true}, 'version':CONFIG_VER}, callback);
        }
        queryActivityConfig(function(err, doc){
            if(!err && !doc){
                var config = {
                    version: CONFIG_VER,
                    activityConfig: {
                        //类型列表
                        types: [
                            //类型名、可选字段、禁用字段
                            {value:1, label:'公开课',   optional:['desc'], disabled:[]},
                            {value:2, label:'学校活动', optional:['desc'], disabled:[]},
                            {value:3, label:'区级活动', optional:['desc'], disabled:[]},
                            {value:4, label:'市级活动', optional:['desc'], disabled:[]},
                            {value:5, label:'其他活动', optional:['desc'], disabled:[]}
                        ],
                        //学科列表
                        subjects: [
                            '不分学科', '语文', '数学', '英语', '科学', '综合实践', '写字', '美术', '舞蹈音乐', '体育', '品德与生活', '品德与社会', '校本'
                        ],
                        classes: [
                            //班级列表
                            {grade:'不分年级', cls:['不分班级']},
                            {grade:'一年级', cls:['不分班级', '一班', '二班', '三班', '四班', '五班', '六班', '七班', '八班', '九班', '十班', '十一班', '十二班', '十三班', '十四班']},
                            {grade:'二年级', cls:['不分班级', '一班', '二班', '三班', '四班', '五班', '六班', '七班', '八班', '九班', '十班', '十一班', '十二班', '十三班', '十四班']},
                            {grade:'三年级', cls:['不分班级', '一班', '二班', '三班', '四班', '五班', '六班', '七班', '八班', '九班', '十班', '十一班', '十二班', '十三班', '十四班']},
                            {grade:'四年级', cls:['不分班级', '一班', '二班', '三班', '四班', '五班', '六班', '七班', '八班', '九班', '十班', '十一班', '十二班', '十三班', '十四班']},
                            {grade:'五年级', cls:['不分班级', '一班', '二班', '三班', '四班', '五班', '六班', '七班', '八班', '九班', '十班', '十一班', '十二班', '十三班', '十四班']},
                            {grade:'六年级', cls:['不分班级', '一班', '二班', '三班', '四班', '五班', '六班', '七班', '八班', '九班', '十班', '十一班', '十二班', '十三班', '十四班']}
                        ]
                    }
                };
                activityConfigCollection.insert(config, {w:1}, function(err, newDoc){
                    console.log('[mongodb] activity configuration initialized, err:', err);
                });
            }
        });

        function isSuperUser(uid, callback){
            activityUserCollection.findOne({'$and':[{uid:uid}, {super:true}]}, function(err, doc){
                callback((!err && doc !== null), doc);
            });
        }

        exports.db = db;
        exports.activityDataCollection = activityDataCollection;
        exports.activityConfigCollection = activityConfigCollection;
        exports.activityUserCollection = activityUserCollection;
        exports.utils = {
            queryActivityConfig: queryActivityConfig,
            isSuperUser: isSuperUser
        };
    });
    console.log('[mongodb] connecting ' + url + ' ...');
}

connect();
