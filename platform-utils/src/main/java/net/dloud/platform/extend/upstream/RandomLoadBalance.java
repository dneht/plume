package net.dloud.platform.extend.upstream;

import java.util.List;
import java.util.Random;

/**
 * @author QuDasheng
 * @create 2019-04-17 10:09
 **/
public class RandomLoadBalance implements LoadBalance {
    private final Random random = new Random();


    @Override
    public <T> Invoker<T> doSelect(List<Invoker<T>> invokers) {
        int length = invokers.size();
        int totalWeight = 0;
        boolean sameWeight = true;
        for (int i = 0; i < length; i++) {
            int weight = getWeight(invokers.get(i));
            totalWeight += weight;
            if (sameWeight && i > 0
                    && weight != getWeight(invokers.get(i - 1))) {
                sameWeight = false;
            }
        }
        if (totalWeight > 0 && !sameWeight) {
            int offset = random.nextInt(totalWeight);
            for (int i = 0; i < length; i++) {
                offset -= getWeight(invokers.get(i));
                if (offset < 0) {
                    return invokers.get(i);
                }
            }
        }
        return invokers.get(random.nextInt(length));
    }
}
