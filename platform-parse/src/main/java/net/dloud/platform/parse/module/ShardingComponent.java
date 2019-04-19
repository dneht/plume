package net.dloud.platform.parse.module;

import com.alibaba.dubbo.rpc.RpcContext;
import net.dloud.platform.common.extend.StringUtil;
import net.dloud.platform.common.mapper.MapperComponent;
import net.dloud.platform.extend.constant.PlatformConstants;

import static net.dloud.platform.parse.utils.SourceGet.clientFrom;

/**
 * @author QuDasheng
 * @create 2019-02-24 20:43
 **/
public interface ShardingComponent extends MapperComponent {
    @Override
    default String source() {
        if (StringUtil.notBlank(clientFrom.get())) {
            return '_' + clientFrom.get();
        }
        return '_' + RpcContext.getContext().getAttachment(PlatformConstants.FROM_KEY);
    }
}
