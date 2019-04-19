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

    public WhereMapperElement and(String... ands) {
        if (notEmpty(ands)) {
            this.wheres.addAll(Arrays.asList(ands));
        }
        return this;
    }

    public WhereMapperElement and(List<String> ands) {
        if (notEmpty(ands)) {
            this.wheres.addAll(ands);
        }
        return this;
    }

    public WhereMapperElement force(String prefix) {
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
        final int length = wheres.size();
        if (length > 0) {
            if (sentence.indexOf(WHERE) > 0) {
                sentence.append(AND);
            } else {
                sentence.append(WHERE);
            }
            if (!force) {
                if (notEmpty(prefix)) {
                    sentence.append(prefix).append(".").append(AND_SOFT_DELETE).append(AND);
                } else {
                    sentence.append(AND_SOFT_DELETE).append(AND);
                }
            }

            stringJoin(this, sentence, length, AND, wheres);
        }
        return sentence.toString();
    }
}
