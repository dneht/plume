package net.dloud.platform.extend.wrapper;

import net.dloud.platform.common.extend.CollectionUtil;
import net.dloud.platform.common.extend.StringUtil;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author QuDasheng
 * @create 2018-09-12 10:38
 **/
public class ValueWrapper {

    public static <T> T ifNull(T input, T give) {
        if (null == input) {
            return give;
        } else {
            return input;
        }
    }

    public static String ifEmpty(String input) {
        return ifEmpty(input, "");
    }

    public static String ifEmpty(String input, String give) {
        if (null == input) {
            return give;
        } else {
            return input;
        }
    }

    public static String ifBlank(String input, String give) {
        if (StringUtil.isBlank(input)) {
            return give;
        } else {
            return input;
        }
    }

    public static <T> List<T> ifEmpty(List<T> input) {
        return ifEmpty(input, Collections.emptyList());
    }

    public static <T> List<T> ifEmpty(List<T> input, List<T> give) {
        if (CollectionUtil.isEmpty(input)) {
            return give;
        } else {
            return input;
        }
    }

    public static <T, R> Map<T, R> ifEmpty(Map<T, R> input) {
        return ifEmpty(input, Collections.emptyMap());
    }

    public static <T, R> Map<T, R> ifEmpty(Map<T, R> input, Map<T, R> give) {
        if (CollectionUtil.isEmpty(input)) {
            return give;
        } else {
            return input;
        }
    }
}
