var http = require('http');


var GET_ENCODE_KEY_API      = {host:'my.71xiaoxue.com', path:'/authenticationUser.do'},
    VERIFY_ENCODE_KEY_API   = {host:'my.71xiaoxue.com', path:'/authenticationKey.do'},
    GET_PROFILE_API         = {host:'mapp.71xiaoxue.com', path:'/components/getUserInfo.htm'};


function login(username, password, callback){
    if(username && password){
        post(
            GET_ENCODE_KEY_API,
            'loginName=' + username + '&password=' + password,
            function(data, res){
                try{
                    var json = JSON.parse(data),
                        nick, encodeKey;
                    if(json && json['success'] && json['resultObject']){
                        nick = json['resultObject']['userName'];
                        encodeKey = json['resultObject']['encodeKey'];
                    }
                    callback(null, res.statusCode, encodeKey ? {nick:nick, skey:encodeKey} : null);
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
    var k = 'skey',
        encodeKey = req.cookies[k];

    verifyEncodeKey(encodeKey, function(error, statusCode, userInfo){
        if(error || statusCode!=200 || !userInfo || !userInfo['loginName']){
            res.json(401, {c:10, m:'Unauthoirzed'});
        }
        else {
            callback(userInfo);
        }
    });
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
        res.setEncoding('utf8');
        res.on('data', function(chunk){
            onData(chunk, res);
        });
    });
    req.on('error', onError);
    req.write(body);
    req.end();
}


exports.api = {
    login: login,
    verifyEncodeKey: verifyEncodeKey,
    response401IfUnauthoirzed: response401IfUnauthoirzed
};