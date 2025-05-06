package cn.thtns.test.auto.response;

import cn.thtns.test.auto.enums.TimeFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.testng.collections.Lists;

import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DetectRes {

    /**
     * 手机号码
     */
    private String phone;

    private List<DetectResData> detectResData = Lists.newArrayList();

    private List<ProfitResData> ProfitResData = Lists.newArrayList();

    private String context;


    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class DetectResData {

        /**
         * 时间类型
         */
        private TimeFormat timeFormat;

        /**
         * 支付方式
         */
        private String payType;

        /**
         * 平均收益
         */
        private BigDecimal average;


        private BigDecimal current;

        public DetectResData(String payType, BigDecimal average, BigDecimal current) {
            this.payType = payType;
            this.average = average;
            this.current = current;
        }

        public DetectResData( BigDecimal average, BigDecimal current) {
            this.average = average;
            this.current = current;
        }
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ProfitResData {

        /**
         * 数据对比
         */
        private String time;

        /**
         * 支付方式
         */
        private String payType;

        private BigDecimal oldData;

        private BigDecimal newData;

        public ProfitResData(String time, BigDecimal oldData, BigDecimal newData) {
            this.time = time;
            this.oldData = oldData;
            this.newData = newData;
        }
    }


}
