package net.dloud.platform.common.member;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.dloud.platform.common.annotation.Transient;

import java.sql.Timestamp;
import java.time.Instant;

/**
 * @author QuDasheng
 * @create 2018-10-04 00:13
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MemberIdInfo {
    @Transient
    private static MemberIdInfo empty;

    /**
     * 用户id
     */
    private Long userId;

    /**
     * 等级
     */
    private Integer level;

    /**
     * 上一次登录时间
     */
    private Timestamp loginAt;


    public static MemberIdInfo empty() {
        if (null == empty) {
            empty = new MemberIdInfo(0L, 0, Timestamp.from(Instant.now()));
        }
        return empty;
    }
}
