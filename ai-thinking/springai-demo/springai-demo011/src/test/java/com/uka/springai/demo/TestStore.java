package com.uka.springai.demo;

import org.junit.jupiter.api.Test;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.Resource;

import java.io.File;
import java.util.List;

@SpringBootTest(classes = SpringAiDemo010Application.class)
public class TestStore {



    @Value("classpath:/docs/alibaba-java-guide.pdf")
    private Resource pdfResource;

    @Autowired
    private VectorStore vectorStore;

    @Test
    void simpleStoreTest() {
        // ---------------- 1. E (Extract) - 读取文件 ----------------
        System.out.println("--- 1. 执行 ETL-E (Extract) 读取 PDF ---");
        PagePdfDocumentReader reader = new PagePdfDocumentReader(pdfResource);
        List<Document> rawDocuments = reader.get();
        System.out.println("读取到长文档总页数: " + rawDocuments.size());

        // ---------------- 2. T (Transform) - 智能滑窗文本切块 ----------------
        System.out.println("2、[Transform] 执行 Token 滑动窗口防割裂切分...");
        TokenTextSplitter splitter = new TokenTextSplitter(800, 350, 5, 10000, true);
        List<Document> chunkedDocuments = splitter.apply(rawDocuments);
        System.out.println("切分为 " + chunkedDocuments.size() + " 个文本块。");

        // ---------------- 3. L (Load) - 向量化并落库 ----------------
        System.out.println("3、 [Load] 调用模型生成向量数据，并入库...");

        // Spring AI 会自动批量拦截 -> 发起 Embedding 请求 -> 写入数据库。
//        vectorStore.add(chunkedDocuments);

        // 如果我们使用的是 SimpleVectorStore ，为了防止断电丢失，手动触发一次持久化
        if (vectorStore instanceof SimpleVectorStore simpleStore) {
            File vectorStoreFile = new File("local_vector_store.json");
            simpleStore.save(vectorStoreFile);
            System.out.println("持久化到本地 JSON 文件。");
        }
    }

}
