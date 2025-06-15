package com.example.AWMI.controller;

import com.example.AWMI.entity.FileEntity;
import com.example.AWMI.service.UploadService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


@RestController
@CrossOrigin
public class FileUploadController {

    private final UploadService uploadService;

    @Autowired
    public FileUploadController(UploadService uploadService) {
        this.uploadService = uploadService;
    }

    private static final String UPLOAD_DIR = "/home/sdu/server/ITAN/java/file_upload";

    @PostMapping("/upload")
    public ResponseEntity<Map<String, String>> uploadFile(@RequestParam("file") MultipartFile file) {
        Map<String, String> response = new HashMap<>();

        // 检查文件是否为空
        if (file.isEmpty()) {
            response.put("message", "Please select a file to upload.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        try {
            // 检查上传目录是否存在，不存在则创建
            File uploadDir = new File(UPLOAD_DIR);
            if (!uploadDir.exists()) {
                uploadDir.mkdirs();
            }

            // 获取文件名并生成唯一文件名
            String originalFileName = file.getOriginalFilename();
            String uniqueFileName = UUID.randomUUID().toString() + "_" + originalFileName;

            // 构建文件的完整路径
            Path filePath = Paths.get(UPLOAD_DIR + File.separator + uniqueFileName);

            // 将文件保存到指定位置
            InputStream inputStream = file.getInputStream();
            Files.copy(inputStream, filePath);
            inputStream.close();

            String fileType = getFileType(file.getOriginalFilename());

            // 调用 Service 提取文件内容并创建 FileEntity 对象
            FileEntity fileEntity = new FileEntity();
            switch (fileType){
                case "pdf":
                    fileEntity = uploadService.readPdf(filePath.toString());
                    break;
                case "word":
                    fileEntity = uploadService.readWord(filePath.toString());
                    break;
                case "image":
                    fileEntity = uploadService.readPicture(filePath.toString());
                    break;
            }

            // 打印提取的文字到控制台
            System.out.println("Extracted Text from File:");
            String content = fileEntity.getFileContent();
            System.out.println(content);

            // 返回成功响应
            response.put("message", "File uploaded successfully.");
            response.put("fileName", uniqueFileName);
            response.put("content", content);
            return ResponseEntity.ok(response);
        } catch (IOException e) {
            // 返回失败响应
            response.put("message", "File upload failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 根据文件名获取文件类型
     * @param fileName 文件名
     * @return 文件类型（pdf, word, 图片等）
     */
    private String getFileType(String fileName) {
        if (fileName.toLowerCase().endsWith(".pdf")) {
            return "pdf";
        } else if (fileName.toLowerCase().endsWith(".docx") || fileName.toLowerCase().endsWith(".doc")) {
            return "word";
        } else if (fileName.toLowerCase().endsWith(".jpg") || fileName.toLowerCase().endsWith(".png")) {
            return "image";
        } else {
            return "unknown";
        }
    }

}
