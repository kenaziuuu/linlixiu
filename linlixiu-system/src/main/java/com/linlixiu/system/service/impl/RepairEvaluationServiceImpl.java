package com.linlixiu.system.service.impl;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.linlixiu.common.utils.SecurityUtils;
import com.linlixiu.common.utils.StringUtils;
import com.linlixiu.system.domain.RepairEvaluation;
import com.linlixiu.system.domain.RepairMessage;
import com.linlixiu.system.domain.RepairOrder;
import com.linlixiu.system.domain.RepairProgress;
import com.linlixiu.system.domain.RepairWorker;
import com.linlixiu.system.mapper.RepairEvaluationMapper;
import com.linlixiu.system.mapper.RepairMessageMapper;
import com.linlixiu.system.mapper.RepairOrderMapper;
import com.linlixiu.system.mapper.RepairProgressMapper;
import com.linlixiu.system.mapper.RepairWorkerMapper;
import com.linlixiu.system.service.IRepairEvaluationService;

/**
 * 报修评价Service业务层处理
 *
 * 评价是工单状态机的最后一环：completed(待评价) → rated(已完成)。
 * 提交评价后同步刷新维修工的平均分，并向维修工推送一条消息。
 *
 * @author linlixiu
 * @date 2026-11-06
 */
@Service
public class RepairEvaluationServiceImpl implements IRepairEvaluationService
{
    @Autowired
    private RepairEvaluationMapper repairEvaluationMapper;

    @Autowired
    private RepairOrderMapper repairOrderMapper;

    @Autowired
    private RepairProgressMapper repairProgressMapper;

    @Autowired
    private RepairMessageMapper repairMessageMapper;

    @Autowired
    private RepairWorkerMapper repairWorkerMapper;

    /**
     * 查询报修评价
     */
    @Override
    public RepairEvaluation selectRepairEvaluationById(Long evaluationId)
    {
        return repairEvaluationMapper.selectRepairEvaluationById(evaluationId);
    }

    /**
     * 按工单查询评价
     */
    @Override
    public RepairEvaluation selectEvaluationByOrderId(Long orderId)
    {
        return repairEvaluationMapper.selectEvaluationByOrderId(orderId);
    }

    /**
     * 查询报修评价列表
     */
    @Override
    public List<RepairEvaluation> selectRepairEvaluationList(RepairEvaluation repairEvaluation)
    {
        return repairEvaluationMapper.selectRepairEvaluationList(repairEvaluation);
    }

    /**
     * 新增报修评价：校验工单状态 → 写评价 → 工单置为已完成 → 记进度 → 通知维修工 → 刷新统计
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertRepairEvaluation(RepairEvaluation evaluation)
    {
        if (evaluation.getOrderId() == null)
        {
            throw new RuntimeException("缺少工单编号");
        }
        RepairOrder order = repairOrderMapper.selectRepairOrderByOrderId(evaluation.getOrderId());
        if (order == null)
        {
            throw new RuntimeException("报修工单不存在");
        }
        if (!RepairOrderServiceImpl.STATUS_COMPLETED.equals(order.getStatus()))
        {
            throw new RuntimeException("只有【待评价】的工单可以评价");
        }
        if (order.getEvaluateFlag() != null && order.getEvaluateFlag() == 1)
        {
            throw new RuntimeException("该工单已评价，请勿重复提交");
        }
        if (evaluation.getScore() == null || evaluation.getScore() < 1 || evaluation.getScore() > 5)
        {
            throw new RuntimeException("综合评分必须在 1～5 之间");
        }

        evaluation.setResidentId(order.getResidentId());
        evaluation.setWorkerId(order.getWorkerId());
        int rows = repairEvaluationMapper.insertRepairEvaluation(evaluation);

        // 工单流转到终态
        RepairOrder update = new RepairOrder();
        update.setOrderId(order.getOrderId());
        update.setStatus(RepairOrderServiceImpl.STATUS_RATED);
        update.setEvaluateFlag(1);
        repairOrderMapper.updateRepairOrder(update);

        RepairProgress progress = new RepairProgress();
        progress.setOrderId(order.getOrderId());
        progress.setNode("rate");
        progress.setNodeName("居民评价");
        progress.setContent("居民给出 " + evaluation.getScore() + " 星评价"
                + (StringUtils.isEmpty(evaluation.getContent()) ? "" : "：" + evaluation.getContent()));
        progress.setOperatorId(order.getResidentId());
        repairProgressMapper.insertRepairProgress(progress);

        if (order.getWorkerId() != null)
        {
            RepairMessage message = new RepairMessage();
            message.setReceiverId(order.getWorkerId());
            message.setTitle("收到新的服务评价");
            message.setContent("工单 " + order.getOrderNo() + " 获得 " + evaluation.getScore() + " 星评价。");
            message.setMsgType("rate");
            message.setOrderId(order.getOrderId());
            message.setIsRead(0);
            repairMessageMapper.insertRepairMessage(message);

            RepairWorker worker = repairWorkerMapper.selectRepairWorkerByUserId(order.getWorkerId());
            if (worker != null)
            {
                repairWorkerMapper.refreshWorkerStat(worker.getWorkerId());
            }
        }
        return rows;
    }

    /**
     * 回复评价
     */
    @Override
    public int replyEvaluation(Long evaluationId, String replyContent)
    {
        return repairEvaluationMapper.replyEvaluation(evaluationId, replyContent);
    }

    /**
     * 批量删除报修评价
     */
    @Override
    public int deleteRepairEvaluationByIds(Long[] evaluationIds)
    {
        return repairEvaluationMapper.deleteRepairEvaluationByIds(evaluationIds);
    }

    /**
     * 评价汇总
     */
    @Override
    public Map<String, Object> selectScoreSummary(Long residentId)
    {
        return repairEvaluationMapper.selectScoreSummary(residentId);
    }
}
