package com.linlixiu.system.service.impl;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.linlixiu.common.utils.DateUtils;
import com.linlixiu.common.utils.SecurityUtils;
import com.linlixiu.common.utils.StringUtils;
import com.linlixiu.system.domain.RepairMessage;
import com.linlixiu.system.domain.RepairOrder;
import com.linlixiu.system.domain.RepairProgress;
import com.linlixiu.system.domain.RepairWorker;
import com.linlixiu.system.mapper.RepairMessageMapper;
import com.linlixiu.system.mapper.RepairOrderMapper;
import com.linlixiu.system.mapper.RepairProgressMapper;
import com.linlixiu.system.mapper.RepairWorkerMapper;
import com.linlixiu.system.service.IRepairOrderService;

/**
 * 报修工单Service业务层处理
 *
 * 工单状态机：
 * pending(待派单) → assigned(待接单) → repairing(维修中) → completed(待评价) → rated(已完成)
 * pending / assigned 可被居民取消，进入 cancelled；
 * assigned / repairing 可被维修工退回，回到 pending 重新派单。
 *
 * @author linlixiu
 * @date 2026-11-06
 */
@Service
public class RepairOrderServiceImpl implements IRepairOrderService
{
    /** 待派单 */
    public static final String STATUS_PENDING = "pending";
    /** 待接单 */
    public static final String STATUS_ASSIGNED = "assigned";
    /** 维修中 */
    public static final String STATUS_REPAIRING = "repairing";
    /** 待评价 */
    public static final String STATUS_COMPLETED = "completed";
    /** 已完成 */
    public static final String STATUS_RATED = "rated";
    /** 已取消 */
    public static final String STATUS_CANCELLED = "cancelled";

    @Autowired
    private RepairOrderMapper repairOrderMapper;

    @Autowired
    private RepairProgressMapper repairProgressMapper;

    @Autowired
    private RepairWorkerMapper repairWorkerMapper;

    @Autowired
    private RepairMessageMapper repairMessageMapper;

    /**
     * 查询报修工单
     */
    @Override
    public RepairOrder selectRepairOrderByOrderId(Long orderId)
    {
        return repairOrderMapper.selectRepairOrderByOrderId(orderId);
    }

    /**
     * 查询报修工单列表
     */
    @Override
    public List<RepairOrder> selectRepairOrderList(RepairOrder repairOrder)
    {
        return repairOrderMapper.selectRepairOrderList(repairOrder);
    }

    /**
     * 新增报修工单（居民提交报修）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertRepairOrder(RepairOrder repairOrder)
    {
        // 生成报修单号：BX + 日期 + 毫秒序列，保证同一天内不重复
        if (StringUtils.isEmpty(repairOrder.getOrderNo()))
        {
            repairOrder.setOrderNo("BX" + DateUtils.dateTimeNow("yyyyMMdd")
                    + String.format("%06d", System.currentTimeMillis() % 1000000));
        }
        // 初始状态由服务端强制指定，避免前端伪造
        repairOrder.setStatus(STATUS_PENDING);
        repairOrder.setIsOvertime(0);
        repairOrder.setEvaluateFlag(0);
        if (repairOrder.getResidentId() == null)
        {
            repairOrder.setResidentId(SecurityUtils.getUserId());
        }
        if (repairOrder.getDeptId() == null)
        {
            repairOrder.setDeptId(SecurityUtils.getDeptId());
        }
        int rows = repairOrderMapper.insertRepairOrder(repairOrder);

        addProgress(repairOrder.getOrderId(), "submit", "提交报修",
                "居民提交报修申请：" + repairOrder.getTitle(), repairOrder.getResidentId());
        return rows;
    }

    /**
     * 修改报修工单
     */
    @Override
    public int updateRepairOrder(RepairOrder repairOrder)
    {
        return repairOrderMapper.updateRepairOrder(repairOrder);
    }

    /**
     * 批量删除报修工单
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteRepairOrderByOrderIds(Long[] orderIds)
    {
        for (Long orderId : orderIds)
        {
            repairProgressMapper.deleteRepairProgressByOrderId(orderId);
        }
        return repairOrderMapper.deleteRepairOrderByOrderIds(orderIds);
    }

    /**
     * 删除报修工单
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteRepairOrderByOrderId(Long orderId)
    {
        repairProgressMapper.deleteRepairProgressByOrderId(orderId);
        return repairOrderMapper.deleteRepairOrderByOrderId(orderId);
    }

    /**
     * 派单：调度员把待派单工单指派给维修工，已派单的工单可改派
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int dispatchOrder(Long orderId, Long workerId, String remark)
    {
        RepairOrder order = repairOrderMapper.selectRepairOrderByOrderId(orderId);
        if (order == null)
        {
            throw new RuntimeException("报修工单不存在");
        }
        if (!STATUS_PENDING.equals(order.getStatus()) && !STATUS_ASSIGNED.equals(order.getStatus()))
        {
            throw new RuntimeException("当前工单状态为【" + order.getStatus() + "】，不允许派单");
        }
        RepairWorker worker = repairWorkerMapper.selectRepairWorkerById(workerId);
        if (worker == null)
        {
            throw new RuntimeException("维修工不存在");
        }
        if (!"0".equals(worker.getStatus()))
        {
            throw new RuntimeException("维修工【" + worker.getWorkerName() + "】当前不在岗，无法派单");
        }

        boolean reassign = STATUS_ASSIGNED.equals(order.getStatus());
        RepairOrder update = new RepairOrder();
        update.setOrderId(orderId);
        update.setWorkerId(worker.getUserId());
        update.setDispatcherId(SecurityUtils.getUserId());
        update.setDispatchTime(new Date());
        update.setStatus(STATUS_ASSIGNED);
        update.setIsOvertime(0);
        int rows = repairOrderMapper.updateRepairOrder(update);

        addProgress(orderId, "dispatch", reassign ? "改派工单" : "派单",
                "调度员将工单指派给维修工【" + worker.getWorkerName() + "】"
                        + (StringUtils.isEmpty(remark) ? "" : "，说明：" + remark),
                SecurityUtils.getUserId());

        sendMessage(worker.getUserId(), "新报修工单待接单",
                "工单 " + order.getOrderNo() + "（" + order.getTitle() + "）已指派给您，请及时接单。",
                "dispatch", orderId);
        if (reassign)
        {
            sendMessage(order.getResidentId(), "报修工单已改派",
                    "您的报修 " + order.getOrderNo() + " 已改派给维修工【" + worker.getWorkerName() + "】。",
                    "dispatch", orderId);
        }
        return rows;
    }

    /**
     * 维修工接单
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int acceptOrder(Long orderId, Long workerId)
    {
        RepairOrder order = repairOrderMapper.selectRepairOrderByOrderId(orderId);
        if (order == null)
        {
            throw new RuntimeException("报修工单不存在");
        }
        if (!STATUS_ASSIGNED.equals(order.getStatus()))
        {
            throw new RuntimeException("只有【待接单】的工单可以接单");
        }
        if (!workerId.equals(order.getWorkerId()))
        {
            throw new RuntimeException("该工单未指派给您，无法接单");
        }

        RepairOrder update = new RepairOrder();
        update.setOrderId(orderId);
        update.setStatus(STATUS_REPAIRING);
        update.setAcceptTime(new Date());
        int rows = repairOrderMapper.updateRepairOrder(update);

        addProgress(orderId, "accept", "维修工接单", "维修工已接单，开始上门维修", workerId);
        sendMessage(order.getResidentId(), "维修工已接单",
                "您的报修 " + order.getOrderNo() + " 已由维修工接单，正在处理中。", "accept", orderId);
        return rows;
    }

    /**
     * 完工：维修工填写维修结果，工单进入待评价
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int finishOrder(Long orderId, String finishResult, Long workerId)
    {
        RepairOrder order = repairOrderMapper.selectRepairOrderByOrderId(orderId);
        if (order == null)
        {
            throw new RuntimeException("报修工单不存在");
        }
        if (!STATUS_REPAIRING.equals(order.getStatus()))
        {
            throw new RuntimeException("只有【维修中】的工单可以完工");
        }
        if (!workerId.equals(order.getWorkerId()))
        {
            throw new RuntimeException("该工单不属于您，无法提交完工");
        }
        if (StringUtils.isEmpty(finishResult))
        {
            throw new RuntimeException("请填写维修结果");
        }

        RepairOrder update = new RepairOrder();
        update.setOrderId(orderId);
        update.setStatus(STATUS_COMPLETED);
        update.setFinishTime(new Date());
        update.setFinishResult(finishResult);
        int rows = repairOrderMapper.updateRepairOrder(update);

        addProgress(orderId, "finish", "维修完工", "维修结果：" + finishResult, workerId);
        sendMessage(order.getResidentId(), "报修已完工，待评价",
                "您的报修 " + order.getOrderNo() + " 已维修完成，请对本次服务进行评价。", "finish", orderId);
        refreshWorkerStatByUserId(workerId);
        return rows;
    }

    /**
     * 居民取消报修
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int cancelOrder(Long orderId, String cancelReason, Long residentId)
    {
        RepairOrder order = repairOrderMapper.selectRepairOrderByOrderId(orderId);
        if (order == null)
        {
            throw new RuntimeException("报修工单不存在");
        }
        if (!STATUS_PENDING.equals(order.getStatus()) && !STATUS_ASSIGNED.equals(order.getStatus()))
        {
            throw new RuntimeException("工单已进入维修环节，不能取消");
        }
        if (residentId != null && !residentId.equals(order.getResidentId()))
        {
            throw new RuntimeException("只能取消本人提交的报修");
        }

        RepairOrder update = new RepairOrder();
        update.setOrderId(orderId);
        update.setStatus(STATUS_CANCELLED);
        update.setCancelReason(cancelReason);
        int rows = repairOrderMapper.updateRepairOrder(update);

        addProgress(orderId, "cancel", "取消报修",
                "居民取消报修" + (StringUtils.isEmpty(cancelReason) ? "" : "，原因：" + cancelReason),
                SecurityUtils.getUserId());
        if (order.getWorkerId() != null)
        {
            sendMessage(order.getWorkerId(), "报修工单已取消",
                    "工单 " + order.getOrderNo() + " 已被居民取消，无需继续处理。", "system", orderId);
        }
        return rows;
    }

    /**
     * 维修工退回工单：回到待派单，等待重新指派
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int returnOrder(Long orderId, String reason, Long workerId)
    {
        RepairOrder order = repairOrderMapper.selectRepairOrderByOrderId(orderId);
        if (order == null)
        {
            throw new RuntimeException("报修工单不存在");
        }
        if (!STATUS_ASSIGNED.equals(order.getStatus()) && !STATUS_REPAIRING.equals(order.getStatus()))
        {
            throw new RuntimeException("当前状态不允许退回");
        }
        if (!workerId.equals(order.getWorkerId()))
        {
            throw new RuntimeException("该工单不属于您，无法退回");
        }

        RepairOrder update = new RepairOrder();
        update.setOrderId(orderId);
        update.setStatus(STATUS_PENDING);
        int rows = repairOrderMapper.updateRepairOrder(update);

        // 退回后清空维修工与时间，重新进入待派单池
        repairOrderMapper.clearWorker(orderId);

        addProgress(orderId, "return", "退回工单",
                "维修工退回工单" + (StringUtils.isEmpty(reason) ? "" : "，原因：" + reason), workerId);
        sendMessage(order.getResidentId(), "报修工单已退回重新调度",
                "您的报修 " + order.getOrderNo() + " 因故退回，社区将重新安排维修人员。", "system", orderId);
        refreshWorkerStatByUserId(workerId);
        return rows;
    }

    /**
     * 追加维修进度
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int addProgress(Long orderId, String content, Long operatorId)
    {
        RepairProgress progress = new RepairProgress();
        progress.setOrderId(orderId);
        progress.setNode("repair");
        progress.setNodeName("维修进度");
        progress.setContent(content);
        progress.setOperatorId(operatorId);
        return repairProgressMapper.insertRepairProgress(progress);
    }

    /**
     * 统计概览
     */
    @Override
    public Map<String, Object> selectOverview()
    {
        Map<String, Object> raw = repairOrderMapper.selectOverview();
        Map<String, Object> result = new HashMap<>();
        result.put("total", toLong(raw == null ? null : raw.get("total")));
        result.put("pendingCount", toLong(raw == null ? null : raw.get("pendingCount")));
        result.put("assignedCount", toLong(raw == null ? null : raw.get("assignedCount")));
        result.put("repairingCount", toLong(raw == null ? null : raw.get("repairingCount")));
        result.put("completedCount", toLong(raw == null ? null : raw.get("completedCount")));
        result.put("ratedCount", toLong(raw == null ? null : raw.get("ratedCount")));
        result.put("cancelledCount", toLong(raw == null ? null : raw.get("cancelledCount")));
        result.put("overtimeCount", toLong(raw == null ? null : raw.get("overtimeCount")));
        result.put("todayCount", toLong(raw == null ? null : raw.get("todayCount")));

        long total = (Long) result.get("total");
        long finished = (Long) result.get("completedCount") + (Long) result.get("ratedCount");
        long valid = total - (Long) result.get("cancelledCount");
        // 完成率 = 已完成（待评价 + 已评价）/ 有效工单数
        double rate = valid <= 0 ? 0D : Math.round(finished * 1000.0 / valid) / 10.0;
        result.put("finishRate", rate);
        return result;
    }

    /**
     * 按报修类型统计
     */
    @Override
    public List<Map<String, Object>> selectCountByType()
    {
        return repairOrderMapper.selectCountByType();
    }

    /**
     * 按工单状态统计
     */
    @Override
    public List<Map<String, Object>> selectCountByStatus()
    {
        return repairOrderMapper.selectCountByStatus();
    }

    /**
     * 近 N 天报修趋势
     */
    @Override
    public List<Map<String, Object>> selectTrend(Integer days)
    {
        return repairOrderMapper.selectTrend(days == null || days <= 0 ? 7 : days);
    }

    /**
     * 维修工绩效排行
     */
    @Override
    public List<Map<String, Object>> selectWorkerRank(Integer limit)
    {
        return repairOrderMapper.selectWorkerRank(limit == null || limit <= 0 ? 5 : limit);
    }

    /**
     * 扫描并标记超时工单
     */
    @Override
    public int scanOvertimeOrders()
    {
        return repairOrderMapper.scanOvertimeOrders();
    }

    /**
     * 查询待派单的超时工单
     */
    @Override
    public List<RepairOrder> selectPendingOvertimeOrders()
    {
        return repairOrderMapper.selectPendingOvertimeOrders();
    }

    /**
     * 统计维修工待处理的任务数（待接单 + 维修中）
     */
    @Override
    public int countWorkerPending(Long workerId)
    {
        if (workerId == null)
        {
            return 0;
        }
        return repairOrderMapper.countWorkerPending(workerId);
    }

    /**
     * 统计居民待评价的报修数（工单已完成、尚未评价）
     */
    @Override
    public int countResidentPendingEvaluation(Long residentId)
    {
        if (residentId == null)
        {
            return 0;
        }
        return repairOrderMapper.countResidentPendingEvaluation(residentId);
    }

    // ------------------------------------------------------------------ 内部方法

    /**
     * 刷新维修工统计（入参为工单中保存的用户ID）
     */
    private void refreshWorkerStatByUserId(Long userId)
    {
        RepairWorker worker = repairWorkerMapper.selectRepairWorkerByUserId(userId);
        if (worker != null)
        {
            repairWorkerMapper.refreshWorkerStat(worker.getWorkerId());
        }
    }

    /**
     * 写入一条进度记录
     */
    private void addProgress(Long orderId, String node, String nodeName, String content, Long operatorId)
    {
        RepairProgress progress = new RepairProgress();
        progress.setOrderId(orderId);
        progress.setNode(node);
        progress.setNodeName(nodeName);
        progress.setContent(content);
        progress.setOperatorId(operatorId);
        repairProgressMapper.insertRepairProgress(progress);
    }

    /**
     * 发送站内消息
     */
    private void sendMessage(Long receiverId, String title, String content, String msgType, Long orderId)
    {
        if (receiverId == null)
        {
            return;
        }
        RepairMessage message = new RepairMessage();
        message.setReceiverId(receiverId);
        message.setTitle(title);
        message.setContent(content);
        message.setMsgType(msgType);
        message.setOrderId(orderId);
        message.setIsRead(0);
        repairMessageMapper.insertRepairMessage(message);
    }

    /**
     * 把数据库返回的聚合值统一转成 Long，避免前端拿到 BigDecimal
     */
    private Long toLong(Object value)
    {
        if (value == null)
        {
            return 0L;
        }
        if (value instanceof Number)
        {
            return ((Number) value).longValue();
        }
        try
        {
            return Long.parseLong(value.toString());
        }
        catch (NumberFormatException e)
        {
            return 0L;
        }
    }
}
