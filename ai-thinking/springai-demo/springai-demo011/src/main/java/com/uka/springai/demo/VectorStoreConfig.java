package com.uka.springai.demo;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.File;

/**
 * 公众号： 春风不晚
 */
@ConditionalOnProperty(
        name = {"spring.ai.vectorstore.type"},
        havingValue = "simple",
        matchIfMissing = true
)
@Configuration
public class VectorStoreConfig {


    // VectorStore 必须强依赖注入一个 EmbeddingModel！框架在入库时，需要底层调用它去自动把文字算成向量。
    @Bean
    public SimpleVectorStore vectorStore(EmbeddingModel embeddingModel) {
        SimpleVectorStore vectorStore = SimpleVectorStore.builder(embeddingModel).build();

        // 如果服务器本地存在之前保存过的数据文件，启动时直接加载它！
        File vectorStoreFile = new File("local_vector_store.json");
        if (vectorStoreFile.exists()) {
            vectorStore.load(vectorStoreFile);
            System.out.println(" [System] 从本地 JSON 文件成功加载了现有的向量数据库！");
        }
        return vectorStore;
    }
}
