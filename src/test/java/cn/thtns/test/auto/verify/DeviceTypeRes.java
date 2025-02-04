package cn.thtns.test.auto.verify;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true)
public class DeviceTypeRes {
    private Integer code;
    private String message;
    private List<DataBean> data;

    @Data
    @Accessors(chain = true)
    public static class DataBean {
        private Integer chargeMode;
        private Integer count;
        private Integer deviceType;
        private String name;
    }
}
