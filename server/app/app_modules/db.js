var mongodb = require('mongodb');


//TODO 有没官方点的方法来替换这些环境变量？
var HOST        = /* grunt:MONGODB.HOST */"localhost"/* end */, //正式環境：'192.168.98.59'
    PORT        = /* grunt:MONGODB.PORT */"27017"/* end */,
    DB          = 'swall',
    USERNAME    = /* grunt:MONGODB.USERNAME */"swall"/* end */,
    PASSWORD    = /* grunt:MONGODB.PASSWORD */"DfvszXKePFtfB9KM"/* end */;


//connect to mongodb
function connect(){
    var url = (USERNAME && PASSWORD) ? 'mongodb://' + USERNAME + ':' + PASSWORD + '@' + HOST + ':' + PORT + '/' + DB
                                     : 'mongodb://' + HOST + ':' + PORT + '/' + DB;
    mongodb.MongoClient.connect(url, null, function(err, db){
        if(err) throw err;

        //活动数据和配置
        var activityDataCollection = db.collection('activity.data');
        var activityConfigCollection = db.collection('activity.config');
        console.log('[mongodb] connected');

        //TODO 创建indexes来优化检索性能

        //初始化活动配置
        //TODO 后面需要开发API和页面来管理这些配置
        function queryActivityConfig(callback){
            activityConfigCollection.findOne({'activityConfig':{'$exists':true}}, callback);
        }
        queryActivityConfig(function(err, doc){
            if(!err && !doc){
                var config = {
                    activityConfig: {
                        //类型列表
                        types: [
                            //类型名、可选字段、禁用字段
                            {value:1, label:'公开课', optional:['desc'], disabled:[]}
                        ],
                        //学科列表
                        subjects: [
                            '语文', '数学', '英语', '科学', '综合实践', '写字', '美术', '舞蹈音乐', '体育', '品德与生活',
                            '品德与社会', '校本'
                        ],
                        classes: [
                            //班级列表
                            {grade:'一年级', cls:['一班', '二班', '三班', '四班', '五班', '六班', '七班', '八班', '九班', '十班', '十一班', '十二班', '十三班', '十四班']},
                            {grade:'二年级', cls:['一班', '二班', '三班', '四班', '五班', '六班', '七班', '八班', '九班', '十班', '十一班', '十二班', '十三班', '十四班']},
                            {grade:'三年级', cls:['一班', '二班', '三班', '四班', '五班', '六班', '七班', '八班', '九班', '十班', '十一班', '十二班', '十三班', '十四班']},
                            {grade:'四年级', cls:['一班', '二班', '三班', '四班', '五班', '六班', '七班', '八班', '九班', '十班', '十一班', '十二班', '十三班', '十四班']},
                            {grade:'五年级', cls:['一班', '二班', '三班', '四班', '五班', '六班', '七班', '八班', '九班', '十班', '十一班', '十二班', '十三班', '十四班']},
                            {grade:'六年级', cls:['一班', '二班', '三班', '四班', '五班', '六班', '七班', '八班', '九班', '十班', '十一班', '十二班', '十三班', '十四班']}
                        ]
                    }
                };
                activityConfigCollection.insert(config, {w:1}, function(err, newDoc){
                    console.log('[mongodb] activity configuration initialized, err:', err);
                });
            }
        });

        exports.db = db;
        exports.activityDataCollection = activityDataCollection;
        exports.activityConfigCollection = activityConfigCollection;
        exports.utils = {
            queryActivityConfig: queryActivityConfig
        };
    });
    console.log('[mongodb] connecting ' + url + ' ...');
}

connect();
