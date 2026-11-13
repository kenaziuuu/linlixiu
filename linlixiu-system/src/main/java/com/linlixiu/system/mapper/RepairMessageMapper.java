package com.linlixiu.system.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.linlixiu.system.domain.RepairMessage;

/**
 * 站内消息Mapper接口
 * 
 * @author linlixiu
 * @date 2026-11-06
 */
public interface RepairMessageMapper
{
    /**
     * 查询消息列表
     * 
     * @param repairMessage 站内消息
     * @return 消息集合
     */
    public List<RepairMessage> selectRepairMessageList(RepairMessage repairMessage);

    /**
     * 新增消息
     * 
     * @param repairMessage 站内消息
     * @return 结果
     */
    public int insertRepairMessage(RepairMessage repairMessage);

    /**
     * 标记已读
     * 
     * @param messageId 消息ID
     * @param receiverId 接收人ID
     * @return 结果
     */
    public int markRead(@Param("messageId") Long messageId, @Param("receiverId") Long receiverId);

    /**
     * 全部已读
     * 
     * @param receiverId 接收人ID
     * @return 结果
     */
    public int markAllRead(Long receiverId);

    /**
     * 未读数量
     * 
     * @param receiverId 接收人ID
     * @return 未读条数
     */
    public int selectUnreadCount(Long receiverId);

    /**
     * 批量删除消息
     * 
     * @param messageIds 消息ID数组
     * @return 结果
     */
    public int deleteRepairMessageByIds(Long[] messageIds);
}
