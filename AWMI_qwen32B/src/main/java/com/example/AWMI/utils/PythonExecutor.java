package com.example.AWMI.utils;

import java.io.*;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;

public class PythonExecutor {

    public static void runPythonScript(String scriptPath, String inputData, ResponseBodyEmitter emitter) {
        try {
            ProcessBuilder processBuilder = new ProcessBuilder(
                    "/home/sdu/anaconda3/envs/lightrag/bin/python", scriptPath);
            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();

            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(process.getOutputStream(), "UTF-8"));
            writer.write(inputData);
            writer.newLine();
            writer.flush();
            writer.close();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), "UTF-8"));
            String line;

            // 直接读取 Python 输出并流式发送
            while ((line = reader.readLine()) != null) {
                emitter.send(line + "\n"); // 逐行发送输出
            }

            emitter.complete();
            reader.close();
            process.waitFor();
        } catch (Exception e) {
            try {
                emitter.send("Error: " + e.getMessage());
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            emitter.completeWithError(e);
        }
    }
}
