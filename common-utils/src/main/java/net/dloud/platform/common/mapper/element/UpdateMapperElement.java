package net.dloud.platform.common.mapper.element;

import static net.dloud.platform.common.mapper.element.MapperBuildUtil.COMMA;
import static net.dloud.platform.common.mapper.element.MapperBuildUtil.DELETE;
import static net.dloud.platform.common.mapper.element.MapperBuildUtil.SET;
import static net.dloud.platform.common.mapper.element.MapperBuildUtil.SET_SOFT_DELETE;
import static net.dloud.platform.common.mapper.element.MapperBuildUtil.UPDATE;
import static net.dloud.platform.common.mapper.element.MapperBuildUtil.notEmpty;
import static net.dloud.platform.common.mapper.element.MapperBuildUtil.stringJoin;

/**
 * @author QuDasheng
 * @create 2018-09-20 09:56
 **/
public class UpdateMapperElement implements BaseMapperElement {
    private StringBuilder sentence = new StringBuilder();

    private boolean force;
    private String[] wheres;


    public UpdateMapperElement() {
        this.force = false;
    }

    public UpdateMapperElement(boolean force) {
        this.force = force;
    }

    public UpdateMapperElement force() {
        this.force = true;
        return this;
    }

    public UpdateMapperElement update(TableMapperElement table, String... values) {
        final int length = values.length;
        sentence.append(UPDATE).append(table.build()).append(SET);
        return stringJoin(this, sentence, length, COMMA, values);
    }

    public UpdateMapperElement softDelete(TableMapperElement table) {
        return update(table, SET_SOFT_DELETE);
    }

    public UpdateMapperElement delete(TableMapperElement table) {
        sentence.append(DELETE).append(table.build());
        return this;
    }

    public UpdateMapperElement where(String... wheres) {
        this.wheres = wheres;
        return this;
    }

    @Override
    public String build() {
        if (notEmpty(wheres)) {
            sentence.append(new WhereMapperElement(force).where(wheres).build());
        }
        return sentence.toString();
    }
}
