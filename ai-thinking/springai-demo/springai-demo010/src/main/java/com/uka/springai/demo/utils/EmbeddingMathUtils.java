package com.uka.springai.demo.utils;

import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealVector;

/**
 * 公众号： 春风不晚
 * 向量计算工具类
 *
 * - **欧式距离**：适用于关心向量的实际距离，且向量尺度相对一致时（越小越相似）。
 * - **余弦相似度**：更适合比较文本、关键词，关注向量的方向而非模长（越大越相似）。
 */
public class EmbeddingMathUtils {
    /**
     * 判断两组向量相似度：使用余弦距离 -- 越大越相似
     */
    public static double cosSim(float[] embeddingA, float[] embeddingB) {
        return cosSim(new ArrayRealVector(toDoubleArray(embeddingA)), new ArrayRealVector(toDoubleArray(embeddingB)));
    }

    public static double cosSim(RealVector a, RealVector b) {
        // 公式：A 与 B 的点积 / (A的模长 * B的模长)
        return a.dotProduct(b) / (a.getNorm() * b.getNorm());
    }

    /**
     * 判断两组向量相似度：欧式距离 -- 越小越相似
     */
    public static double l2(float[] embeddingA, float[] embeddingB) {
        return l2(new ArrayRealVector(toDoubleArray(embeddingA)), new ArrayRealVector(toDoubleArray(embeddingB)));
    }

    public static double l2(RealVector a, RealVector b) {
        // 公式：A 减去 B 后的模长 (直线距离)
        return a.subtract(b).getNorm();
    }

    /**
     * Spring AI 默认返回 float[]，需转为 double[]
     *
     * @param floats 原始 float 数组，可为 null
     * @return 对应的 double 数组；若输入为 null，则返回 null
     */
    public static double[] toDoubleArray(float[] floats) {
        if (floats == null) {
            return new double[0];
        }
        double[] doubles = new double[floats.length];
        for (int i = 0; i < floats.length; i++) {
            doubles[i] = floats[i];
        }
        return doubles;
    }
}
