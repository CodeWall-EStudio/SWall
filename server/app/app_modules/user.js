/**
 * 獲取並驗證當前用戶身份
 * @param req
 * @returns {String}
 */
function uid(req){
    //TODO 從cookie里取出sid和skey，驗證用戶身份
    var uid = req.cookies['uid'],
        skey = req.cookies['skey'];
    if(uid && skey /*TODO && verify(uid, skey)*/) return uid;
    return null;
}

function login(req, res){
    var uid = req.params['uid'],
        password = req.body['pwd'];
    console.log(uid, password);
    if(uid && password /*TODO && verify(uid, password)*/){
        var skey = generateSkey(),
            expiresDate = new Date(Date.now() + 3600000),
            options = {/*domain:'localhost:8080', */path:'/', expires:expiresDate};
        res.cookie('uid', uid, options);
        res.cookie('skey', skey, options);
        return true;
    }
    return false;
}

/**
 * 检查是否已登录，若未登录则返回401
 * @param req
 * @param res
 * @return {boolean}
 */
function response401IfUnauthoirzed(req, res){
    if(!uid(req)){
        res.json(401, {c:10, m:'Unauthoirzed'});
        return true;
    }
    return false;
}

/**
 * 檢查是否已登錄，若未登錄則跳轉到登錄頁面
 * @param req
 * @param res
 * @param {String} [path]
 * @returns {boolean}
 */
function redirectToLoginIfUnauthorized(req, res, path){
    if(!uid(req)){
        res.redirect(path || '/login');
        return true;
    }
    return false;
}

function generateSkey(){
    return Math.random().toString(36).substring(2,8)
          +Math.random().toString(36).substring(9,15);
}

exports.user = {
    uid: uid,
    login: login,
    response401IfUnauthoirzed: response401IfUnauthoirzed,
    redirectToLoginIfUnauthorized: redirectToLoginIfUnauthorized
};