package com.example.AWMI.utils;

import java.io.*;
import java.util.regex.*;

public class testWhat {
    public static String extractAfterDollarDollar(String input) {
        // 定义正则表达式：匹配 $$ 及其后面的内容
        Pattern pattern = Pattern.compile("\\$\\$(.*)", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(input);

        if (matcher.find()) {
            return matcher.group(1).trim(); // 提取 $$ 之后的内容，并去除首尾空格
        }
        return ""; // 如果没有匹配到 $$，返回空字符串
    }

    public static void main(String[] args) {
        // 测试字符串
        String testStr = "INFO: some logs...\nDEBUG: more logs...\n$$ 这是 Pyth$$on 输出$$的数据\n继续的$$内容\nEND";

        // 运行方法
        String result = extractAfterDollarDollar(testStr);

        // 打印结果
        System.out.println(result);
    }
}
