package com.linlixiu.system.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.linlixiu.common.annotation.Excel;
import com.linlixiu.common.core.domain.BaseEntity;

/**
 * 报修评价对象 repair_evaluation
 * 
 * @author linlixiu
 * @date 2026-11-06
 */
public class RepairEvaluation extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 评价ID */
    @Excel(name = "评价ID")
    private Long evaluationId;

    /** 工单ID */
    @Excel(name = "工单ID")
    private Long orderId;

    /** 报修单号 */
    @Excel(name = "报修单号")
    private String orderNo;

    /** 评价人ID */
    private Long residentId;

    /** 评价人 */
    @Excel(name = "评价人")
    private String residentName;

    /** 维修工ID */
    private Long workerId;

    /** 维修工 */
    @Excel(name = "维修工")
    private String workerName;

    /** 综合评分 */
    @Excel(name = "综合评分")
    private Integer score;

    /** 响应速度 */
    @Excel(name = "响应速度")
    private Integer speedScore;

    /** 维修质量 */
    @Excel(name = "维修质量")
    private Integer qualityScore;

    /** 服务态度 */
    @Excel(name = "服务态度")
    private Integer attitudeScore;

    /** 评价内容 */
    @Excel(name = "评价内容")
    private String content;

    /** 回复内容 */
    private String replyContent;

    /** 回复时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date replyTime;

    public void setEvaluationId(Long evaluationId)
    {
        this.evaluationId = evaluationId;
    }

    public Long getEvaluationId()
    {
        return evaluationId;
    }

    public void setOrderId(Long orderId)
    {
        this.orderId = orderId;
    }

    public Long getOrderId()
    {
        return orderId;
    }

    public void setOrderNo(String orderNo)
    {
        this.orderNo = orderNo;
    }

    public String getOrderNo()
    {
        return orderNo;
    }

    public void setResidentId(Long residentId)
    {
        this.residentId = residentId;
    }

    public Long getResidentId()
    {
        return residentId;
    }

    public void setResidentName(String residentName)
    {
        this.residentName = residentName;
    }

    public String getResidentName()
    {
        return residentName;
    }

    public void setWorkerId(Long workerId)
    {
        this.workerId = workerId;
    }

    public Long getWorkerId()
    {
        return workerId;
    }

    public void setWorkerName(String workerName)
    {
        this.workerName = workerName;
    }

    public String getWorkerName()
    {
        return workerName;
    }

    public void setScore(Integer score)
    {
        this.score = score;
    }

    public Integer getScore()
    {
        return score;
    }

    public void setSpeedScore(Integer speedScore)
    {
        this.speedScore = speedScore;
    }

    public Integer getSpeedScore()
    {
        return speedScore;
    }

    public void setQualityScore(Integer qualityScore)
    {
        this.qualityScore = qualityScore;
    }

    public Integer getQualityScore()
    {
        return qualityScore;
    }

    public void setAttitudeScore(Integer attitudeScore)
    {
        this.attitudeScore = attitudeScore;
    }

    public Integer getAttitudeScore()
    {
        return attitudeScore;
    }

    public void setContent(String content)
    {
        this.content = content;
    }

    public String getContent()
    {
        return content;
    }

    public void setReplyContent(String replyContent)
    {
        this.replyContent = replyContent;
    }

    public String getReplyContent()
    {
        return replyContent;
    }

    public void setReplyTime(Date replyTime)
    {
        this.replyTime = replyTime;
    }

    public Date getReplyTime()
    {
        return replyTime;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
            .append("evaluationId", getEvaluationId())
            .append("orderId", getOrderId())
            .append("orderNo", getOrderNo())
            .append("residentId", getResidentId())
            .append("residentName", getResidentName())
            .append("workerId", getWorkerId())
            .append("workerName", getWorkerName())
            .append("score", getScore())
            .append("speedScore", getSpeedScore())
            .append("qualityScore", getQualityScore())
            .append("attitudeScore", getAttitudeScore())
            .append("content", getContent())
            .append("replyContent", getReplyContent())
            .append("replyTime", getReplyTime())
            .toString();
    }
}
