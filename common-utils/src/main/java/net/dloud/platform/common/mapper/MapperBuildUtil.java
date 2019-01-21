package net.dloud.platform.common.mapper;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import net.dloud.platform.common.annotation.Transient;
import net.dloud.platform.common.extend.StringUtil;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * @author QuDasheng
 * @create 2018-09-18 16:31
 **/
public class MapperBuildUtil {
    final static String EQUAL = " = ";

    final static String BLANK = " ";

    final static String COMMA = ", ";

    final static String LEFT_BRACKET = " (";

    final static String RIGHT_BRACKET = ") ";

    final static String ON_DUPLICATE_VALUE = "values(";

    final static int FIELD = 1;

    final static int PARAM = 2;

    final static int RENEW = 3;

    final static int COLUMN = 4;

    final static int VALUE = 5;


    static Cache<String, List<MapperFieldInfo>> FIELD_CACHE = Caffeine.newBuilder()
            .maximumSize(10_000)
            .expireAfterWrite(5, TimeUnit.MINUTES)
            .build();


    static List<MapperFieldInfo> fieldCache(Class<?> clazz) {
        return fieldCache(clazz, true);
    }

    static List<MapperFieldInfo> fieldCache(Class<?> clazz, boolean useSuper) {
        String name = clazz.getName();
        if (!useSuper) {
            name += "@";
        }
        List<MapperFieldInfo> present = FIELD_CACHE.getIfPresent(name);
        if (null == present) {
            present = new ArrayList<>();
            for (Field field : clazz.getDeclaredFields()) {
                if (!checkTransient(field)) {
                    final String fieldName = field.getName();
                    present.add(new MapperFieldInfo(fieldName, ":" + fieldName, StringUtil.camel2UnderLower(fieldName)));
                }
            }
            if (useSuper) {
                //遍历父类
                checkSuper(clazz, present);
            }
            FIELD_CACHE.put(name, present);
        }
        return present;
    }

    static void checkSuper(Class<?> clazz, List<MapperFieldInfo> present) {
        final Class<?> superclass = clazz.getSuperclass();
        if (null != superclass) {
            if (null != superclass.getSuperclass()) {
                checkSuper(clazz.getSuperclass(), present);
            }

            for (Field field : superclass.getDeclaredFields()) {
                if (!checkTransient(field)) {
                    final String fieldName = field.getName();
                    present.add(new MapperFieldInfo(fieldName, ":" + fieldName, StringUtil.camel2UnderLower(fieldName)));
                }
            }
        }
    }

    static boolean checkTransient(Field field) {
        return field.isAnnotationPresent(Transient.class);
    }

    static String listString(List<MapperFieldInfo> list, int type, Set<String> set, int only) {
        if (null != set && !set.isEmpty()) {
            list = new ArrayList<>(list);
            for (Iterator<MapperFieldInfo> it = list.iterator(); it.hasNext();) {
                final MapperFieldInfo info = it.next();
                final String fieldName = info.getFieldName();

                if (only == 1) {
                    if (!set.contains(fieldName)) {
                        it.remove();
                    }
                } else if (only == 2) {
                    if (set.contains(fieldName)) {
                        it.remove();
                    }
                }
            }
        }

        int size = list.size();
        final StringBuilder builder = new StringBuilder();
        for (int i = 0; i < size; i++) {
            final MapperFieldInfo info = list.get(i);
            final String fieldName = info.getFieldName();
            final String columnName = info.getColumnName();
            final String paramName = info.getParamName();

            switch (type) {
                case COLUMN:
                    builder.append(columnName);
                    break;
                case PARAM:
                    builder.append(paramName);
                    break;
                case RENEW:
                    builder.append(columnName).append(EQUAL).append(paramName);
                    break;
                case VALUE:
                    builder.append(columnName).append(EQUAL).append(ON_DUPLICATE_VALUE).append(columnName).append(RIGHT_BRACKET);
                    break;
                default:
                    builder.append(fieldName);
                    break;
            }
            if (i < size - 1) {
                builder.append(COMMA);
            }
        }
        return builder.toString();
    }
}
