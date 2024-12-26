package cn.thtns.test.auto.verify_lombok.verify;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class OldProfitRes {

	@JsonProperty("code")
	private Integer code;
	@JsonProperty("message")
	private String message;
	@JsonProperty("data")
	private DataDTO data;
	@JsonProperty("timestamp")
	private Integer timestamp;


	@Data
	@NoArgsConstructor
	public static class DataDTO {
		@JsonProperty("cacheProfit")
		private CacheProfitDTO cacheProfit;
		@JsonProperty("superviseProfit")
		private SuperviseProfitDTO superviseProfit;
		@JsonProperty("totalProfit")
		private TotalProfitDTO totalProfit;


		@Data
		@NoArgsConstructor
		public static class CacheProfitDTO {
			@JsonProperty("day")
			private Profit day;
			@JsonProperty("week")
			private Profit week;
			@JsonProperty("month")
			private Profit month;
			@JsonProperty("season")
			private Profit season;
			@JsonProperty("year")
			private Profit year;



			@Data
			@NoArgsConstructor
			public static class Profit {
				@JsonProperty("wxpay")
				private List<TimeAndProfit> wxpay;
				@JsonProperty("alipay")
				private List<TimeAndProfit> alipay;
				@JsonProperty("purse")
				private List<TimeAndProfit> purse;
				@JsonProperty("icc_wxpay")
				private List<TimeAndProfit> iccWxpay;
				@JsonProperty("icc_alipay")
				private List<TimeAndProfit> iccAlipay;
				@JsonProperty("icc_wxpay_recharge")
				private List<TimeAndProfit> iccWxpayRecharge;
				@JsonProperty("icc_alipay_recharge")
				private List<TimeAndProfit> iccAlipayRecharge;
				@JsonProperty("icc_virtual")
				private List<TimeAndProfit> iccVirtual;
				@JsonProperty("discount_wxpay_recharge")
				private List<TimeAndProfit> discountWxpayRecharge;
				@JsonProperty("discount_alipay_recharge")
				private List<TimeAndProfit> discountAlipayRecharge;
				@JsonProperty("safeguard")
				private List<TimeAndProfit> safeguard;
				@JsonProperty("bill")
				private List<TimeAndProfit> bill;
				@JsonProperty("card")
				private List<TimeAndProfit> card;

				@Data
				@NoArgsConstructor
				public static class TimeAndProfit {
					@JsonProperty("time")
					private String time;
					@JsonProperty("profit")
					private Integer profit;


				}



			}
		}

		@Data
		@NoArgsConstructor
		public static class SuperviseProfitDTO {
			@JsonProperty("todayAmount")
			private Integer todayAmount;
			@JsonProperty("arrivalAmount")
			private Integer arrivalAmount;
			@JsonProperty("unArrivalAmount")
			private Integer unArrivalAmount;


		}

		@Data
		@NoArgsConstructor
		public static class TotalProfitDTO {
			@JsonProperty("totalWxpay")
			private Integer totalWxpay;
			@JsonProperty("totalAlipay")
			private Integer totalAlipay;
			@JsonProperty("totalPurse")
			private Integer totalPurse;
			@JsonProperty("totalIccWxpay")
			private Integer totalIccWxpay;
			@JsonProperty("totalIccAlipay")
			private Integer totalIccAlipay;
			@JsonProperty("totalBill")
			private Integer totalBill;
			@JsonProperty("totalCard")
			private Integer totalCard;
			@JsonProperty("totalIccWxpayRecharge")
			private Integer totalIccWxpayRecharge;
			@JsonProperty("totalIccAlipayRecharge")
			private Integer totalIccAlipayRecharge;
			@JsonProperty("totalVirtual")
			private Integer totalVirtual;
			@JsonProperty("totalDiscountAlipayRecharge")
			private Integer totalDiscountAlipayRecharge;
			@JsonProperty("totalDiscountWxpayRecharge")
			private Integer totalDiscountWxpayRecharge;
			@JsonProperty("totalSafeguard")
			private Integer totalSafeguard;


		}
	}
}
