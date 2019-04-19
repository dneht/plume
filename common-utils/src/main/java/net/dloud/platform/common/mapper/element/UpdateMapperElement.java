package net.dloud.platform.common.mapper.element;

import java.util.ArrayList;
import java.util.List;

import static net.dloud.platform.common.mapper.element.MapperBuildUtil.COMMA;
import static net.dloud.platform.common.mapper.element.MapperBuildUtil.DELETE;
import static net.dloud.platform.common.mapper.element.MapperBuildUtil.SET;
import static net.dloud.platform.common.mapper.element.MapperBuildUtil.SET_SOFT_DELETE;
import static net.dloud.platform.common.mapper.element.MapperBuildUtil.SET_UPDATE_NOW;
import static net.dloud.platform.common.mapper.element.MapperBuildUtil.UPDATE;
import static net.dloud.platform.common.mapper.element.MapperBuildUtil.notEmpty;
import static net.dloud.platform.common.mapper.element.MapperBuildUtil.stringJoin;

/**
 * @author QuDasheng
 * @create 2018-09-20 09:56
 **/
public class UpdateMapperElement implements BaseMapperElement {
    private StringBuilder sentence = new StringBuilder();
    private List<String> updates = new ArrayList<>();

    private boolean force;
    private String prefix;
    private String[] wheres;
    private List<String> ands;
    private String[] values;


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

    public UpdateMapperElement soft(String prefix) {
        this.prefix = prefix;
        return this;
    }

    public UpdateMapperElement update(TableMapperElement table) {
        sentence.append(UPDATE).append(table.build()).append(SET);
        return this;
    }

    public UpdateMapperElement update(TableMapperElement table, String... values) {
        this.values = values;
        sentence.append(UPDATE).append(table.build()).append(SET);
        return this;
    }

    public UpdateMapperElement softDelete(TableMapperElement table) {
        return update(table, SET_SOFT_DELETE);
    }

    public UpdateMapperElement delete(TableMapperElement table) {
        this.force = true;
        sentence.append(DELETE).append(table.build());
        return this;
    }

    public UpdateMapperElement add(boolean cond, String set) {
        if (cond) {
            updates.add(set);
        }
        return this;
    }


    public UpdateMapperElement where(String... wheres) {
        this.wheres = wheres;
        return this;
    }

    public UpdateMapperElement and(String andSome) {
        if (null == this.ands) {
            this.ands = new ArrayList<>();
        }
        this.ands.add(andSome);
        return this;
    }

    @Override
    public String build() {
        if (!force) {
            updates.add(SET_UPDATE_NOW);
        }
        boolean empty = true;
        if (notEmpty(updates)) {
            empty = false;
            sentence.append(String.join(COMMA, updates));
        }
        if (notEmpty(values)) {
            if (!empty) {
                sentence.append(COMMA);
            }
            stringJoin(this, sentence, values.length, COMMA, values);
        }

        if (notEmpty(wheres)) {
            sentence.append(new WhereMapperElement(force).where(wheres).and(ands).force(prefix).build());
        }
        return sentence.toString();
    }
}
