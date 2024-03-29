/* QQ互聯 */
var http = require('http'),
    https = require('https'),
    _ = require('underscore')._;


var APP_ID = '101029853',
    APP_SECRET = 'ee6a36c9eb6f01fde2dbbd4170e5ddaa',
    OAUTH2_REDIRECT = 'http://media.71xiaoxue.com/qqconnect/redirect';


//localStorage
if (typeof localStorage === "undefined" || localStorage === null) {
    var LocalStorage = require('node-localstorage').LocalStorage;
    localStorage = new LocalStorage('./qqconnect');
}


/**
 * 用戶從QQ互聯登錄授權後，應調用此函數來獲取accessToken（相當於skey）和openid（相當於uid）
 * @param req
 * @param {Function} callback function(ret, tokens, profile)
 * TODO 爲了快速實現，先把tokens和userInfo都存到本地一個文件上
 */
function handleAuthorizationRedirect(req, callback){
    //取得authorization code換取access token
    var authorizationCode = req.query['code'];
    fetchAccessToken(authorizationCode, function(accessToken){
        if(accessToken && accessToken.length){
            //access token到手後，獲取openid
            fetchOpenID(accessToken, function(openID){
                if(openID && openID.length){
                    //openid也拿到後，調用api獲取用戶信息
                    fetchUserInfo(accessToken, openID, function(userInfo){
                        var tokens = {
                            accessToken: accessToken,
                            openID: openID
                        };
                        //存儲accessToken
                        localStorage.setItem('user.tokens.' + openID, JSON.stringify(tokens));
                        if(userInfo){
                            //用戶信息成功獲取後，將信息存儲起來
                            localStorage.setItem('user.profile.' + openID, JSON.stringify(userInfo));
                            callback(0, tokens, userInfo);
                        }
                        else callback(3, tokens);
                    });
                }
                else callback(2);
            });
        }
        else callback(1);
    });
}


function response401IfUnauthoirzed(req, res, callback){
    var accessToken = req.cookies['skey'],
        openID = req.cookies['uid'];
    //檢查cookie里是否有access token和openid
    if(accessToken && openID){
        //從localStorage里讀取tokens和profile記錄
        var storedTokens = localStorage.getItem('user.tokens.' + openID),
            storedUserInfo = localStorage.getItem('user.profile.' + openID);
        if(storedTokens && storedUserInfo){
            try{
                //如果和用戶提交請求里的cookie匹配，則認爲驗證通過
                var tokens = JSON.parse(storedTokens),
                    userInfo = JSON.parse(storedUserInfo);
                if(tokens.accessToken == accessToken){
                    callback({
                        name: userInfo.nickname,
                        loginName: openID
                    });
                    return;
                }
            }
            catch(e){

            }
        }
    }
    res.json(401, {c:10, m:'Unauthorized'});
}


function getProfileOfUid(uid, callback){
    var storedUserInfo = localStorage.getItem('user.profile.' + uid),
        userInfo = storedUserInfo ? JSON.parse(storedUserInfo) : null,
        profile = userInfo ? {nick:userInfo.nickname} : null;
    if(callback) callback(null, profile);
    else return profile;
}
function getProfilesOfUids(uids, callback){
    var profiles = _.reduce(uids, function(result, uid){
        var profile = getProfileOfUid(uid);
        if(profile){
            result[uid] = profile;
        }
        return result;
    }, {});
    if(callback) callback(null, profiles);
    else return profiles;
}


/**
 * 發一個GET請求並處理返回
 * @param {String} host
 * @param {String} path
 * @param {Object} query
 * @param {Boolean} [isHttps]
 * @param {Function} [onData]
 * @param {Function} [onError]
 */
function sendGetRequest(host, path, query, isHttps, onData, onError){
    path = path + '?' + _.reduce(query, function(result, value, key){
        result += ((result.length ? '&' : '') + key + '=' + encodeURIComponent(value));
        return result;
    }, '');
    var options = {
        hostname: host,
        path: path,
        port: isHttps ? 443 : 80,
        method: 'GET'
    };
    //console.log('[QQConnect] sending request to ' + path, options);
    var req = (isHttps ? https : http).request(options, function(res) {
        var responseText = '';
        res.setEncoding('utf8');
        res.on('data', function(chunk){
            responseText += chunk;
        });
        res.on('end', function(){
            //console.log('[QQConnect] response', responseText);
            onData(responseText, res);
        });
    });
    req.on('error', onError);
    req.end();
}


function fetchAccessToken(authCode, next){
    sendGetRequest(
        'graph.qq.com',
        '/oauth2.0/token',
        {
            grant_type:     'authorization_code',
            client_id:      APP_ID,
            client_secret:  APP_SECRET,
            code:           authCode,
            redirect_uri:   OAUTH2_REDIRECT
        },
        true,
        function(text, res){
            var result = _.reduce(text.split('&'), function(r, item){
                var kv = item.split('=');
                r[kv[0]] = kv[1];
                return r;
            }, {});
            next(result['access_token']);
        },
        function(){
            next();
        }
    );
}

function fetchOpenID(accessToken, next){
    sendGetRequest(
        'graph.qq.com',
        '/oauth2.0/me',
        {access_token: accessToken},
        true,
        function(text, res){
            var matches = text.match(/"openid":"(.*?)"/);
            if(matches && matches[1]){
                next(matches[1]);
            }
            else next();
        },
        function(){
            next();
        }
    )
}


function fetchUserInfo(accessToken, openID, next){
    sendGetRequest(
        'graph.qq.com',
        '/user/get_user_info',
        {
            oauth_consumer_key: APP_ID,
            access_token: accessToken,
            openid: openID
        },
        true,
        function(text, res){
            try{ next(JSON.parse(text)); }
            catch(e){ next(); }
        },
        function(){
            next();
        }
    )
}


exports.api = {
    constants: {
        APP_ID: APP_ID,
        APP_SECRET: APP_SECRET,
        OAUTH2_REDIRECT: OAUTH2_REDIRECT
    },
    handleAuthorizationRedirect:    handleAuthorizationRedirect,
    response401IfUnauthoirzed:      response401IfUnauthoirzed,
    getProfileOfUid:                getProfileOfUid,
    getProfilesOfUids:              getProfilesOfUids
};