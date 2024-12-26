package cn.thtns.test.auto.verify_lombok.verify;

import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.qameta.allure.Description;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.springframework.web.client.RestClient;
import org.testng.collections.Lists;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.WeekFields;
import java.util.List;

/**
 * @author liuyj
 * Node 收益比对
 */
@SpringBootTest
public class ProfitTest  {

	private static final String AUTHORIZATION_HEADER = "authorization";
	private static final String OLD_PROFIT_API = "https://agentv2.wanzhuangkj.com/api/profit/get/all?profit_type=dealer";
	private static final String NEW_PROFIT_API = "https://agentv2.wanzhuangkj.com/api/operator/income/profitReport";

	private static final String BEARER_TOKEN = "bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpYXQiOjE3MzUwNDI0MTUsIm5iZiI6MTczNTA0MjQxNCwiZXhwIjoxNzM1NjQ3MjE1LCJ1X2lkIjo0NzI2OSwidV90eXBlIjoyLCJjb21wYW55X2lkIjoyLCJpc3MiOiJkaWRpX3d6IiwiaXNfc3VwZXIiOjAsImlzX2FkbWluIjoxLCJtaW5pX3NvdXJjZSI6Ind6IiwiaXNfc2hvd19zZXR0bGVhY2NvdW50X3JlY29yZCI6MH0.nETgfvM-SBU9AHFi5aRk6OPILiRZi7WmbQo1uNpalGQ";

	@Autowired
	private RestClient restClient;

	@Autowired
	private ObjectMapper objectMapper;

	private boolean compareProfitByTime(OldProfitRes.DataDTO.CacheProfitDTO.Profit.TimeAndProfit oldRes, NewProfitRes.DataDTO.ProfitListDTO.Profit.TimeAndProfit newRes) {
		if (oldRes == null || newRes == null) {
			throw new IllegalArgumentException("对象不能为空");
		}
		if (oldRes.getTime() == null || newRes.getTime() == null) {
			throw new IllegalArgumentException("时间字段不能为空");
		}
		return oldRes.getTime().equals(newRes.getTime()) && NumberUtil.equals(NumberUtil.div(oldRes.getProfit(), BigDecimal.valueOf(100)), newRes.getProfit());
	}

	private void compareProfitData(String paymentType, List<OldProfitRes.DataDTO.CacheProfitDTO.Profit.TimeAndProfit> oldData, List<NewProfitRes.DataDTO.ProfitListDTO.Profit.TimeAndProfit> newData) {
		oldData.forEach(ow -> newData.forEach(nw -> {
			if (ow.getTime().equals(nw.getTime()) && !compareProfitByTime(ow, nw)) {
				System.out.println(StrUtil.format("{}支付，日期：{} 数据不一致，旧接口数据：{}，新接口数据：{}", paymentType, ow.getTime(), ow.getProfit(), nw.getProfit()));
			}
		}));
	}


	public static LocalDate getMondayOfWeek(int year, int week) {
		// 获取该年第一天
		LocalDate firstDayOfYear = LocalDate.of(year, 1, 1);

		// 使用 WeekFields 获取该年特定周的第一天 (周一)
		WeekFields weekFields = WeekFields.ISO; // 默认区域

		return firstDayOfYear.with(weekFields.weekOfYear(), week).with(weekFields.dayOfWeek(), 1);
	}

	private List<OldProfitRes.DataDTO.CacheProfitDTO.Profit.TimeAndProfit> convertSeason(List<OldProfitRes.DataDTO.CacheProfitDTO.Profit.TimeAndProfit> oldData) {

		List<OldProfitRes.DataDTO.CacheProfitDTO.Profit.TimeAndProfit> objects = Lists.newArrayList();

		oldData.forEach(o -> {
			if (StrUtil.isNotBlank(o.getTime())) {
				String[] split = o.getTime().split("/");
				// 周一日期
				LocalDate mondayOfWeek = getMondayOfWeek(Integer.parseInt(split[0]), Integer.parseInt(split[1]));
				// 周日日期
				LocalDate localDate = mondayOfWeek.plusDays(6);

				OldProfitRes.DataDTO.CacheProfitDTO.Profit.TimeAndProfit timeAndProfit = new OldProfitRes.DataDTO.CacheProfitDTO.Profit.TimeAndProfit();
				DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd");
				timeAndProfit.setTime(mondayOfWeek.format(formatter) +"-"+ localDate.format(formatter));
				timeAndProfit.setProfit(o.getProfit());
				objects.add(timeAndProfit);
			}
		});
		return objects;
	}

	@Description("对比数据")
	@Test
	public void profitComparison() throws JsonProcessingException {
		OldProfitRes oldProfitRes = objectMapper.readValue(getApiResponse(OLD_PROFIT_API), OldProfitRes.class);
		NewProfitRes newProfitRes = objectMapper.readValue(getApiResponse(NEW_PROFIT_API), NewProfitRes.class);

		System.out.println("开始对比天统计数据：~~~~~~~~~~~~~~~~~~~~~~~~~~~");
		compareProfitData("wxPay", oldProfitRes.getData().getCacheProfit().getDay().getWxpay(), newProfitRes.getData().getProfitList().getDay().getWxPay());
		compareProfitData("aliPay", oldProfitRes.getData().getCacheProfit().getDay().getAlipay(), newProfitRes.getData().getProfitList().getDay().getAliPay());
		compareProfitData("bill", oldProfitRes.getData().getCacheProfit().getDay().getBill(), newProfitRes.getData().getProfitList().getDay().getBill());
		compareProfitData("card", oldProfitRes.getData().getCacheProfit().getDay().getCard(), newProfitRes.getData().getProfitList().getDay().getCard());
		compareProfitData("purse", oldProfitRes.getData().getCacheProfit().getDay().getPurse(), newProfitRes.getData().getProfitList().getDay().getPurse());
		compareProfitData("safeguard", oldProfitRes.getData().getCacheProfit().getDay().getSafeguard(), newProfitRes.getData().getProfitList().getDay().getSafeguard());
		System.out.println("结束对比天统计数据：~~~~~~~~~~~~~~~~~~~~~~~~~~~");


		System.out.println("开始对比周统计数据：~~~~~~~~~~~~~~~~~~~~~~~~~~~");
		compareProfitData("wxPay", convertSeason(oldProfitRes.getData().getCacheProfit().getWeek().getWxpay()), newProfitRes.getData().getProfitList().getDay().getWxPay());
		compareProfitData("aliPay", convertSeason(oldProfitRes.getData().getCacheProfit().getWeek().getAlipay()), newProfitRes.getData().getProfitList().getDay().getAliPay());
		compareProfitData("bill", convertSeason(oldProfitRes.getData().getCacheProfit().getWeek().getBill()), newProfitRes.getData().getProfitList().getDay().getBill());
		compareProfitData("card", convertSeason(oldProfitRes.getData().getCacheProfit().getWeek().getCard()), newProfitRes.getData().getProfitList().getDay().getCard());
		compareProfitData("purse", convertSeason(oldProfitRes.getData().getCacheProfit().getWeek().getPurse()), newProfitRes.getData().getProfitList().getDay().getPurse());
		compareProfitData("safeguard", convertSeason(oldProfitRes.getData().getCacheProfit().getWeek().getSafeguard()), newProfitRes.getData().getProfitList().getDay().getSafeguard());
		System.out.println("结束对比周统计数据：~~~~~~~~~~~~~~~~~~~~~~~~~~~");


		System.out.println("开始对比月统计数据：~~~~~~~~~~~~~~~~~~~~~~~~~~~");
		compareProfitData("wxPay", oldProfitRes.getData().getCacheProfit().getMonth().getWxpay(), newProfitRes.getData().getProfitList().getMonth().getWxPay());
		compareProfitData("aliPay", oldProfitRes.getData().getCacheProfit().getMonth().getAlipay(), newProfitRes.getData().getProfitList().getMonth().getAliPay());
		compareProfitData("bill", oldProfitRes.getData().getCacheProfit().getMonth().getBill(), newProfitRes.getData().getProfitList().getMonth().getBill());
		compareProfitData("card", oldProfitRes.getData().getCacheProfit().getMonth().getCard(), newProfitRes.getData().getProfitList().getMonth().getCard());
		compareProfitData("purse", oldProfitRes.getData().getCacheProfit().getMonth().getPurse(), newProfitRes.getData().getProfitList().getMonth().getPurse());
		compareProfitData("safeguard", oldProfitRes.getData().getCacheProfit().getMonth().getSafeguard(), newProfitRes.getData().getProfitList().getMonth().getSafeguard());
		System.out.println("结束对比月统计数据：~~~~~~~~~~~~~~~~~~~~~~~~~~~");

		System.out.println("开始对比季度统计数据：~~~~~~~~~~~~~~~~~~~~~~~~~~~");
		compareProfitData("wxPay", oldProfitRes.getData().getCacheProfit().getSeason().getWxpay(), newProfitRes.getData().getProfitList().getSeason().getWxPay());
		compareProfitData("aliPay", oldProfitRes.getData().getCacheProfit().getSeason().getAlipay(), newProfitRes.getData().getProfitList().getSeason().getAliPay());
		compareProfitData("bill", oldProfitRes.getData().getCacheProfit().getSeason().getBill(), newProfitRes.getData().getProfitList().getSeason().getBill());
		compareProfitData("card", oldProfitRes.getData().getCacheProfit().getSeason().getCard(), newProfitRes.getData().getProfitList().getSeason().getCard());
		compareProfitData("purse", oldProfitRes.getData().getCacheProfit().getSeason().getPurse(), newProfitRes.getData().getProfitList().getSeason().getPurse());
		compareProfitData("safeguard", oldProfitRes.getData().getCacheProfit().getSeason().getSafeguard(), newProfitRes.getData().getProfitList().getSeason().getSafeguard());
		System.out.println("结束对比季度统计数据：~~~~~~~~~~~~~~~~~~~~~~~~~~~");

		System.out.println("开始对比年度统计数据：~~~~~~~~~~~~~~~~~~~~~~~~~~~");
		compareProfitData("wxPay", oldProfitRes.getData().getCacheProfit().getYear().getWxpay(), newProfitRes.getData().getProfitList().getYear().getWxPay());
		compareProfitData("aliPay", oldProfitRes.getData().getCacheProfit().getYear().getAlipay(), newProfitRes.getData().getProfitList().getYear().getAliPay());
		compareProfitData("bill", oldProfitRes.getData().getCacheProfit().getYear().getBill(), newProfitRes.getData().getProfitList().getYear().getBill());
		compareProfitData("card", oldProfitRes.getData().getCacheProfit().getYear().getCard(), newProfitRes.getData().getProfitList().getYear().getCard());
		compareProfitData("purse", oldProfitRes.getData().getCacheProfit().getYear().getPurse(), newProfitRes.getData().getProfitList().getYear().getPurse());
		compareProfitData("safeguard", oldProfitRes.getData().getCacheProfit().getYear().getSafeguard(), newProfitRes.getData().getProfitList().getYear().getSafeguard());
		System.out.println("结束对比年度统计数据：~~~~~~~~~~~~~~~~~~~~~~~~~~~");
	}

	private String getApiResponse(String apiUrl) {
		return restClient.get()
				.uri(apiUrl)
				.header(AUTHORIZATION_HEADER, BEARER_TOKEN)
				.retrieve()
				.body(String.class);
	}
}
