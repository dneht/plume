package net.dloud.platform.dal.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class InfoMethodDetail {
    private String clazzName;

    private String pathName;

    private String invokeName;

    private Integer invokeLength;

    private Boolean isWhitelist = false;

    private Boolean isBackground = false;

    private Boolean cacheTime;

    private Boolean isTrack = false;

    private byte[] parameterInfo;

    private byte[] returnInfo;

    private byte[] commentInfo;

    private byte[] injectionInfo;

    private byte[] permissionInfo;

    private byte[] methodData;

    private String paramMock;

    private String returnMock;

    private Timestamp createdAt;

    private Timestamp updatedAt;
}

