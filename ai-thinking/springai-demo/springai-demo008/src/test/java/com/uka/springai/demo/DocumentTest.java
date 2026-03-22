package com.uka.springai.demo;

import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.content.Media;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.ExtractedTextFormatter;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.reader.pdf.config.PdfDocumentReaderConfig;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.Resource;
import org.springframework.util.MimeTypeUtils;

import java.util.Base64;
import java.util.List;

@SpringBootTest(classes = SpringAiDemo007Application.class)
public class DocumentTest {


    @Autowired
    private ChatClient.Builder chatClientBuilder;

    // 1. 使用 Spring 的 Resource 抽象读取类路径下的文件
    // （实际开发环境中，这里通常是上传的 MultipartFile 转成的 Resource）
    @Value("classpath:/docs/sample.pdf")
    private Resource samplePdfResource;

    @Test
    void extractWithTika() {
        // 2. 实例化 TikaDocumentReader 并传入物理资源
        TikaDocumentReader reader = new TikaDocumentReader(samplePdfResource);

        // 3. 调用 get() 方法，触发底层引擎全量解析！
        List<Document> documents = reader.get();

        // 4. 打印窥探结果
        Document doc = documents.get(0);
        System.out.println("【文档 ID】: " + doc.getId());
        System.out.println("【文档内容前100字】: " +
                doc.getText().substring(0, Math.min(100, doc.getText().length())));
        System.out.println("【元数据 Metadata】: " + doc.getMetadata());
    }

    @Value("classpath:/docs/sample2.pdf")
    private Resource sample2PdfResource;
    /**
     * 按页面解析
     */
    @Test
    void extractWithPdfReader() {
        // 1. PDF 解析规则配置
        PdfDocumentReaderConfig config = PdfDocumentReaderConfig.builder()
                .withPageTopMargin(0)    // 忽略页眉 (防止无关文本干扰 AI)
                .withPageBottomMargin(0) // 忽略页脚
                .withPageExtractedTextFormatter(ExtractedTextFormatter.builder()
                        .withNumberOfTopTextLinesToDelete(0)
                        .build())
                .build();

        // 2. 实例化专用的 PagePdfDocumentReader
        PagePdfDocumentReader reader = new PagePdfDocumentReader(sample2PdfResource, config);

        // 3. 触发解析
        List<Document> documents = reader.get();

        System.out.println("共解析出 " + documents.size() + " 页独立内容。");

        // 4. 每一页的数据
        for (int i = 0; i < documents.size(); i++) {
            Document doc = documents.get(i);
            System.out.println("\n--- 第 " + (i + 1) + " 个 Document 对象 ---");
            System.out.println("【文档 ID】: " + doc.getId());
            System.out.println("【纯文本内容】: " + doc.getText().trim());
            // 重点看这里的输出！
            System.out.println("【元数据 Metadata】: " + doc.getMetadata());
        }
    }

    /**
     * 多模态解析
     */
    @Test
    void extractWithSeparatedMultimodalPdfReader() {
        // 1. PDF 解析规则配置
        PdfDocumentReaderConfig config = PdfDocumentReaderConfig.builder()
                .withPageTopMargin(0)    // 忽略页眉 (防止无关文本干扰 AI)
                .withPageBottomMargin(0) // 忽略页脚
                .withPageExtractedTextFormatter(ExtractedTextFormatter.builder()
                        .withNumberOfTopTextLinesToDelete(0)
                        .build())
                .build();

        // 2. 实例化专用的 SeparatedMultimodalPdfReader
        SeparatedMultimodalPdfReader reader = new SeparatedMultimodalPdfReader(sample2PdfResource, config);

        // 3. 触发解析
        List<Document> documents = reader.get();

        System.out.println("共解析出 " + documents.size() + " 页独立内容。");

        // 4. 每一页的数据
        for (int i = 0; i < documents.size(); i++) {
            Document doc = documents.get(i);
            System.out.println("\n--- 第 " + (i + 1) + " 个 Document 对象 ---");
            System.out.println("【文档 ID】: " + doc.getId());
            if (doc.isText()) {
                System.out.println("【纯文本内容】: " + doc.getText().trim());
            } else {
                UserMessage userMessage = UserMessage.builder()
                        .text("查看这张图的内容")
                        .media(Media.builder()
                                .mimeType(MimeTypeUtils.IMAGE_PNG)
                                .data(doc.getMedia().getData())
                                .build())
                        .build();
                String content = chatClientBuilder.build().prompt()
                        .messages(userMessage)
                        .call()
                        .content();
                System.out.println("【图片内容】: " + content);
            }

            // 重点看这里的输出！
            System.out.println("【元数据 Metadata】: " + doc.getMetadata());
        }
    }
}
