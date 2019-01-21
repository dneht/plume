package net.dloud.platform.maven.client;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.dloud.platform.common.annotation.Enquire;
import net.dloud.platform.common.annotation.Transient;

/**
 * @author QuDasheng
 * @create 2019-01-21 20:22
 **/
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TestEntry {
    /**
     * 静态字段
     */
    @Enquire
    private static int some;

    /**
     * 常量字段
     */
    @Transient
    private final int ints = 0;

    /**
     * 一些输入
     */
    private String input;
}
