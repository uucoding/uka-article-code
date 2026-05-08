package com.uka.saa.boundary.service;

import com.uka.saa.boundary.model.AgentBoundaryRequest;
import com.uka.saa.boundary.model.AgentBoundaryResult;

/**
 * ReactAgent 边界演示服务。
 *
 * @author 公众号：春风不晚
 */
public interface AgentBoundaryService {

    /**
     * 执行写作任务，并返回四个边界的运行快照。
     *
     * @param request 写作请求
     * @return 写作结果
     * @author 公众号：春风不晚
     */
    AgentBoundaryResult write(AgentBoundaryRequest request);

}
