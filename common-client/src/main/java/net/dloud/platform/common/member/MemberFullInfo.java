package net.dloud.platform.common.member;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import net.dloud.platform.common.annotation.Transient;

import java.sql.Timestamp;
import java.time.Instant;

/**
 * 用户详情
 *
 * @author QuDasheng
 * @create 2018-08-23 18:06
 **/
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class MemberFullInfo extends MemberInfo {
    @Transient
    private static MemberFullInfo empty;

    /**
     * 真实姓名
     */
    private String realname;

    /**
     * 积分
     */
    private Long point;

    /**
     * 上一次登录时间
     */
    private Timestamp loginAt;

    /**
     * 注册时间
     */
    private Timestamp createdAt;


    public MemberFullInfo() {
    }

    public MemberFullInfo(Long userId, Integer level, String nickname, String avatar, Byte gender, Short age, String city, String realname, Long point, Timestamp loginAt, Timestamp createdAt) {
        super(userId, level, nickname, avatar, gender, age, city);
        this.realname = realname;
        this.point = point;
        this.loginAt = loginAt;
        this.createdAt = createdAt;
    }

    public static MemberFullInfo empty() {
        if (null == empty) {
            final Timestamp now = Timestamp.from(Instant.now());
            empty = new MemberFullInfo(0L, 0, "", "", (byte) 0, (short) 0, "",
                    "", 0L, now, now);
        }
        return empty;
    }
}
