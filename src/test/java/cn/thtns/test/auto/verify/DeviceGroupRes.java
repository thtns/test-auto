package cn.thtns.test.auto.verify;

import lombok.Data;
import lombok.NoArgsConstructor;



import java.util.List;

@Data
@NoArgsConstructor
public class DeviceGroupRes {
    private Integer code;
    private String message;
    private Data data;

    @lombok.Data
    public static class Data {
        private List<ListBean> list;
        private Integer count;

        @lombok.Data
        public static class ListBean {
            private Integer groupId;
            private String groupName;
            private Integer ownOfflineCount;
            private Integer ownOnlineCount;
            private Integer ownCount;
        }
    }
}
