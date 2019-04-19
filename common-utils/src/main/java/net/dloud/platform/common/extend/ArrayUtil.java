package net.dloud.platform.common.extend;

import net.dloud.platform.common.domain.Pair;

/**
 * @author QuDasheng
 * @create 2019-04-03 15:06
 **/
public class ArrayUtil {
    public static byte[] concat(byte[][] arrays) {
        int size = 0;
        for (int i = 0; i != arrays.length; i++) {
            size += arrays[i].length;
        }

        byte[] rv = new byte[size];

        int offSet = 0;
        for (int i = 0; i != arrays.length; i++) {
            System.arraycopy(arrays[i], 0, rv, offSet, arrays[i].length);
            offSet += arrays[i].length;
        }
        return rv;
    }

    public static Pair<int[], byte[]> concatAndOffset(byte[][] arrays) {
        int size = 0;
        for (int i = 0; i != arrays.length; i++) {
            size += arrays[i].length;
        }

        byte[] rv = new byte[size];
        int[] os = new int[arrays.length];

        int offSet = 0;
        for (int i = 0; i != arrays.length; i++) {
            System.arraycopy(arrays[i], 0, rv, offSet, arrays[i].length);
            offSet += arrays[i].length;
            os[i] = offSet;
        }
        return new Pair<>(os, rv);
    }
}
