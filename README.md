# radar
本项目是hooker的radar.dex增强功能

使用jdk1.8， 在linux环境下执行sh makeDex4linux.sh，生成xradar.jar和radar.dex。

xradar.jar 可用于你的爬虫注入工程

radar.dex用于替换hooker根目录下的radar.dex

https://github.com/jacky1234/hooker

感谢支持！！！


## 注意点
一、请使用Jdk1.8编译、否则 Java.openClassFile(dexPath).load(); 可能加载失败