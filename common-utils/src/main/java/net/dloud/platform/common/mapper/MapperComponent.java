package net.dloud.platform.common.mapper;

import net.dloud.platform.common.mapper.element.InsertMapperElement;
import net.dloud.platform.common.mapper.element.SelectMapperElement;
import net.dloud.platform.common.mapper.element.TableMapperElement;
import net.dloud.platform.common.mapper.element.UnionMapperElement;
import net.dloud.platform.common.mapper.element.UpdateMapperElement;

/**
 * @author QuDasheng
 * @create 2018-09-13 15:55
 **/
public interface MapperComponent extends BaseComponent {

    default String source() {
        return null;
    }

    default SelectMapperElement choose(String fields) {
        return new SelectMapperElement().choose(fields);
    }

    default SelectMapperElement select(TableMapperElement table) {
        return new SelectMapperElement().select(table);
    }

    default SelectMapperElement select(String table) {
        return new SelectMapperElement().select(table(table));
    }

    default SelectMapperElement select(TableMapperElement table, String fields) {
        return new SelectMapperElement().select(table, fields);
    }

    default SelectMapperElement select(String table, String fields) {
        return new SelectMapperElement().select(table(table), fields);
    }

    default SelectMapperElement select(TableMapperElement table, String... fields) {
        return new SelectMapperElement().select(table, fields);
    }

    default SelectMapperElement select(String table, String... fields) {
        return new SelectMapperElement().select(table(table), fields);
    }

    default InsertMapperElement insert(TableMapperElement table, String fields, String values) {
        return new InsertMapperElement().insert(table, fields, values);
    }

    default InsertMapperElement insert(String table, String fields, String values) {
        return new InsertMapperElement().insert(table(table), fields, values);
    }

    default InsertMapperElement insertSelect(TableMapperElement table, String fields, SelectMapperElement select) {
        return new InsertMapperElement().insertSelect(table, fields, select);
    }

    default InsertMapperElement insertSelect(String table, String fields, SelectMapperElement select) {
        return new InsertMapperElement().insertSelect(table(table), fields, select);
    }

    default InsertMapperElement upsert(TableMapperElement table, String fields, String values, String serts) {
        return new InsertMapperElement().upsert(table, fields, values, serts);
    }

    default InsertMapperElement upsert(String table, String fields, String values, String serts) {
        return new InsertMapperElement().upsert(table(table), fields, values, serts);
    }

    default InsertMapperElement upsertSelect(TableMapperElement table, String fields, SelectMapperElement select, String serts) {
        return new InsertMapperElement().upsertSelect(table, fields, select, serts);
    }

    default InsertMapperElement upsertSelect(String table, String fields, SelectMapperElement select, String serts) {
        return new InsertMapperElement().upsertSelect(table(table), fields, select, serts);
    }

    default UpdateMapperElement update(TableMapperElement table, String... values) {
        return new UpdateMapperElement().update(table, values);
    }

    default UpdateMapperElement update(String table, String... values) {
        return new UpdateMapperElement().update(table(table), values);
    }

    default UpdateMapperElement delete(TableMapperElement table) {
        return new UpdateMapperElement().delete(table);
    }

    default UpdateMapperElement delete(String table) {
        return new UpdateMapperElement().delete(table(table));
    }

    default UpdateMapperElement softDelete(TableMapperElement table) {
        return new UpdateMapperElement().softDelete(table);
    }

    default UpdateMapperElement softDelete(String table) {
        return new UpdateMapperElement().softDelete(table(table));
    }

    default UnionMapperElement union(SelectMapperElement... elements) {
        return new UnionMapperElement().union(elements);
    }

    default UnionMapperElement unionAll(SelectMapperElement... elements) {
        return new UnionMapperElement().unionAll(elements);
    }

    default TableMapperElement table(String name) {
        return new TableMapperElement(name, source());
    }
}


