package com.uka.saa.structured.model;

import java.util.ArrayList;
import java.util.List;

/**
 * 写作外围资产。
 *
 * @author 公众号：春风不晚
 */
public class WritingAssets {

    /**
     * 推荐标题。
     *
     * @author 公众号：春风不晚
     */
    private String recommendedTitle;

    /**
     * 标题候选。
     *
     * @author 公众号：春风不晚
     */
    private List<String> titleCandidates = new ArrayList<>();

    /**
     * 摘要。
     *
     * @author 公众号：春风不晚
     */
    private String summary;

    /**
     * 关键词。
     *
     * @author 公众号：春风不晚
     */
    private List<String> keywords = new ArrayList<>();

    public String getRecommendedTitle() {
        return recommendedTitle;
    }

    public void setRecommendedTitle(String recommendedTitle) {
        this.recommendedTitle = recommendedTitle;
    }

    public List<String> getTitleCandidates() {
        return titleCandidates;
    }

    public void setTitleCandidates(List<String> titleCandidates) {
        this.titleCandidates = titleCandidates;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public List<String> getKeywords() {
        return keywords;
    }

    public void setKeywords(List<String> keywords) {
        this.keywords = keywords;
    }

}
