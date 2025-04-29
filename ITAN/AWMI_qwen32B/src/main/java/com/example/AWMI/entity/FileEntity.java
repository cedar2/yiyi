package com.example.AWMI.entity;

public class FileEntity {
    private String fileName;       // 文件名
    private String fileType;       // 文件类型（pdf, word, 图片等）
    private String fileContent;    // 文件文本内容

    // 构造方法
    public FileEntity(String fileName, String fileType, String fileContent) {
        this.fileName = fileName;
        this.fileType = fileType;
        this.fileContent = fileContent;
    }

    public FileEntity() {}

    // Getter 和 Setter 方法
    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public String getFileContent() {
        return fileContent;
    }

    public void setFileContent(String fileContent) {
        this.fileContent = fileContent;
    }

    @Override
    public String toString() {
        return "FileEntity{" +
                "fileName='" + fileName + '\'' +
                ", fileType='" + fileType + '\'' +
                ", fileContent='" + fileContent + '\'' +
                '}';
    }
}