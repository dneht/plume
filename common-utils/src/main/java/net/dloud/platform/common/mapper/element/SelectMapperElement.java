package net.dloud.platform.common.mapper.element;

import java.util.ArrayList;
import java.util.List;

import static net.dloud.platform.common.mapper.element.MapperBuildUtil.AND;
import static net.dloud.platform.common.mapper.element.MapperBuildUtil.ASC;
import static net.dloud.platform.common.mapper.element.MapperBuildUtil.COMMA;
import static net.dloud.platform.common.mapper.element.MapperBuildUtil.DESC;
import static net.dloud.platform.common.mapper.element.MapperBuildUtil.FROM;
import static net.dloud.platform.common.mapper.element.MapperBuildUtil.GROUP_BY;
import static net.dloud.platform.common.mapper.element.MapperBuildUtil.HAVING;
import static net.dloud.platform.common.mapper.element.MapperBuildUtil.INNER_JOIN;
import static net.dloud.platform.common.mapper.element.MapperBuildUtil.LEFT_BRACKET;
import static net.dloud.platform.common.mapper.element.MapperBuildUtil.LEFT_JOIN;
import static net.dloud.platform.common.mapper.element.MapperBuildUtil.LIMIT;
import static net.dloud.platform.common.mapper.element.MapperBuildUtil.ON;
import static net.dloud.platform.common.mapper.element.MapperBuildUtil.ORDER_BY;
import static net.dloud.platform.common.mapper.element.MapperBuildUtil.RIGHT_BRACKET;
import static net.dloud.platform.common.mapper.element.MapperBuildUtil.RIGHT_JOIN;
import static net.dloud.platform.common.mapper.element.MapperBuildUtil.SELECT;
import static net.dloud.platform.common.mapper.element.MapperBuildUtil.notEmpty;
import static net.dloud.platform.common.mapper.element.MapperBuildUtil.stringJoin;

/**
 * @author QuDasheng
 * @create 2018-09-20 09:56
 **/
public class SelectMapperElement implements BaseMapperElement {
    private StringBuilder sentence = new StringBuilder();

    private boolean force;
    private String prefix;
    private String[] wheres;
    private List<String> ands;
    private List<String> whereSubs;
    private String[] groups;
    private String[] having;
    private String order;
    private String limit;


    public SelectMapperElement() {
        this.force = false;
    }

    public SelectMapperElement(boolean force) {
        this.force = force;
    }

    public SelectMapperElement force() {
        this.force = true;
        return this;
    }

    public SelectMapperElement soft(String prefix) {
        this.prefix = prefix;
        return this;
    }

    public SelectMapperElement select(TableMapperElement table) {
        sentence.append(SELECT).append("*").append(FROM).append(table.build());
        return this;
    }

    public SelectMapperElement select(TableMapperElement table, String fields) {
        sentence.append(SELECT).append(fields).append(FROM).append(table.build());
        return this;
    }

    public SelectMapperElement select(TableMapperElement table, String... fields) {
        sentence.append(SELECT);
        stringJoin(this, sentence, COMMA, fields);
        sentence.append(FROM).append(table.build());
        return this;
    }

    public SelectMapperElement choose(String fields) {
        sentence.append(SELECT).append(fields);
        return this;
    }

    public SelectMapperElement from(TableMapperElement table) {
        sentence.append(FROM).append(table.build());
        return this;
    }

    public SelectMapperElement fromSub(String asTable, BaseMapperElement subSelect) {
        sentence.append(FROM).append(LEFT_BRACKET).append(subSelect.build()).append(RIGHT_BRACKET).append(asTable);
        return this;
    }

    public SelectMapperElement innerJoin(TableMapperElement joinTable) {
        sentence.append(INNER_JOIN).append(joinTable.build());
        return this;
    }

    public SelectMapperElement innerJoin(String asTable, TableMapperElement joinTable) {
        sentence.append(INNER_JOIN).append(joinTable.build()).append(" ").append(asTable);
        return this;
    }

    public SelectMapperElement innerJoin(String asTable, TableMapperElement joinTable, String joinOn) {
        sentence.append(INNER_JOIN).append(joinTable.build()).append(" ").append(asTable).append(ON).append(joinOn);
        return this;
    }

    public SelectMapperElement innerJoin(BaseMapperElement joinSelect) {
        sentence.append(INNER_JOIN).append(LEFT_BRACKET).append(joinSelect.build()).append(RIGHT_BRACKET);
        return this;
    }

    public SelectMapperElement innerJoin(String asTable, BaseMapperElement joinSelect) {
        sentence.append(INNER_JOIN).append(LEFT_BRACKET).append(joinSelect.build()).append(RIGHT_BRACKET).append(asTable);
        return this;
    }

    public SelectMapperElement innerJoin(String asTable, BaseMapperElement joinSelect, String joinOn) {
        sentence.append(INNER_JOIN).append(LEFT_BRACKET).append(joinSelect.build()).append(RIGHT_BRACKET).append(asTable).append(ON).append(joinOn);
        return this;
    }

    public SelectMapperElement leftJoin(TableMapperElement joinTable) {
        sentence.append(LEFT_JOIN).append(joinTable.build());
        return this;
    }

    public SelectMapperElement leftJoin(String asTable, TableMapperElement joinTable) {
        sentence.append(LEFT_JOIN).append(joinTable.build()).append(" ").append(asTable);
        return this;
    }

    public SelectMapperElement leftJoin(String asTable, TableMapperElement joinTable, String joinOn) {
        sentence.append(LEFT_JOIN).append(joinTable.build()).append(" ").append(asTable).append(ON).append(joinOn);
        return this;
    }

    public SelectMapperElement leftJoin(BaseMapperElement joinSelect) {
        sentence.append(LEFT_JOIN).append(LEFT_BRACKET).append(joinSelect.build()).append(RIGHT_BRACKET);
        return this;
    }

    public SelectMapperElement leftJoin(String asTable, BaseMapperElement joinSelect) {
        sentence.append(LEFT_JOIN).append(LEFT_BRACKET).append(joinSelect.build()).append(RIGHT_BRACKET).append(asTable);
        return this;
    }

    public SelectMapperElement leftJoin(String asTable, BaseMapperElement joinSelect, String joinOn) {
        sentence.append(LEFT_JOIN).append(LEFT_BRACKET).append(joinSelect.build()).append(RIGHT_BRACKET).append(asTable).append(ON).append(joinOn);
        return this;
    }

    public SelectMapperElement rightJoin(TableMapperElement joinTable) {
        sentence.append(RIGHT_JOIN).append(joinTable.build());
        return this;
    }

    public SelectMapperElement rightJoin(String asTable, TableMapperElement joinTable) {
        sentence.append(RIGHT_JOIN).append(joinTable.build()).append(" ").append(asTable);
        return this;
    }

    public SelectMapperElement rightJoin(String asTable, TableMapperElement joinTable, String joinOn) {
        sentence.append(RIGHT_JOIN).append(joinTable.build()).append(" ").append(asTable).append(ON).append(joinOn);
        return this;
    }

    public SelectMapperElement rightJoin(BaseMapperElement joinSelect) {
        sentence.append(RIGHT_JOIN).append(LEFT_BRACKET).append(joinSelect.build()).append(RIGHT_BRACKET);
        return this;
    }

    public SelectMapperElement rightJoin(String asTable, BaseMapperElement joinSelect) {
        sentence.append(RIGHT_JOIN).append(LEFT_BRACKET).append(joinSelect.build()).append(RIGHT_BRACKET).append(asTable);
        return this;
    }

    public SelectMapperElement rightJoin(String asTable, BaseMapperElement joinSelect, String joinOn) {
        sentence.append(RIGHT_JOIN).append(LEFT_BRACKET).append(joinSelect.build()).append(RIGHT_BRACKET).append(asTable).append(ON).append(joinOn);
        return this;
    }

    public SelectMapperElement on(String joinOn) {
        sentence.append(ON).append(joinOn);
        return this;
    }

    public SelectMapperElement as(String asTable) {
        sentence.append(" ").append(asTable);
        return this;
    }

    public SelectMapperElement where(String... wheres) {
        this.wheres = wheres;
        return this;
    }

    public SelectMapperElement and(String andSome) {
        if (null == this.ands) {
            this.ands = new ArrayList<>();
        }
        this.ands.add(andSome);
        return this;
    }

    public SelectMapperElement whereSub(BaseMapperElement subSelect, String append) {
        if (null == this.whereSubs) {
            this.whereSubs = new ArrayList<>();
        }
        this.whereSubs.add(LEFT_BRACKET + subSelect.build() + RIGHT_BRACKET + " " + append);
        return this;
    }

    public SelectMapperElement andSub(BaseMapperElement subSelect, String append) {
        this.whereSubs.add(LEFT_BRACKET + subSelect.build() + RIGHT_BRACKET + " " + append);
        return this;
    }

    public SelectMapperElement group(String... groups) {
        this.groups = groups;
        return this;
    }

    public SelectMapperElement having(String... having) {
        this.having = having;
        return this;
    }

    public SelectMapperElement order(String... fields) {
        this.order = ORDER_BY + stringJoin(this, sentence, COMMA, fields);
        return this;
    }

    public SelectMapperElement order(String field) {
        this.order = ORDER_BY + field;
        return this;
    }

    public SelectMapperElement asc() {
        if (null != this.order) {
            this.order += ASC;
        }
        return this;
    }

    public SelectMapperElement desc() {
        if (null != this.order) {
            this.order += DESC;
        }
        return this;
    }

    public SelectMapperElement limit(String size) {
        this.limit = LIMIT + size;
        return this;
    }

    public SelectMapperElement limit(String skip, String size) {
        this.limit = LIMIT + skip + COMMA + size;
        return this;
    }

    @Override
    public String build() {
        final WhereMapperElement where = new WhereMapperElement(force);
        if (notEmpty(wheres)) {
            where.where(wheres).and(ands).force(prefix);
        }
        if (notEmpty(whereSubs)) {
            final String[] subs = new String[whereSubs.size()];
            for (int i = 0; i < whereSubs.size(); i++) {
                subs[i] = whereSubs.get(i);
            }
            where.where(subs).force(prefix);
        }
        if (!where.isEmpty()) {
            sentence.append(where.build());
        }

        if (notEmpty(groups)) {
            sentence.append(GROUP_BY);
            stringJoin(this, sentence, COMMA, groups);
        }
        if (notEmpty(having)) {
            sentence.append(HAVING);
            stringJoin(this, sentence, AND, having);
        }
        if (notEmpty(order)) {
            sentence.append(order);
        }
        if (notEmpty(limit)) {
            sentence.append(limit);
        }
        return sentence.toString();
    }
}
