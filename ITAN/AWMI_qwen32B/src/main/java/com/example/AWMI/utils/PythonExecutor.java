package com.example.AWMI.utils;

import java.io.*;
import java.util.regex.*;

public class PythonExecutor {
//    public static String runPythonScript(String scriptPath, String inputData) {
//        try {
////            ProcessBuilder processBuilder = new ProcessBuilder(
////                    "/home/sdu/anaconda3/envs/lightrag/bin/python", scriptPath);
//            ProcessBuilder processBuilder = new ProcessBuilder(
//                    "/home/sdu/anaconda3/envs/lightrag/bin/python", scriptPath);
//            processBuilder.redirectErrorStream(true);
//            Process process = processBuilder.start();
//
//            // **1. 使用 UTF-8 编码向 Python 进程写入数据**
//            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(process.getOutputStream(), "UTF-8"));
//            writer.write(inputData);
//            writer.newLine();
//            writer.flush();
//            writer.close();
//
//            // **2. 使用 UTF-8 编码读取 Python 进程的输出**
//            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), "UTF-8"));
//            StringBuilder output = new StringBuilder();
//            String line;
//            while ((line = reader.readLine()) != null) {
//                output.append(line).append("\n");
//            }
//            reader.close();
//
//            process.waitFor();
//            return output.toString().trim();
//        } catch (Exception e) {
//            e.printStackTrace();
//            return "Error executing Python script";
//        }
//    }
//public static String runPythonScript(String scriptPath, String inputData) {
//    try {
//        ProcessBuilder processBuilder = new ProcessBuilder(
//                "/home/sdu/anaconda3/envs/lightrag/bin/python", scriptPath);
//        processBuilder.redirectErrorStream(true);
//        Process process = processBuilder.start();
//
//        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(process.getOutputStream(), "UTF-8"));
//        writer.write(inputData);
//        writer.newLine();
//        writer.flush();
//        writer.close();
//
//        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), "UTF-8"));
//        StringBuilder output = new StringBuilder();
//        String line;
//        boolean startCapturing = false;
//
//        while ((line = reader.readLine()) != null) {
//            if (!startCapturing && line.startsWith("###")) {
//                startCapturing = true; // **遇到第一个 "###" 后，开始收集内容**
//                output.setLength(0);   // **清空之前的日志信息**
//            }
//            if (startCapturing) {
//                output.append(line).append("\n");
//            }
//        }
//        reader.close();
//        process.waitFor();
//
//        return output.toString().trim(); // **确保返回干净数据**
//    } catch (Exception e) {
//        e.printStackTrace();
//        return "Error executing Python script";
//    }
//}

        public static String runPythonScript(String scriptPath, String inputData) {
            try {
                ProcessBuilder processBuilder = new ProcessBuilder(
                        "/home/sdu/anaconda3/envs/lightrag/bin/python", scriptPath);
                processBuilder.redirectErrorStream(true);
                Process process = processBuilder.start();

                // 向 Python 传输输入数据
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(process.getOutputStream(), "UTF-8"));
                writer.write(inputData);
                writer.newLine();
                writer.flush();
                writer.close();

                // 读取 Python 输出
                BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), "UTF-8"));
                StringBuilder output = new StringBuilder();
                StringBuilder rawOutput = new StringBuilder(); // 存储所有输出，防止正则匹配丢失信息
                String line;

                // 正则表达式匹配 "###" 之后的文本
                Pattern pattern = Pattern.compile("###\\s*(.*)", Pattern.DOTALL | Pattern.MULTILINE);
                Matcher matcher;
                boolean capturing = false;

                while ((line = reader.readLine()) != null) {
                    rawOutput.append(line).append("\n"); // 记录所有输出，调试用
                }

                reader.close();
                process.waitFor();

                // 在完整的输出中匹配
                matcher = pattern.matcher(rawOutput.toString());
                if (matcher.find()) {
                    capturing = true;
                    output.append(matcher.group(1)).append("\n");
                    output.append(rawOutput.substring(matcher.end()).trim()); // 继续收集后续内容
                }

                return capturing ? output.toString().trim() : "No valid output found";
            } catch (Exception e) {
                e.printStackTrace();
                return "Error executing Python script";
            }
        }




//    public static void main(String[] args) {
//        String inputData = "你好，测试一下"; // Java 需要传递给 Python 的字符串
//        String output = runPythonScript("D:\\易学大模型数据\\selfQA\\scripts\\util\\Test225.py", inputData);
//        System.out.println(output);
//    }
}
