angular.module('ts.directives.activityDate', [])
    /**
     * Activity Date Directive
     * scope.item.date:String => "x年x月x日"
     */
    .directive('activityDate', function(){
        function link(scope, element, attrs){
            var date = new Date(scope.item.activities[0].info.date);
            element.text(date.getFullYear() + '年' + (date.getMonth()+1) + '月' + date.getDate() + '日');
        }
        return {link:link};
    });