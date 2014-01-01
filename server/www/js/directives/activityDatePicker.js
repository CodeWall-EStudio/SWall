angular.module('ts.directives.activityDatePicker', [])
    /**
     * Activity Date Picker Directive
     * 1.禁止选择今天以前的日期
     * 2.选择日期后更新scope[ngModel]=date
     */
    .directive('activityDatePicker', function(){
        function link(scope, element, attrs){
            var now = new Date();
            now.setHours(0);
            now.setMinutes(0);
            now.setSeconds(0);
            now.setMilliseconds(0);
            var datePicker = $(element).datepicker({
                onRender: function(date){
                    return date.getTime() < now.getTime() ? 'disabled' : '';
                }
            })
                .on('changeDate', function(e){
                    datePicker.hide();

                    //update model value
                    var modelName = attrs['ngDateModel'];
                    if(modelName) {
                        _.bind(function(event){
                            eval('this.' + modelName + '= event.date');
                        }, scope)(e);
                    }
                })
                .data('datepicker');
        }
        return {link:link};
    });