## 0.7.2.0 (19.04.19)
1. 添加http动态路由
2. 继续优化dubbo缓存
3. 组件更新

## 0.7.1.0 (19.04.10)
1. 移除redisson及相关的组件
2. 优化dubbo缓存
3. k8s支持优化
4. 其它组件优化及bugfix

## 0.7.0.0 (19.03.15)
1. spring boot -> 2.1.3.RELEASE | dubbo -> 2.6.6
2. kafka添加all模式，可以发送给当前topic下的所有消费者
3. 网关中的dubbo reference失效时执行其destroy方法
4. 添加对于k8s的部分支持
5. 修改启动时的端口选择及常量
6. 修复本地网关缓存清理不及时造成的调用问题
7. 修复及优化MapperComponent
8. 其它结构调整及修改