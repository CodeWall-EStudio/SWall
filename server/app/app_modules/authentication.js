var http = require('http'),
    redis = require('redis'),
    every = require('schedule').every,
    _ = require('underscore')._;


var redisClient = redis.createClient();


var GET_ENCODE_KEY_API      = {host:'my.71xiaoxue.com', path:'/authenticationUser.do'},
    VERIFY_ENCODE_KEY_API   = {host:'my.71xiaoxue.com', path:'/authenticationKey.do'},
    GET_PROFILE_API         = {host:'mapp.71xiaoxue.com', path:'/components/getUserInfo.htm'},
    GET_ORGANIZATION_TREE   = {host:'mapp.71xiaoxue.com', path:'/components/getOrgTree.htm'};


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


exports.api = {
    login:                      login,
    verifyEncodeKey:            verifyEncodeKey,
    response401IfUnauthoirzed:  response401IfUnauthoirzed,
    fetchOrganizationTree:      fetchOrganizationTree,
    getProfileOfUid:            getProfileOfUid,
    getProfilesOfUids:          getProfilesOfUids
};