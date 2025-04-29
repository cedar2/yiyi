package com.example.AWMI.service;

import com.example.AWMI.entity.FileEntity;

import java.io.IOException;

/**
 * UploadService 接口，定义 上传文件后的基本操作。
 * @param filePath 文件路径
 * @return PDF文件内容
 * @throws IOException 文件读取失败时抛出
 */

public interface UploadService {
    FileEntity readPdf(String filePath) throws IOException;

    FileEntity readWord(String filePath) throws IOException;

    FileEntity readPicture(String filePath) throws IOException;

}
