var http = require('http'),
    redis = require('redis'),
    every = require('schedule').every,
    CAS = require('./cas'),
    _ = require('underscore')._;

var querystring = require('querystring');
var url = require('url');

var redisClient = redis.createClient();


var GET_ENCODE_KEY_API      = {host:'my.71xiaoxue.com', path:'/authenticationUser.do'},
    VERIFY_ENCODE_KEY_API   = {host:'my.71xiaoxue.com', path:'/authenticationKey.do'},
    GET_PROFILE_API         = {host:'mapp.71xiaoxue.com', path:'/components/getUserInfo.htm'},
    GET_ORGANIZATION_TREE   = {host:'mapp.71xiaoxue.com', path:'/components/getOrgTree.htm'},
    UC_ORGANIZATION_TREE    = {host:'szone.71xiaoxue.com', path:'/api/media/departments'};


function login(username, password, callback){
    if(username && password){
        //FOR LOCAL TESTING ONLY
        /*callback(null, 200, {
            uid: username,
            skey: '----------',
            nick: username
        });*/
        post(
            GET_ENCODE_KEY_API,
            'loginName=' + username + '&password=' + password,
            function(data, res){
                try{
                    var json = JSON.parse(data);
                    if(json && json['success'] && json['resultObject']){
                        var obj = json['resultObject'] || {},
                            nick = obj['userName'],
                            encodeKey = obj['encodeKey'];
                        callback(null, res.statusCode, {
                            uid: username,
                            skey: encodeKey,
                            nick: nick
                        });
                    }
                    else {
                        callback(null, res.statusCode);
                    }
                }
                catch(e){
                    callback(e);
                }
            },
            function(e){
                callback(e);
            }
        )
    }
    else callback(new Error('Invaild username or password'));
}


/**
 * verify encodeKey and returns the user info object
 * @param {String} encodeKey
 * @param {Function} callback function(error:Error, statusCode:int, userInfo:Object)
 */
function verifyEncodeKey(encodeKey, callback){
    //callback(null, 200, {loginName:'tangqihong'});
    if(encodeKey){
        post(
            GET_PROFILE_API,
            'encodeKey='+encodeKey,
            function (data, res) {
                try{
                    var json = JSON.parse(data);
                    callback(null, res.statusCode, json['userInfo']);
                }
                catch(e){
                    callback(e);
                }
            },
            function(e) {
                callback(e);
            }
        );
    }
    else callback(new Error('Invalid EncodeKey'));
}
/**
 * verify the request, if no vaild encodeKey response 401, otherwise callback with user info
 * @param req
 * @param res
 * @param {Function} callback function(userInfo:Object)
 */
function response401IfUnauthoirzed(req, res, callback){
    var encodeKey = req.cookies['skey'];
    verifyEncodeKey(encodeKey, function(error, statusCode, userInfo){
        if(error || statusCode!=200 || !userInfo || !userInfo['loginName']){
            res.json(401, {c:10, m:'Unauthoirzed'});
        }
        else {
            callback(userInfo);
        }
    });
}

/*71 单点登陆*/
function cas(req, res) {
    if (!cas.ins) {
        var host = req.protocol+'://'+req.headers.host;
        cas.ins = new CAS({
            base_url: 'http://dand.71xiaoxue.com:80/sso.web',
            service: host + '/users/callback'
        });
    }
    return cas.ins;
}

function casrequest(params, callback) {

    var obj = url.parse(params.url);

    var options = {
        hostname: obj.hostname,
        port: obj.port,
        path: obj.path
    };

    options.method = params.method;
    options.headers = params.headers || {};

    var req = (obj.protocol === 'https:' ? https : http).request(options, function(res) {
        if (res.statusCode === 200) {
            res.setEncoding(params.encoding || 'utf8');
            var response = '';
            res.on('error', callback);

            res.on('data', function(chunk) {
                response += chunk;
            });
            res.on('end', function() {
                callback(null, response, res);
            });

        } else {
            callback(null, '', res);
        }

    });
    if (options.method === 'POST' && params.data) {
        req.write(params.data);
    }
    req.on('error', callback);
    req.end();
};

function decode(skey, callback) {

    var data = querystring.stringify({
        encodeKey: skey
    });

    casrequest({
        url: 'http://mapp.71xiaoxue.com/components/getUserInfo.htm',
        method: 'POST',
        data: data,
        headers: {
            "Content-Type": 'application/x-www-form-urlencoded',
            "Content-Length": data.length
        }

    }, function(err, data) {
        console.log('decode function:         '  ,data,err);
        if (err) {
            console.log('error');
            callback(err);
        } else {
            try {
                if (!data) {
                    callback('the sso server does not return any thing');
                } else {
                    console.log(typeof data,data);
                    callback(null, JSON.parse(data));
                }
            } catch (e) {
                //Logger.error('getUserInfo Error', data);
                callback('getUserInfo JSON parse error: ' + e.message);
            }
        }
    });
}

function info(req,res){
    var url = cas(req, res).login();
        //Logger.info('[login] redirect to: ' + url);
    res.redirect(url);
}

function callback(req,res){
    var ticket = req.param('ticket');

    if (!ticket) {
        res.json({
            err: 100001
        });
        return;
    }

    cas(req, res).validate(ticket, function(err, status, data) {

        if (err) {
            return res.json({
                err: 100002,
                msg: err
            });
        } else if (!status) {
            return res.json({
                err: 100003,
                msg: 'cas ticket "' + ticket + '" 验证失败!'
            });
        }
        try {
            data = JSON.parse(data);
        } catch (e) {
            return res.json({
                err: 100002,
                msg: 'CAS返回的数据格式异常'
            });
        }
        console.log('return:    ' ,data);

        var user = {};
        user.id = data.loginName;
        user.skey = data.encodeKey;
        user.role = 2; //'teacher';

        var expiresDate = new Date(Date.now() + 3600000*24),
            options = {
                //domain: "." + /* grunt|env:server.host */"71xiaoxue.com"/* end */,
                path: '/',
                expires: expiresDate
            };

        //req.session.user = user;
        res.cookie('uid',user.id, options);
        res.cookie('skey', data.encodeKey, options);
        res.cookie('connect.sid', 'no value', options);

        decode(data.encodeKey, function(err, data) {
            console.log('decode', data);
            if (err) {
                return res.json({
                    err: 100001,
                    msg: err
                });
            }

            if (!data.success) {
                return res.json({
                    err: ERR.LOGIN_FAILURE,
                    msg: data.resultMsg
                });
            }
            data = data.userInfo;

            res.cookie('loginname',data.loginName,options);
            res.cookie('username',data.name,options);
            return res.redirect('/teacher_space.html');
            //console.log(data);
            // callback(null, res.statusCode, {
            //     uid: user.id,
            //     skey: user.skey,
            //     nick: data.name
            // });            
        });

    });
}
/*71 单点登陆　ｅｎｄ*/



/**
 * 拉取用户和组织信息，返回一个[{userID:nickName}, ...]的数组
 * @param encodeKey
 * @param callback
 */
function fetchOrganizationTree(encodeKey, callback){
    if(encodeKey){
        post(
            GET_ORGANIZATION_TREE,
            'encodeKey='+encodeKey,
            function(data, res){
                var json = JSON.parse(data),
                    result = {};
                _.each(json['departmentTree']['children'], function(department){
                    _.each(department['children'], function(item){
                        if(item['classes'] == 'user'){
                            var uid = item['loginName'],
                                nick = item['name'],
                                profile = {nick:nick};
                            result[uid] = profile;

                            //save the result in redis
                            redisClient.set(uid, JSON.stringify(profile));
                        }
                    });
                });
                callback(null, res.statusCode, result);
            },
            function(e){
                callback(e);
            }
        )
    }
    else callback(new Error('Invalid EncodeKey'));
}
//自動用我的帳號密碼登錄來拉取組織信息
function fetchOrganizationTreeEveryHour(){
    console.log('[OrganizationTree] logging in ...');
    login('tangqihong', '8888', function(error, status, result){
        if(!error && status == 200){
            console.log('[OrganizationTree] fetching organization tree ...');
            fetchOrganizationTree(result.skey, function(error, status, result){
                if(error || status !== 200) {
                    console.error('[OrganizationTree] FAIL TO FETCH', error, status);
                }
                else {
                    console.log('[OrganizationTree] update success');
                }
            });
        }
    });
}

function fetchOrganizationTreeFromAZ(skey, session, callback){
    if(skey && session){
        request(
            'GET',
            {
                host: UC_ORGANIZATION_TREE.host,
                path: UC_ORGANIZATION_TREE.path + '?skey=' + skey
            },
            '',
            null,
            function(responseText, res){
                callback(null, res.statusCode, JSON.parse(responseText));
            },
            function(error){
                callback(error, 200);
            }
        )
    }
    else callback(new Error('Invalid Keys'), 400);
}

//每小時自動同步一次組織信息
every('1h').do(function(){
    fetchOrganizationTreeEveryHour();
});
fetchOrganizationTreeEveryHour();


function getProfileOfUid(uid, callback){
    redisClient.get(uid, function(err, reply){
        var json = reply ? JSON.parse(reply) : null;
        callback(err, json);
    });
}
function getProfilesOfUids(uids, callback){
    if(!uids || !uids.length) callback(null, []);
    else{
        redisClient.mget(uids, function(err, reply){
            var parsedProfiles = _.reduce(_.object(uids, reply), function(profiles, item, uid){
                if(item) profiles[uid] = JSON.parse(item);
                return profiles;
            }, {});
            callback(err, parsedProfiles);
        });
    }
}

function post(api, body, onData, onError){
    var options = {
        hostname: api.host,
        path: api.path,
        port: 80,
        method: 'POST',
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded'
        }
    };
    var req = http.request(options, function(res) {
        var responseText = '';
        res.setEncoding('utf8');
        res.on('data', function(chunk){
            responseText += chunk;
        });
        res.on('end', function(){
            onData(responseText, res);
        });
    });
    req.on('error', onError);
    req.write(body);
    req.end();
}

function request(method, api, body, cookies, onData, onError){
    var cookieHeader = '';
    if(cookies){
        cookieHeader = _.map(cookies, function(value, key){
            return key + '=' + value;
        }).join('; ');
    }
    var hostAndPort = api.host.split(':'),
        options = {
            hostname: hostAndPort[0],
            path: api.path,
            port: parseInt(hostAndPort[1]) || 80,
            method: method,
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded',
                'Content-Length': body.length,
                'Cookie': cookieHeader
            }
        },
        req = http.request(options, function(res) {
            var responseText = '';
            res.setEncoding('utf8');
            res.on('data', function(chunk){
                responseText += chunk;
            });
            res.on('end', function(){
                console.log('[Response]', responseText);
                onData(responseText, res);
            });
        });
    console.log('[Request]', options);
    req.on('error', onError);
    req.write(body);
    req.end();
}

exports.api = {
    login:                      login,
    info : info,
    callback : callback,     
    verifyEncodeKey:            verifyEncodeKey,
    response401IfUnauthoirzed:  response401IfUnauthoirzed,
    fetchOrganizationTree:      fetchOrganizationTree,
    fetchOrganizationTreeFromAZ:fetchOrganizationTreeFromAZ,
    getProfileOfUid:            getProfileOfUid,
    getProfilesOfUids:          getProfilesOfUids
};

// var http = require('http'),
//     redis = require('redis'),
//     every = require('schedule').every,
//     _ = require('underscore')._;


// var redisClient = redis.createClient();


// var GET_ENCODE_KEY_API      = {host:'my.71xiaoxue.com',     path:'/authenticationUser.do'},
//     VERIFY_ENCODE_KEY_API   = {host:'my.71xiaoxue.com',     path:'/authenticationKey.do'},
//     GET_PROFILE_API         = {host:'mapp.71xiaoxue.com',   path:'/components/getUserInfo.htm'},
//     GET_ORGANIZATION_TREE   = {host:'mapp.71xiaoxue.com',   path:'/components/getOrgTree.htm'},
//     UC_ORGANIZATION_TREE    = {host:/* grunt|env:server.api.login.host */"szone.71xiaoxue.com"/* end */,  path:'/api/organization/tree'},
//     UC_LOGIN_API            = {host:/* grunt|env:server.api.login.host */"szone.71xiaoxue.com"/* end */,  path:'/api/user/login'},
//     UC_VERIFY_API           = {host:/* grunt|env:server.api.login.host */"szone.71xiaoxue.com"/* end */,  path:'/api/user/validate'};


// function login(username, password, callback){
//     console.log('[login] username=' + username + ', pwd=' + password);
//     if(username && password){
//         //小龙提供的用户中心登录接口
//         post(
//             UC_LOGIN_API,
//             'name=' + username + '&pwd=' + password + '&json=true',
//             null,
//             function(data, res){
//                 try {
//                     console.log('[login] json =', data);
//                     var json = JSON.parse(data);
//                     if(json && !json['err'] && json['result']){
//                         var cookies = res.headers['set-cookie'].join('; '),
//                             //从响应头里获取skey和sid
//                             mSkey = cookies.match(/skey=(.*?);/),
//                             mSession = cookies.match(/connect\.sid=(.*?);/),
//                             skey = (mSkey && mSkey[1]) ? mSkey[1] : '',
//                             session = (mSession && mSession[1]) ? mSession[1] : '',
//                             //从响应内容里读取昵称
//                             nick = json['result']['nick'],
//                             result = {
//                                 uid: username,
//                                 nick: nick,
//                                 skey: skey,
//                                 session: session
//                             };
//                         console.log('[login] result =', result);
//                         callback(null, res.statusCode, result);
//                     }
//                     else {
//                         console.error('[login] response error ' + res.statusCode);
//                         callback(null, res.statusCode);
//                     }
//                 }
//                 catch(e){
//                     console.error('[login] logic error', e);
//                     callback(e);
//                 }
//             },
//             function(e){
//                 console.error('[login] post error', e);
//                 callback(e);
//             }
//         );
//     }
//     else callback(new Error('Invaild username or password'));
// }


// /**
//  * verify encodeKey and returns the user info object
//  * @param req
//  * @param {Function} callback function(error:Error, statusCode:int, userInfo:Object)
//  */
// //function verifyEncodeKey(encodeKey, callback){
// function verifyKeys(req, callback){
//     var skey = req.cookies['skey'],
//         session = req.cookies['connect.sid'];
//     if(skey && session){
//         request(
//             'GET',
//             UC_VERIFY_API,
//             '',
//             {skey:skey, 'connect.sid':session},
//             function(data, res){
//                 try{
//                     var json = JSON.parse(data);
//                     callback(null, res.statusCode, json);
//                 }
//                 catch(e){
//                     callback(e);
//                 }
//             },
//             function(e){
//                 callback(e);
//             }
//         );
//     }
//     else callback(new Error('Invalid Keys'));
// }
// /**
//  * verify the request, if no vaild encodeKey response 401, otherwise callback with user info
//  * @param req
//  * @param res
//  * @param {Function} callback function(userInfo:Object)
//  */
// function response401IfUnauthoirzed(req, res, callback){
//     verifyKeys(req, function(error, statusCode, json){
//         console.log('error:', error);
//         console.log('status:', statusCode);
//         console.log('json:', json);
//         if (error || statusCode!=200 || !json || json['err']){
//             res.json(401, {c:10, m:'Unauthorized'});
//         }
//         else {
//             callback({
//                 loginName: json['result']['name']
//             });
//         }
//     });
// }


// *
//  * 拉取用户和组织信息，返回一个[{userID:nickName}, ...]的数组
//  * @param encodeKey
//  * @param callback
 
// function fetchOrganizationTree(encodeKey, callback){
//     if(encodeKey){
//         post(
//             GET_ORGANIZATION_TREE,
//             'encodeKey='+encodeKey,
//             null,
//             function(data, res){
//                 var json = JSON.parse(data),
//                     result = {};
//                 _.each(json['departmentTree']['children'], function(department){
//                     _.each(department['children'], function(item){
//                         if(item['classes'] == 'user'){
//                             var uid = item['loginName'],
//                                 nick = item['name'],
//                                 profile = {nick:nick};
//                             result[uid] = profile;

//                             //save the result in redis
//                             redisClient.set(uid, JSON.stringify(profile));
//                         }
//                     });
//                 });
//                 callback(null, res.statusCode, result);
//             },
//             function(e){
//                 callback(e);
//             }
//         )
//     }
//     else callback(new Error('Invalid EncodeKey'));
// }
// function fetchOrganizationTreeFromAZ(skey, session, callback){
//     if(skey && session){
//         request(
//             'GET',
//             UC_ORGANIZATION_TREE,
//             '',
//             {skey:skey, 'connect.sid':session},
//             function(responseText, res){
//                 callback(null, res.statusCode, JSON.parse(responseText));
//             },
//             function(error){
//                 callback(error, 200);
//             }
//         )
//     }
//     else callback(new Error('Invalid Keys'), 400);
// }
// //自動用我的帳號密碼登錄來拉取組織信息
// function fetchOrganizationTreeEveryHour(){
//     console.log('[OrganizationTree] logging in ...');
//     login('xzone_admin', '8888', function(error, status, result){
//         if(!error && status == 200){
//             console.log('[OrganizationTree] fetching organization tree ...');
//             fetchOrganizationTree(result.skey, function(error, status, result){
//                 if(error || status !== 200) {
//                     console.error('[OrganizationTree] FAIL TO FETCH', error, status);
//                 }
//                 else {
//                     console.log('[OrganizationTree] update success');
//                 }
//             });
//         }
//     });
// }
// //每小時自動同步一次組織信息
// every('1h').do(function(){
//     fetchOrganizationTreeEveryHour();
// });
// fetchOrganizationTreeEveryHour();


// function getProfileOfUid(uid, callback){
//     redisClient.get(uid, function(err, reply){
//         var json = reply ? JSON.parse(reply) : null;
//         callback(err, json);
//     });
// }
// function getProfilesOfUids(uids, callback){
//     if(!uids || !uids.length) callback(null, []);
//     else{
//         redisClient.mget(uids, function(err, reply){
//             var parsedProfiles = _.reduce(_.object(uids, reply), function(profiles, item, uid){
//                 if(item) profiles[uid] = JSON.parse(item);
//                 return profiles;
//             }, {});
//             callback(err, parsedProfiles);
//         });
//     }
// }

// function post(api, body, cookies, onData, onError){
//     request('POST', api, body, cookies, onData, onError);
// }
// function request(method, api, body, cookies, onData, onError){
//     var cookieHeader = '';
//     if(cookies){
//         cookieHeader = _.map(cookies, function(value, key){
//             return key + '=' + value;
//         }).join('; ');
//     }
//     var hostAndPort = api.host.split(':'),
//         options = {
//             hostname: hostAndPort[0],
//             path: api.path,
//             port: parseInt(hostAndPort[1]) || 80,
//             method: method,
//             headers: {
//                 'Content-Type': 'application/x-www-form-urlencoded',
//                 'Content-Length': body.length,
//                 'Cookie': cookieHeader
//             }
//         },
//         req = http.request(options, function(res) {
//             var responseText = '';
//             res.setEncoding('utf8');
//             res.on('data', function(chunk){
//                 responseText += chunk;
//             });
//             res.on('end', function(){
//                 onData(responseText, res);
//             });
//         });
//     req.on('error', onError);
//     req.write(body);
//     req.end();
// }


// exports.api = {
//     login:                          login,
//     response401IfUnauthoirzed:      response401IfUnauthoirzed,
//     fetchOrganizationTree:          fetchOrganizationTree,
//     fetchOrganizationTreeFromAZ:    fetchOrganizationTreeFromAZ,
//     getProfileOfUid:                getProfileOfUid,
//     getProfilesOfUids:              getProfilesOfUids
// };