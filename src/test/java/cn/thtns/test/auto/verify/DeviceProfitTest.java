package cn.thtns.test.auto.verify;

import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import cn.thtns.test.auto.entity.SysUser;
import cn.thtns.test.auto.mapper.SysUserMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.qameta.allure.Description;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.client.RestClient;
import org.testng.collections.Lists;
import org.testng.collections.Maps;

import org.apache.poi.ss.usermodel.*;

import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.WeekFields;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author liuyj
 * Node 收益比对
 */
@SpringBootTest
@Slf4j
public class DeviceProfitTest {

    private static final String AUTHORIZATION_HEADER = "authorization";
    private static final String OLD_PROFIT_API = "https://agentv2.wanzhuangkj.com/api/profit/get/all?profit_type=dealer";

    // 分组收益
    private static final String OLD_PROFIT_API_group = "https://agentv2.wanzhuangkj.com/api/profit/get/all?profit_type=group&group_id={}";
    private static final String NEW_PROFIT_API_group = "https://agentv2.wanzhuangkj.com/api/operator/income/profitReport?groupId={}";

    // 设备类型
    private static final String OLD_PROFIT_API_DEVICE_TYPE = "https://agentv2.wanzhuangkj.com/api/device/getDeviceType?group_id={}";

    // 根据设备类型查设备
    private static final String OLD_PROFIT_API_DEVICE_BY_TYPE = "https://agentv2.wanzhuangkj.com/api/device/getDevicesByType?page=1&limit=10&device_type={}&group_id={}";

    // 设备收益
    private static final String DEVICE_PROFIT = "https://agentv2.wanzhuangkj.com/api/profit/get/all?profit_type=device&device_num={}";

    private static final String NEW_DevicePROFIT_API = "https://agentv2.wanzhuangkj.com/api/operator/income/profitReport?deviceId={}";




    //获取分组
    private static final String GET_DEVICE_GROUP_LIST = "https://agentv2.wanzhuangkj.com/api/device/getDeviceGroupList?page=1&limit=20&need_count=1";





    @Autowired
    private RestClient restClient;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TokenManager tokenManager;

    @Autowired
    private SysUserMapper sysUserMapper;


    private final Map<String, String> phoneMap = Maps.newHashMap();


    {
            try {
                // 读取 Excel 文件
                FileInputStream file = new FileInputStream("E:\\Desktop\\测试用户账号.xlsx");
                Workbook workbook = new XSSFWorkbook(file);

                // 获取第一个工作表
                Sheet sheet = workbook.getSheetAt(0);

                // 遍历每一行
                for (Row row : sheet) {
                    // 获取第一列（电话号码）和第二列（密码）
                    Cell phoneCell = row.getCell(0);
                    Cell passwordCell = row.getCell(1);

                    // 确保单元格不为空
                    if (phoneCell != null && passwordCell != null) {
                        // 获取单元格的值
                        String phoneNumber = getCellValueAsString(phoneCell);
                        String password = getCellValueAsString(passwordCell);

                        // 将数据放入 phoneMap
                        phoneMap.put(phoneNumber, password);
                    }
                }

                // 关闭工作簿和文件流
                workbook.close();
                file.close();

            } catch (IOException e) {
                e.printStackTrace();
            }
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
        oldData.forEach(ow -> newData.forEach(nw -> {
            if (ow.getTime().equals(nw.getTime()) && !compareProfitByTime(ow, nw)) {
                log.info(StrUtil.format("{}支付，日期：{} 数据不一致，旧接口数据：{}，新接口数据：{}", paymentType, ow.getTime(), ow.getProfit(), nw.getProfit()));
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

        List<SysUser> userVoByUsername = sysUserMapper.listAll();

//        log.info("user:{}",userVoByUsername);


        for (Map.Entry<String, String> entry : phoneMap.entrySet()) {


            String k = entry.getKey();
            String v = entry.getValue();
            String bearerToken = tokenManager.getToken(k, v);

            log.info("账号：{} 正在对比数据", k);

//            OldProfitRes oldProfitRes = objectMapper.readValue(getApiResponse(OLD_PROFIT_API, bearerToken), OldProfitRes.class);
//            NewProfitRes newProfitRes = objectMapper.readValue(getApiResponse(NEW_PROFIT_API, bearerToken), NewProfitRes.class);

            DeviceGroupRes deviceGroupRes = objectMapper.readValue(getApiResponse(GET_DEVICE_GROUP_LIST, bearerToken), DeviceGroupRes.class);

            List<DeviceGroupRes.Data.ListBean> list = deviceGroupRes.getData().getList().stream().limit(3).toList();
            for (DeviceGroupRes.Data.ListBean listBean : list) {
                Integer groupId = listBean.getGroupId();
                //分组收益
                OldProfitRes oldProfitResGroup = objectMapper.readValue(getApiResponse(StrUtil.format(OLD_PROFIT_API_group,groupId), bearerToken), OldProfitRes.class);
                NewProfitRes newProfitResGroup = objectMapper.readValue(getApiResponse(StrUtil.format(NEW_PROFIT_API_group,groupId), bearerToken), NewProfitRes.class);

                DeviceTypeRes deviceTypeRes = objectMapper.readValue(getApiResponse(StrUtil.format(OLD_PROFIT_API_DEVICE_TYPE,groupId), bearerToken), DeviceTypeRes.class);

                for (DeviceTypeRes.DataBean datum : deviceTypeRes.getData()) {
                    Integer deviceType = datum.getDeviceType();
                    DeviceByTypeRes deviceByTypeRes = objectMapper.readValue(getApiResponse(StrUtil.format(OLD_PROFIT_API_DEVICE_BY_TYPE,deviceType,groupId), bearerToken), DeviceByTypeRes.class);
                    for (DeviceByTypeRes.Data.ListBean bean : deviceByTypeRes.getData().getList()) {

                        Long deviceNum = bean.getDeviceNum();
                        Integer deviceId = bean.getId();
                        //设备收益
                        OldProfitRes deviceProfit = objectMapper.readValue(getApiResponse(StrUtil.format(DEVICE_PROFIT,deviceNum), bearerToken), OldProfitRes.class);
                        NewProfitRes newDeviceProfit = objectMapper.readValue(getApiResponse(StrUtil.format(NEW_DevicePROFIT_API,deviceId), bearerToken), NewProfitRes.class);
                        log.info("deviceProfit:{}",JSONUtil.toJsonStr(deviceProfit));
//                      对比设备收益数据
                        verify(deviceProfit, newDeviceProfit, k);
                    }

                }

//             对比分组收益数据
                verify(oldProfitResGroup, newProfitResGroup, k);

//                log.info("oldProfitResGroup:{}",JSONUtil.toJsonStr(oldProfitResGroup));

            }





        }


    }

    private void verify(OldProfitRes oldProfitRes, NewProfitRes newProfitRes, String k) {
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
        compareProfitData("bill", oldProfitRes.getData().getCacheProfit().getSeason().getBill(), newProfitRes.getData().getProfitList().getSeason().getBill());
        compareProfitData("card", oldProfitRes.getData().getCacheProfit().getSeason().getCard(), newProfitRes.getData().getProfitList().getSeason().getCard());
        compareProfitData("purse", oldProfitRes.getData().getCacheProfit().getSeason().getPurse(), newProfitRes.getData().getProfitList().getSeason().getPurse());
        compareProfitData("safeguard", oldProfitRes.getData().getCacheProfit().getSeason().getSafeguard(), newProfitRes.getData().getProfitList().getSeason().getSafeguard());
        compareProfitData("virtual", oldProfitRes.getData().getCacheProfit().getSeason().getIccVirtual(), newProfitRes.getData().getProfitList().getSeason().getVirtual());

        List<OldProfitRes.DataDTO.CacheProfitDTO.Profit.TimeAndProfit> seasonDiscountObjects = Lists.newArrayList();
        seasonDiscountObjects.addAll(oldProfitRes.getData().getCacheProfit().getSeason().getDiscountAlipayRecharge());
        seasonDiscountObjects.addAll(oldProfitRes.getData().getCacheProfit().getSeason().getDiscountWxpayRecharge());

        compareProfitData("discount", add(seasonDiscountObjects), newProfitRes.getData().getProfitList().getSeason().getDiscount());


        List<OldProfitRes.DataDTO.CacheProfitDTO.Profit.TimeAndProfit> seasonIccObjects = Lists.newArrayList();
        seasonIccObjects.addAll(oldProfitRes.getData().getCacheProfit().getSeason().getIccAlipay());
        seasonIccObjects.addAll(oldProfitRes.getData().getCacheProfit().getSeason().getIccWxpay());
        seasonIccObjects.addAll(oldProfitRes.getData().getCacheProfit().getSeason().getIccWxpayRecharge());
        seasonIccObjects.addAll(oldProfitRes.getData().getCacheProfit().getSeason().getIccAlipayRecharge());

        compareProfitData("icc", add(seasonIccObjects), newProfitRes.getData().getProfitList().getSeason().getIcc());

        log.info("结束对比季度统计数据");

        log.info("开始对比年度统计数据");
        compareProfitData("wxPay", oldProfitRes.getData().getCacheProfit().getYear().getWxpay(), newProfitRes.getData().getProfitList().getYear().getWxPay());
        compareProfitData("aliPay", oldProfitRes.getData().getCacheProfit().getYear().getAlipay(), newProfitRes.getData().getProfitList().getYear().getAliPay());
        compareProfitData("bill", oldProfitRes.getData().getCacheProfit().getYear().getBill(), newProfitRes.getData().getProfitList().getYear().getBill());
        compareProfitData("card", oldProfitRes.getData().getCacheProfit().getYear().getCard(), newProfitRes.getData().getProfitList().getYear().getCard());
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


        log.info("账号：{} 对比数据结束 ", k);
    }

    private String getApiResponse(String apiUrl, String BEARER_TOKEN) {
        return restClient.get()
                .uri(apiUrl)
                .header(AUTHORIZATION_HEADER, BEARER_TOKEN)
                .retrieve()
                .body(String.class);
    }


    public String getCellValueAsString(Cell cell) {
        if (cell == null) {
            return ""; // 如果单元格为空，返回空字符串
        }
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue(); // 字符串类型
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue().toString(); // 日期类型
                } else {
                    return String.valueOf(cell.getNumericCellValue()); // 数字类型
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue()); // 布尔类型
            case FORMULA:
                return cell.getCellFormula(); // 公式类型
            case BLANK:
                return ""; // 空单元格
            default:
                return "";
        }
    }
}
