package com.linlixiu.system.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.linlixiu.system.domain.RepairMessage;
import com.linlixiu.system.mapper.RepairMessageMapper;
import com.linlixiu.system.service.IRepairMessageService;

/**
 * 站内消息Service业务层处理
 * 
 * @author linlixiu
 * @date 2026-11-06
 */
@Service
public class RepairMessageServiceImpl implements IRepairMessageService
{
    @Autowired
    private RepairMessageMapper repairMessageMapper;

    /**
     * 查询消息列表
     */
    @Override
    public List<RepairMessage> selectRepairMessageList(RepairMessage repairMessage)
    {
        return repairMessageMapper.selectRepairMessageList(repairMessage);
    }

    /**
     * 新增消息
     */
    @Override
    public int insertRepairMessage(RepairMessage repairMessage)
    {
        if (repairMessage.getIsRead() == null)
        {
            repairMessage.setIsRead(0);
        }
        return repairMessageMapper.insertRepairMessage(repairMessage);
    }

    /**
     * 标记已读
     */
    @Override
    public int markRead(Long messageId, Long receiverId)
    {
        return repairMessageMapper.markRead(messageId, receiverId);
    }

    /**
     * 全部已读
     */
    @Override
    public int markAllRead(Long receiverId)
    {
        return repairMessageMapper.markAllRead(receiverId);
    }

    /**
     * 未读数量
     */
    @Override
    public int selectUnreadCount(Long receiverId)
    {
        return repairMessageMapper.selectUnreadCount(receiverId);
    }

    /**
     * 批量删除消息
     */
    @Override
    public int deleteRepairMessageByIds(Long[] messageIds)
    {
        return repairMessageMapper.deleteRepairMessageByIds(messageIds);
    }
}
