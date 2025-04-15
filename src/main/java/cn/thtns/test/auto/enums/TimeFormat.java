package cn.thtns.test.auto.enums;

public enum TimeFormat {
    DAY("yyyy-MM-dd"),       // 例如: 2025-04-14
    WEEK("yyyy/MM/dd-yyyy/MM/dd"),       // 例如: 2025-04-14

    MONTH("yyyy-MM"),        // 例如: 2025-04
    SEASON("yyyy_QQ"),       // 例如: 2025_02 (表示2025年第2季度)
    YEAR("yyyy");            // 例如: 2025

    private final String pattern;

    TimeFormat(String pattern) {
        this.pattern = pattern;
    }

    public String getPattern() {
        return pattern;
    }
}