package com.uka.saa.structured.service;

import com.uka.saa.structured.model.StructuredWritingRequest;
import com.uka.saa.structured.model.WritingBlueprintResult;

/**
 * 结构化写作服务。
 *
 * @author 公众号：春风不晚
 */
public interface StructuredWritingService {

    /**
     * 生成结构化写作蓝图。
     *
     * @param request 写作请求
     * @return 写作蓝图结果
     * @author 公众号：春风不晚
     */
    WritingBlueprintResult generateBlueprint(StructuredWritingRequest request);

}
