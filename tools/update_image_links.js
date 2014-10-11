var cursor = db['activity.data'].find();
while ( cursor.hasNext() ) {
    var doc = cursor.next();
    print("length = " + doc.resources.length);
    for(var i=0; i<doc.resources.length; ++i){
        var r = doc.resources[i];
        if(r.type == 1){
            r.type = 4;
            var matches = r.content.match(/fileId=(.*)/);
            if(matches && matches[1]){
                r.content = matches[1];
            }
            print('updated');
        }
        else{
            print(r.type);
        }
    }
    
    db['activity.data'].update({_id:doc._id}, {$set:{resources:doc.resources}});
}