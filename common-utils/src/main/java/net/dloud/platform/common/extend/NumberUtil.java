package net.dloud.platform.common.extend;

import lombok.extern.slf4j.Slf4j;

/**
 * @author QuDasheng
 * @create 2016-09-22 11:03
 */
@Slf4j
public class NumberUtil {

    /**
     * 转换为long
     *
     * @param obj
     * @return
     */
    public static int toInt(Integer obj) {
        return toInt(obj, 0);
    }

    public static int toInt(Integer obj, int def) {
        if (null == obj) {
            return def;
        }

        return obj;
    }

    public static int toInt(Object obj) {
        return toInt(obj, 0);
    }

    public static int toInt(Object obj, int def) {
        if (null == obj) {
            return def;
        }

        return objToInt(obj.toString(), def);
    }

    public static int toInt(String str) {
        return toInt(str, 0);
    }

    public static int toInt(String str, int def) {
        if (null == str) {
            return def;
        }

        return objToInt(str, def);
    }

    private static int objToInt(String input, int def) {
        try {
            return Integer.parseInt(input);
        } catch (Exception e) {
            log.warn("转换为数字失败: ", e);
        }

        return def;
    }

    /**
     * 转换为long
     *
     * @param obj
     * @return
     */
    public static long toLong(Long obj) {
        return toLong(obj, 0);
    }

    public static long toLong(Long obj, long def) {
        if (null == obj) {
            return def;
        }

        return obj;
    }

    public static long toLong(Object obj) {
        return toLong(obj, 0);
    }

    public static long toLong(Object obj, long def) {
        if (null == obj) {
            return def;
        }

        return objToLong(obj.toString(), def);
    }

    public static long toLong(String str) {
        return toLong(str, 0);
    }

    public static long toLong(String str, long def) {
        if (null == str) {
            return def;
        }

        return objToLong(str, def);
    }

    private static long objToLong(String input, long def) {
        try {
            return Long.parseLong(input);
        } catch (Exception e) {
            log.warn("转换为数字失败: ", e);
        }

        return def;
    }

    public static Integer gtZero(Integer input) {
        if (null == input || input <= 0) {
            return 0;
        }
        return input;
    }

    public static int gtZero(int input) {
        if (input <= 0) {
            return 0;
        }
        return input;
    }

    public static Long gtZero(Long input) {
        if (null == input || input <= 0) {
            return 0L;
        }
        return input;
    }

    private static long gtZero(long input) {
        if (input <= 0) {
            return 0;
        }
        return input;
    }
}
