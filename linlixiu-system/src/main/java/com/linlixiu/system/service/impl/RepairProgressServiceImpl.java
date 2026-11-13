package com.linlixiu.system.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.linlixiu.system.domain.RepairProgress;
import com.linlixiu.system.mapper.RepairProgressMapper;
import com.linlixiu.system.service.IRepairProgressService;

/**
 * 报修进度Service业务层处理
 * 
 * @author linlixiu
 * @date 2026-11-06
 */
@Service
public class RepairProgressServiceImpl implements IRepairProgressService
{
    @Autowired
    private RepairProgressMapper repairProgressMapper;

    /**
     * 查询报修进度列表
     */
    @Override
    public List<RepairProgress> selectRepairProgressList(RepairProgress repairProgress)
    {
        return repairProgressMapper.selectRepairProgressList(repairProgress);
    }

    /**
     * 新增报修进度
     */
    @Override
    public int insertRepairProgress(RepairProgress repairProgress)
    {
        return repairProgressMapper.insertRepairProgress(repairProgress);
    }

    /**
     * 按工单删除进度
     */
    @Override
    public int deleteRepairProgressByOrderId(Long orderId)
    {
        return repairProgressMapper.deleteRepairProgressByOrderId(orderId);
    }
}
