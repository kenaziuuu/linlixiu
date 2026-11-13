package com.linlixiu.system.service;

import java.util.List;
import java.util.Map;
import com.linlixiu.system.domain.RepairOrder;

/**
 * 报修工单接口
 * 
 * @author linlixiu
 * @date 2026-11-06
 */
public interface IRepairOrderService
{
    /**
     * 查询报修工单
     * 
     * @param orderId 工单ID
     * @return 查询报修工单
     */
    public RepairOrder selectRepairOrderByOrderId(Long orderId);

    /**
     * 查询报修工单列表
     * 
     * @param repairOrder 报修工单
     * @return 查询报修工单列表
     */
    public List<RepairOrder> selectRepairOrderList(RepairOrder repairOrder);

    /**
     * 新增报修工单
     * 
     * @param repairOrder 报修工单
     * @return 新增报修工单
     */
    public int insertRepairOrder(RepairOrder repairOrder);

    /**
     * 修改报修工单
     * 
     * @param repairOrder 报修工单
     * @return 修改报修工单
     */
    public int updateRepairOrder(RepairOrder repairOrder);

    /**
     * 批量删除报修工单
     * 
     * @param orderIds 需要删除的工单主键
     * @return 批量删除报修工单
     */
    public int deleteRepairOrderByOrderIds(Long[] orderIds);

    /**
     * 删除报修工单
     * 
     * @param orderId 工单主键
     * @return 删除报修工单
     */
    public int deleteRepairOrderByOrderId(Long orderId);

    /**
     * 派单
     * 
     * @param orderId 工单ID
     * @param workerId 维修工ID
     * @param remark 派单说明
     * @return 派单
     */
    public int dispatchOrder(Long orderId, Long workerId, String remark);

    /**
     * 维修工接单
     * 
     * @param orderId 工单ID
     * @param workerId 维修工ID
     * @return 维修工接单
     */
    public int acceptOrder(Long orderId, Long workerId);

    /**
     * 完工
     * 
     * @param orderId 工单ID
     * @param finishResult 维修结果
     * @param workerId 维修工ID
     * @return 完工
     */
    public int finishOrder(Long orderId, String finishResult, Long workerId);

    /**
     * 居民取消报修
     * 
     * @param orderId 工单ID
     * @param cancelReason 取消原因
     * @param residentId 报修人ID
     * @return 居民取消报修
     */
    public int cancelOrder(Long orderId, String cancelReason, Long residentId);

    /**
     * 维修工退回
     * 
     * @param orderId 工单ID
     * @param reason 退回原因
     * @param workerId 维修工ID
     * @return 维修工退回
     */
    public int returnOrder(Long orderId, String reason, Long workerId);

    /**
     * 追加维修进度
     * 
     * @param orderId 工单ID
     * @param content 进度说明
     * @param operatorId 操作人ID
     * @return 追加维修进度
     */
    public int addProgress(Long orderId, String content, Long operatorId);

    /**
     * 统计概览
     * 
     * @return 统计概览
     */
    public Map<String, Object> selectOverview();

    /**
     * 按类型统计
     * 
     * @return 按类型统计
     */
    public List<Map<String, Object>> selectCountByType();

    /**
     * 按状态统计
     * 
     * @return 按状态统计
     */
    public List<Map<String, Object>> selectCountByStatus();

    /**
     * 近 N 天报修趋势
     * 
     * @param days 统计天数
     * @return 近 N 天报修趋势
     */
    public List<Map<String, Object>> selectTrend(Integer days);

    /**
     * 维修工绩效排行
     * 
     * @param limit 取前 N 名
     * @return 维修工绩效排行
     */
    public List<Map<String, Object>> selectWorkerRank(Integer limit);

    /**
     * 扫描并标记超时工单
     * 
     * @return 扫描并标记超时工单
     */
    public int scanOvertimeOrders();

    /**
     * 查询待派单的超时工单
     * 
     * @return 工单集合
     */
    public List<RepairOrder> selectPendingOvertimeOrders();

    /**
     * 统计维修工待处理的任务数（待接单 + 维修中）
     * 
     * @param workerId 维修工用户ID
     * @return 数量
     */
    public int countWorkerPending(Long workerId);

    /**
     * 统计居民待评价的报修数（工单已完成、尚未评价）
     * 
     * @param residentId 居民用户ID
     * @return 数量
     */
    public int countResidentPendingEvaluation(Long residentId);

}
