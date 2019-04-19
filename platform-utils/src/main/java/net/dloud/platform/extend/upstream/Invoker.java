package net.dloud.platform.extend.upstream;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author QuDasheng
 * @create 2019-04-17 10:11
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Invoker<T> {
    private String uri;

    private T target;

    private int weight = 0;

    public Invoker(String uri) {
        this.uri = uri;
    }
}
