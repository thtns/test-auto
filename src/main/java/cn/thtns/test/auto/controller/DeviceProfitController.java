package cn.thtns.test.auto.controller;


import cn.hutool.json.JSONArray;
import cn.thtns.test.auto.request.LoginReq;
import cn.thtns.test.auto.service.DeviceProfitService;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.apache.commons.compress.utils.Lists;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.util.ResourceUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.swing.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("")
@AllArgsConstructor
public class DeviceProfitController {

    private final DeviceProfitService deviceProfitService;

    @PostMapping("profitComparison")
    public void profitComparison(@RequestBody List<LoginReq> loginReqs) {
        deviceProfitService.profitComparison(loginReqs);
    }

    @PostMapping("excel")
    @SneakyThrows
    public void excel() {

        ArrayList<LoginReq> objects = Lists.newArrayList();

        File file = ResourceUtils.getFile("account.xlsx");
        Workbook workbook = new XSSFWorkbook(file);

        // 获取第一个工作表
        Sheet sheet = workbook.getSheetAt(0);

        // 遍历每一行
        for (Row row : sheet) {
            // 获取第二列（手机号）和第三列（公司ID）
            Cell companyCell = row.getCell(2);
            Cell phoneCell = row.getCell(1);

            // 确保单元格不为空
            if (phoneCell != null && companyCell != null) {

                // 获取单元格的值
                String phoneNumber = getCellValueAsString(phoneCell);
                String companyNum = getCellValueAsString(companyCell);
                objects.add(new LoginReq(phoneNumber, companyNum));
                // 关闭工作簿和文件流
                workbook.close();
            }
        }
        profitComparison(objects);
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
