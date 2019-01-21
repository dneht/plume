package net.dloud.platform.common.mapper.element;

import static net.dloud.platform.common.mapper.element.MapperBuildUtil.UNDER;
import static net.dloud.platform.common.mapper.element.MapperBuildUtil.notEmpty;

/**
 * @author QuDasheng
 * @create 2018-09-20 09:56
 **/
public class TableMapperElement implements BaseMapperElement {
    private String table;
    private String extend;


    public TableMapperElement(String table, String extend) {
        this.table = table;
        this.extend = extend;
    }

    @Override
    public String build() {
        if (notEmpty(extend) && !UNDER.equals(extend) && !extend.endsWith("null")) {
            return table + extend;
        } else {
            return table;
        }
    }
}
