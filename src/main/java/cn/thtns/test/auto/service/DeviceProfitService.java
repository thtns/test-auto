package cn.thtns.test.auto.service;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import cn.thtns.test.auto.config.TokenManager;
import cn.thtns.test.auto.enums.TimeFormat;
import cn.thtns.test.auto.request.LoginReq;
import cn.thtns.test.auto.response.DetectRes;
import cn.thtns.test.auto.response.NewProfitRes;
import cn.thtns.test.auto.response.OldProfitRes;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.stereotype.Service;
import org.testng.collections.Lists;
import org.testng.collections.Maps;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
@Slf4j
public class DeviceProfitService {


    private final TokenManager tokenManager;
    private final OldProfitService oldProfitService;
    private final NewProfitService newProfitService;
    private final OperatorsOrPartnersService operatorsOrPartnersService;
    private final DingSendMessageService dingSendMessageService;


    public void profitComparison(List<LoginReq> loginReqs) {

//        String basicToken = tokenManager.manageLogin(null, null, null);


        loginReqs.forEach(loginReq -> {
//            Integer uid = operatorsOrPartnersService.getUId(basicToken, loginReq);

            String k = loginReq.getUsername();
            String v = loginReq.getCompany();
            String bearerToken = tokenManager.getToken(k, v);
            log.info("公司ID：{} 账号：{} 正在对比数据", v, k);

//            OldProfitRes oldProfitRes = oldProfitService.oldProfitRes(basicToken, k, uid, 2);
            NewProfitRes newProfitRes = newProfitService.newProfitRes(bearerToken);

            verify(new OldProfitRes(), newProfitRes, k, "groupProfit");

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

        if (CollUtil.isEmpty(oldData)) {
            return Lists.newArrayList();
        }

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
     * <p>
     * "day": [
     * "2025-04-14",
     * "2025-04-13",
     * "2025-04-12",
     * "2025-04-11",
     * "2025-04-10",
     * "2025-04-09",
     * "2025-04-08"
     * ]
     *
     * @param data           数据列表（需按时间降序排序）
     * @param timeFormat     时间粒度（DAY/MONTH/SEASON/YEAR）
     * @param lookbackPeriod 回溯周期（如7天、6个月）
     * @param threshold      阈值（如20%）
     */
    public DetectRes.DetectResData detectAnomaly(String k,
                                                 List<NewProfitRes.DataDTO.ProfitListDTO.Profit.TimeAndProfit> data,
                                                 TimeFormat timeFormat,
                                                 int lookbackPeriod,
                                                 double threshold, String paymentType
    ) {
        NewProfitRes.DataDTO.ProfitListDTO.Profit.TimeAndProfit current = data.get(1);

        List<NewProfitRes.DataDTO.ProfitListDTO.Profit.TimeAndProfit> subbedList = data.subList(2, data.size());

        // 计算历史平均收益（如过去6个月的平均）
        BigDecimal sum = BigDecimal.ZERO;

        for (NewProfitRes.DataDTO.ProfitListDTO.Profit.TimeAndProfit timeAndProfit : subbedList) {
            sum = sum.add(timeAndProfit.getProfit());
        }

        if (sum.compareTo(BigDecimal.ZERO) == 0) {
            return new DetectRes.DetectResData();
        }

        // 平均值
        BigDecimal average = sum.divide(BigDecimal.valueOf(subbedList.size()), 2, RoundingMode.HALF_UP);

        // 绝对值
        BigDecimal abs = NumberUtil.sub(current.getProfit(), average).abs();

        // 是否异常
        BigDecimal percentage = abs.divide(average, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));

        boolean isAnomaly = percentage.abs().compareTo(BigDecimal.valueOf(threshold)) > 0;


        log.info("结果输出开始~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
        // 4. 输出结果
        log.info("支付方式：{}", paymentType);
        log.info("最近{}{}平均收益:{}", lookbackPeriod, timeFormat, average);
        log.info("前一{}收益:{} ", timeFormat, current.getProfit());
        log.info("差异百分比:{}%", percentage);

        log.info("是否异常:{} ", isAnomaly);

        if (isAnomaly) {
//            dingSendMessageService.sendMessage(StrUtil.format("手机号{}：支付方式：{},差异百分比超过{}%,请注意！", k, paymentType, threshold));
            return new DetectRes.DetectResData(timeFormat, paymentType, average, current.getProfit());
        }
        log.info("结果输出结束~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
        return new DetectRes.DetectResData();
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

    private void compareProfitData(DetectRes detectRes, String k, String paymentType, TimeFormat timeFormat, List<OldProfitRes.DataDTO.CacheProfitDTO.Profit.TimeAndProfit> oldData, List<NewProfitRes.DataDTO.ProfitListDTO.Profit.TimeAndProfit> newData) {
        if (CollUtil.isEmpty(newData)) {//新接口返回为空，可能没权限
            return;
        }
        detectRes.setPhone(k);
        List<DetectRes.ProfitResData> profitResData = detectRes.getProfitResData();
        if (CollUtil.isNotEmpty(oldData)) {
            oldData.forEach(ow -> newData.forEach(nw -> {
                if (ow.getTime().equals(nw.getTime()) && !compareProfitByTime(ow, nw)) {
                    log.info(StrUtil.format("{}支付，日期：{} 数据不一致，旧接口数据：{}，新接口数据：{}", paymentType, ow.getTime(), NumberUtil.div(ow.getProfit(), BigDecimal.valueOf(100)), nw.getProfit()));
                    profitResData.add(new DetectRes.ProfitResData(ow.getTime(), paymentType, NumberUtil.div(ow.getProfit(), BigDecimal.valueOf(100)), nw.getProfit()));
                }
            }));
        }

        detectRes.setProfitResData(profitResData);
        List<DetectRes.DetectResData> objects = detectRes.getDetectResData();
        DetectRes.DetectResData detectResData = detectAnomaly(k, newData, timeFormat, newData.size(), 20, paymentType);
        if (Objects.nonNull(detectResData) && Objects.nonNull(detectResData.getPayType()) && Objects.nonNull(detectResData.getTimeFormat())) {
            objects.add(detectResData);
            detectRes.setDetectResData(objects);
        }
//        checkProfitDrop(newData);

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


        DetectRes detectRes = new DetectRes();
        detectRes.setPhone(k);

//        log.info("开始对比天统计数据");
        compareProfitData(detectRes, k, "wxPay", TimeFormat.DAY, null, newProfitRes.getData().getProfitList().getDay().getWxPay());
        compareProfitData(detectRes, k, "aliPay", TimeFormat.DAY, null, newProfitRes.getData().getProfitList().getDay().getAliPay());
        compareProfitData(detectRes, k, "bill", TimeFormat.DAY, null, newProfitRes.getData().getProfitList().getDay().getBill());
        compareProfitData(detectRes, k, "card", TimeFormat.DAY, null, newProfitRes.getData().getProfitList().getDay().getCard());
        compareProfitData(detectRes, k, "purse", TimeFormat.DAY, null, newProfitRes.getData().getProfitList().getDay().getPurse());

        if (device != "deviceProfit") {
            compareProfitData(detectRes, k, "safeguard", TimeFormat.DAY, null, newProfitRes.getData().getProfitList().getDay().getSafeguard());
            compareProfitData(detectRes, k, "virtual", TimeFormat.DAY, null, newProfitRes.getData().getProfitList().getDay().getVirtual());

//            List<OldProfitRes.DataDTO.CacheProfitDTO.Profit.TimeAndProfit> dayDiscountObjects = Lists.newArrayList();
//            dayDiscountObjects.addAll(oldProfitRes.getData().getCacheProfit().getDay().getDiscountAlipayRecharge());
//            dayDiscountObjects.addAll(oldProfitRes.getData().getCacheProfit().getDay().getDiscountWxpayRecharge());

            compareProfitData(detectRes, k, "discount", TimeFormat.DAY, null, newProfitRes.getData().getProfitList().getDay().getDiscount());


//            List<OldProfitRes.DataDTO.CacheProfitDTO.Profit.TimeAndProfit> dayIccObjects = Lists.newArrayList();
//            dayIccObjects.addAll(oldProfitRes.getData().getCacheProfit().getDay().getIccAlipay());
//            dayIccObjects.addAll(oldProfitRes.getData().getCacheProfit().getDay().getIccWxpay());
//            dayIccObjects.addAll(oldProfitRes.getData().getCacheProfit().getDay().getIccAlipayRecharge());
//            dayIccObjects.addAll(oldProfitRes.getData().getCacheProfit().getDay().getIccWxpayRecharge());

            compareProfitData(detectRes, k, "icc", TimeFormat.DAY, null, newProfitRes.getData().getProfitList().getDay().getIcc());

        }
//        log.info("结束对比天统计数据");


//        log.info("开始对比周统计数据");
        compareProfitData(detectRes, k, "wxPay", TimeFormat.WEEK, null, newProfitRes.getData().getProfitList().getWeek().getWxPay());
        compareProfitData(detectRes, k, "aliPay", TimeFormat.WEEK, null, newProfitRes.getData().getProfitList().getWeek().getAliPay());
        compareProfitData(detectRes, k, "bill", TimeFormat.WEEK, null, newProfitRes.getData().getProfitList().getWeek().getBill());
        compareProfitData(detectRes, k, "card", TimeFormat.WEEK, null, newProfitRes.getData().getProfitList().getWeek().getCard());
        compareProfitData(detectRes, k, "purse", TimeFormat.WEEK, null, newProfitRes.getData().getProfitList().getWeek().getPurse());
        if (device != "deviceProfit") {
            compareProfitData(detectRes, k, "safeguard", TimeFormat.WEEK, null, newProfitRes.getData().getProfitList().getWeek().getSafeguard());
            compareProfitData(detectRes, k, "virtual", TimeFormat.WEEK, null, newProfitRes.getData().getProfitList().getWeek().getVirtual());

//            List<OldProfitRes.DataDTO.CacheProfitDTO.Profit.TimeAndProfit> weekDiscountObjects = Lists.newArrayList();
//            weekDiscountObjects.addAll(oldProfitRes.getData().getCacheProfit().getWeek().getDiscountAlipayRecharge());
//            weekDiscountObjects.addAll(oldProfitRes.getData().getCacheProfit().getWeek().getDiscountWxpayRecharge());

            compareProfitData(detectRes, k, "discount", TimeFormat.WEEK, null, newProfitRes.getData().getProfitList().getDay().getDiscount());


//            List<OldProfitRes.DataDTO.CacheProfitDTO.Profit.TimeAndProfit> weekIccObjects = Lists.newArrayList();
//            weekIccObjects.addAll(oldProfitRes.getData().getCacheProfit().getWeek().getIccAlipay());
//            weekIccObjects.addAll(oldProfitRes.getData().getCacheProfit().getWeek().getIccWxpay());
//            weekIccObjects.addAll(oldProfitRes.getData().getCacheProfit().getWeek().getIccAlipayRecharge());
//            weekIccObjects.addAll(oldProfitRes.getData().getCacheProfit().getWeek().getIccWxpayRecharge());

            compareProfitData(detectRes, k, "icc", TimeFormat.WEEK, null, newProfitRes.getData().getProfitList().getWeek().getIcc());
//        log.info("结束对比周统计数据");

        }
//        log.info("开始对比月统计数据");
        compareProfitData(detectRes, k, "wxPay", TimeFormat.MONTH, null, newProfitRes.getData().getProfitList().getMonth().getWxPay());
        compareProfitData(detectRes, k, "aliPay", TimeFormat.MONTH, null, newProfitRes.getData().getProfitList().getMonth().getAliPay());
        compareProfitData(detectRes, k, "bill", TimeFormat.MONTH, null, newProfitRes.getData().getProfitList().getMonth().getBill());
        compareProfitData(detectRes, k, "card", TimeFormat.MONTH, null, newProfitRes.getData().getProfitList().getMonth().getCard());
        compareProfitData(detectRes, k, "purse", TimeFormat.MONTH, null, newProfitRes.getData().getProfitList().getMonth().getPurse());
        if (device != "deviceProfit") {
            compareProfitData(detectRes, k, "safeguard", TimeFormat.MONTH, null, newProfitRes.getData().getProfitList().getMonth().getSafeguard());
            compareProfitData(detectRes, k, "virtual", TimeFormat.MONTH, null, newProfitRes.getData().getProfitList().getMonth().getVirtual());

//            List<OldProfitRes.DataDTO.CacheProfitDTO.Profit.TimeAndProfit> monthDiscountObjects = Lists.newArrayList();
//            monthDiscountObjects.addAll(oldProfitRes.getData().getCacheProfit().getMonth().getDiscountAlipayRecharge());
//            monthDiscountObjects.addAll(oldProfitRes.getData().getCacheProfit().getMonth().getDiscountWxpayRecharge());

            compareProfitData(detectRes, k, "discount", TimeFormat.MONTH, null, newProfitRes.getData().getProfitList().getMonth().getDiscount());


//            List<OldProfitRes.DataDTO.CacheProfitDTO.Profit.TimeAndProfit> monthIccObjects = Lists.newArrayList();
//            monthIccObjects.addAll(oldProfitRes.getData().getCacheProfit().getMonth().getIccAlipay());
//            monthIccObjects.addAll(oldProfitRes.getData().getCacheProfit().getMonth().getIccWxpay());
//            monthIccObjects.addAll(oldProfitRes.getData().getCacheProfit().getMonth().getIccAlipayRecharge());
//            monthIccObjects.addAll(oldProfitRes.getData().getCacheProfit().getMonth().getIccWxpayRecharge());

            compareProfitData(detectRes, k, "icc", TimeFormat.MONTH, null, newProfitRes.getData().getProfitList().getMonth().getIcc());
        }

//        log.info("结束对比月统计数据");

//        log.info("开始对比季度统计数据");
        compareProfitData(detectRes, k, "wxPay", TimeFormat.SEASON, null, newProfitRes.getData().getProfitList().getSeason().getWxPay());
        compareProfitData(detectRes, k, "aliPay", TimeFormat.SEASON, null, newProfitRes.getData().getProfitList().getSeason().getAliPay());
        compareProfitData(detectRes, k, "bill", TimeFormat.SEASON, null, newProfitRes.getData().getProfitList().getSeason().getBill());
        compareProfitData(detectRes, k, "card", TimeFormat.SEASON, null, newProfitRes.getData().getProfitList().getSeason().getCard());
        compareProfitData(detectRes, k, "purse", TimeFormat.SEASON, null, newProfitRes.getData().getProfitList().getSeason().getPurse());
        if (device != "deviceProfit") {
            compareProfitData(detectRes, k, "safeguard", TimeFormat.SEASON, null, newProfitRes.getData().getProfitList().getSeason().getSafeguard());
            compareProfitData(detectRes, k, "virtual", TimeFormat.SEASON, null, newProfitRes.getData().getProfitList().getSeason().getVirtual());

//            List<OldProfitRes.DataDTO.CacheProfitDTO.Profit.TimeAndProfit> seasonDiscountObjects = Lists.newArrayList();
//            seasonDiscountObjects.addAll(oldProfitRes.getData().getCacheProfit().getSeason().getDiscountAlipayRecharge());
//            seasonDiscountObjects.addAll(oldProfitRes.getData().getCacheProfit().getSeason().getDiscountWxpayRecharge());

            compareProfitData(detectRes, k, "discount", TimeFormat.SEASON, null, newProfitRes.getData().getProfitList().getSeason().getDiscount());


//            List<OldProfitRes.DataDTO.CacheProfitDTO.Profit.TimeAndProfit> seasonIccObjects = Lists.newArrayList();
////            seasonIccObjects.addAll(oldProfitRes.getData().getCacheProfit().getSeason().getIccAlipay());
////            seasonIccObjects.addAll(oldProfitRes.getData().getCacheProfit().getSeason().getIccWxpay());
//            seasonIccObjects.addAll(oldProfitRes.getData().getCacheProfit().getSeason().getIccWxpayRecharge());
//            seasonIccObjects.addAll(oldProfitRes.getData().getCacheProfit().getSeason().getIccAlipayRecharge());

            compareProfitData(detectRes, k, "icc", TimeFormat.SEASON, null, newProfitRes.getData().getProfitList().getSeason().getIcc());
        }
//        log.info("结束对比季度统计数据");

//        log.info("开始对比年度统计数据");
//        compareProfitData(detectRes, k, "wxPay", TimeFormat.YEAR, null, newProfitRes.getData().getProfitList().getYear().getWxPay());
//        compareProfitData(detectRes, k, "aliPay", TimeFormat.YEAR, null, newProfitRes.getData().getProfitList().getYear().getAliPay());
//        compareProfitData(detectRes, k, "bill", TimeFormat.YEAR, null, newProfitRes.getData().getProfitList().getYear().getBill());
//        compareProfitData(detectRes, k, "card", TimeFormat.YEAR, null, newProfitRes.getData().getProfitList().getYear().getCard());
//        compareProfitData(detectRes, k, "purse", TimeFormat.YEAR, null, newProfitRes.getData().getProfitList().getYear().getPurse());
//        if (device != "deviceProfit") {
//            compareProfitData(detectRes, k, "safeguard", TimeFormat.YEAR, null, newProfitRes.getData().getProfitList().getYear().getSafeguard());
//            compareProfitData(detectRes, k, "virtual", TimeFormat.YEAR, null, newProfitRes.getData().getProfitList().getYear().getVirtual());
//
////            List<OldProfitRes.DataDTO.CacheProfitDTO.Profit.TimeAndProfit> yearDiscountObjects = Lists.newArrayList();
////            yearDiscountObjects.addAll(oldProfitRes.getData().getCacheProfit().getYear().getDiscountAlipayRecharge());
////            yearDiscountObjects.addAll(oldProfitRes.getData().getCacheProfit().getYear().getDiscountWxpayRecharge());
//
//            compareProfitData(detectRes, k, "discount", TimeFormat.YEAR, null, newProfitRes.getData().getProfitList().getYear().getDiscount());
//
//
////            List<OldProfitRes.DataDTO.CacheProfitDTO.Profit.TimeAndProfit> yearIccObjects = Lists.newArrayList();
////            yearIccObjects.addAll(oldProfitRes.getData().getCacheProfit().getYear().getIccAlipay());
////            yearIccObjects.addAll(oldProfitRes.getData().getCacheProfit().getYear().getIccWxpay());
////            yearIccObjects.addAll(oldProfitRes.getData().getCacheProfit().getYear().getIccWxpayRecharge());
////            yearIccObjects.addAll(oldProfitRes.getData().getCacheProfit().getYear().getIccAlipayRecharge());
//
//            compareProfitData(detectRes, k, "icc", TimeFormat.YEAR, null, newProfitRes.getData().getProfitList().getYear().getIcc());
//        }
//        log.info("结束对比年度统计数据");


        if (device != "deviceProfit") {
            log.info("开始对比各项总收益数据");
            compareProfitArrrivise("微信总收益", null, newProfitRes.getData().getTotalProfit().getWxPay());
            compareProfitArrrivise("支付宝总收益", null, newProfitRes.getData().getTotalProfit().getAliPay());
            compareProfitArrrivise("钱包支付总收益", null, newProfitRes.getData().getTotalProfit().getPurse());

//            Integer oldIccrech = oldProfitRes.getData().getTotalProfit().getTotalIccWxpayRecharge() + oldProfitRes.getData().getTotalProfit().getTotalIccAlipayRecharge();
//            compareProfitArrrivise("IC卡充值总收益", oldIccrech, newProfitRes.getData().getTotalProfit().getIcc());

//            Integer olddiscountRech = oldProfitRes.getData().getTotalProfit().getTotalDiscountAlipayRecharge() + oldProfitRes.getData().getTotalProfit().getTotalDiscountWxpayRecharge();
//            compareProfitArrrivise("充电券充值总收益", olddiscountRech, newProfitRes.getData().getTotalProfit().getDiscount());
//            compareProfitArrrivise("投币总收益",oldProfitRes.getData().getTotalProfit().getTotalBill(),newProfitRes.getData().getTotalProfit().getBill());
//            compareProfitArrrivise("刷卡总收益",oldProfitRes.getData().getTotalProfit().getTotalAlipay(),newProfitRes.getData().getTotalProfit().getAliPay());

//            Object obj = newProfitRes.getData().getTotalProfit().getVirtual();
//            if (obj instanceof Double) {
//                Double target = (Double) obj;
//                compareProfitArrrivise("IC卡虚拟充值", oldProfitRes.getData().getTotalProfit().getTotalVirtual(), target);
//            }
            log.info("结束对比各项总收益数据");
        }

        log.info("账号：{} 对比数据结束 ", k);


        // alipay ,wxpay, purse,icc,discount，safeguard
        // todayUnArrival-todayArrival =(alipay ,wxpay, purse,icc,discount，safeguard)
        // 12点之前
        // todayUnArrival - unTransferFee = (alipay ,wxpay, purse,icc,discount，safeguard)

        NewProfitRes.DataDTO.ProfitListDTO.Profit.TimeAndProfit aliPay = newProfitRes.getData().getProfitList().getDay().getAliPay().get(1);
        NewProfitRes.DataDTO.ProfitListDTO.Profit.TimeAndProfit wxPay = newProfitRes.getData().getProfitList().getDay().getWxPay().get(1);
        NewProfitRes.DataDTO.ProfitListDTO.Profit.TimeAndProfit purse = newProfitRes.getData().getProfitList().getDay().getPurse().get(1);
        NewProfitRes.DataDTO.ProfitListDTO.Profit.TimeAndProfit icc = newProfitRes.getData().getProfitList().getDay().getIcc().get(1);
        NewProfitRes.DataDTO.ProfitListDTO.Profit.TimeAndProfit discount = newProfitRes.getData().getProfitList().getDay().getDiscount().get(1);
        NewProfitRes.DataDTO.ProfitListDTO.Profit.TimeAndProfit safeguard = newProfitRes.getData().getProfitList().getDay().getSafeguard().get(1);

        BigDecimal add = NumberUtil.add(aliPay.getProfit(), wxPay.getProfit(), purse.getProfit(), icc.getProfit(), discount.getProfit(), safeguard.getProfit());

        // 当天12点之前
        if (LocalDateTime.now().isBefore(LocalDateTime.of(LocalDate.now(), LocalTime.of(12, 0)))) {
            BigDecimal sub = NumberUtil.sub(newProfitRes.getData().getSuperviseProfit().getTodayUnArrival(), newProfitRes.getData().getSuperviseProfit().getTodayArrival());
            if (!NumberUtil.equals(add, sub)) {
                detectRes.setContext("收益不对");
            }
        }else {
            if (!NumberUtil.equals(BigDecimal.valueOf(newProfitRes.getData().getSuperviseProfit().getTodayArrival()), add)) {
                detectRes.setContext("收益不对");
            }
        }




        log.info("数据汇总~~~~~~~~~~~~~~~");
        String jsonStr = convertJsonStr(detectRes);
        log.info("数据汇总详情：{}", jsonStr);





        dingSendMessageService.sendMessage(jsonStr);


    }

    public String convertJsonStr(DetectRes detectRes) {

        // 处理detectResData按timeFormat分组
        Map<TimeFormat, List<DetectRes.DetectResData>> groupedDetect = detectRes.getDetectResData().stream()
                .collect(Collectors.groupingBy(
                        DetectRes.DetectResData::getTimeFormat,
                        Collectors.mapping(d -> new DetectRes.DetectResData(d.getPayType(), d.getAverage(), d.getCurrent()),
                                Collectors.toList())
                ));

        // 处理ProfitResData按payType分组
        Map<String, List<DetectRes.ProfitResData>> groupedProfit = detectRes.getProfitResData().stream()
                .collect(Collectors.groupingBy(
                        DetectRes.ProfitResData::getPayType,
                        Collectors.mapping(p -> new DetectRes.ProfitResData(p.getTime(), p.getOldData(), p.getNewData()),
                                Collectors.toList())
                ));

        Map<Object, Object> objectObjectMap = Maps.newLinkedHashMap();
        objectObjectMap.put("phone", detectRes.getPhone());
        objectObjectMap.put("detectResData", groupedDetect);
//        objectObjectMap.put("profitResData", groupedProfit);
        objectObjectMap.put("context", detectRes.getContext());

        return JSONUtil.toJsonStr(objectObjectMap);
    }


}
