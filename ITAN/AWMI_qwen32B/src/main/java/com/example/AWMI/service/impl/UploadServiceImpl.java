package com.example.AWMI.service.impl;

import com.example.AWMI.service.UploadService;
import net.sourceforge.tess4j.ITessAPI;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.util.TempFile;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.springframework.stereotype.Service;
import com.example.AWMI.entity.FileEntity;

import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;


import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;


@Service
public class UploadServiceImpl implements UploadService {

    /**
     * 提取 PDF 文件内容并返回 FileEntity 对象
     * @param filePath 文件路径
     * @return FileEntity 对象
     * @throws IOException 文件读取失败时抛出
     */
    @Override
    public FileEntity readPdf(String filePath) throws IOException {
        StringBuilder content = new StringBuilder();

        try (PDDocument document = PDDocument.load(new File(filePath))) {
            if (document.isEncrypted()) {
                throw new IOException("Encrypted documents are not supported");
            }

            PDFTextStripper textStripper = new PDFTextStripper();
            String pdfText = textStripper.getText(document);

            // 按行处理文本
            String[] lines = pdfText.split("\\r?\\n");
            for (String line : lines) {
                content.append(line).append("\n");
                // System.out.println(line);
            }
        }

        // 创建 FileEntity 对象
        String fileName = new File(filePath).getName();
        return new FileEntity(fileName, "pdf", content.toString());
    }

    @Override
    public FileEntity readWord(String filePath) throws IOException {
        StringBuilder content = new StringBuilder();

        try (FileInputStream fis = new FileInputStream(new File(filePath));
             XWPFDocument document = new XWPFDocument(fis);
             XWPFWordExtractor extractor = new XWPFWordExtractor(document)) {

            // 提取 Word 文档内容
            String wordText = extractor.getText();
            content.append(wordText);
        } catch (IOException e) {
            e.printStackTrace();
            throw new IOException("Failed to read Word file: " + e.getMessage(), e);
        }

        // 获取文件名
        String fileName = new File(filePath).getName();
        return new FileEntity(fileName, "word", content.toString());
    }

    @Override
    public FileEntity readPicture(String filePath) throws IOException {
        Tesseract tesseract = new Tesseract();
        tesseract.setDatapath("D:\\ITAN\\tessdata-main\\tessdata-main");
        tesseract.setLanguage("chi_tra");
        tesseract.setOcrEngineMode(ITessAPI.TessOcrEngineMode.OEM_LSTM_ONLY);
        try {
            String result = tesseract.doOCR(new File(filePath));
            String fileName = new File(filePath).getName();
            return new FileEntity(fileName, "image", result);
        } catch (TesseractException e) {
            throw new IOException("Error during OCR processing", e);
        }
    }
}