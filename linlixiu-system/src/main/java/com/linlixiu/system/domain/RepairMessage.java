package com.linlixiu.system.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.linlixiu.common.annotation.Excel;
import com.linlixiu.common.core.domain.BaseEntity;

/**
 * 站内消息对象 repair_message
 * 
 * @author linlixiu
 * @date 2026-11-06
 */
public class RepairMessage extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 消息ID */
    @Excel(name = "消息ID")
    private Long messageId;

    /** 接收人ID */
    private Long receiverId;

    /** 接收人 */
    @Excel(name = "接收人")
    private String receiverName;

    /** 消息标题 */
    @Excel(name = "消息标题")
    private String title;

    /** 消息内容 */
    @Excel(name = "消息内容")
    private String content;

    /** 消息类型 */
    @Excel(name = "消息类型", readConverterExp = "dispatch=派单通知,accept=接单通知,finish=完工通知,rate=评价通知,urge=催办通知,system=系统消息")
    private String msgType;

    /** 关联工单ID */
    private Long orderId;

    /** 是否已读 */
    @Excel(name = "是否已读", readConverterExp = "0=未读,1=已读")
    private Integer isRead;

    public void setMessageId(Long messageId)
    {
        this.messageId = messageId;
    }

    public Long getMessageId()
    {
        return messageId;
    }

    public void setReceiverId(Long receiverId)
    {
        this.receiverId = receiverId;
    }

    public Long getReceiverId()
    {
        return receiverId;
    }

    public void setReceiverName(String receiverName)
    {
        this.receiverName = receiverName;
    }

    public String getReceiverName()
    {
        return receiverName;
    }

    public void setTitle(String title)
    {
        this.title = title;
    }

    public String getTitle()
    {
        return title;
    }

    public void setContent(String content)
    {
        this.content = content;
    }

    public String getContent()
    {
        return content;
    }

    public void setMsgType(String msgType)
    {
        this.msgType = msgType;
    }

    public String getMsgType()
    {
        return msgType;
    }

    public void setOrderId(Long orderId)
    {
        this.orderId = orderId;
    }

    public Long getOrderId()
    {
        return orderId;
    }

    public void setIsRead(Integer isRead)
    {
        this.isRead = isRead;
    }

    public Integer getIsRead()
    {
        return isRead;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
            .append("messageId", getMessageId())
            .append("receiverId", getReceiverId())
            .append("receiverName", getReceiverName())
            .append("title", getTitle())
            .append("content", getContent())
            .append("msgType", getMsgType())
            .append("orderId", getOrderId())
            .append("isRead", getIsRead())
            .toString();
    }
}
