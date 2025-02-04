package cn.thtns.test.auto.verify;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true)
public class DeviceByTypeRes {
    private Integer code;
    private String message;
    private Data data;

    @lombok.Data
    @Accessors(chain = true)
    public static class Data {
        private List<ListBean> list;

        @lombok.Data
        @Accessors(chain = true)
        public static class ListBean {
            private Integer id;
            private Long deviceNum;
            private Integer groupId;
            private Integer deviceType;
            private String groupName;
            private String deviceName;
            private String address;
            private Integer alipay;
            private Integer wxpay;
            private Integer card;
            private Integer bill;
            private String online;
            private Integer signal;
            private String connectionType;
            private Integer loraNodeCount;
            private Integer groupStatus;
            private Integer status;
            private Integer parkingNo;
            private Object socketNum;
            private Long imsi;
            private Netcard netcard;
            private Integer isSupportPeakValley;
            private Integer templateId;

            @lombok.Data
            @Accessors(chain = true)
            public static class Netcard {
                private Integer isStop;
                private String ePeriod;
                private Long msisdn;
                private Integer supplier;
                private Integer renewStatus;
                private Integer cState;
            }
        }
    }
}
