var mongodb = require('mongodb');


var HOST        = 'localhost',
    PORT        = 27017,
    DB          = 'swall';


//connect to mongodb
function connect(){
    //TODO 这里是不是应该搞个连接池？还是说MongoDB自己维护了一个池？
    var url = 'mongodb://' + HOST + ':' + PORT + '/' + DB;
    mongodb.MongoClient.connect(url, null, function(err, db){
        if(err) throw err;

        //活动数据和配置
        var activityDataCollection = db.collection('activity.data');
        var activityConfigCollection = db.collection('activity.config');
        console.log('[mongodb] connected');

        //初始化活动配置
        //TODO 后面需要开发API和页面来管理这些配置
        activityConfigCollection.findOne({'config':{'$exists':true}}, function(err, doc){
            if(!err && !doc){
                var config = {
                    config: {
                        types: {
                            //类型列表
                            //类型名、可选字段、禁用字段
                            1:{name:'公开课', optional:['desc'], disabled:[]}
                        },
                        subjects: [
                            //学科列表
                        ],
                        classes: [
                            //班级列表
                            {grade:'一年级', cls:['一班', '二班', '三班', '四班']},
                            {grade:'二年级', cls:['一班', '二班', '三班', '四班']},
                            {grade:'三年级', cls:['一班', '二班', '三班', '四班']},
                            {grade:'四年级', cls:['一班', '二班', '三班', '四班']},
                            {grade:'五年级', cls:['一班', '二班', '三班', '四班']},
                            {grade:'六年级', cls:['一班', '二班', '三班', '四班']}
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
    });
    console.log('[mongodb] connecting ' + url + ' ...');
}

connect();
