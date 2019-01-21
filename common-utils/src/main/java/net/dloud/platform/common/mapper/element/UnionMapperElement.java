package net.dloud.platform.common.mapper.element;

import static net.dloud.platform.common.mapper.element.MapperBuildUtil.UNION;
import static net.dloud.platform.common.mapper.element.MapperBuildUtil.UNION_ALL;
import static net.dloud.platform.common.mapper.element.MapperBuildUtil.stringJoin;

/**
 * @author QuDasheng
 * @create 2018-09-20 09:56
 **/
public class UnionMapperElement implements BaseMapperElement {
    private StringBuilder sentence = new StringBuilder();

    private String[] selects;


    public UnionMapperElement union(SelectMapperElement... selects) {
        selectString(selects);
        stringJoin(this, sentence, UNION, this.selects);
        return this;
    }

    public UnionMapperElement unionAll(SelectMapperElement... selects) {
        selectString(selects);
        stringJoin(this, sentence, UNION_ALL, this.selects);
        return this;
    }

    private void selectString(SelectMapperElement... selects) {
        this.selects = new String[selects.length];
        for (int i = 0; i < selects.length; i++) {
            this.selects[i] = selects[i].build();
        }
    }

    @Override
    public String build() {
        return sentence.toString();
    }
}
