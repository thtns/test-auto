package cn.thtns.test.auto.utils;

import cn.thtns.test.auto.enums.TimeFormat;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.TemporalAccessor;

public class TimeParser {
    /**
     * 解析时间字符串，自动识别格式
     */
    public static TemporalAccessor parseTime(String timeStr) {
        // 尝试按 DAY 格式解析
        try {
            return LocalDate.parse(timeStr, DateTimeFormatter.ISO_LOCAL_DATE);
        } catch (DateTimeParseException ignored) {}


        // 尝试按 WEEK 格式解析
        try {
            return LocalDate.parse(timeStr, DateTimeFormatter.ISO_LOCAL_DATE);
        } catch (DateTimeParseException ignored) {}


        // 尝试按 MONTH 格式解析
        try {
            return YearMonth.parse(timeStr, DateTimeFormatter.ofPattern("yyyy-MM"));
        } catch (DateTimeParseException ignored) {}

        // 尝试按 SEASON 格式解析 (例如: 2025_02)
        if (timeStr.matches("\\d{4}_\\d{2}")) {
            String[] parts = timeStr.split("_");
            int year = Integer.parseInt(parts[0]);
            int quarter = Integer.parseInt(parts[1]);
            return YearMonth.of(year, quarter * 3); // 转换为季度最后一个月
        }

        // 尝试按 YEAR 格式解析
        try {
            return java.time.Year.parse(timeStr);
        } catch (DateTimeParseException ignored) {}

        throw new IllegalArgumentException("无法解析的时间格式: " + timeStr);
    }

    /**
     * 判断时间字符串的格式类型
     */
    public static TimeFormat detectFormat(String timeStr) {
        if (timeStr.matches("\\d{4}-\\d{2}-\\d{2}")) {
            return TimeFormat.DAY;
        } else if (timeStr.matches("\\d{4}-\\d{2}-\\d{2}-\\d{4}-\\d{2}-\\d{2}")) {
            return TimeFormat.WEEK;
        }else if (timeStr.matches("\\d{4}-\\d{2}")) {
            return TimeFormat.MONTH;
        } else if (timeStr.matches("\\d{4}_\\d{2}")) {
            return TimeFormat.SEASON;
        } else if (timeStr.matches("\\d{4}")) {
            return TimeFormat.YEAR;
        } else {
            throw new IllegalArgumentException("未知的时间格式: " + timeStr);
        }
    }
}