package net.dloud.platform.common.domain.shape;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author QuDasheng
 * @create 2019-04-01 21:01
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Point {
    /**
     * latitude  纬度，0-90
     */
    private String latitude;

    /**
     * longitude 经度，如120
     */
    private String longitude;
}
