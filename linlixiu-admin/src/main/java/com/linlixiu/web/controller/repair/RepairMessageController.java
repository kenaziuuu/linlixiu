package com.linlixiu.web.controller.repair;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.linlixiu.common.core.controller.BaseController;
import com.linlixiu.common.core.domain.AjaxResult;
import com.linlixiu.common.core.page.TableDataInfo;
import com.linlixiu.common.utils.SecurityUtils;
import com.linlixiu.system.domain.RepairMessage;
import com.linlixiu.system.service.IRepairMessageService;

/**
 * 消息通知Controller
 *
 * 面向全部角色：查看本人的站内消息、标记已读、查询未读数量。
 *
 * @author linlixiu
 * @date 2026-11-06
 */
@RestController
@RequestMapping("/repair/message")
public class RepairMessageController extends BaseController
{
    @Autowired
    private IRepairMessageService repairMessageService;

    /**
     * 查询我的消息列表
     */
    @PreAuthorize("@ss.hasPermi('repair:message:list')")
    @GetMapping("/list")
    public TableDataInfo list(RepairMessage repairMessage)
    {
        repairMessage.setReceiverId(SecurityUtils.getUserId());
        startPage();
        List<RepairMessage> list = repairMessageService.selectRepairMessageList(repairMessage);
        return getDataTable(list);
    }

    /**
     * 未读消息数量
     */
    @PreAuthorize("@ss.hasPermi('repair:message:list')")
    @GetMapping("/unreadCount")
    public AjaxResult unreadCount()
    {
        return success(repairMessageService.selectUnreadCount(SecurityUtils.getUserId()));
    }

    /**
     * 标记单条已读
     */
    @PreAuthorize("@ss.hasPermi('repair:message:list')")
    @PutMapping("/read/{messageId}")
    public AjaxResult read(@PathVariable("messageId") Long messageId)
    {
        return toAjax(repairMessageService.markRead(messageId, SecurityUtils.getUserId()));
    }

    /**
     * 全部标记已读
     */
    @PreAuthorize("@ss.hasPermi('repair:message:list')")
    @PutMapping("/readAll")
    public AjaxResult readAll()
    {
        return toAjax(repairMessageService.markAllRead(SecurityUtils.getUserId()));
    }

    /**
     * 删除消息
     */
    @PreAuthorize("@ss.hasPermi('repair:message:remove')")
    @DeleteMapping("/{messageIds}")
    public AjaxResult remove(@PathVariable Long[] messageIds)
    {
        return toAjax(repairMessageService.deleteRepairMessageByIds(messageIds));
    }
}
