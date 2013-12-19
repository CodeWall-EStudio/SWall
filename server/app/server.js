var express = require('express'),
    mongodb = require('mongodb'),
    _       = require('underscore')._,
    db      = require('./app_modules/db'),
    user    = require('./app_modules/user'),
    utf8    = require('./app_modules/utf8');


var PORT = 8080;


var SHORT_STR_MAXLEN = 90,
    LONG_STR_MAXLEN = 450;


var app = express();
//allows CORS
/*var allowCrossDomain = function(req, res, next) {
    res.header('Access-Control-Allow-Origin', '*');
    res.header('Access-Control-Allow-Methods', 'GET,PUT,POST,DELETE');
    res.header('Access-Control-Allow-Headers', 'Content-Type');
    next();
};*/
//app.use(allowCrossDomain);
app.use(express.cookieParser());
app.use(express.bodyParser());


////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//POST, GET /activities


//创建活动
app.post('/activities', function(req, res){
    if(!user.redirectToLoginIfUnauthorized(req, res)){
        var uid     = user.uid(req);
        var rawUids = req.body['uids'],
            uids    = rawUids ? rawUids.split(',') : [],
            title   = req.body['title'],
            desc    = req.body['desc'],
            teacher = req.body['teacher'],
            grade   = req.body['grade'],
            cls     = req.body['class'],
            subject = req.body['subject'],
            domain  = req.body['domain'],
            type    = parseInt(req.body['type'])||0,
            ts      = parseInt(req.body['date'])||0;

        //TODO 以后不同的type可能会对应不同的必填字段，后面要考虑下怎么支持
        //TODO 應該把讀取和規整參數、檢查參數規格和是否必填這些操作做成通用可配置
        if(uids.length){
            if(title && utf8.length(title) <= SHORT_STR_MAXLEN){
                if(!desc || utf8.length(desc) <= LONG_STR_MAXLEN){ //要不没有desc，如果有则不能超过长度限制
                    if(type){
                        if(ts > (new Date()).getTime()){ //活动开始时间必须比现在要晚
                            if(teacher && utf8.length(teacher) <= SHORT_STR_MAXLEN){
                                if(grade && utf8.length(grade) <= SHORT_STR_MAXLEN){
                                    if(cls && utf8.length(cls) <= SHORT_STR_MAXLEN){
                                        if(subject && utf8.length(subject) <= SHORT_STR_MAXLEN){
                                            if(domain && utf8.length(domain) <= SHORT_STR_MAXLEN){
                                                var doc = {
                                                    users: {creator:uid, invitedUsers:uids, participators:[]},
                                                    resources: [],
                                                    active: true,
                                                    info: {
                                                        title:title, desc:desc, type:type,
                                                        date:new Date(ts), createDate:new Date(),
                                                        teacher:teacher, grade:grade, 'class':cls,
                                                        subject:subject, domain:domain
                                                    }
                                                };

                                                console.log('[server] insert document', doc);
                                                db.activityDataCollection.insert(doc, {w:1}, function(err, newDoc){
                                                    if(err) res.json(500, {c:1, m:err.message});
                                                    else    res.json(201, {c:0, r:newDoc});
                                                });
                                            }
                                            else res.json(400, {c:1, m:'Invalid Domain'}); }
                                        else res.json(400, {c:1, m:'Invalid Subject'}); }
                                    else res.json(400, {c:1, m:'Invalid Class'}); }
                                else res.json(400, {c:1, m:'Invalid Grade'}); }
                            else res.json(400, {c:1, m:'Invalid Teacher'}); }
                        else res.json(400, {c:1, m:'Invalid Date'}); }
                    else res.json(400, {c:1, m:'Invalid Type'}); }
                else res.json(400, {c:1, m:'Invalid Description'}); }
            else res.json(400, {c:1, m:'Invalid Title'}); }
        else res.json(400, {c:1, m:'Invalid Uids'});
    }
});

//获取活动列表
app.get('/activities', function(req, res){
    if(!user.redirectToLoginIfUnauthorized(req, res)){
        var uid = user.uid(req),
            query = {};

        //根據活動狀態過濾，active(true) | closed(false)
        if(req.query['status']){
            switch(req.query['status']){
                case 'active': query.active = true;  break;
                case 'closed': query.active = false; break;
            }
        }
        //根據活動授權過濾，只能搜出開放的、或是授權我能參與的、或是我正在参与的、或是我创建的活動
        //默认会搜索所有符合这些条件的活动
        switch(req.query['authorize']){
            case 'public':      query['users.invitedUsers']  = {'$in':['*']}; break; //公开的
            case 'invited':     query['users.invitedUsers']  = {'$in':[uid]}; break; //我受邀请参与的
            case 'available':   query['users.invitedUsers']  = {'$in':['*', uid]}; break; //public+invited
            case 'joined':      query['users.participators'] = {'$in':[uid]}; break; //我正在参与的
            case 'mine':        query['users.creator'] = uid; break; //我创建的
            default:
                query['$or'] = [
                    {'users.creator': uid},
                    {'users.invitedUsers': {'$in':['*', uid]}}
                    //这里就不用加上participators这个条件了，因为和invitedUsers是重合的
                ];
                break;
        }

        //起始位置和條數
        var index = (req.query['index'] || 0)|0;
        var count = (req.query['count'] || 0)|0;

        console.log('[server] find documents', query, 'limit=' + count + ', skip=' + index);
        db.activityDataCollection
            .find(query)
            .limit(count)
            .skip(index)
            .sort({'info.date':1, 'info.title':1})
            .toArray(function(err, docs){
            if(err) res.json(500, {c:1, m:err.message});
            else    res.json(200, {c:0, r:docs});
        });
    }
});


////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//GET, DELETE, PUT /activities/:aid


//获取单个活动
app.get('/activities/:aid', function(req, res){
    if(!user.redirectToLoginIfUnauthorized(req, res)){
        var uid = user.uid(req),
            aid = new mongodb.ObjectID(req.params['aid']),
            //找出特定id且是我創建的/我獲授權參與的/公開的
            query = {
                '_id': aid,
                '$or': [
                    {'users.creator': uid},
                    {'users.invitedUsers': {'$in': ['*', uid]}}
                ]
            };

        console.log('[server] find one document', query);
        db.activityDataCollection.findOne(query, function(err, doc){
            if(err) res.json(500, {c:1, m:err.message});
            else{
                if(!doc)    res.json(404, {c:1});
                else        res.json(200, {c:0, r:doc});
            }
        });
    }
});

//删除活动
app.delete('/activities/:aid', function(req, res){
    if(!user.redirectToLoginIfUnauthorized(req, res)){
        var uid = user.uid(req),
            aid = new mongodb.ObjectID(req.params['aid']),
            query = {
                '_id': aid, //匹配id
                'users.creator': uid, //自能刪我自己創建的活動
                '$or': [
                    //TODO 驗證一下這個邏輯
                    //若是開放的活動：當前必須沒人參與且沒上傳過資源
                    //若是已關閉的活動：必須是沒人上傳過資源的活動
                    {'active':true, 'users.participators':{$size:0}, 'resources':{$size:0}},
                    {'active':false, 'resources':{$size:0}}
                ]
            };

        console.log('[server] delete document that matches:', query);
        db.activityDataCollection.remove(query, {w:1}, function(err, count){
            if(err)         res.json(500, {c:1, m:err.message});
            else if(!count) res.json(404, {c:1});
            else            res.json(200, {c:0, r:count});
        });
    }
});

//更新活动
app.put('/activities/:aid', function(req, res){
    if(!user.redirectToLoginIfUnauthorized(req, res)){
        var uid     = user.uid(req),
            aid     = new mongodb.ObjectID(req.params['aid']);

        //先把這活動找出來
        var query = {
            '_id': aid,
            'users.creator': uid
        };
        console.log('[server] looking for activity', query);
        db.activityDataCollection.findOne(query, function(err, doc){
            //沒找到或出錯的話就直接返回吧
            if(err)         res.json(500, {c:1, m:err.message});
            else if(!doc)   res.json(404, {c:1});
            else {
                //把請求的參數都讀出來
                var rawUids = req.body['uids'],
                    uids    = rawUids ? rawUids.split(',') : [],
                    title   = req.body['title'],
                    desc    = req.body['desc'],
                    teacher = req.body['teacher'],
                    grade   = req.body['grade'],
                    cls     = req.body['class'],
                    subject = req.body['subject'],
                    domain  = req.body['domain'],
                    status  = req.body['status'],
                    type    = parseInt(req.body['type'])||0,
                    ts      = parseInt(req.body['date'])||0;

                //如果是開放中的活動，則所有字段都可以編輯
                var updates = {};
                if(doc.active){
                    updates['info.desc'] = desc;
                    if(status == 'closed'){
                        updates['active'] = false; //只能關閉開放中的活動，不能重新开放已关闭的活动
                        updates['users.participators'] = []; //活动关闭后，自动把所有参与者踢出去 TODO 确认一下是不是有这个逻辑
                    }

                    if(title)       updates['info.title']           = title;
                    if(type)        updates['info.type']            = type;
                    if(ts)          updates['info.date']            = new Date(ts);
                    if(teacher)     updates['info.teacher']         = teacher;
                    if(grade)       updates['info.grade']           = grade;
                    if(cls)         updates['info.class']           = cls;
                    if(subject)     updates['info.subject']         = subject;
                    if(domain)      updates['info.domain']          = domain;
                }
                //否則的話就只能改用戶權限
                //可以隨便新增用戶，但是只能刪除沒上傳過資源的用戶
                if(uids.length){
                    //先取出活动里上传过资源的用户uid
                    var uidsWhichHaveResources = _.map(doc.resources, function(resource){
                        return resource.user;
                    });
                    //合并请求传入的uids和上传过资源的用户uid，目的是保证上传过资源的用户uid必须是在合并结果里
                    uids = _.uniq(uids.concat(uidsWhichHaveResources)); //TODO 验证一下这个逻辑
                    updates['users.invitedUsers'] = uids;
                }

                //最後更新一下這個活動
                console.log('[server] update activity', updates);
                db.activityDataCollection.update(doc, {$set:updates}, {w:1}, function(err, count){ //TODO 可以返回修改後的doc不
                    if(err) res.json(500, {c:1, m:err.message});
                    else    res.json(200, {c:0, r:count});
                });
            }
        });
    }
});


////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//Join / Quit Activity


//参与活动
app.post('/activities/:aid/participators', function(req, res){
    if(!user.redirectToLoginIfUnauthorized(req, res)){
        var uid = user.uid(req),
            aid = new mongodb.ObjectID(req.params['aid']),
            //这是用来检查我是否已经加入别的活动的
            joinedActivityQuery = {
                'users.participators': {'$in':[uid]}
            },
            //找出指定id、开放的、我能參與的活動
            findActivityQuery = {
                '_id': aid,
                'active': true,
                '$or': [
                    {'users.creator': uid},
                    {'users.invitedUsers': {'$in': ['*', uid]}}
                ]
            },
            //把我的uid插入到participators
            updates = {
                '$addToSet': {
                    'users.participators': uid
                }
            };

        console.log('[server] join activity', findActivityQuery);
        //先检查用户有没已经加入了活动，如果已经加入了这个或者任何别的活动，都不允许再重复加入
        db.activityDataCollection.findOne(joinedActivityQuery, function(err, doc){
            if(err)         res.json(500, {c:1, m:err.message});
            else if(doc)    res.json(403, {c:1}); //禁止同时加入多个活动
            else{
                //好吧这用户还是挺老实的，我们现在把用户插到participators里
                db.activityDataCollection.findAndModify(findActivityQuery, null, updates, {w:1, new:true}, function(err, doc){
                    if(err)         res.json(500, {c:1, m:err.message});
                    else if(!doc)   res.json(401, {c:1});
                    else            res.json(201, {c:0, r:doc});
                });
            }
        });
    }
});

//退出活动
app.delete('/activities/:aid/participators/:uid', function(req, res){
    if(!user.redirectToLoginIfUnauthorized(req, res)){
        var uid = user.uid(req),
            aid = new mongodb.ObjectID(req.params['aid']),
            //找出指定id且我已经参与的活动
            query = {
                '_id': aid,
                'users.participators': {'$in': [uid]}
            },
            //把我的uid移出参与者里
            updates = {
                '$pull': {
                    'users.participators': uid
                }
            };

        console.log('[server] quit activity', query);
        db.activityDataCollection.findAndModify(query, null, updates, {w:1, new:true}, function(err, doc){
            if(err)         res.json(500, {c:1, m:err.message});
            else if(!doc)   res.json(404, {c:1});
            else            res.json(200, {c:0, r:doc});
        });
    }
});


////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
// Resources


app.post('/activities/:aid/resources', function(req, res){
    if(!user.redirectToLoginIfUnauthorized(req, res)){
        var uid = user.uid(req),
            aid = new mongodb.ObjectID(req.params['aid']),
            type = parseInt(req.body['type'])||0,
            content = req.body['content'],
            comment = req.body['comment'];

        if(content){ //TODO 文本内容和url的长度限制是多少？
            //必须是我正在参与的活动哦不可以随便上传到别的活动上去
            var query = {
                    '_id': aid,
                    'users.participators': {'$in': [uid]}
                },
                resource = {
                    _id: new mongodb.ObjectID(),
                    user: uid,
                    activity: aid,
                    type: type,
                    content: content,
                    comment: comment,
                    date: new Date()
                },
                updates = {'$push':{'resources':resource}};

            //找出活动并添加资源
            db.activityDataCollection.findAndModify(query, null, updates, {w:1, new:true}, function(err, doc){
                if(err)         res.json(500, {c:1, m:err.message});
                else if(!doc)   res.json(404, {c:1});
                else            res.json(201, {c:0, r:doc});
            });
        }
        else res.json(400, {c:1, m:'Invalid Content'});
    }
});

app.get('/activities/:aid/resources', function(req, res){
    if(!user.redirectToLoginIfUnauthorized(req, res)){
        var uid = user.uid(req),
            aid = new mongodb.ObjectID(req.params['aid']),
            index = parseInt(req.query['index'])||0,
            count = parseInt(req.query['count'])||0,
            query = {
                '_id': aid,
                '$or': [
                    {'users.creator': uid},
                    {'users.invitedUsers': {'$in':['*', uid]}}
                ]
            };

        //查找对应的活动
        db.activityDataCollection.findOne(query, function(err, doc){
            if(err)         res.json(500, {c:1, m:err.message});
            else if(!doc)   res.json(404, {c:1});
            else {
                //取出这活动的资源
                var resources = doc.resources;
                //如果url参数上带有mine，则过滤出我自己上传的资源
                if(req.query.hasOwnProperty('mine')){
                    resources = _.filter(resources, function(resource){
                        return resource.user == uid;
                    });
                }
                if(index){
                    if(count)   resources = resources.slice(index, index+count);
                    else        resources = resources.slice(index);
                }
                res.json(200, {c:0, r:resources});
            }
        });
    }
});

app.delete('/activities/:aid/resources/:rid', function(req, res){
    if(!user.redirectToLoginIfUnauthorized(req, res)){
        var uid = user.uid(req),
            aid = new mongodb.ObjectID(req.params['aid']),
            rid = new mongodb.ObjectID(req.params['rid']),
            //只能删我能参与的活动中我自己上传的资源
            query = {
                '_id': aid,
                'users.invitedUsers': {'$in':['*', uid]},
                'resources._id': rid,
                'resources.user': uid
            },
            updates = {
                '$pull': {
                    'resources': {'_id':rid}
                }
            };

        db.activityDataCollection.update(query, updates, {w:1}, function(err, count){
            if(err)         res.json(500, {c:1, m:err.message});
            else if(!count) res.json(404, {c:1});
            else            res.json(200, {c:0, r:count});
        });
    }
});


////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
// Statistics


app.get('/activities/:aid/stat/topUsers', function(req, res){
    if(!user.redirectToLoginIfUnauthorized(req, res)){
        var uid = user.uid(req),
            aid = new mongodb.ObjectID(req.params['aid']),
            count = parseInt(req.query['n']),
            query = {
                '_id': aid,
                '$or': [
                    {'users.creator': uid},
                    {'users.invitedUsers': {'$in':['*', uid]}}
                ]
            };

        db.activityDataCollection.findOne(query, function(err, doc){
            if(err)         res.json(500, {c:1, m:err.message});
            else if(!doc)   res.json(404, {c:1});
            else {
                //首先统计每个用户都上传了多少资源，然后排个顺
                var uidCount  = _.countBy(  doc.resources,  function(resource){     return resource.user;           }),
                    countArr  = _.map(      uidCount,       function(count, uid){   return {uid:uid, count:count};  }),
                    sortedArr = _.sortBy(   countArr,       function(item){         return -item.count;             });

                //如果限制了数量，则割头N名出来
                if(count) sortedArr = sortedArr.slice(0, count);
                res.json(200, {c:0, r:sortedArr});
            }
        });
    }
});

app.get('/activities/:aid/stat/topTimes', function(req, res){

});


app.listen(PORT);
console.log('[server] running on ' + PORT);