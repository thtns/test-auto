package cn.thtns.test.auto.verify;

import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.qameta.allure.Description;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.springframework.web.client.RestClient;
import org.testng.annotations.Test;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author liuyj
 * Node 收益比对
 */
@SpringBootTest
public class ProfitTest extends AbstractTestNGSpringContextTests {

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

	@Test
	@Description("对比数据")
	public void profitComparison() throws JsonProcessingException {
		OldProfitRes oldProfitRes = objectMapper.readValue(getApiResponse(OLD_PROFIT_API), OldProfitRes.class);
		NewProfitRes newProfitRes = objectMapper.readValue(getApiResponse(NEW_PROFIT_API), NewProfitRes.class);

		System.out.println("开始对比天统计数据：~~~~~~~~~~~~~~~~~~~~~~~~~~~");
		compareProfitData("微信", oldProfitRes.getData().getCacheProfit().getDay().getWxpay(), newProfitRes.getData().getProfitList().getDay().getWxPay());
		compareProfitData("支付宝", oldProfitRes.getData().getCacheProfit().getDay().getAlipay(), newProfitRes.getData().getProfitList().getDay().getAliPay());
		System.out.println("结束对比天统计数据：~~~~~~~~~~~~~~~~~~~~~~~~~~~");

		System.out.println("开始对比月统计数据：~~~~~~~~~~~~~~~~~~~~~~~~~~~");
		compareProfitData("微信", oldProfitRes.getData().getCacheProfit().getMonth().getWxpay(), newProfitRes.getData().getProfitList().getMonth().getWxPay());
		compareProfitData("支付宝", oldProfitRes.getData().getCacheProfit().getMonth().getAlipay(), newProfitRes.getData().getProfitList().getMonth().getAliPay());
		System.out.println("结束对比月统计数据：~~~~~~~~~~~~~~~~~~~~~~~~~~~");
	}

	private String getApiResponse(String apiUrl) {
		return restClient.get()
				.uri(apiUrl)
				.header(AUTHORIZATION_HEADER, BEARER_TOKEN)
				.retrieve()
				.body(String.class);
	}
}
