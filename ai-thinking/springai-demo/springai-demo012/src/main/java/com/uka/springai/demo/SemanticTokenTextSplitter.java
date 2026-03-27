package com.uka.springai.demo;

import com.knuddels.jtokkit.Encodings;
import com.knuddels.jtokkit.api.Encoding;
import com.knuddels.jtokkit.api.EncodingRegistry;
import com.knuddels.jtokkit.api.EncodingType;
import com.knuddels.jtokkit.api.IntArrayList;
import org.springframework.ai.transformer.splitter.TextSplitter;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 纯语义 Token 切分器 (Semantic Token Text Splitter)
 * <p>
 * 专为大语言模型 (LLM) 的 RAG 文档预处理设计，核心目标是：
 * **在严格保证不超过 Token 上限的前提下，绝对不破坏原有文本的句子完整性。**
 * <p>
 * 核心工作流：
 * 1. 【语义断句】：利用正则后瞻，根据标点符号（如句号、换行等），将长文本切分为完整的“句子”。
 * 2. 【智能组装】：不断累加完整的句子，直到它们的总 Token 数量逼近设定的 {@code chunkSize}。
 * 3. 【句子级重叠 (Semantic Overlap)】：触发切分时，从当前切好的块中，从后往前提取完整的句子作为下一个块的开头。
 *    例如：
 *      段落 1: "你好，我是张三。我的名字是我爸爸取的。"
 *      段落 2: "我的名字是我爸爸取的。我今年十岁了。" (完美重叠了上一句的完整语义，不生硬截断单词)
 * 4. 【极限兜底】：遇极少数无标点的超长乱码串，自动降级为滑动窗口硬切分，防报错。
 *
 * @author YourName
 */
public class SemanticTokenTextSplitter extends TextSplitter {

    /** 默认单个文本块的最大 Token 容量 */
    private static final int DEFAULT_CHUNK_SIZE = 800;

    /** 默认相邻两个文本块之间允许重叠的最大 Token 数量 */
    private static final int DEFAULT_OVERLAP_SIZE = 100;

    /** 默认的断句标点符号：涵盖英文句末标点和换行符 */
    private static final List<String> DEFAULT_PUNCTUATIONS = Arrays.asList(".", "?", "!", "\n");

    // JTokkit 编码注册表，用于将文本转换为大模型能理解的 Token
    private final EncodingRegistry registry = Encodings.newLazyEncodingRegistry();

    // 默认使用 OpenAI 的 CL100K_BASE 编码（GPT-3.5/GPT-4 / text-embedding-ada-002 标准编码）
    private final Encoding encoding = this.registry.getEncoding(EncodingType.CL100K_BASE);

    // --- 用户配置属性 ---
    private final int chunkSize;
    private final int overlapSize;
    private final List<String> punctuations;
    private final boolean mergeEmptyLines;

    /**
     * 私有构造函数，强制要求通过 Builder 创建实例，并在创建时进行严格的参数校验
     */
    private SemanticTokenTextSplitter(int chunkSize, int overlapSize, List<String> punctuations, boolean mergeEmptyLines) {
        Assert.isTrue(chunkSize > 0, "chunkSize (块大小) 必须大于 0");
        Assert.isTrue(overlapSize >= 0, "overlapSize (重叠大小) 必须大于等于 0");
        Assert.isTrue(overlapSize < chunkSize, "overlapSize 必须严格小于 chunkSize，否则滑动窗口无法向前推进");

        this.chunkSize = chunkSize;
        this.overlapSize = overlapSize;
        this.punctuations = punctuations;
        this.mergeEmptyLines = mergeEmptyLines;
    }

    public static Builder builder() {
        return new Builder();
    }

    /**
     * 核心切分逻辑的入口方法
     *
     * @param text 待切分的原始长文本
     * @return 切分后的文本块列表（Chunks）
     */
    @Override
    protected List<String> splitText(String text) {
        if (text == null || text.trim().isEmpty()) {
            return new ArrayList<>();
        }

        // ==========================================
        // 步骤 1：预处理，合并多余的空行
        // ==========================================
        // 正则解释：匹配连续 2 个及以上的换行符（中间允许包含空格或制表符 \s*）。
        // 作用：将它们压缩为单一的换行符 \n。防止大模型浪费 Token 去处理大段的空白区域。
        if (this.mergeEmptyLines) {
            text = text.replaceAll("(\\r?\\n\\s*){2,}", "\n");
        }

        // ==========================================
        // 步骤 2：语义切分（按标点符号完美断句）
        // ==========================================
        // 正则解释：使用“正向后瞻 (Positive Lookbehind)” (?<=...)
        // 它的核心作用是：在指定的标点符号处切开文本，但【保留该标点符号在句子的末尾】。
        // 举例：按 "。" 切分 "你好。我是张三。"，得到的是 ["你好。", "我是张三。"]
        // 如果不用后瞻直接 split("。")，句号会被“吃掉”，变成 ["你好", "我是张三"]，破坏原文。
        String regex = "(?<=" + this.punctuations.stream()
                .map(Pattern::quote) // 转义标点，防止正则语法冲突（如 ? 在正则中代表可选）
                .collect(Collectors.joining("|")) + ")";

        // 将整篇文章打碎成一个个包含结尾标点符号的完整“句子片段”
        String[] fragments = text.split(regex);

        // ==========================================
        // 步骤 3：遍历句子，拼装 Chunk 并计算 Overlap
        // ==========================================
        List<String> chunks = new ArrayList<>();              // 存放最终切分好的所有文本块 (Chunks)
        List<String> currentChunkFragments = new ArrayList<>(); // 暂存器：存放当前正在拼装的文本块包含的句子
        int currentChunkTokens = 0;                           // 计数器：当前正在拼装的文本块已经累积的 Token 数量

        for (String fragment : fragments) {
            if (fragment.isEmpty()) {
                continue; // 过滤掉由于正则切分产生的空片段
            }

            int fragTokens = getEncodedTokens(fragment).size();

            // ------------------------------------------
            // 异常分支：极其变态的长句（没标点）兜底防御机制
            // ------------------------------------------
            // 如果遇到没有标点符号的极端脏数据（如一大段乱码），单句话长度直接超过 chunkSize。
            // 为防止程序陷入死循环或大模型报错，对这句特殊的话隐式使用“纯 Token 硬切分”进行降级处理。
            if (fragTokens > this.chunkSize) {
                // 先把目前正常攒好的句子打包成一个 Chunk 输出掉
                flushCurrentChunk(chunks, currentChunkFragments);
                currentChunkFragments.clear(); // 结算后清空暂存器
                currentChunkTokens = 0;

                // 调用硬切分降级算法
                List<String> hardChunks = fallbackHardSplit(fragment);

                // 除了硬切分出的最后一块，前面所有的块都直接作为最终 Chunk 输出
                for (int i = 0; i < hardChunks.size() - 1; i++) {
                    chunks.add(hardChunks.get(i));
                }

                // 将硬切分出来的最后一块留下来，塞进暂存器，作为下一个 Chunk 的开头，保持连贯性
                String lastHardChunk = hardChunks.get(hardChunks.size() - 1);
                currentChunkFragments.add(lastHardChunk);
                currentChunkTokens = getEncodedTokens(lastHardChunk).size();
                continue; // 这句超长的话处理完毕，跳过后面的逻辑，处理下一句话
            }

            // ------------------------------------------
            // 核心逻辑：触发切分，并计算基于完整句子的 Overlap
            // ------------------------------------------
            // 判断条件：如果 【目前已经攒的 Token】 + 【这句新话的 Token】 超出了最大容量限制
            if (currentChunkTokens + fragTokens > this.chunkSize && !currentChunkFragments.isEmpty()) {

                // 第一步：结算。将目前攒好的句子拼接为一个完整的 Chunk，加入结果集中。
                // （注意：此时 currentChunkFragments 里的句子还没有被清空，用于后续计算重叠！）
                flushCurrentChunk(chunks, currentChunkFragments);

                // 第二步：回溯计算 Overlap（重叠区）。
                List<String> overlapFragments = new ArrayList<>();
                int overlapTokens = 0;

                // 【倒着遍历】刚才还没被清空的暂存器里的句子
                for (int i = currentChunkFragments.size() - 1; i >= 0; i--) {
                    String sentence = currentChunkFragments.get(i);
                    int sentenceTokens = getEncodedTokens(sentence).size();

                    // 只要加上这个完整的句子，重叠部分 Token 没有超过 overlapSize，就把它拿来重叠
                    if (overlapTokens + sentenceTokens <= this.overlapSize) {
                        // 使用 add(0, ...) 每次插入头部，保证倒着找出来的句子，顺序依然是正向连贯的
                        overlapFragments.add(0, sentence);
                        overlapTokens += sentenceTokens;
                    } else {
                        // 一旦超过了重叠限制，停止回溯
                        break;
                    }
                }

                // 第三步：完成交接。
                // 彻底清空上一轮的旧句子，把找出来的 Overlap 句子塞进去，作为下一个新 Chunk 的“基础手牌”。
                currentChunkFragments.clear();
                currentChunkFragments.addAll(overlapFragments);
                currentChunkTokens = overlapTokens;
            }

            // ------------------------------------------
            // 正常流程：容量未超限，将新遍历到的这句话追加到暂存器中
            // ------------------------------------------
            currentChunkFragments.add(fragment);
            currentChunkTokens += fragTokens;
        }

        // ==========================================
        // 步骤 4：收尾，把文章最后剩下的一点未打包的片段输出
        // ==========================================
        flushCurrentChunk(chunks, currentChunkFragments);

        return chunks;
    }

    /**
     * 辅助方法：将暂存器中的句子片段拼接成完整的字符串文本，并加入最终结果列表。
     * <p>
     * ⚠️ 注意：此方法只负责组装和加入，【绝对不能】在这里调用 clear() 清空 currentChunkFragments。
     * 因为外部的主循环需要保留这些片段的数据，去计算下一个 Chunk 的 Overlap。
     */
    private void flushCurrentChunk(List<String> chunks, List<String> currentChunkFragments) {
        if (!currentChunkFragments.isEmpty()) {
            // 直接拼接（因为标点符号已经通过正则保留在每个片段的末尾了），并去除首尾可能产生的多余空格
            String joinedChunk = String.join("", currentChunkFragments).trim();
            if (!joinedChunk.isEmpty()) {
                chunks.add(joinedChunk);
            }
        }
    }

    /**
     * 内部私有降级算法：纯 Token 滑动窗口硬切分。
     * <p>
     * 仅当某一句单一的文本（中间没有任何定义的标点符号）自身长度就超过 chunkSize 时触发。
     * 它会无视语义，严格按照 Token 数量和 Overlap 大小切分开，防止内存溢出或 API 报错。
     */
    private List<String> fallbackHardSplit(String text) {
        List<Integer> tokens = getEncodedTokens(text);
        List<String> chunks = new ArrayList<>();
        int totalTokens = tokens.size();

        // 滑动窗口的步长 = 块总大小 - 重叠大小。即每次切完后，窗口向后挪动步长的距离。
        int stepSize = this.chunkSize - this.overlapSize;

        for (int i = 0; i < totalTokens; i += stepSize) {
            // 使用 Math.min 防止截取时发生数组越界
            int endIndex = Math.min(i + this.chunkSize, totalTokens);
            List<Integer> chunkTokens = tokens.subList(i, endIndex);

            // 解码回字符串并去掉前后无意义的空格
            String chunkText = decodeTokens(chunkTokens).trim();
            if (!chunkText.isEmpty()) {
                chunks.add(chunkText);
            }
            // 如果已经切到了最末尾，退出循环
            if (endIndex == totalTokens) {
                break;
            }
        }
        return chunks;
    }

    /**
     * 调用底层的 JTokkit 库，将普通文本字符串编码为大语言模型 Token ID 列表
     */
    private List<Integer> getEncodedTokens(String text) {
        Assert.notNull(text, "待编码的文本不能为 null");
        return this.encoding.encode(text).boxed();
    }

    /**
     * 调用底层的 JTokkit 库，将 Token ID 列表解码还原回普通文本字符串
     */
    private String decodeTokens(List<Integer> tokens) {
        Assert.notNull(tokens, "Token 列表不能为 null");
        var tokensIntArray = new IntArrayList(tokens.size());
        tokens.forEach(tokensIntArray::add);
        return this.encoding.decode(tokensIntArray);
    }

    // ==========================================
    // 构造器模式 (Builder)
    // 提供了优雅的链式调用 API 用于初始化配置
    // ==========================================
    public static final class Builder {
        private int chunkSize = DEFAULT_CHUNK_SIZE;
        private int overlapSize = DEFAULT_OVERLAP_SIZE;
        private List<String> punctuations = DEFAULT_PUNCTUATIONS;
        private boolean mergeEmptyLines = true;

        private Builder() {}

        /**
         * 设置单个文本块的最大 Token 容量。
         * 默认：800。
         */
        public Builder withChunkSize(int chunkSize) {
            this.chunkSize = chunkSize;
            return this;
        }

        /**
         * 设置相邻两个文本块之间允许重叠的最大 Token 数量。
         * 默认：100。
         */
        public Builder withOverlapSize(int overlapSize) {
            this.overlapSize = overlapSize;
            return this;
        }

        /**
         * 自定义用于断句的标点符号列表。
         * 如果处理纯中文文档，建议直接调用 {@link #withDefaultChinesePunctuations()}。
         */
        public Builder withPunctuations(List<String> punctuations) {
            this.punctuations = punctuations;
            return this;
        }

        /**
         * 快捷配置：使用最适合中文+英文混合场景的标点符号集。
         * 包含：英文句号、问号、感叹号、换行符，以及 中文句号、问号、感叹号、分号。
         */
        public Builder withDefaultChinesePunctuations() {
            this.punctuations = Arrays.asList(".", "?", "!", "\n", "。", "？", "！", "；");
            return this;
        }

        /**
         * 是否在切分前，自动将文本中无意义的连续空行压缩为一个换行符。
         * 默认：true (建议开启)。
         */
        public Builder withMergeEmptyLines(boolean mergeEmptyLines) {
            this.mergeEmptyLines = mergeEmptyLines;
            return this;
        }

        /**
         * 构建出 SemanticTokenTextSplitter 实例
         */
        public SemanticTokenTextSplitter build() {
            return new SemanticTokenTextSplitter(this.chunkSize, this.overlapSize, this.punctuations, this.mergeEmptyLines);
        }
    }
}