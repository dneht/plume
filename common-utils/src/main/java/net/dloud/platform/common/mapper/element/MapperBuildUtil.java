package net.dloud.platform.common.mapper.element;

import java.util.List;
import java.util.Objects;

/**
 * @author QuDasheng
 * @create 2018-09-18 16:31
 **/
public class MapperBuildUtil {
    final static String UNDER = "_";

    final static String COMMA = ", ";

    final static String LEFT_BRACKET = " (";

    final static String RIGHT_BRACKET = ") ";

    final static String SELECT = "select ";

    final static String INSERT = "insert into ";

    final static String MERGE = "merge into ";

    final static String UPDATE = "update ";

    final static String DELETE = "delete from ";

    final static String FROM = " from ";

    final static String WHERE = " where ";

    final static String VALUES = " values ";

    final static String AND = " and ";

    final static String SET = " set ";

    final static String GROUP_BY = " group by ";

    final static String HAVING = " having ";

    final static String ORDER_BY = " order by ";

    final static String ASC = " asc ";

    final static String DESC = " desc ";

    final static String LIMIT = " limit ";

    final static String UNION = " union ";

    final static String UNION_ALL = " union all ";

    final static String JOIN = " join ";

    final static String LEFT_JOIN = " left join ";

    final static String RIGHT_JOIN = " right join ";

    final static String INNER_JOIN = " inner join ";

    final static String ON = " on ";

    final static String ON_DUPLICATE_UPDATE = " on duplicate key update ";

    final static String SET_UPDATE_NOW = "updated_at = now()";

    final static String AND_SOFT_DELETE = "deleted_at is null";

    final static String SET_SOFT_DELETE = "deleted_at = now()";


    static boolean notEmpty(String input) {
        return Objects.nonNull(input) && !input.trim().isEmpty();
    }

    static boolean notEmpty(String[] input) {
        return Objects.nonNull(input) && input.length > 0;
    }

    static boolean notEmpty(List<String> input) {
        return Objects.nonNull(input) && !input.isEmpty();
    }

    static <T> T stringJoin(T input, StringBuilder sentence, String split, String... joins) {
        final int length = joins.length;
        return stringJoin(input, sentence, length, split, joins);
    }

    static <T> T stringJoin(T input, StringBuilder sentence, int length, String split, String... joins) {
        for (int i = 0; i < length; i++) {
            sentence.append(joins[i]);
            if (i < length - 1) {
                sentence.append(split);
            }
        }
        return input;
    }

    static <T> T stringJoin(T input, StringBuilder sentence, String split, List<String> joins) {
        final int length = joins.size();
        return stringJoin(input, sentence, length, split, joins);
    }

    static <T> T stringJoin(T input, StringBuilder sentence, int length, String split, List<String> joins) {
        for (int i = 0; i < length; i++) {
            sentence.append(joins.get(i));
            if (i < length - 1) {
                sentence.append(split);
            }
        }
        return input;
    }
}
