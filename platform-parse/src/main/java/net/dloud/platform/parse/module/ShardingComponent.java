package net.dloud.platform.parse.module;

import net.dloud.platform.common.mapper.MapperComponent;
import net.dloud.platform.parse.context.LocalContext;

/**
 * @author QuDasheng
 * @create 2019-02-24 20:43
 **/
public interface ShardingComponent extends MapperComponent {
    @Override
    default String source() {
        return '_' + LocalContext.load().getFrom();
    }
}
