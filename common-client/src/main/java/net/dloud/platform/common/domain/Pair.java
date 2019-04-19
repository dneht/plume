package net.dloud.platform.common.domain;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author QuDasheng
 * @create 2018-09-27 17:34
 **/
@Data
@AllArgsConstructor
public class Pair<T, R> {
    private final T first;

    private final R last;
}
