package com.linlixiu.system.mapper;

import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Param;
import com.linlixiu.system.domain.RepairOrder;

/**
 * 报修工单Mapper接口
 * 
 * @author linlixiu
 * @date 2026-11-06
 */
public interface RepairOrderMapper
{
    /**
     * 查询报修工单
     * 
     * @param orderId 工单ID
     * @return 报修工单
     */
    public RepairOrder selectRepairOrderByOrderId(Long orderId);

    /**
     * 查询报修工单列表
     * 
     * @param repairOrder 报修工单
     * @return 报修工单集合
     */
    public List<RepairOrder> selectRepairOrderList(RepairOrder repairOrder);

    /**
     * 新增报修工单
     * 
     * @param repairOrder 报修工单
     * @return 结果
     */
    public int insertRepairOrder(RepairOrder repairOrder);

    /**
     * 修改报修工单
     * 
     * @param repairOrder 报修工单
     * @return 结果
     */
    public int updateRepairOrder(RepairOrder repairOrder);

    /**
     * 删除报修工单
     * 
     * @param orderId 工单ID
     * @return 结果
     */
    public int deleteRepairOrderByOrderId(Long orderId);

    /**
     * 批量删除报修工单
     * 
     * @param orderIds 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteRepairOrderByOrderIds(Long[] orderIds);

    /**
     * 清空工单的维修工与接单时间（退回后重新进入待派单池）
     * 
     * @param orderId 工单ID
     * @return 结果
     */
    public int clearWorker(Long orderId);

    /**
     * 统计概览
     * 
     * @return 统计结果
     */
    public Map<String, Object> selectOverview();

    /**
     * 按报修类型统计
     * 
     * @return 统计结果
     */
    public List<Map<String, Object>> selectCountByType();

    /**
     * 按工单状态统计
     * 
     * @return 统计结果
     */
    public List<Map<String, Object>> selectCountByStatus();

    /**
     * 近 N 天报修趋势
     * 
     * @param days 统计天数
     * @return 统计结果
     */
    public List<Map<String, Object>> selectTrend(@Param("days") Integer days);

    /**
     * 维修工绩效排行
     * 
     * @param limit 取前 N 名
     * @return 统计结果
     */
    public List<Map<String, Object>> selectWorkerRank(@Param("limit") Integer limit);

    /**
     * 扫描并标记超时工单
     * 
     * @return 结果
     */
    public int scanOvertimeOrders();

    /**
     * 查询待派单超时工单（用于发送催办消息）
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
    public int countWorkerPending(@Param("workerId") Long workerId);

    /**
     * 统计居民待评价的报修数（工单已完成、尚未评价）
     * 
     * @param residentId 居民用户ID
     * @return 数量
     */
    public int countResidentPendingEvaluation(@Param("residentId") Long residentId);
}
