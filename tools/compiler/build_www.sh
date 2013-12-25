#!/bin/bash

JS_COMPILER=closure_compiler.jar
CSS_COMPILER=closure-stylesheets.jar
WWW_DIR=../../server/www

java -jar $JS_COMPILER --js $WWW_DIR/lib/bootstrap_datepicker_20130312/js/bootstrap-datepicker.js \
					   --js $WWW_DIR/js/teacherSpace.js \
 					   --js_output_file $WWW_DIR/js/teacherSpace.min.js

java -jar $CSS_COMPILER $WWW_DIR/css/common.css > $WWW_DIR/css/teacherSpace.min.css