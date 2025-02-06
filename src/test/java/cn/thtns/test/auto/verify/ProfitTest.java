package cn.thtns.test.auto.verify;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.qameta.allure.Description;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestTemplate;
import org.testng.collections.Lists;
import org.testng.collections.Maps;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.WeekFields;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * @author liuyj
 * Node 收益比对
 */
@SpringBootTest
@Slf4j
public class ProfitTest {

    private static final String AUTHORIZATION_HEADER = "authorization";
    private static final String OLD_PROFIT_API = "https://agentv2.wanzhuangkj.com/api/profit/get/all?profit_type=dealer";
    private static final String NEW_PROFIT_API = "https://agentv2.wanzhuangkj.com/api/operator/income/profitReport";



    @Autowired
    private RestClient restClient;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TokenManager tokenManager;


	private final Map<String, String> phoneMap = Maps.newHashMap();


	 {

		 phoneMap.put("19034658903", "");

	}





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
        if (CollUtil.isEmpty(newData)) {//新接口返回为空，可能没权限
            return;
        }
        oldData.forEach(ow -> newData.forEach(nw -> {
            if (ow.getTime().equals(nw.getTime()) && !compareProfitByTime(ow, nw)) {
                log.info(StrUtil.format("{}支付，日期：{} 数据不一致，旧接口数据：{}，新接口数据：{}", paymentType, ow.getTime(), ow.getProfit(), nw.getProfit()));
            }
        }));
    }
    private void compareProfitArrrivise(String paymentType, Integer oldData, Double newData) {
        if (newData==null) {//新接口返回为空，可能没权限
            return;
        }

        if (NumberUtil.equals(NumberUtil.div(oldData, BigDecimal.valueOf(100)), BigDecimal.valueOf(newData))==false) {
            log.info(StrUtil.format("{}支付，数据不一致，旧接口数据：{}，新接口数据：{}", paymentType, NumberUtil.div(oldData, BigDecimal.valueOf(100)), newData));
        }

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
                timeAndProfit.setTime(mondayOfWeek.format(formatter) + "-" + localDate.format(formatter));
                timeAndProfit.setProfit(o.getProfit());
                objects.add(timeAndProfit);
            }
        });
        return objects;
    }

	public List<OldProfitRes.DataDTO.CacheProfitDTO.Profit.TimeAndProfit> add(List<OldProfitRes.DataDTO.CacheProfitDTO.Profit.TimeAndProfit> oldTimeAndProfits) {

		Map<String, Integer> result = oldTimeAndProfits.stream()
				.collect(Collectors.groupingBy(
						OldProfitRes.DataDTO.CacheProfitDTO.Profit.TimeAndProfit::getTime,
						Collectors.summingInt(OldProfitRes.DataDTO.CacheProfitDTO.Profit.TimeAndProfit::getProfit)
				));

		List<OldProfitRes.DataDTO.CacheProfitDTO.Profit.TimeAndProfit> objects = Lists.newArrayList();

		result.forEach((time, totalProfit) -> {
			OldProfitRes.DataDTO.CacheProfitDTO.Profit.TimeAndProfit timeAndProfit = new OldProfitRes.DataDTO.CacheProfitDTO.Profit.TimeAndProfit();
			timeAndProfit.setTime(time);
			timeAndProfit.setProfit(totalProfit);
			objects.add(timeAndProfit);
		});

		return objects;
	}



    @Description("对比数据")
    @Test
    public void profitComparison() throws JsonProcessingException {

        for (Map.Entry<String, String> entry : phoneMap.entrySet()) {


            String k = entry.getKey();
            String v = entry.getValue();
            String bearerToken = tokenManager.getToken(k, v);

			log.info("账号：{} 正在对比数据", k);

            OldProfitRes oldProfitRes = objectMapper.readValue(getApiResponse(OLD_PROFIT_API, bearerToken), OldProfitRes.class);
            NewProfitRes newProfitRes = objectMapper.readValue(getApiResponse(NEW_PROFIT_API, bearerToken), NewProfitRes.class);
            if (newProfitRes.getData()==null) {
                continue;
            }
            log.info("开始对比天统计数据");
            compareProfitData("wxPay", oldProfitRes.getData().getCacheProfit().getDay().getWxpay(), newProfitRes.getData().getProfitList().getDay().getWxPay());
            compareProfitData("aliPay", oldProfitRes.getData().getCacheProfit().getDay().getAlipay(), newProfitRes.getData().getProfitList().getDay().getAliPay());
            compareProfitData("bill", oldProfitRes.getData().getCacheProfit().getDay().getBill(), newProfitRes.getData().getProfitList().getDay().getBill());
            compareProfitData("card", oldProfitRes.getData().getCacheProfit().getDay().getCard(), newProfitRes.getData().getProfitList().getDay().getCard());
            compareProfitData("purse", oldProfitRes.getData().getCacheProfit().getDay().getPurse(), newProfitRes.getData().getProfitList().getDay().getPurse());
            compareProfitData("safeguard", oldProfitRes.getData().getCacheProfit().getDay().getSafeguard(), newProfitRes.getData().getProfitList().getDay().getSafeguard());
            compareProfitData("virtual", oldProfitRes.getData().getCacheProfit().getDay().getIccVirtual(), newProfitRes.getData().getProfitList().getDay().getVirtual());

            List<OldProfitRes.DataDTO.CacheProfitDTO.Profit.TimeAndProfit> dayDiscountObjects = Lists.newArrayList();
            dayDiscountObjects.addAll(oldProfitRes.getData().getCacheProfit().getDay().getDiscountAlipayRecharge());
            dayDiscountObjects.addAll(oldProfitRes.getData().getCacheProfit().getDay().getDiscountWxpayRecharge());

            compareProfitData("discount", add(dayDiscountObjects), newProfitRes.getData().getProfitList().getDay().getDiscount());


            List<OldProfitRes.DataDTO.CacheProfitDTO.Profit.TimeAndProfit> dayIccObjects = Lists.newArrayList();
            dayIccObjects.addAll(oldProfitRes.getData().getCacheProfit().getDay().getIccAlipay());
            dayIccObjects.addAll(oldProfitRes.getData().getCacheProfit().getDay().getIccWxpay());
            dayIccObjects.addAll(oldProfitRes.getData().getCacheProfit().getDay().getIccAlipayRecharge());
            dayIccObjects.addAll(oldProfitRes.getData().getCacheProfit().getDay().getIccWxpayRecharge());

            compareProfitData("icc", add(dayIccObjects), newProfitRes.getData().getProfitList().getDay().getIcc());


            log.info("结束对比天统计数据");


            log.info("开始对比周统计数据");
            compareProfitData("wxPay", convertSeason(oldProfitRes.getData().getCacheProfit().getWeek().getWxpay()), newProfitRes.getData().getProfitList().getWeek().getWxPay());
            compareProfitData("aliPay", convertSeason(oldProfitRes.getData().getCacheProfit().getWeek().getAlipay()), newProfitRes.getData().getProfitList().getWeek().getAliPay());
            compareProfitData("bill", convertSeason(oldProfitRes.getData().getCacheProfit().getWeek().getBill()), newProfitRes.getData().getProfitList().getWeek().getBill());
            compareProfitData("card", convertSeason(oldProfitRes.getData().getCacheProfit().getWeek().getCard()), newProfitRes.getData().getProfitList().getWeek().getCard());
            compareProfitData("purse", convertSeason(oldProfitRes.getData().getCacheProfit().getWeek().getPurse()), newProfitRes.getData().getProfitList().getWeek().getPurse());
            compareProfitData("safeguard", convertSeason(oldProfitRes.getData().getCacheProfit().getWeek().getSafeguard()), newProfitRes.getData().getProfitList().getWeek().getSafeguard());
            compareProfitData("virtual", convertSeason(oldProfitRes.getData().getCacheProfit().getWeek().getIccVirtual()), newProfitRes.getData().getProfitList().getWeek().getVirtual());

            List<OldProfitRes.DataDTO.CacheProfitDTO.Profit.TimeAndProfit> weekDiscountObjects = Lists.newArrayList();
            weekDiscountObjects.addAll(oldProfitRes.getData().getCacheProfit().getWeek().getDiscountAlipayRecharge());
            weekDiscountObjects.addAll(oldProfitRes.getData().getCacheProfit().getWeek().getDiscountWxpayRecharge());

            compareProfitData("discount", add(weekDiscountObjects), newProfitRes.getData().getProfitList().getDay().getDiscount());


            List<OldProfitRes.DataDTO.CacheProfitDTO.Profit.TimeAndProfit> weekIccObjects = Lists.newArrayList();
            weekIccObjects.addAll(oldProfitRes.getData().getCacheProfit().getWeek().getIccAlipay());
            weekIccObjects.addAll(oldProfitRes.getData().getCacheProfit().getWeek().getIccWxpay());
            weekIccObjects.addAll(oldProfitRes.getData().getCacheProfit().getWeek().getIccAlipayRecharge());
            weekIccObjects.addAll(oldProfitRes.getData().getCacheProfit().getWeek().getIccWxpayRecharge());

            compareProfitData("icc", add(weekIccObjects), newProfitRes.getData().getProfitList().getWeek().getIcc());
            log.info("结束对比周统计数据");


            log.info("开始对比月统计数据");
            compareProfitData("wxPay", oldProfitRes.getData().getCacheProfit().getMonth().getWxpay(), newProfitRes.getData().getProfitList().getMonth().getWxPay());
            compareProfitData("aliPay", oldProfitRes.getData().getCacheProfit().getMonth().getAlipay(), newProfitRes.getData().getProfitList().getMonth().getAliPay());
            compareProfitData("bill", oldProfitRes.getData().getCacheProfit().getMonth().getBill(), newProfitRes.getData().getProfitList().getMonth().getBill());
            compareProfitData("card", oldProfitRes.getData().getCacheProfit().getMonth().getCard(), newProfitRes.getData().getProfitList().getMonth().getCard());
            compareProfitData("purse", oldProfitRes.getData().getCacheProfit().getMonth().getPurse(), newProfitRes.getData().getProfitList().getMonth().getPurse());
            compareProfitData("safeguard", oldProfitRes.getData().getCacheProfit().getMonth().getSafeguard(), newProfitRes.getData().getProfitList().getMonth().getSafeguard());
            compareProfitData("virtual", oldProfitRes.getData().getCacheProfit().getMonth().getIccVirtual(), newProfitRes.getData().getProfitList().getMonth().getVirtual());

            List<OldProfitRes.DataDTO.CacheProfitDTO.Profit.TimeAndProfit> monthDiscountObjects = Lists.newArrayList();
            monthDiscountObjects.addAll(oldProfitRes.getData().getCacheProfit().getMonth().getDiscountAlipayRecharge());
            monthDiscountObjects.addAll(oldProfitRes.getData().getCacheProfit().getMonth().getDiscountWxpayRecharge());

            compareProfitData("discount", add(monthDiscountObjects), newProfitRes.getData().getProfitList().getMonth().getDiscount());


            List<OldProfitRes.DataDTO.CacheProfitDTO.Profit.TimeAndProfit> monthIccObjects = Lists.newArrayList();
            monthIccObjects.addAll(oldProfitRes.getData().getCacheProfit().getMonth().getIccAlipay());
            monthIccObjects.addAll(oldProfitRes.getData().getCacheProfit().getMonth().getIccWxpay());
            monthIccObjects.addAll(oldProfitRes.getData().getCacheProfit().getMonth().getIccAlipayRecharge());
            monthIccObjects.addAll(oldProfitRes.getData().getCacheProfit().getMonth().getIccWxpayRecharge());

            compareProfitData("icc", add(monthIccObjects), newProfitRes.getData().getProfitList().getMonth().getIcc());

            log.info("结束对比月统计数据");

            log.info("开始对比季度统计数据");
            compareProfitData("wxPay", oldProfitRes.getData().getCacheProfit().getSeason().getWxpay(), newProfitRes.getData().getProfitList().getSeason().getWxPay());
            compareProfitData("aliPay", oldProfitRes.getData().getCacheProfit().getSeason().getAlipay(), newProfitRes.getData().getProfitList().getSeason().getAliPay());
//            compareProfitData("bill", oldProfitRes.getData().getCacheProfit().getSeason().getBill(), newProfitRes.getData().getProfitList().getSeason().getBill());
//            compareProfitData("card", oldProfitRes.getData().getCacheProfit().getSeason().getCard(), newProfitRes.getData().getProfitList().getSeason().getCard());
            compareProfitData("purse", oldProfitRes.getData().getCacheProfit().getSeason().getPurse(), newProfitRes.getData().getProfitList().getSeason().getPurse());
            compareProfitData("safeguard", oldProfitRes.getData().getCacheProfit().getSeason().getSafeguard(), newProfitRes.getData().getProfitList().getSeason().getSafeguard());
            compareProfitData("virtual", oldProfitRes.getData().getCacheProfit().getSeason().getIccVirtual(), newProfitRes.getData().getProfitList().getSeason().getVirtual());

            List<OldProfitRes.DataDTO.CacheProfitDTO.Profit.TimeAndProfit> seasonDiscountObjects = Lists.newArrayList();
            seasonDiscountObjects.addAll(oldProfitRes.getData().getCacheProfit().getSeason().getDiscountAlipayRecharge());
            seasonDiscountObjects.addAll(oldProfitRes.getData().getCacheProfit().getSeason().getDiscountWxpayRecharge());

            compareProfitData("discount", add(seasonDiscountObjects), newProfitRes.getData().getProfitList().getSeason().getDiscount());


            List<OldProfitRes.DataDTO.CacheProfitDTO.Profit.TimeAndProfit> seasonIccObjects = Lists.newArrayList();
//            seasonIccObjects.addAll(oldProfitRes.getData().getCacheProfit().getSeason().getIccAlipay());
//            seasonIccObjects.addAll(oldProfitRes.getData().getCacheProfit().getSeason().getIccWxpay());
            seasonIccObjects.addAll(oldProfitRes.getData().getCacheProfit().getSeason().getIccWxpayRecharge());
            seasonIccObjects.addAll(oldProfitRes.getData().getCacheProfit().getSeason().getIccAlipayRecharge());

            compareProfitData("icc", add(seasonIccObjects), newProfitRes.getData().getProfitList().getSeason().getIcc());

            log.info("结束对比季度统计数据");

            log.info("开始对比年度统计数据");
            compareProfitData("wxPay", oldProfitRes.getData().getCacheProfit().getYear().getWxpay(), newProfitRes.getData().getProfitList().getYear().getWxPay());
            compareProfitData("aliPay", oldProfitRes.getData().getCacheProfit().getYear().getAlipay(), newProfitRes.getData().getProfitList().getYear().getAliPay());
//            compareProfitData("bill", oldProfitRes.getData().getCacheProfit().getYear().getBill(), newProfitRes.getData().getProfitList().getYear().getBill());
//            compareProfitData("card", oldProfitRes.getData().getCacheProfit().getYear().getCard(), newProfitRes.getData().getProfitList().getYear().getCard());
            compareProfitData("purse", oldProfitRes.getData().getCacheProfit().getYear().getPurse(), newProfitRes.getData().getProfitList().getYear().getPurse());
            compareProfitData("safeguard", oldProfitRes.getData().getCacheProfit().getYear().getSafeguard(), newProfitRes.getData().getProfitList().getYear().getSafeguard());
            compareProfitData("virtual", oldProfitRes.getData().getCacheProfit().getYear().getIccVirtual(), newProfitRes.getData().getProfitList().getYear().getVirtual());

            List<OldProfitRes.DataDTO.CacheProfitDTO.Profit.TimeAndProfit> yearDiscountObjects = Lists.newArrayList();
            yearDiscountObjects.addAll(oldProfitRes.getData().getCacheProfit().getYear().getDiscountAlipayRecharge());
            yearDiscountObjects.addAll(oldProfitRes.getData().getCacheProfit().getYear().getDiscountWxpayRecharge());

            compareProfitData("discount", add(yearDiscountObjects), newProfitRes.getData().getProfitList().getYear().getDiscount());


            List<OldProfitRes.DataDTO.CacheProfitDTO.Profit.TimeAndProfit> yearIccObjects = Lists.newArrayList();
            yearIccObjects.addAll(oldProfitRes.getData().getCacheProfit().getYear().getIccAlipay());
            yearIccObjects.addAll(oldProfitRes.getData().getCacheProfit().getYear().getIccWxpay());
            yearIccObjects.addAll(oldProfitRes.getData().getCacheProfit().getYear().getIccWxpayRecharge());
            yearIccObjects.addAll(oldProfitRes.getData().getCacheProfit().getYear().getIccAlipayRecharge());

            compareProfitData("icc", add(yearIccObjects), newProfitRes.getData().getProfitList().getYear().getIcc());

            log.info("结束对比年度统计数据");


            log.info("开始对比各项总收益数据");
            compareProfitArrrivise("微信总收益",oldProfitRes.getData().getTotalProfit().getTotalWxpay(),newProfitRes.getData().getTotalProfit().getWxPay());
            compareProfitArrrivise("支付宝总收益",oldProfitRes.getData().getTotalProfit().getTotalAlipay(),newProfitRes.getData().getTotalProfit().getAliPay());
            compareProfitArrrivise("钱包支付总收益",oldProfitRes.getData().getTotalProfit().getTotalPurse(),newProfitRes.getData().getTotalProfit().getPurse());

            Integer oldIccrech=oldProfitRes.getData().getTotalProfit().getTotalIccWxpayRecharge()+oldProfitRes.getData().getTotalProfit().getTotalIccAlipayRecharge();
            compareProfitArrrivise("IC卡充值总收益",oldIccrech,newProfitRes.getData().getTotalProfit().getIcc());

            Integer olddiscountRech=oldProfitRes.getData().getTotalProfit().getTotalDiscountAlipayRecharge()+oldProfitRes.getData().getTotalProfit().getTotalDiscountWxpayRecharge();
            compareProfitArrrivise("充电券充值总收益",olddiscountRech,newProfitRes.getData().getTotalProfit().getDiscount());
//            compareProfitArrrivise("投币总收益",oldProfitRes.getData().getTotalProfit().getTotalBill(),newProfitRes.getData().getTotalProfit().getBill());
//            compareProfitArrrivise("刷卡总收益",oldProfitRes.getData().getTotalProfit().getTotalAlipay(),newProfitRes.getData().getTotalProfit().getAliPay());

            Object obj = newProfitRes.getData().getTotalProfit().getVirtual();
            if (obj instanceof Double) {
                Double target = (Double) obj;
                compareProfitArrrivise("IC卡虚拟充值",oldProfitRes.getData().getTotalProfit().getTotalVirtual(),target);
            }
            log.info("结束对比各项总收益数据");

            log.info("账号：{} 对比数据结束 ", k);
            ThreadUtil.sleep(2000);

        }


    }

	private String getApiResponse(String apiUrl, String BEARER_TOKEN) {
		return restClient.get()
				.uri(apiUrl)
				.header(AUTHORIZATION_HEADER, BEARER_TOKEN)
				.retrieve()
				.body(String.class);
	}





}
