package net.dloud.platform.extend.upstream;

import java.util.List;

/**
 * @author QuDasheng
 * @create 2019-04-17 10:10
 **/
public interface LoadBalance {
    /**
     * 使用这个方法来获取一个可用实例
     *
     * @param invokers
     * @param <T>
     * @return
     */
    default <T> Invoker<T> select(List<Invoker<T>> invokers) {
        if (invokers == null || invokers.isEmpty()) {
            return null;
        }
        if (invokers.size() == 1) {
            return invokers.get(0);
        }

        return doSelect(invokers);
    }

    /**
     * 获取代理对象的权重
     *
     * @param invoker
     * @return
     */
    default int getWeight(Invoker<?> invoker) {
        return invoker.getWeight();
    }

    /**
     * 需要实现的具体选择方法
     *
     * @param invokers
     * @param <T>
     * @return
     */
    <T> Invoker<T> doSelect(List<Invoker<T>> invokers);
}
