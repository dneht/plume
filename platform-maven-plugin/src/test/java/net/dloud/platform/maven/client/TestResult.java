package net.dloud.platform.maven.client;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author QuDasheng
 * @create 2019-01-21 20:22
 **/
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TestResult {
    private int code;

    private String message;

    private List<String> result1;
}
