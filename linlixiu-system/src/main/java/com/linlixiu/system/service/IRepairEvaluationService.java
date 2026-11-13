package com.linlixiu.system.service;

import java.util.List;
import java.util.Map;
import com.linlixiu.system.domain.RepairEvaluation;

/**
 * 报修评价接口
 * 
 * @author linlixiu
 * @date 2026-11-06
 */
public interface IRepairEvaluationService
{
    /**
     * 查询报修评价
     * 
     * @param evaluationId 评价ID
     * @return 查询报修评价
     */
    public RepairEvaluation selectRepairEvaluationById(Long evaluationId);

    /**
     * 按工单查询评价
     * 
     * @param orderId 工单ID
     * @return 按工单查询评价
     */
    public RepairEvaluation selectEvaluationByOrderId(Long orderId);

    /**
     * 查询报修评价列表
     * 
     * @param repairEvaluation 报修评价
     * @return 查询报修评价列表
     */
    public List<RepairEvaluation> selectRepairEvaluationList(RepairEvaluation repairEvaluation);

    /**
     * 新增报修评价
     * 
     * @param repairEvaluation 报修评价
     * @return 新增报修评价
     */
    public int insertRepairEvaluation(RepairEvaluation repairEvaluation);

    /**
     * 回复评价
     * 
     * @param evaluationId 评价ID
     * @param replyContent 回复内容
     * @return 回复评价
     */
    public int replyEvaluation(Long evaluationId, String replyContent);

    /**
     * 删除报修评价
     * 
     * @param evaluationIds 评价ID数组
     * @return 删除报修评价
     */
    public int deleteRepairEvaluationByIds(Long[] evaluationIds);

    /**
     * 评价汇总
     * 
     * @return 评价汇总
     */
    public Map<String, Object> selectScoreSummary(Long residentId);

}
