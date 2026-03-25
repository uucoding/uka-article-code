package com.uka.springai.demo;

import com.uka.springai.demo.utils.EmbeddingMathUtils;
import org.junit.jupiter.api.Test;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;
import java.util.List;

@SpringBootTest(classes = SpringAiDemo010Application.class)
public class TestEmbedding {

    @Autowired
    private EmbeddingModel embeddingModel;

    @Test
    void testVectorMath() {
        // 目标文本：我们想要查询的基准语句
        String targetText = "AI会对生活产生影响";

        // 测试样本库
        List<String> textList = Arrays.asList(
                "AI可以帮助人类提高效率，在生活中可以帮助我们", // 语义相近
                "这是一只小狗"                             // 语义毫无关联
        );

        // 1. 获取基准语句的高维向量
        float[] targetVector = embeddingModel.embed(targetText);

        // 2. 相同数据对比 (自己和自己比)
        double selfCos = EmbeddingMathUtils.cosSim(targetVector, targetVector);
        double selfL2 = EmbeddingMathUtils.l2(targetVector, targetVector);

        System.out.println("【相同数据对比】: [" + targetText + "] vs [" + targetText + "]");
        System.out.println("余弦距离(越大越相似): " + selfCos + " ； 欧式距离(越小越相似): " + selfL2 + "\n");

        // 3. 循环对比样本库
        for (String sample : textList) {
            // 将样本语句转换为向量
            float[] sampleVector = embeddingModel.embed(sample);

            // 对比
            double cosSim = EmbeddingMathUtils.cosSim(targetVector, sampleVector);
            double l2Dist = EmbeddingMathUtils.l2(targetVector, sampleVector);

            System.out.println("【比对样本】: [" + targetText + "] vs [" + sample + "]");
            System.out.println("余弦距离(越大越相似): " + cosSim + " ； 欧式距离(越小越相似): " + l2Dist + "\n");
        }
    }

}
