package cn.thtns.test.auto.service;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.thtns.test.auto.config.TokenManager;
import cn.thtns.test.auto.enums.TimeFormat;
import cn.thtns.test.auto.request.LoginReq;
import cn.thtns.test.auto.response.NewProfitRes;
import cn.thtns.test.auto.response.OldProfitRes;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;
import org.testng.collections.Lists;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.WeekFields;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
@Slf4j
public class DeviceProfitService {


    private final TokenManager tokenManager;
    private final OldProfitService oldProfitService;
    private final NewProfitService newProfitService;
    private final OperatorsOrPartnersService operatorsOrPartnersService;


    public void profitComparison(List<LoginReq> loginReqs) {

        String basicToken = tokenManager.manageLogin(null, null, null);

        loginReqs.forEach(loginReq -> {
            Integer uid = operatorsOrPartnersService.getUId(basicToken, loginReq);

            String k = loginReq.getUsername();
            String v = loginReq.getCompany();
            String bearerToken = tokenManager.getToken(k, v);
            log.info("公司ID：{} 账号：{} 正在对比数据", v, k);

            OldProfitRes oldProfitRes = oldProfitService.oldProfitRes(basicToken, k, uid, 2);
            NewProfitRes newProfitRes = newProfitService.newProfitRes(bearerToken);

            verify(oldProfitRes, newProfitRes, k, "groupProfit");




        });
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

    public static void main(String[] args) {

        System.out.println(getMondayOfWeek(2025, 16));

    }


    /**
     * 检测指定时间粒度的收益异常
     *
     * @param data           数据列表（需按时间降序排序）
     * @param timeFormat     时间粒度（DAY/MONTH/SEASON/YEAR）
     * @param lookbackPeriod 回溯周期（如7天、6个月）
     * @param threshold      阈值（如20%）
     */
    public static boolean detectAnomaly(
            List<NewProfitRes.DataDTO.ProfitListDTO.Profit.TimeAndProfit> data,
            TimeFormat timeFormat,
            int lookbackPeriod,
            double threshold
    ) {
        if (data.size() < lookbackPeriod) {
            throw new IllegalArgumentException("数据不足，需至少 " + lookbackPeriod + " 条数据");
        }

        // 获取当前时间段和前一时间段（如最新月 vs 前一月）
        NewProfitRes.DataDTO.ProfitListDTO.Profit.TimeAndProfit current = data.get(0);
        NewProfitRes.DataDTO.ProfitListDTO.Profit.TimeAndProfit previous = data.get(1);

        // 验证时间粒度一致性
        if (current.getTimeFormat() != timeFormat || previous.getTimeFormat() != timeFormat) {
            throw new IllegalArgumentException("时间粒度不一致");
        }

        // 计算历史平均收益（如过去6个月的平均）
        BigDecimal sum = BigDecimal.ZERO;
        for (int i = 2; i <= lookbackPeriod + 1; i++) {  // 跳过前两个最新数据
            sum = sum.add(data.get(i).getProfit());
        }
        BigDecimal average = sum.divide(BigDecimal.valueOf(lookbackPeriod), 2, RoundingMode.HALF_UP);

        // 计算差异百分比
        BigDecimal difference = average.subtract(previous.getProfit());
        BigDecimal percentage = difference.divide(average, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));

        boolean isAnomaly = percentage.abs().compareTo(BigDecimal.valueOf(threshold)) > 0;

        log.info("最近{}{}平均收益:", lookbackPeriod, timeFormat);
        // 4. 输出结果
        log.info("前一{}收益:{} ", previous.getProfit(), percentage);
        log.info("差异百分比:{}%", percentage);
        log.info("是否异常（低于20%阈值）:{} ", isAnomaly);

        // 判断是否超过阈值
        return isAnomaly;
    }


        /**
         * 检查前一天收益是否比前5天平均值低20%
         * @param dailyProfits 按日期排序的每日收益列表，最新日期在最后
         * @return 如果前一天收益比前5天平均值低20%返回true，否则false
         */
        public static boolean checkProfitDrop(List<NewProfitRes.DataDTO.ProfitListDTO.Profit.TimeAndProfit> dailyProfits) {
            if (dailyProfits.size() < 6) {
                throw new IllegalArgumentException("需要至少6天的数据");
            }

            // 获取前一天收益
            BigDecimal bigDecimal = dailyProfits.get(dailyProfits.size() - 1).getProfit(); // 可能为null
            double yesterdayProfit = (bigDecimal != null) ? bigDecimal.doubleValue() : 0.0; // 默认值0.0

            String yesterdayTime = (String) dailyProfits.get(dailyProfits.size() - 1).getTime();

            // 计算前5天平均值
            BigDecimal bigDecimalProfit =null;
            double sum = 0;
            for (int i = dailyProfits.size() - 6; i < dailyProfits.size() - 1; i++) {
                bigDecimalProfit=dailyProfits.get(i).getProfit();
                sum +=  (bigDecimalProfit != null) ? bigDecimalProfit.doubleValue() : 0.0;
            }
            double fiveDayAvg = sum / 5;

            // 计算20%阈值
            double threshold = fiveDayAvg * 0.8;

            log.info("最近{}平均收益:", fiveDayAvg);
            log.info("前一{}收益:{},前5天平均值：{} ",yesterdayTime, yesterdayProfit,sum);
            log.info("计算20%阈值:{} ",threshold);
            log.info("是否低于20%阈值:{} ", yesterdayProfit < threshold);

            // 检查是否低于阈值
            return yesterdayProfit < threshold;
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

    private void compareProfitData(String paymentType, TimeFormat timeFormat,List<OldProfitRes.DataDTO.CacheProfitDTO.Profit.TimeAndProfit> oldData, List<NewProfitRes.DataDTO.ProfitListDTO.Profit.TimeAndProfit> newData) {
        if (CollUtil.isEmpty(newData)) {//新接口返回为空，可能没权限
            return;
        }
        oldData.forEach(ow -> newData.forEach(nw -> {
            if (ow.getTime().equals(nw.getTime()) && !compareProfitByTime(ow, nw)) {
                log.info(StrUtil.format("{}支付，日期：{} 数据不一致，旧接口数据：{}，新接口数据：{}", paymentType, ow.getTime(), NumberUtil.div(ow.getProfit(), BigDecimal.valueOf(100)), nw.getProfit()));
            }
        }));

//        detectAnomaly(newData, timeFormat, newData.size(), 20);
        checkProfitDrop(newData);

    }

    private void compareProfitArrrivise(String paymentType, Integer oldData, Double newData) {
        if (newData == null) {//新接口返回为空，可能没权限
            return;
        }

        if (NumberUtil.equals(NumberUtil.div(oldData, BigDecimal.valueOf(100)), BigDecimal.valueOf(newData)) == false) {
            log.info(StrUtil.format("{}支付，数据不一致，旧接口数据：{}，新接口数据：{}", paymentType, NumberUtil.div(oldData, BigDecimal.valueOf(100)), newData));
        }

    }





    private void verify(OldProfitRes oldProfitRes, NewProfitRes newProfitRes, String k, String device) {


//        log.info("开始对比天统计数据");
        compareProfitData("wxPay", TimeFormat.DAY,oldProfitRes.getData().getCacheProfit().getDay().getWxpay(), newProfitRes.getData().getProfitList().getDay().getWxPay());
        compareProfitData("aliPay", TimeFormat.DAY,oldProfitRes.getData().getCacheProfit().getDay().getAlipay(), newProfitRes.getData().getProfitList().getDay().getAliPay());
        compareProfitData("bill", TimeFormat.DAY,oldProfitRes.getData().getCacheProfit().getDay().getBill(), newProfitRes.getData().getProfitList().getDay().getBill());
        compareProfitData("card", TimeFormat.DAY,oldProfitRes.getData().getCacheProfit().getDay().getCard(), newProfitRes.getData().getProfitList().getDay().getCard());
        compareProfitData("purse", TimeFormat.DAY,oldProfitRes.getData().getCacheProfit().getDay().getPurse(), newProfitRes.getData().getProfitList().getDay().getPurse());

        if (device != "deviceProfit") {
            compareProfitData("safeguard", TimeFormat.DAY,oldProfitRes.getData().getCacheProfit().getDay().getSafeguard(), newProfitRes.getData().getProfitList().getDay().getSafeguard());
            compareProfitData("virtual", TimeFormat.DAY,oldProfitRes.getData().getCacheProfit().getDay().getIccVirtual(), newProfitRes.getData().getProfitList().getDay().getVirtual());

            List<OldProfitRes.DataDTO.CacheProfitDTO.Profit.TimeAndProfit> dayDiscountObjects = Lists.newArrayList();
            dayDiscountObjects.addAll(oldProfitRes.getData().getCacheProfit().getDay().getDiscountAlipayRecharge());
            dayDiscountObjects.addAll(oldProfitRes.getData().getCacheProfit().getDay().getDiscountWxpayRecharge());

            compareProfitData("discount",TimeFormat.DAY, add(dayDiscountObjects), newProfitRes.getData().getProfitList().getDay().getDiscount());


            List<OldProfitRes.DataDTO.CacheProfitDTO.Profit.TimeAndProfit> dayIccObjects = Lists.newArrayList();
            dayIccObjects.addAll(oldProfitRes.getData().getCacheProfit().getDay().getIccAlipay());
            dayIccObjects.addAll(oldProfitRes.getData().getCacheProfit().getDay().getIccWxpay());
            dayIccObjects.addAll(oldProfitRes.getData().getCacheProfit().getDay().getIccAlipayRecharge());
            dayIccObjects.addAll(oldProfitRes.getData().getCacheProfit().getDay().getIccWxpayRecharge());

            compareProfitData("icc", TimeFormat.DAY,add(dayIccObjects), newProfitRes.getData().getProfitList().getDay().getIcc());

        }
//        log.info("结束对比天统计数据");


//        log.info("开始对比周统计数据");
        compareProfitData("wxPay",TimeFormat.WEEK, convertSeason(oldProfitRes.getData().getCacheProfit().getWeek().getWxpay()), newProfitRes.getData().getProfitList().getWeek().getWxPay());
        compareProfitData("aliPay",TimeFormat.WEEK, convertSeason(oldProfitRes.getData().getCacheProfit().getWeek().getAlipay()), newProfitRes.getData().getProfitList().getWeek().getAliPay());
        compareProfitData("bill", TimeFormat.WEEK,convertSeason(oldProfitRes.getData().getCacheProfit().getWeek().getBill()), newProfitRes.getData().getProfitList().getWeek().getBill());
        compareProfitData("card",TimeFormat.WEEK, convertSeason(oldProfitRes.getData().getCacheProfit().getWeek().getCard()), newProfitRes.getData().getProfitList().getWeek().getCard());
        compareProfitData("purse", TimeFormat.WEEK,convertSeason(oldProfitRes.getData().getCacheProfit().getWeek().getPurse()), newProfitRes.getData().getProfitList().getWeek().getPurse());
        if (device != "deviceProfit") {
            compareProfitData("safeguard",TimeFormat.WEEK, convertSeason(oldProfitRes.getData().getCacheProfit().getWeek().getSafeguard()), newProfitRes.getData().getProfitList().getWeek().getSafeguard());
            compareProfitData("virtual",TimeFormat.WEEK, convertSeason(oldProfitRes.getData().getCacheProfit().getWeek().getIccVirtual()), newProfitRes.getData().getProfitList().getWeek().getVirtual());

            List<OldProfitRes.DataDTO.CacheProfitDTO.Profit.TimeAndProfit> weekDiscountObjects = Lists.newArrayList();
            weekDiscountObjects.addAll(oldProfitRes.getData().getCacheProfit().getWeek().getDiscountAlipayRecharge());
            weekDiscountObjects.addAll(oldProfitRes.getData().getCacheProfit().getWeek().getDiscountWxpayRecharge());

            compareProfitData("discount", TimeFormat.WEEK,add(weekDiscountObjects), newProfitRes.getData().getProfitList().getDay().getDiscount());


            List<OldProfitRes.DataDTO.CacheProfitDTO.Profit.TimeAndProfit> weekIccObjects = Lists.newArrayList();
            weekIccObjects.addAll(oldProfitRes.getData().getCacheProfit().getWeek().getIccAlipay());
            weekIccObjects.addAll(oldProfitRes.getData().getCacheProfit().getWeek().getIccWxpay());
            weekIccObjects.addAll(oldProfitRes.getData().getCacheProfit().getWeek().getIccAlipayRecharge());
            weekIccObjects.addAll(oldProfitRes.getData().getCacheProfit().getWeek().getIccWxpayRecharge());

            compareProfitData("icc", TimeFormat.WEEK,add(weekIccObjects), newProfitRes.getData().getProfitList().getWeek().getIcc());
//        log.info("结束对比周统计数据");

        }
//        log.info("开始对比月统计数据");
        compareProfitData("wxPay",TimeFormat.MONTH, oldProfitRes.getData().getCacheProfit().getMonth().getWxpay(), newProfitRes.getData().getProfitList().getMonth().getWxPay());
        compareProfitData("aliPay",TimeFormat.MONTH, oldProfitRes.getData().getCacheProfit().getMonth().getAlipay(), newProfitRes.getData().getProfitList().getMonth().getAliPay());
        compareProfitData("bill",TimeFormat.MONTH, oldProfitRes.getData().getCacheProfit().getMonth().getBill(), newProfitRes.getData().getProfitList().getMonth().getBill());
        compareProfitData("card",TimeFormat.MONTH, oldProfitRes.getData().getCacheProfit().getMonth().getCard(), newProfitRes.getData().getProfitList().getMonth().getCard());
        compareProfitData("purse", TimeFormat.MONTH,oldProfitRes.getData().getCacheProfit().getMonth().getPurse(), newProfitRes.getData().getProfitList().getMonth().getPurse());
        if (device != "deviceProfit") {
            compareProfitData("safeguard",TimeFormat.MONTH, oldProfitRes.getData().getCacheProfit().getMonth().getSafeguard(), newProfitRes.getData().getProfitList().getMonth().getSafeguard());
            compareProfitData("virtual",TimeFormat.MONTH, oldProfitRes.getData().getCacheProfit().getMonth().getIccVirtual(), newProfitRes.getData().getProfitList().getMonth().getVirtual());

            List<OldProfitRes.DataDTO.CacheProfitDTO.Profit.TimeAndProfit> monthDiscountObjects = Lists.newArrayList();
            monthDiscountObjects.addAll(oldProfitRes.getData().getCacheProfit().getMonth().getDiscountAlipayRecharge());
            monthDiscountObjects.addAll(oldProfitRes.getData().getCacheProfit().getMonth().getDiscountWxpayRecharge());

            compareProfitData("discount",TimeFormat.MONTH, add(monthDiscountObjects), newProfitRes.getData().getProfitList().getMonth().getDiscount());


            List<OldProfitRes.DataDTO.CacheProfitDTO.Profit.TimeAndProfit> monthIccObjects = Lists.newArrayList();
            monthIccObjects.addAll(oldProfitRes.getData().getCacheProfit().getMonth().getIccAlipay());
            monthIccObjects.addAll(oldProfitRes.getData().getCacheProfit().getMonth().getIccWxpay());
            monthIccObjects.addAll(oldProfitRes.getData().getCacheProfit().getMonth().getIccAlipayRecharge());
            monthIccObjects.addAll(oldProfitRes.getData().getCacheProfit().getMonth().getIccWxpayRecharge());

            compareProfitData("icc",TimeFormat.MONTH, add(monthIccObjects), newProfitRes.getData().getProfitList().getMonth().getIcc());
        }

//        log.info("结束对比月统计数据");

//        log.info("开始对比季度统计数据");
        compareProfitData("wxPay",TimeFormat.SEASON, oldProfitRes.getData().getCacheProfit().getSeason().getWxpay(), newProfitRes.getData().getProfitList().getSeason().getWxPay());
        compareProfitData("aliPay",TimeFormat.SEASON, oldProfitRes.getData().getCacheProfit().getSeason().getAlipay(), newProfitRes.getData().getProfitList().getSeason().getAliPay());
        compareProfitData("bill",TimeFormat.SEASON, oldProfitRes.getData().getCacheProfit().getSeason().getBill(), newProfitRes.getData().getProfitList().getSeason().getBill());
        compareProfitData("card",TimeFormat.SEASON, oldProfitRes.getData().getCacheProfit().getSeason().getCard(), newProfitRes.getData().getProfitList().getSeason().getCard());
        compareProfitData("purse", TimeFormat.SEASON,oldProfitRes.getData().getCacheProfit().getSeason().getPurse(), newProfitRes.getData().getProfitList().getSeason().getPurse());
        if (device != "deviceProfit") {
            compareProfitData("safeguard", TimeFormat.SEASON,oldProfitRes.getData().getCacheProfit().getSeason().getSafeguard(), newProfitRes.getData().getProfitList().getSeason().getSafeguard());
            compareProfitData("virtual",TimeFormat.SEASON, oldProfitRes.getData().getCacheProfit().getSeason().getIccVirtual(), newProfitRes.getData().getProfitList().getSeason().getVirtual());

            List<OldProfitRes.DataDTO.CacheProfitDTO.Profit.TimeAndProfit> seasonDiscountObjects = Lists.newArrayList();
            seasonDiscountObjects.addAll(oldProfitRes.getData().getCacheProfit().getSeason().getDiscountAlipayRecharge());
            seasonDiscountObjects.addAll(oldProfitRes.getData().getCacheProfit().getSeason().getDiscountWxpayRecharge());

            compareProfitData("discount",TimeFormat.SEASON, add(seasonDiscountObjects), newProfitRes.getData().getProfitList().getSeason().getDiscount());


            List<OldProfitRes.DataDTO.CacheProfitDTO.Profit.TimeAndProfit> seasonIccObjects = Lists.newArrayList();
//            seasonIccObjects.addAll(oldProfitRes.getData().getCacheProfit().getSeason().getIccAlipay());
//            seasonIccObjects.addAll(oldProfitRes.getData().getCacheProfit().getSeason().getIccWxpay());
            seasonIccObjects.addAll(oldProfitRes.getData().getCacheProfit().getSeason().getIccWxpayRecharge());
            seasonIccObjects.addAll(oldProfitRes.getData().getCacheProfit().getSeason().getIccAlipayRecharge());

            compareProfitData("icc",TimeFormat.SEASON, add(seasonIccObjects), newProfitRes.getData().getProfitList().getSeason().getIcc());
        }
//        log.info("结束对比季度统计数据");

//        log.info("开始对比年度统计数据");
        compareProfitData("wxPay",TimeFormat.YEAR, oldProfitRes.getData().getCacheProfit().getYear().getWxpay(), newProfitRes.getData().getProfitList().getYear().getWxPay());
        compareProfitData("aliPay", TimeFormat.YEAR,oldProfitRes.getData().getCacheProfit().getYear().getAlipay(), newProfitRes.getData().getProfitList().getYear().getAliPay());
        compareProfitData("bill",TimeFormat.YEAR, oldProfitRes.getData().getCacheProfit().getYear().getBill(), newProfitRes.getData().getProfitList().getYear().getBill());
        compareProfitData("card", TimeFormat.YEAR,oldProfitRes.getData().getCacheProfit().getYear().getCard(), newProfitRes.getData().getProfitList().getYear().getCard());
        compareProfitData("purse",TimeFormat.YEAR, oldProfitRes.getData().getCacheProfit().getYear().getPurse(), newProfitRes.getData().getProfitList().getYear().getPurse());
        if (device != "deviceProfit") {
            compareProfitData("safeguard", TimeFormat.YEAR,oldProfitRes.getData().getCacheProfit().getYear().getSafeguard(), newProfitRes.getData().getProfitList().getYear().getSafeguard());
            compareProfitData("virtual",TimeFormat.YEAR, oldProfitRes.getData().getCacheProfit().getYear().getIccVirtual(), newProfitRes.getData().getProfitList().getYear().getVirtual());

            List<OldProfitRes.DataDTO.CacheProfitDTO.Profit.TimeAndProfit> yearDiscountObjects = Lists.newArrayList();
            yearDiscountObjects.addAll(oldProfitRes.getData().getCacheProfit().getYear().getDiscountAlipayRecharge());
            yearDiscountObjects.addAll(oldProfitRes.getData().getCacheProfit().getYear().getDiscountWxpayRecharge());

            compareProfitData("discount",TimeFormat.YEAR, add(yearDiscountObjects), newProfitRes.getData().getProfitList().getYear().getDiscount());


            List<OldProfitRes.DataDTO.CacheProfitDTO.Profit.TimeAndProfit> yearIccObjects = Lists.newArrayList();
            yearIccObjects.addAll(oldProfitRes.getData().getCacheProfit().getYear().getIccAlipay());
            yearIccObjects.addAll(oldProfitRes.getData().getCacheProfit().getYear().getIccWxpay());
            yearIccObjects.addAll(oldProfitRes.getData().getCacheProfit().getYear().getIccWxpayRecharge());
            yearIccObjects.addAll(oldProfitRes.getData().getCacheProfit().getYear().getIccAlipayRecharge());

            compareProfitData("icc", TimeFormat.YEAR,add(yearIccObjects), newProfitRes.getData().getProfitList().getYear().getIcc());
        }
//        log.info("结束对比年度统计数据");


        if (device != "deviceProfit") {
            log.info("开始对比各项总收益数据");
            compareProfitArrrivise("微信总收益", oldProfitRes.getData().getTotalProfit().getTotalWxpay(), newProfitRes.getData().getTotalProfit().getWxPay());
            compareProfitArrrivise("支付宝总收益", oldProfitRes.getData().getTotalProfit().getTotalAlipay(), newProfitRes.getData().getTotalProfit().getAliPay());
            compareProfitArrrivise("钱包支付总收益", oldProfitRes.getData().getTotalProfit().getTotalPurse(), newProfitRes.getData().getTotalProfit().getPurse());

            Integer oldIccrech = oldProfitRes.getData().getTotalProfit().getTotalIccWxpayRecharge() + oldProfitRes.getData().getTotalProfit().getTotalIccAlipayRecharge();
            compareProfitArrrivise("IC卡充值总收益", oldIccrech, newProfitRes.getData().getTotalProfit().getIcc());

            Integer olddiscountRech = oldProfitRes.getData().getTotalProfit().getTotalDiscountAlipayRecharge() + oldProfitRes.getData().getTotalProfit().getTotalDiscountWxpayRecharge();
            compareProfitArrrivise("充电券充值总收益", olddiscountRech, newProfitRes.getData().getTotalProfit().getDiscount());
//            compareProfitArrrivise("投币总收益",oldProfitRes.getData().getTotalProfit().getTotalBill(),newProfitRes.getData().getTotalProfit().getBill());
//            compareProfitArrrivise("刷卡总收益",oldProfitRes.getData().getTotalProfit().getTotalAlipay(),newProfitRes.getData().getTotalProfit().getAliPay());

            Object obj = newProfitRes.getData().getTotalProfit().getVirtual();
            if (obj instanceof Double) {
                Double target = (Double) obj;
                compareProfitArrrivise("IC卡虚拟充值", oldProfitRes.getData().getTotalProfit().getTotalVirtual(), target);
            }
            log.info("结束对比各项总收益数据");
        }
        log.info("账号：{} 对比数据结束 ", k);
    }


}
