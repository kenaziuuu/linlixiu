package com.linlixiu.system.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.linlixiu.common.utils.StringUtils;
import com.linlixiu.system.domain.RepairWorker;
import com.linlixiu.system.mapper.RepairWorkerMapper;
import com.linlixiu.system.service.IRepairWorkerService;

/**
 * 维修工档案Service业务层处理
 * 
 * @author linlixiu
 * @date 2026-11-06
 */
@Service
public class RepairWorkerServiceImpl implements IRepairWorkerService
{
    @Autowired
    private RepairWorkerMapper repairWorkerMapper;

    /**
     * 查询维修工
     */
    @Override
    public RepairWorker selectRepairWorkerById(Long workerId)
    {
        return repairWorkerMapper.selectRepairWorkerById(workerId);
    }

    /**
     * 按用户ID查询维修工
     */
    @Override
    public RepairWorker selectRepairWorkerByUserId(Long userId)
    {
        return repairWorkerMapper.selectRepairWorkerByUserId(userId);
    }

    /**
     * 查询维修工列表
     */
    @Override
    public List<RepairWorker> selectRepairWorkerList(RepairWorker repairWorker)
    {
        return repairWorkerMapper.selectRepairWorkerList(repairWorker);
    }

    /**
     * 查询可派单的维修工
     */
    @Override
    public List<RepairWorker> selectAvailableWorkers(String skill)
    {
        return repairWorkerMapper.selectAvailableWorkers(skill);
    }

    /**
     * 新增维修工
     */
    @Override
    public int insertRepairWorker(RepairWorker repairWorker)
    {
        if (StringUtils.isEmpty(repairWorker.getWorkerNo()))
        {
            repairWorker.setWorkerNo("W" + String.format("%04d", System.currentTimeMillis() % 10000));
        }
        if (StringUtils.isEmpty(repairWorker.getStatus()))
        {
            repairWorker.setStatus("0");
        }
        if (repairWorker.getTotalCount() == null)
        {
            repairWorker.setTotalCount(0);
        }
        if (repairWorker.getFinishCount() == null)
        {
            repairWorker.setFinishCount(0);
        }
        return repairWorkerMapper.insertRepairWorker(repairWorker);
    }

    /**
     * 修改维修工
     */
    @Override
    public int updateRepairWorker(RepairWorker repairWorker)
    {
        return repairWorkerMapper.updateRepairWorker(repairWorker);
    }

    /**
     * 按用户ID同步维修工档案的联系电话
     */
    @Override
    public int updatePhoneByUserId(Long userId, String phone)
    {
        if (userId == null)
        {
            return 0;
        }
        return repairWorkerMapper.updatePhoneByUserId(userId, phone);
    }

    /**
     * 批量删除维修工
     */
    @Override
    public int deleteRepairWorkerByIds(Long[] workerIds)
    {
        return repairWorkerMapper.deleteRepairWorkerByIds(workerIds);
    }

    /**
     * 刷新维修工统计
     */
    @Override
    public int refreshWorkerStat(Long workerId)
    {
        return repairWorkerMapper.refreshWorkerStat(workerId);
    }
}
