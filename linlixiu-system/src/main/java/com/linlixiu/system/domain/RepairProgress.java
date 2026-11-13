package com.linlixiu.system.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.linlixiu.common.annotation.Excel;
import com.linlixiu.common.core.domain.BaseEntity;

/**
 * 报修进度对象 repair_progress
 * 
 * @author linlixiu
 * @date 2026-11-06
 */
public class RepairProgress extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 进度ID */
    @Excel(name = "进度ID")
    private Long progressId;

    /** 工单ID */
    @Excel(name = "工单ID")
    private Long orderId;

    /** 节点类型 */
    @Excel(name = "节点类型", readConverterExp = "submit=提交报修,dispatch=派单,accept=接单,repair=维修中,finish=完工,rate=评价,cancel=取消,return=退回")
    private String node;

    /** 节点名称 */
    @Excel(name = "节点名称")
    private String nodeName;

    /** 进度说明 */
    @Excel(name = "进度说明")
    private String content;

    /** 操作人ID */
    private Long operatorId;

    /** 操作人 */
    @Excel(name = "操作人")
    private String operatorName;

    /** 现场图片 */
    private String images;

    public void setProgressId(Long progressId)
    {
        this.progressId = progressId;
    }

    public Long getProgressId()
    {
        return progressId;
    }

    public void setOrderId(Long orderId)
    {
        this.orderId = orderId;
    }

    public Long getOrderId()
    {
        return orderId;
    }

    public void setNode(String node)
    {
        this.node = node;
    }

    public String getNode()
    {
        return node;
    }

    public void setNodeName(String nodeName)
    {
        this.nodeName = nodeName;
    }

    public String getNodeName()
    {
        return nodeName;
    }

    public void setContent(String content)
    {
        this.content = content;
    }

    public String getContent()
    {
        return content;
    }

    public void setOperatorId(Long operatorId)
    {
        this.operatorId = operatorId;
    }

    public Long getOperatorId()
    {
        return operatorId;
    }

    public void setOperatorName(String operatorName)
    {
        this.operatorName = operatorName;
    }

    public String getOperatorName()
    {
        return operatorName;
    }

    public void setImages(String images)
    {
        this.images = images;
    }

    public String getImages()
    {
        return images;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
            .append("progressId", getProgressId())
            .append("orderId", getOrderId())
            .append("node", getNode())
            .append("nodeName", getNodeName())
            .append("content", getContent())
            .append("operatorId", getOperatorId())
            .append("operatorName", getOperatorName())
            .append("images", getImages())
            .toString();
    }
}
