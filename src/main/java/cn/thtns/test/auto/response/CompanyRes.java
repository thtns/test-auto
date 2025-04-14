package cn.thtns.test.auto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@Data
public class CompanyRes {


    @JsonProperty("code")
    private Integer code;
    @JsonProperty("message")
    private String message;
    @JsonProperty("data")
    private DataDTO data;

    @NoArgsConstructor
    @Data
    public static class DataDTO {
        @JsonProperty("count")
        private Integer count;
        @JsonProperty("list")
        private List<ListDTO> list;

        @NoArgsConstructor
        @Data
        public static class ListDTO {
            @JsonProperty("id")
            private Integer id;
            @JsonProperty("nickname")
            private String nickname;
            @JsonProperty("phone")
            private String phone;
            @JsonProperty("wxName")
            private String wxName;
            @JsonProperty("aliName")
            private String aliName;
            @JsonProperty("wxOpenid")
            private String wxOpenid;
            @JsonProperty("unionId")
            private String unionId;
            @JsonProperty("aliOpenid")
            private String aliOpenid;
            @JsonProperty("headImgFile")
            private String headImgFile;
            @JsonProperty("settleStatus")
            private Integer settleStatus;
            @JsonProperty("settleMode")
            private Integer settleMode;
            @JsonProperty("settleNext")
            private Integer settleNext;
            @JsonProperty("extends")
            private String extendsX;
            @JsonProperty("createdAt")
            private String createdAt;
            @JsonProperty("updatedAt")
            private String updatedAt;
            @JsonProperty("memberId")
            private Integer memberId;
            @JsonProperty("auditStatus")
            private String auditStatus;
            @JsonProperty("settleTime")
            private String settleTime;
            @JsonProperty("isBlack")
            private Integer isBlack;
            @JsonProperty("mchType")
            private Integer mchType;
            @JsonProperty("settleCompanyName")
            private String settleCompanyName;
            @JsonProperty("companyId")
            private Integer companyId;
            @JsonProperty("companyName")
            private String companyName;
            @JsonProperty("onlineCount")
            private Integer onlineCount;
            @JsonProperty("offlineCount")
            private Integer offlineCount;
            @JsonProperty("totalCard")
            private Integer totalCard;
            @JsonProperty("totalBill")
            private Integer totalBill;
            @JsonProperty("totalWxpay")
            private Integer totalWxpay;
            @JsonProperty("totalAlipay")
            private Integer totalAlipay;
            @JsonProperty("totalIccWxpay")
            private Integer totalIccWxpay;
            @JsonProperty("totalIccAlipay")
            private Integer totalIccAlipay;
            @JsonProperty("totalIccAlipayRecharge")
            private Integer totalIccAlipayRecharge;
            @JsonProperty("totalIccWxpayRecharge")
            private Integer totalIccWxpayRecharge;
            @JsonProperty("totalVirtual")
            private Integer totalVirtual;
            @JsonProperty("wzRate")
            private Integer wzRate;
            @JsonProperty("ydyRate")
            private Integer ydyRate;
            @JsonProperty("rate")
            private Integer rate;
            @JsonProperty("isForbiddenPushException")
            private Integer isForbiddenPushException;
        }
    }
}
