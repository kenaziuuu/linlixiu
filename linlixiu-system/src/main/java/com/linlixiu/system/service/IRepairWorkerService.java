package com.linlixiu.system.service;

import java.util.List;
import com.linlixiu.system.domain.RepairWorker;

/**
 * 维修工档案接口
 * 
 * @author linlixiu
 * @date 2026-11-06
 */
public interface IRepairWorkerService
{
    /**
     * 查询维修工
     * 
     * @param workerId 维修工ID
     * @return 查询维修工
     */
    public RepairWorker selectRepairWorkerById(Long workerId);

    /**
     * 按用户查询维修工
     * 
     * @param userId 用户ID
     * @return 按用户查询维修工
     */
    public RepairWorker selectRepairWorkerByUserId(Long userId);

    /**
     * 查询维修工列表
     * 
     * @param repairWorker 维修工档案
     * @return 查询维修工列表
     */
    public List<RepairWorker> selectRepairWorkerList(RepairWorker repairWorker);

    /**
     * 查询可派单维修工
     * 
     * @param skill 技能类型
     * @return 查询可派单维修工
     */
    public List<RepairWorker> selectAvailableWorkers(String skill);

    /**
     * 新增维修工
     * 
     * @param repairWorker 维修工档案
     * @return 新增维修工
     */
    public int insertRepairWorker(RepairWorker repairWorker);

    /**
     * 修改维修工
     * 
     * @param repairWorker 维修工档案
     * @return 修改维修工
     */
    public int updateRepairWorker(RepairWorker repairWorker);

    /**
     * 按用户ID同步维修工档案的联系电话
     * 
     * @param userId 用户ID
     * @param phone 联系电话
     * @return 结果
     */
    public int updatePhoneByUserId(Long userId, String phone);

    /**
     * 删除维修工
     * 
     * @param workerIds 维修工ID数组
     * @return 删除维修工
     */
    public int deleteRepairWorkerByIds(Long[] workerIds);

    /**
     * 刷新维修工统计
     * 
     * @param workerId 维修工ID
     * @return 刷新维修工统计
     */
    public int refreshWorkerStat(Long workerId);

}
