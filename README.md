# radar
本项目是hooker的radar.dex增强功能

使用jdk1.8， 在linux环境下执行sh makeDex4linux.sh，生成xradar.jar和radar.dex。

xradar.jar 可用于你的爬虫注入工程

radar.dex用于替换hooker根目录下的radar.dex

https://github.com/jacky1234/hooker

感谢支持！！！


## 注意点
1、请使用Jdk1.8编译、否则 Java.openClassFile(dexPath).load(); 可能加载失败
2、尽量使用更加原始的特性。 如不要使用lambda、for each特性等等
3、内部类的使用： 内部累的使用需要注意下，jvm做一些优化，比如内部类的私有方法可以被外部类直接访问。 所以为了避免不必要的麻烦，尽量避免使用内部类