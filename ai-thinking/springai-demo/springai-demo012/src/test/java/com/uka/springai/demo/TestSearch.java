package com.uka.springai.demo;

import org.junit.jupiter.api.Test;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.TextReader;
import org.springframework.ai.reader.markdown.MarkdownDocumentReader;
import org.springframework.ai.reader.markdown.config.MarkdownDocumentReaderConfig;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.Resource;

import java.util.List;
import java.util.Map;

@SpringBootTest(classes = SpringAiDemo012Application.class)
public class TestSearch {

    @Autowired
    private VectorStore vectorStore;

    @Value("classpath:/docs/2026 年企业内部管理综合手册.md")
    private Resource resource;

    /**
     * 阶段一：全自动 ETL (抽取 -> 打标记 -> 切片 -> 入库)
     */
    @Test
    void ingestFileAutomatically() {
        // ---------------- 1. E (Extract) - 自动读取文件 ----------------
        // 使用 spring-ai-markdown-document-reader 读取 Markdown 文档
        MarkdownDocumentReaderConfig config = MarkdownDocumentReaderConfig.builder()
                // 统一设置“2026版”和“公司制度”的标记
                .withAdditionalMetadata(Map.of(
                        "filename", resource.getFilename(),
                        "doc_category", "COMPANY_POLICY",
                        "year", "2026"
                ))
                .build();
        MarkdownDocumentReader reader = new MarkdownDocumentReader(this.resource, config);

        List<Document> rawDocuments = reader.get();
        // ---------------- 2. T (Transform) - 智能滑窗文本切块----------------
        // 初始化语义切分器：默认 每块最多 800 Token，重叠区 100 Token防止语义割裂
        SemanticTokenTextSplitter splitter = SemanticTokenTextSplitter.builder().withDefaultChinesePunctuations() // 设置中文标点符号
                .build();
        // 执行切块
        List<Document> chunkedDocs = splitter.apply(rawDocuments);

        // ---------------- 3. L (Load) - 向量化并物理入库 ----------------
        vectorStore.add(chunkedDocs);
        System.out.println("向量化并入库成功！");
    }
    /**
     * 测试场景 A：搜索错误年份（验证 Filter 拦截）
     */
    @Test
    void testSearchWrongYear() {
        searchCompanyPolicy("晚上加班太晚了，自己打车能报销吗", "2025");
    }
    /**
     * 测试场景 B：正确条件检索
     */
    @Test
    void testSearchCorrect() {
        searchCompanyPolicy("晚上加班太晚了，自己打车能报销吗", "2026");
    }


    void searchCompanyPolicy(String query, String targetYear) {
        // 1. 初始化过滤器构建器 (Spring AI 提供的强类型跨库过滤工具)
        FilterExpressionBuilder filter = new FilterExpressionBuilder();

        // 2. 构建 SearchRequest，设置“三大标尺”
        SearchRequest searchRequest = SearchRequest.builder()
                .query(query)                  // 传入自然语言（底层自动转为向量）
                .topK(2)                       // 【切蛋糕】只取最近的 2 块
                .similarityThreshold(0.75)     // 【安检门】余弦相似度必须达到 0.75
                .filterExpression(             // 【找抽屉】硬过滤：严格匹配年份
                        filter.eq("year", targetYear).build()
                )
                .build();

        // 3. 执行检索：计算向量夹角距离并应用过滤召回
        List<Document> results = vectorStore.similaritySearch(searchRequest);

        // 打印诊断结果
        if (results.isEmpty()) {
            System.out.println("在【" + targetYear + "】年的制度抽屉中，未找到高度相关的答案。");
            return;
        }

        System.out.println("检索成功！共召回 " + results.size() + " 条记录：\n");
        for (int i = 0; i < results.size(); i++) {
            Document doc = results.get(i);
            System.out.println("【命中知识 " + (i + 1) + "】(相似度得分: " + doc.getScore() + ")");
            System.out.println("核心片段: " + doc.getText().trim());
            System.out.println("溯源标签 (Metadata): " + doc.getMetadata());
        }
    }
}
