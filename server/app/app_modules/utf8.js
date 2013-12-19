function length(str){
    return unescape(encodeURIComponent(str)).length;
}

exports.length = length;