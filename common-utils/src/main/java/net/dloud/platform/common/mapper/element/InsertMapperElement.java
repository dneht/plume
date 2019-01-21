package net.dloud.platform.common.mapper.element;

import static net.dloud.platform.common.mapper.element.MapperBuildUtil.INSERT;
import static net.dloud.platform.common.mapper.element.MapperBuildUtil.LEFT_BRACKET;
import static net.dloud.platform.common.mapper.element.MapperBuildUtil.MERGE;
import static net.dloud.platform.common.mapper.element.MapperBuildUtil.ON_DUPLICATE_UPDATE;
import static net.dloud.platform.common.mapper.element.MapperBuildUtil.RIGHT_BRACKET;
import static net.dloud.platform.common.mapper.element.MapperBuildUtil.VALUES;

/**
 * @author QuDasheng
 * @create 2018-09-20 09:56
 **/
public class InsertMapperElement implements BaseMapperElement {
    private StringBuilder sentence = new StringBuilder();


    public InsertMapperElement insert(TableMapperElement table, String fields, String values) {
        return insert(INSERT, table, fields, values);
    }

    public InsertMapperElement insertSelect(TableMapperElement table, String fields, BaseMapperElement select) {
        return insertSelect(INSERT, table, fields, select);
    }


    public InsertMapperElement upsert(TableMapperElement table, String fields, String values, String serts) {
        sentence.append(INSERT).append(table.build()).append(LEFT_BRACKET).append(fields).append(RIGHT_BRACKET)
                .append(VALUES).append(LEFT_BRACKET).append(values).append(RIGHT_BRACKET)
                .append(ON_DUPLICATE_UPDATE).append(serts);
        return this;
    }

    public InsertMapperElement upsertSelect(TableMapperElement table, String fields, BaseMapperElement select, String serts) {
        sentence.append(INSERT).append(table.build()).append(LEFT_BRACKET).append(fields).append(RIGHT_BRACKET)
                .append(LEFT_BRACKET).append(select.build()).append(RIGHT_BRACKET)
                .append(ON_DUPLICATE_UPDATE).append(serts);
        return this;
    }

    public InsertMapperElement merge(TableMapperElement table, String fields, String values) {
        return insert(MERGE, table, fields, values);
    }

    public InsertMapperElement mergeSelect(TableMapperElement table, String fields, BaseMapperElement select) {
        return insertSelect(MERGE, table, fields, select);
    }

    private InsertMapperElement insert(String opera, TableMapperElement table, String fields, String values) {
        sentence.append(opera).append(table.build()).append(LEFT_BRACKET).append(fields).append(RIGHT_BRACKET)
                .append(VALUES).append(LEFT_BRACKET).append(values).append(RIGHT_BRACKET);
        return this;
    }

    private InsertMapperElement insertSelect(String opera, TableMapperElement table, String fields, BaseMapperElement select) {
        sentence.append(opera).append(table.build()).append(LEFT_BRACKET).append(fields).append(RIGHT_BRACKET)
                .append(LEFT_BRACKET).append(select.build()).append(RIGHT_BRACKET);
        return this;
    }

    @Override
    public String build() {
        return sentence.toString();
    }
}
