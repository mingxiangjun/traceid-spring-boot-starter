# traceid-spring-boot-starter
dubbo跨服务日志追踪starter
<pre>
  版本信息：dubbo 2.6.0 or 2.6.1
  springboot：1.x or 2.x（待测试，当前在1.5.9.RELEASE，2.0.2.RELEASE版本使用均没有问题）
</pre>
项目运行原理：
<pre>
  前置了解：
  1、ThreadContext
  2、aop切面
  3、springboot-starter
  4、dubbo ：RpcContext、Filter
  实现：
    通过aop切面类，方法前后进行增强操作。获取想要的内容，并将内容放入ThreadContext，在项目内部流转。在需要跨服务的时候，将需要传递的
  内容放入RpcContext中，随着dubbo服务一起传递到被调用的服务方。再通过springboot-starter将这个aop注入到目标系统的spring容器。这样一来
  就可以实现服务方提供服务的同时可以收集服务被调用的情况。
</pre>
