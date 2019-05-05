package net.dloud.platform.common.mapper.element;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static net.dloud.platform.common.mapper.element.MapperBuildUtil.AND;
import static net.dloud.platform.common.mapper.element.MapperBuildUtil.AND_SOFT_DELETE;
import static net.dloud.platform.common.mapper.element.MapperBuildUtil.WHERE;
import static net.dloud.platform.common.mapper.element.MapperBuildUtil.notEmpty;
import static net.dloud.platform.common.mapper.element.MapperBuildUtil.stringJoin;

/**
 * @author QuDasheng
 * @create 2018-09-20 09:56
 **/
public class WhereMapperElement implements BaseMapperElement {
    private StringBuilder sentence = new StringBuilder();

    private boolean force;
    private String prefix;
    private List<String> wheres;

    public WhereMapperElement(boolean force) {
        this.force = force;
    }

    public WhereMapperElement where(String... wheres) {
        if (notEmpty(wheres)) {
            if (null == this.wheres) {
                this.wheres = new ArrayList<>();
            }
            this.wheres.addAll(Arrays.asList(wheres));
        }
        return this;
    }

    public WhereMapperElement and(String and) {
        if (notEmpty(and)) {
            if (null == this.wheres) {
                this.wheres = new ArrayList<>();
            }
            this.wheres.add(and);
        }
        return this;
    }

    public WhereMapperElement and(List<String> ands) {
        if (notEmpty(ands)) {
            if (null == this.wheres) {
                this.wheres = new ArrayList<>();
            }
            this.wheres.addAll(ands);
        }
        return this;
    }

    public WhereMapperElement prefix(String prefix) {
        if (notEmpty(prefix)) {
            this.prefix = prefix;
        }
        return this;
    }

    public boolean isEmpty() {
        return !notEmpty(wheres);
    }

    @Override
    public String build() {
        if (notEmpty(wheres)) {
            stringJoin(this, handle(), wheres.size(), AND, wheres);
        }
        if (!force) {
            if (notEmpty(prefix)) {
                handle().append(prefix).append(".").append(AND_SOFT_DELETE);
            } else {
                handle().append(AND_SOFT_DELETE);
            }
        }
        return sentence.toString();
    }

    private StringBuilder handle() {
        if (sentence.indexOf(WHERE) >= 0) {
            sentence.append(AND);
        } else {
            sentence.append(WHERE);
        }
        return sentence;
    }
}
