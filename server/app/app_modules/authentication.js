var http = require('http');


var GET_ENCODE_KEY_API      = {host:'my.71xiaoxue.com', path:'/authenticationUser.do'},
    VERIFY_ENCODE_KEY_API   = {host:'my.71xiaoxue.com', path:'/authenticationKey.do'},
    GET_PROFILE_API         = {host:'mapp.71xiaoxue.com', path:'/components/getUserInfo.htm'};


/**
 * verify encodeKey and returns the user info object
 * @param {String} encodeKey
 * @param {Function} callback function(error:Error, statusCode:int, userInfo:Object)
 */
function verifyEncodeKey(encodeKey, callback){
    var options = {
        hostname: GET_PROFILE_API.host,
        path: GET_PROFILE_API.path,
        port: 80,
        method: 'POST',
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded'
        }
    };
    var req = http.request(options, function(res) {
        res.setEncoding('utf8');
        res.on('data', function (chunk) {
            var json;
            try{
                json = JSON.parse(chunk);
                callback(null, res.statusCode, json['userInfo']);
            }
            catch(e){
                callback(e);
            }
        });
    });

    req.on('error', function(e) {
        callback(e);
    });

    req.write('encodeKey=' + encodeKey);
    req.end();
}


/**
 * verify the request, if no vaild encodeKey response 401, otherwise callback with user info
 * @param req
 * @param res
 * @param {Function} callback function(userInfo:Object)
 */
function response401IfUnauthoirzed(req, res, callback){
    var k = 'skey',
        //這裏是爲了方便調試，正式環境里應該只從cookie里讀
        encodeKey = req.cookies[k] || req.body[k] || req.query[k];

    verifyEncodeKey(encodeKey, function(error, statusCode, userInfo){
        if(error || statusCode!=200 || !userInfo || !userInfo['loginName']){
            res.json(401, {c:10, m:'Unauthoirzed'});
        }
        else {
            callback(userInfo);
        }
    });
}


exports.api = {
    verifyEncodeKey: verifyEncodeKey,
    response401IfUnauthoirzed: response401IfUnauthoirzed
};