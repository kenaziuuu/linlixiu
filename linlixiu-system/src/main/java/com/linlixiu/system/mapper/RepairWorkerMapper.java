package com.linlixiu.system.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.linlixiu.system.domain.RepairWorker;

/**
 * 维修工档案Mapper接口
 * 
 * @author linlixiu
 * @date 2026-11-06
 */
public interface RepairWorkerMapper
{
    /**
     * 查询维修工
     * 
     * @param workerId 维修工ID
     * @return 维修工档案
     */
    public RepairWorker selectRepairWorkerById(Long workerId);

    /**
     * 按用户ID查询维修工
     * 
     * @param userId 用户ID
     * @return 维修工档案
     */
    public RepairWorker selectRepairWorkerByUserId(Long userId);

    /**
     * 查询维修工列表
     * 
     * @param repairWorker 维修工档案
     * @return 维修工集合
     */
    public List<RepairWorker> selectRepairWorkerList(RepairWorker repairWorker);

    /**
     * 查询可派单的维修工
     * 
     * @param skill 技能类型，可为空
     * @return 维修工集合
     */
    public List<RepairWorker> selectAvailableWorkers(@Param("skill") String skill);

    /**
     * 新增维修工
     * 
     * @param repairWorker 维修工档案
     * @return 结果
     */
    public int insertRepairWorker(RepairWorker repairWorker);

    /**
     * 修改维修工
     * 
     * @param repairWorker 维修工档案
     * @return 结果
     */
    public int updateRepairWorker(RepairWorker repairWorker);

    /**
     * 按用户ID同步维修工档案的联系电话
     * 
     * @param userId 用户ID
     * @param phone 联系电话
     * @return 结果
     */
    public int updatePhoneByUserId(@Param("userId") Long userId, @Param("phone") String phone);

    /**
     * 批量删除维修工
     * 
     * @param workerIds 维修工ID数组
     * @return 结果
     */
    public int deleteRepairWorkerByIds(Long[] workerIds);

    /**
     * 刷新维修工接单统计
     * 
     * @param workerId 维修工ID
     * @return 结果
     */
    public int refreshWorkerStat(Long workerId);
}
