package cn.thtns.test.auto.verify;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;


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

	public Integer getCode() {
		return code;
	}

	public void setCode(Integer code) {
		this.code = code;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public DataDTO getData() {
		return data;
	}

	public void setData(DataDTO data) {
		this.data = data;
	}

	public Integer getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Integer timestamp) {
		this.timestamp = timestamp;
	}

	public String getTraceId() {
		return traceId;
	}

	public void setTraceId(String traceId) {
		this.traceId = traceId;
	}


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

		public DateDTO getDate() {
			return date;
		}

		public void setDate(DateDTO date) {
			this.date = date;
		}

		public TotalProfitDTO getTotalProfit() {
			return totalProfit;
		}

		public void setTotalProfit(TotalProfitDTO totalProfit) {
			this.totalProfit = totalProfit;
		}

		public SuperviseProfitDTO getSuperviseProfit() {
			return superviseProfit;
		}

		public void setSuperviseProfit(SuperviseProfitDTO superviseProfit) {
			this.superviseProfit = superviseProfit;
		}

		public ProfitListDTO getProfitList() {
			return profitList;
		}

		public void setProfitList(ProfitListDTO profitList) {
			this.profitList = profitList;
		}

		public DeviceInfoDTO getDeviceInfo() {
			return deviceInfo;
		}

		public void setDeviceInfo(DeviceInfoDTO deviceInfo) {
			this.deviceInfo = deviceInfo;
		}


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

			public List<String> getDay() {
				return day;
			}

			public void setDay(List<String> day) {
				this.day = day;
			}

			public List<String> getWeek() {
				return week;
			}

			public void setWeek(List<String> week) {
				this.week = week;
			}

			public List<String> getMonth() {
				return month;
			}

			public void setMonth(List<String> month) {
				this.month = month;
			}

			public List<String> getSeason() {
				return season;
			}

			public void setSeason(List<String> season) {
				this.season = season;
			}

			public List<String> getYear() {
				return year;
			}

			public void setYear(List<String> year) {
				this.year = year;
			}
		}


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

			public Double getWxPay() {
				return wxPay;
			}

			public void setWxPay(Double wxPay) {
				this.wxPay = wxPay;
			}

			public Double getAliPay() {
				return aliPay;
			}

			public void setAliPay(Double aliPay) {
				this.aliPay = aliPay;
			}

			public Double getPurse() {
				return purse;
			}

			public void setPurse(Double purse) {
				this.purse = purse;
			}

			public Double getIcc() {
				return icc;
			}

			public void setIcc(Double icc) {
				this.icc = icc;
			}

			public Double getDiscount() {
				return discount;
			}

			public void setDiscount(Double discount) {
				this.discount = discount;
			}

			public Object getBill() {
				return bill;
			}

			public void setBill(Object bill) {
				this.bill = bill;
			}

			public Object getVirtual() {
				return virtual;
			}

			public void setVirtual(Object virtual) {
				this.virtual = virtual;
			}

			public Object getCard() {
				return card;
			}

			public void setCard(Object card) {
				this.card = card;
			}

			public Integer getIccPay() {
				return iccPay;
			}

			public void setIccPay(Integer iccPay) {
				this.iccPay = iccPay;
			}

			public Double getSafeguard() {
				return safeguard;
			}

			public void setSafeguard(Double safeguard) {
				this.safeguard = safeguard;
			}
		}


		public static class SuperviseProfitDTO {
			@JsonProperty("todayArrival")
			private Integer todayArrival;
			@JsonProperty("todayUnArrival")
			private Integer todayUnArrival;

			public Integer getTodayArrival() {
				return todayArrival;
			}

			public void setTodayArrival(Integer todayArrival) {
				this.todayArrival = todayArrival;
			}

			public Integer getTodayUnArrival() {
				return todayUnArrival;
			}

			public void setTodayUnArrival(Integer todayUnArrival) {
				this.todayUnArrival = todayUnArrival;
			}
		}


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

			public Profit getDay() {
				return day;
			}

			public void setDay(Profit day) {
				this.day = day;
			}

			public Profit getWeek() {
				return week;
			}

			public void setWeek(Profit week) {
				this.week = week;
			}

			public Profit getMonth() {
				return month;
			}

			public void setMonth(Profit month) {
				this.month = month;
			}

			public Profit getSeason() {
				return season;
			}

			public void setSeason(Profit season) {
				this.season = season;
			}

			public Profit getYear() {
				return year;
			}

			public void setYear(Profit year) {
				this.year = year;
			}


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

				public static class TimeAndProfit {
					@JsonProperty("time")
					private String time;
					@JsonProperty("profit")
					private BigDecimal profit;

					public String getTime() {
						return time;
					}

					public void setTime(String time) {
						this.time = time;
					}

					public BigDecimal getProfit() {
						return profit;
					}

					public void setProfit(BigDecimal profit) {
						this.profit = profit;
					}
				}

				public List<TimeAndProfit> getWxPay() {
					return wxPay;
				}

				public void setWxPay(List<TimeAndProfit> wxPay) {
					this.wxPay = wxPay;
				}

				public List<TimeAndProfit> getAliPay() {
					return aliPay;
				}

				public void setAliPay(List<TimeAndProfit> aliPay) {
					this.aliPay = aliPay;
				}

				public List<TimeAndProfit> getPurse() {
					return purse;
				}

				public void setPurse(List<TimeAndProfit> purse) {
					this.purse = purse;
				}

				public List<TimeAndProfit> getIcc() {
					return icc;
				}

				public void setIcc(List<TimeAndProfit> icc) {
					this.icc = icc;
				}

				public List<TimeAndProfit> getDiscount() {
					return discount;
				}

				public void setDiscount(List<TimeAndProfit> discount) {
					this.discount = discount;
				}

				public List<TimeAndProfit> getBill() {
					return bill;
				}

				public void setBill(List<TimeAndProfit> bill) {
					this.bill = bill;
				}

				public List<TimeAndProfit> getVirtual() {
					return virtual;
				}

				public void setVirtual(List<TimeAndProfit> virtual) {
					this.virtual = virtual;
				}

				public List<TimeAndProfit> getCard() {
					return card;
				}

				public void setCard(List<TimeAndProfit> card) {
					this.card = card;
				}

				public List<TimeAndProfit> getSafeguard() {
					return safeguard;
				}

				public void setSafeguard(List<TimeAndProfit> safeguard) {
					this.safeguard = safeguard;
				}
			}
		}


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

			public String getDeviceType() {
				return deviceType;
			}

			public void setDeviceType(String deviceType) {
				this.deviceType = deviceType;
			}

			public String getDeviceNum() {
				return deviceNum;
			}

			public void setDeviceNum(String deviceNum) {
				this.deviceNum = deviceNum;
			}

			public String getDeviceName() {
				return deviceName;
			}

			public void setDeviceName(String deviceName) {
				this.deviceName = deviceName;
			}

			public Integer getCsq() {
				return csq;
			}

			public void setCsq(Integer csq) {
				this.csq = csq;
			}

			public Integer getConnectionType() {
				return connectionType;
			}

			public void setConnectionType(Integer connectionType) {
				this.connectionType = connectionType;
			}

			public String getConnectionTypeName() {
				return connectionTypeName;
			}

			public void setConnectionTypeName(String connectionTypeName) {
				this.connectionTypeName = connectionTypeName;
			}
		}
	}
}
