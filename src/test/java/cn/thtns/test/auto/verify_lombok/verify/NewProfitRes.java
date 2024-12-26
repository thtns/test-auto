package cn.thtns.test.auto.verify_lombok.verify;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
public class NewProfitRes {


	@JsonProperty("code")
	private Integer code;
	@JsonProperty("message")
	private String message;
	@JsonProperty("data")
	private DataDTO data;
	@JsonProperty("timestamp")
	private Integer timestamp;
	@JsonProperty("traceId")
	private String traceId;

	@Data
	@NoArgsConstructor
	public static class DataDTO {
		@JsonProperty("date")
		private DateDTO date;
		@JsonProperty("totalProfit")
		private TotalProfitDTO totalProfit;
		@JsonProperty("superviseProfit")
		private SuperviseProfitDTO superviseProfit;
		@JsonProperty("profitList")
		private ProfitListDTO profitList;
		@JsonProperty("deviceInfo")
		private DeviceInfoDTO deviceInfo;

		@Data
		@NoArgsConstructor
		public static class DateDTO {
			@JsonProperty("day")
			private List<String> day;
			@JsonProperty("week")
			private List<String> week;
			@JsonProperty("month")
			private List<String> month;
			@JsonProperty("season")
			private List<String> season;
			@JsonProperty("year")
			private List<String> year;

		}

		@Data
		@NoArgsConstructor
		public static class TotalProfitDTO {
			@JsonProperty("wxPay")
			private Double wxPay;
			@JsonProperty("aliPay")
			private Double aliPay;
			@JsonProperty("purse")
			private Double purse;
			@JsonProperty("icc")
			private Double icc;
			@JsonProperty("discount")
			private Double discount;
			@JsonProperty("bill")
			private Object bill;
			@JsonProperty("virtual")
			private Object virtual;
			@JsonProperty("card")
			private Object card;
			@JsonProperty("iccPay")
			private Integer iccPay;
			@JsonProperty("safeguard")
			private Double safeguard;


		}

		@Data
		@NoArgsConstructor
		public static class SuperviseProfitDTO {
			@JsonProperty("todayArrival")
			private Integer todayArrival;
			@JsonProperty("todayUnArrival")
			private Integer todayUnArrival;


		}

		@Data
		@NoArgsConstructor
		public static class ProfitListDTO {
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
				@JsonProperty("wxPay")
				private List<TimeAndProfit> wxPay;
				@JsonProperty("aliPay")
				private List<TimeAndProfit> aliPay;
				@JsonProperty("purse")
				private List<TimeAndProfit> purse;
				@JsonProperty("icc")
				private List<TimeAndProfit> icc;
				@JsonProperty("discount")
				private List<TimeAndProfit> discount;
				@JsonProperty("bill")
				private List<TimeAndProfit> bill;
				@JsonProperty("virtual")
				private List<TimeAndProfit> virtual;
				@JsonProperty("card")
				private List<TimeAndProfit> card;
				@JsonProperty("safeguard")
				private List<TimeAndProfit> safeguard;

				@Data
				@NoArgsConstructor
				public static class TimeAndProfit {
					@JsonProperty("time")
					private String time;
					@JsonProperty("profit")
					private BigDecimal profit;


				}


			}
		}

		@Data
		@NoArgsConstructor
		public static class DeviceInfoDTO {
			@JsonProperty("deviceType")
			private String deviceType;
			@JsonProperty("deviceNum")
			private String deviceNum;
			@JsonProperty("deviceName")
			private String deviceName;
			@JsonProperty("csq")
			private Integer csq;
			@JsonProperty("connectionType")
			private Integer connectionType;
			@JsonProperty("connectionTypeName")
			private String connectionTypeName;


		}
	}
}
