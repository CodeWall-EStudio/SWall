/**
 * 獲取並驗證當前用戶身份
 * @param req
 * @returns {String}
 */
function uid(req){
    //TODO 從cookie里取出sid和skey，驗證用戶身份
    return req.query['uid'];
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

exports.uid = uid;
exports.redirectToLoginIfUnauthorized = redirectToLoginIfUnauthorized;