package cn.thtns.test.auto.verify;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.util.List;


public class OldProfitRes {

	@JsonProperty("code")
	private Integer code;
	@JsonProperty("message")
	private String message;
	@JsonProperty("data")
	private DataDTO data;
	@JsonProperty("timestamp")
	private Integer timestamp;

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

	public static class DataDTO {
		@JsonProperty("cacheProfit")
		private CacheProfitDTO cacheProfit;
		@JsonProperty("superviseProfit")
		private SuperviseProfitDTO superviseProfit;
		@JsonProperty("totalProfit")
		private TotalProfitDTO totalProfit;

		public CacheProfitDTO getCacheProfit() {
			return cacheProfit;
		}

		public void setCacheProfit(CacheProfitDTO cacheProfit) {
			this.cacheProfit = cacheProfit;
		}

		public SuperviseProfitDTO getSuperviseProfit() {
			return superviseProfit;
		}

		public void setSuperviseProfit(SuperviseProfitDTO superviseProfit) {
			this.superviseProfit = superviseProfit;
		}

		public TotalProfitDTO getTotalProfit() {
			return totalProfit;
		}

		public void setTotalProfit(TotalProfitDTO totalProfit) {
			this.totalProfit = totalProfit;
		}

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


				public static class TimeAndProfit {
					@JsonProperty("time")
					private String time;
					@JsonProperty("profit")
					private Integer profit;

					public String getTime() {
						return time;
					}

					public void setTime(String time) {
						this.time = time;
					}

					public Integer getProfit() {
						return profit;
					}

					public void setProfit(Integer profit) {
						this.profit = profit;
					}
				}


				public List<TimeAndProfit> getWxpay() {
					return wxpay;
				}

				public void setWxpay(List<TimeAndProfit> wxpay) {
					this.wxpay = wxpay;
				}

				public List<TimeAndProfit> getAlipay() {
					return alipay;
				}

				public void setAlipay(List<TimeAndProfit> alipay) {
					this.alipay = alipay;
				}

				public List<TimeAndProfit> getPurse() {
					return purse;
				}

				public void setPurse(List<TimeAndProfit> purse) {
					this.purse = purse;
				}

				public List<TimeAndProfit> getIccWxpay() {
					return iccWxpay;
				}

				public void setIccWxpay(List<TimeAndProfit> iccWxpay) {
					this.iccWxpay = iccWxpay;
				}

				public List<TimeAndProfit> getIccAlipay() {
					return iccAlipay;
				}

				public void setIccAlipay(List<TimeAndProfit> iccAlipay) {
					this.iccAlipay = iccAlipay;
				}

				public List<TimeAndProfit> getIccWxpayRecharge() {
					return iccWxpayRecharge;
				}

				public void setIccWxpayRecharge(List<TimeAndProfit> iccWxpayRecharge) {
					this.iccWxpayRecharge = iccWxpayRecharge;
				}

				public List<TimeAndProfit> getIccAlipayRecharge() {
					return iccAlipayRecharge;
				}

				public void setIccAlipayRecharge(List<TimeAndProfit> iccAlipayRecharge) {
					this.iccAlipayRecharge = iccAlipayRecharge;
				}

				public List<TimeAndProfit> getIccVirtual() {
					return iccVirtual;
				}

				public void setIccVirtual(List<TimeAndProfit> iccVirtual) {
					this.iccVirtual = iccVirtual;
				}

				public List<TimeAndProfit> getDiscountWxpayRecharge() {
					return discountWxpayRecharge;
				}

				public void setDiscountWxpayRecharge(List<TimeAndProfit> discountWxpayRecharge) {
					this.discountWxpayRecharge = discountWxpayRecharge;
				}

				public List<TimeAndProfit> getDiscountAlipayRecharge() {
					return discountAlipayRecharge;
				}

				public void setDiscountAlipayRecharge(List<TimeAndProfit> discountAlipayRecharge) {
					this.discountAlipayRecharge = discountAlipayRecharge;
				}

				public List<TimeAndProfit> getSafeguard() {
					return safeguard;
				}

				public void setSafeguard(List<TimeAndProfit> safeguard) {
					this.safeguard = safeguard;
				}

				public List<TimeAndProfit> getBill() {
					return bill;
				}

				public void setBill(List<TimeAndProfit> bill) {
					this.bill = bill;
				}

				public List<TimeAndProfit> getCard() {
					return card;
				}

				public void setCard(List<TimeAndProfit> card) {
					this.card = card;
				}
			}
		}


		public static class SuperviseProfitDTO {
			@JsonProperty("todayAmount")
			private Integer todayAmount;
			@JsonProperty("arrivalAmount")
			private Integer arrivalAmount;
			@JsonProperty("unArrivalAmount")
			private Integer unArrivalAmount;

			public Integer getTodayAmount() {
				return todayAmount;
			}

			public void setTodayAmount(Integer todayAmount) {
				this.todayAmount = todayAmount;
			}

			public Integer getArrivalAmount() {
				return arrivalAmount;
			}

			public void setArrivalAmount(Integer arrivalAmount) {
				this.arrivalAmount = arrivalAmount;
			}

			public Integer getUnArrivalAmount() {
				return unArrivalAmount;
			}

			public void setUnArrivalAmount(Integer unArrivalAmount) {
				this.unArrivalAmount = unArrivalAmount;
			}
		}


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

			public Integer getTotalWxpay() {
				return totalWxpay;
			}

			public void setTotalWxpay(Integer totalWxpay) {
				this.totalWxpay = totalWxpay;
			}

			public Integer getTotalAlipay() {
				return totalAlipay;
			}

			public void setTotalAlipay(Integer totalAlipay) {
				this.totalAlipay = totalAlipay;
			}

			public Integer getTotalPurse() {
				return totalPurse;
			}

			public void setTotalPurse(Integer totalPurse) {
				this.totalPurse = totalPurse;
			}

			public Integer getTotalIccWxpay() {
				return totalIccWxpay;
			}

			public void setTotalIccWxpay(Integer totalIccWxpay) {
				this.totalIccWxpay = totalIccWxpay;
			}

			public Integer getTotalIccAlipay() {
				return totalIccAlipay;
			}

			public void setTotalIccAlipay(Integer totalIccAlipay) {
				this.totalIccAlipay = totalIccAlipay;
			}

			public Integer getTotalBill() {
				return totalBill;
			}

			public void setTotalBill(Integer totalBill) {
				this.totalBill = totalBill;
			}

			public Integer getTotalCard() {
				return totalCard;
			}

			public void setTotalCard(Integer totalCard) {
				this.totalCard = totalCard;
			}

			public Integer getTotalIccWxpayRecharge() {
				return totalIccWxpayRecharge;
			}

			public void setTotalIccWxpayRecharge(Integer totalIccWxpayRecharge) {
				this.totalIccWxpayRecharge = totalIccWxpayRecharge;
			}

			public Integer getTotalIccAlipayRecharge() {
				return totalIccAlipayRecharge;
			}

			public void setTotalIccAlipayRecharge(Integer totalIccAlipayRecharge) {
				this.totalIccAlipayRecharge = totalIccAlipayRecharge;
			}

			public Integer getTotalVirtual() {
				return totalVirtual;
			}

			public void setTotalVirtual(Integer totalVirtual) {
				this.totalVirtual = totalVirtual;
			}

			public Integer getTotalDiscountAlipayRecharge() {
				return totalDiscountAlipayRecharge;
			}

			public void setTotalDiscountAlipayRecharge(Integer totalDiscountAlipayRecharge) {
				this.totalDiscountAlipayRecharge = totalDiscountAlipayRecharge;
			}

			public Integer getTotalDiscountWxpayRecharge() {
				return totalDiscountWxpayRecharge;
			}

			public void setTotalDiscountWxpayRecharge(Integer totalDiscountWxpayRecharge) {
				this.totalDiscountWxpayRecharge = totalDiscountWxpayRecharge;
			}

			public Integer getTotalSafeguard() {
				return totalSafeguard;
			}

			public void setTotalSafeguard(Integer totalSafeguard) {
				this.totalSafeguard = totalSafeguard;
			}
		}
	}
}
