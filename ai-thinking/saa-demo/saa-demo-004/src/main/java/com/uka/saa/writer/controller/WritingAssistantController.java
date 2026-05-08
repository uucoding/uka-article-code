package com.uka.saa.writer.controller;

import com.uka.saa.writer.model.WritingRequest;
import com.uka.saa.writer.model.WritingResult;
import com.uka.saa.writer.service.WritingAssistantService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * AI 写作助手控制器。
 *
 * @author 公众号：春风不晚
 */
@RestController
@RequestMapping("/api/writing")
public class WritingAssistantController {

    private final WritingAssistantService writingAssistantService;

    public WritingAssistantController(WritingAssistantService writingAssistantService) {
        this.writingAssistantService = writingAssistantService;
    }

    /**
     * 生成文章初稿。
     *
     * @param request 写作请求
     * @return 初稿结果
     * @author 公众号：春风不晚
     */
    @PostMapping("/draft")
    public WritingResult draft(@RequestBody WritingRequest request) {
        return writingAssistantService.write(request);
    }

}
