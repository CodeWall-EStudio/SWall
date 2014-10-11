#事前准备
1. 安装Node
2. 安装依赖包：npm install

#如何初始化服务器
在服务器上执行 setup.sh 脚本来安装NodeJS、MongoDB、Redis，可以通过参数来禁止安装部分组件，具体见 ./setup.sh -h

#如何编译项目（本地调试使用）
grunt

#如何发布项目
1. 如果是已有的环境（现在支持阿里云体验环境、延庆二小、永定二小这几个环境），直接跳到第三步；
2. 如果是新环境，则打开Gruntfile.js，添加新的环境变量；
3. 执行Grunt命令发布代码到指定环境：grunt deploy --env Env
