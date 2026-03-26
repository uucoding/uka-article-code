package com.uka.springai.demo;

import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.document.Document;
import org.springframework.ai.document.MetadataMode;
import org.springframework.ai.model.transformer.KeywordMetadataEnricher;
import org.springframework.ai.model.transformer.SummaryMetadataEnricher;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.Resource;

import java.util.List;

@SpringBootTest(classes = SpringAiDemo009Application.class)
public class DocumentTest {


    @Autowired
    private ChatModel chatModel;

    @Value("classpath:/docs/alibaba-java-guide.pdf")
    private Resource pdfResource;


    @Test
    void processEtlPipeline() {
        System.out.println("--- 1. 执行 ETL-E (Extract) 读取 PDF ---");
        PagePdfDocumentReader reader = new PagePdfDocumentReader(pdfResource);
        List<Document> rawDocuments = reader.get();
        System.out.println("读取到长文档总页数: " + rawDocuments.size());

        System.out.println("\n--- 2. 执行 ETL-T (Transform): 语义 Token 切块 ---");
        // 初始化语义切分器：默认 每块最多 800 Token，重叠区 100 Token
        SemanticTokenTextSplitter splitter = SemanticTokenTextSplitter.builder().build();

        List<Document> chunkedDocs = splitter.apply(rawDocuments);
        System.out.println("成功切分为小文本块(Chunks)数量: " + chunkedDocs.size());

        System.out.println("\n--- 3. 执行 ETL-T (Transform): 高阶 AI 元数据增强 ---");

        // 3.1 关键字提取器 (告诉大模型：帮我给每个块提取最多 5 个核心关键字)
        KeywordMetadataEnricher keywordEnricher = new KeywordMetadataEnricher(chatModel, 5);

        // 3.2 摘要提取器 (告诉大模型：帮我总结这个块的核心思想)
        SummaryMetadataEnricher summaryEnricher = new SummaryMetadataEnricher(chatModel,
                List.of(SummaryMetadataEnricher.SummaryType.CURRENT),
                """
                这是该章节的内容：
                {context_str}
                
                请总结该章节的关键主题和实体，并使用中文回答。
                
                总结：
                """
                , MetadataMode.ALL); // CURRENT 仅总结当前块内容

        // 执行加工流水线：给被切碎的文档块，打上高价值的 AI 标签！
        System.out.println("正在使用大模型阅读文本块并提取特征 (这可能需要几十秒)...");
        List<Document> enrichedDocs = keywordEnricher.apply(chunkedDocs);
        enrichedDocs = summaryEnricher.apply(enrichedDocs);

        // 4. 窥探最终的完美数字资产
        System.out.println("\n========== 加工完成 ==========");
        for (int i = 0; i < Math.min(2, enrichedDocs.size()); i++) {
            Document chunk = enrichedDocs.get(i);
            System.out.println("\n【文本块 " + (i+1) + " ID】: " + chunk.getId());
            System.out.println("【截取内容】: " + chunk.getText().replace("\n", "").substring(0, Math.min(60, chunk.getText().length())) + "...");

            // 见证奇迹的时刻：Metadata 里多出了关键字 和摘要
            System.out.println("【提取的关键字 (Keywords)】: " + chunk.getMetadata().get("excerpt_keywords"));
            System.out.println("【提取的摘要 (Summary)】: " + chunk.getMetadata().get("section_summary"));
            System.out.println("【溯源页码】: " + chunk.getMetadata().get("page_number"));
        }
    }

}
