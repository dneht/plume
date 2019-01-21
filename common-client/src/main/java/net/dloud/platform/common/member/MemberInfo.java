package net.dloud.platform.common.member;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import net.dloud.platform.common.annotation.Transient;

/**
 * 用户详情
 *
 * @author QuDasheng
 * @create 2018-08-23 18:06
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MemberInfo {
    @Transient
    private static MemberInfo empty;

    /**
     * 用户id
     */
    private Long userId;

    /**
     * 等级
     */
    private Integer level;

    /**
     * 用户昵称
     */
    private String nickname;

    /**
     * 用户头像
     */
    private String avatar;

    /**
     * 性别
     */
    private Byte gender;

    /**
     * 年龄
     */
    private Short age;

    /**
     * 城市
     */
    private String city;


    public static MemberInfo empty() {
        if (null == empty) {
            empty = new MemberInfo(0L, 0, "", "", (byte) 0, (short) 0, "");
        }
        return empty;
    }
}
