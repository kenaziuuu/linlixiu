package com.linlixiu.system.mapper;

import java.util.List;
import com.linlixiu.system.domain.RepairProgress;

/**
 * 报修进度Mapper接口
 * 
 * @author linlixiu
 * @date 2026-11-06
 */
public interface RepairProgressMapper
{
    /**
     * 查询报修进度列表
     * 
     * @param repairProgress 报修进度
     * @return 报修进度集合
     */
    public List<RepairProgress> selectRepairProgressList(RepairProgress repairProgress);

    /**
     * 新增报修进度
     * 
     * @param repairProgress 报修进度
     * @return 结果
     */
    public int insertRepairProgress(RepairProgress repairProgress);

    /**
     * 按工单删除进度
     * 
     * @param orderId 工单ID
     * @return 结果
     */
    public int deleteRepairProgressByOrderId(Long orderId);
}
