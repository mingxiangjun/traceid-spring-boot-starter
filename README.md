# traceid-spring-boot-starter

## 当前版本信息：
    最新版本为：1.0.6
    maven引入：
    <dependency>
        <groupId>org.ming.dubbo.traceid</groupId>
        <artifactId>traceid-spring-boot-starter</artifactId>
        <version>1.0.6</version>
    </dependency>
##版本升级说明：
<pre>
    升级原因：日志格式规范改变，对应新版本日志规范格式。
    升级内容：
        1、修改原有日志格式，新增filter
        2、区分Controller以及Service方法的日志打印
        3、新增filter，用来展示Controller的url+arguments或Dubbo服务appName+arguments
        4、修改如果执行方法抛出异常后，会对插件生成的requestId产生影响（异常导致上一次的requestId没有被清除）
</pre>
# 项目使用方式
* 使用步骤：<br/>
<pre>
    1、添加依赖：版本号请根据实际情况使用
        &lt;dependency&gt;
            &lt;groupId&gt;org.ming.dubbo.traceid&lt;/groupId&gt;
            &lt;artifactId&gt;traceid-spring-boot-starter&lt;/artifactId&gt;
            &lt;version&gt;1.0.6&lt;/version&gt;
        &lt;/dependency&gt;
    
    2、日志文件添加配置信息：
        推荐日志格式：
        &lt;Console name="scmConsole" target="SYSTEM_OUT"&gt;
            &lt;PatternLayout pattern="%d{yyyy-MM-dd'T'HH:mm:ss.SSSZ} [%level] xinche/${projectName} -%X{filter} &lt;%t|%C{1.}.%M(%L)&gt; -%X{requestId} %m%n%ex/&gt;
        &lt;/Console&gt;
        日志格式说明：
            格式：date logLevel projectName filter module requestId msg
            date：%d{yyyy-MM-dd'T'HH:mm:ss.SSSZ} log4j日志格式
            logLevel： trace、info、debug、info、warn、error
            projectName：各自项目名（部门/项目名）
            filter：组件生成内容（controller对应url+arguments，service对应appName+arguments）
            module：日志打印操作发生所在的thread+class+method+line
            requestId：请求唯一标识
            msg：用户自定义打印消息内容
    3、添加配置信息：
        dubbo.trace.open：true/false ## default is false（设置是否开启组件requestId生成收集功能）
        dubbo.trace.printLog：true/false ## defaultis false（设置是否开启组件打印调用链信息）
</pre>

# 后续开发计划：
##短期开发计划
* 增加包名设置：定制组件日志追踪范围
* 增加注解：单独用于某个方法或类追踪，定制日志追踪范围。

##长期开发计划
* 日志拆分功能，单独提取日志文件，离线统计接口耗时，接口调用链深度
* 小目标：APM监控工具

   
    
