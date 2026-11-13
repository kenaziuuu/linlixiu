package com.linlixiu.system.mapper;

import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Param;
import com.linlixiu.system.domain.RepairEvaluation;

/**
 * 报修评价Mapper接口
 * 
 * @author linlixiu
 * @date 2026-11-06
 */
public interface RepairEvaluationMapper
{
    /**
     * 查询报修评价
     * 
     * @param evaluationId 评价ID
     * @return 报修评价
     */
    public RepairEvaluation selectRepairEvaluationById(Long evaluationId);

    /**
     * 按工单查询评价
     * 
     * @param orderId 工单ID
     * @return 报修评价
     */
    public RepairEvaluation selectEvaluationByOrderId(Long orderId);

    /**
     * 查询报修评价列表
     * 
     * @param repairEvaluation 报修评价
     * @return 报修评价集合
     */
    public List<RepairEvaluation> selectRepairEvaluationList(RepairEvaluation repairEvaluation);

    /**
     * 新增报修评价
     * 
     * @param repairEvaluation 报修评价
     * @return 结果
     */
    public int insertRepairEvaluation(RepairEvaluation repairEvaluation);

    /**
     * 回复评价
     * 
     * @param evaluationId 评价ID
     * @param replyContent 回复内容
     * @return 结果
     */
    public int replyEvaluation(@Param("evaluationId") Long evaluationId, @Param("replyContent") String replyContent);

    /**
     * 批量删除报修评价
     * 
     * @param evaluationIds 评价ID数组
     * @return 结果
     */
    public int deleteRepairEvaluationByIds(Long[] evaluationIds);

    /**
     * 评价汇总
     * 
     * @return 统计结果
     */
    public Map<String, Object> selectScoreSummary(@Param("residentId") Long residentId);
}
