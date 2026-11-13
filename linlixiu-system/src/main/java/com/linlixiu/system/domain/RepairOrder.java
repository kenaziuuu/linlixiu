package com.linlixiu.system.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.linlixiu.common.annotation.Excel;
import com.linlixiu.common.core.domain.BaseEntity;

/**
 * 报修工单对象 repair_order
 * 
 * @author linlixiu
 * @date 2026-11-06
 */
public class RepairOrder extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 工单ID */
    @Excel(name = "工单ID")
    private Long orderId;

    /** 报修单号 */
    @Excel(name = "报修单号")
    private String orderNo;

    /** 报修人ID */
    private Long residentId;

    /** 报修人 */
    @Excel(name = "报修人")
    private String residentName;

    /** 所属小区ID */
    private Long deptId;

    /** 所属小区 */
    @Excel(name = "所属小区")
    private String deptName;

    /** 报修类型 */
    @Excel(name = "报修类型", readConverterExp = "water=水电维修,electric=电路维修,door=门窗维修,appliance=家电维修,public=公共设施,other=其他")
    private String repairType;

    /** 报修标题 */
    @Excel(name = "报修标题")
    private String title;

    /** 问题描述 */
    @Excel(name = "问题描述")
    private String description;

    /** 报修地址 */
    @Excel(name = "报修地址")
    private String address;

    /** 联系人 */
    @Excel(name = "联系人")
    private String contactName;

    /** 联系电话 */
    @Excel(name = "联系电话")
    private String contactPhone;

    /** 紧急程度 */
    @Excel(name = "紧急程度", readConverterExp = "normal=普通,urgent=紧急,emergency=特急")
    private String urgency;

    /** 现场图片 */
    private String images;

    /** 工单状态 */
    @Excel(name = "工单状态", readConverterExp = "pending=待派单,assigned=待接单,repairing=维修中,completed=待评价,rated=已完成,cancelled=已取消")
    private String status;

    /** 维修工ID */
    private Long workerId;

    /** 维修工 */
    @Excel(name = "维修工")
    private String workerName;

    /** 派单人ID */
    private Long dispatcherId;

    /** 派单时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "派单时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date dispatchTime;

    /** 接单时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "接单时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date acceptTime;

    /** 完工时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "完工时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date finishTime;

    /** 维修结果 */
    @Excel(name = "维修结果")
    private String finishResult;

    /** 取消原因 */
    private String cancelReason;

    /** 是否超时 */
    @Excel(name = "是否超时", readConverterExp = "0=正常,1=超时")
    private Integer isOvertime;

    /** 是否已评价 */
    @Excel(name = "是否已评价", readConverterExp = "0=未评价,1=已评价")
    private Integer evaluateFlag;

    /** 评价星级 */
    private Integer score;

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

    public void setDeptId(Long deptId)
    {
        this.deptId = deptId;
    }

    public Long getDeptId()
    {
        return deptId;
    }

    public void setDeptName(String deptName)
    {
        this.deptName = deptName;
    }

    public String getDeptName()
    {
        return deptName;
    }

    public void setRepairType(String repairType)
    {
        this.repairType = repairType;
    }

    public String getRepairType()
    {
        return repairType;
    }

    public void setTitle(String title)
    {
        this.title = title;
    }

    public String getTitle()
    {
        return title;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    public String getDescription()
    {
        return description;
    }

    public void setAddress(String address)
    {
        this.address = address;
    }

    public String getAddress()
    {
        return address;
    }

    public void setContactName(String contactName)
    {
        this.contactName = contactName;
    }

    public String getContactName()
    {
        return contactName;
    }

    public void setContactPhone(String contactPhone)
    {
        this.contactPhone = contactPhone;
    }

    public String getContactPhone()
    {
        return contactPhone;
    }

    public void setUrgency(String urgency)
    {
        this.urgency = urgency;
    }

    public String getUrgency()
    {
        return urgency;
    }

    public void setImages(String images)
    {
        this.images = images;
    }

    public String getImages()
    {
        return images;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    public String getStatus()
    {
        return status;
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

    public void setDispatcherId(Long dispatcherId)
    {
        this.dispatcherId = dispatcherId;
    }

    public Long getDispatcherId()
    {
        return dispatcherId;
    }

    public void setDispatchTime(Date dispatchTime)
    {
        this.dispatchTime = dispatchTime;
    }

    public Date getDispatchTime()
    {
        return dispatchTime;
    }

    public void setAcceptTime(Date acceptTime)
    {
        this.acceptTime = acceptTime;
    }

    public Date getAcceptTime()
    {
        return acceptTime;
    }

    public void setFinishTime(Date finishTime)
    {
        this.finishTime = finishTime;
    }

    public Date getFinishTime()
    {
        return finishTime;
    }

    public void setFinishResult(String finishResult)
    {
        this.finishResult = finishResult;
    }

    public String getFinishResult()
    {
        return finishResult;
    }

    public void setCancelReason(String cancelReason)
    {
        this.cancelReason = cancelReason;
    }

    public String getCancelReason()
    {
        return cancelReason;
    }

    public void setIsOvertime(Integer isOvertime)
    {
        this.isOvertime = isOvertime;
    }

    public Integer getIsOvertime()
    {
        return isOvertime;
    }

    public void setEvaluateFlag(Integer evaluateFlag)
    {
        this.evaluateFlag = evaluateFlag;
    }

    public Integer getEvaluateFlag()
    {
        return evaluateFlag;
    }

    public void setScore(Integer score)
    {
        this.score = score;
    }

    public Integer getScore()
    {
        return score;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
            .append("orderId", getOrderId())
            .append("orderNo", getOrderNo())
            .append("residentId", getResidentId())
            .append("residentName", getResidentName())
            .append("deptId", getDeptId())
            .append("deptName", getDeptName())
            .append("repairType", getRepairType())
            .append("title", getTitle())
            .append("description", getDescription())
            .append("address", getAddress())
            .append("contactName", getContactName())
            .append("contactPhone", getContactPhone())
            .append("urgency", getUrgency())
            .append("images", getImages())
            .append("status", getStatus())
            .append("workerId", getWorkerId())
            .append("workerName", getWorkerName())
            .append("dispatcherId", getDispatcherId())
            .append("dispatchTime", getDispatchTime())
            .append("acceptTime", getAcceptTime())
            .append("finishTime", getFinishTime())
            .append("finishResult", getFinishResult())
            .append("cancelReason", getCancelReason())
            .append("isOvertime", getIsOvertime())
            .append("evaluateFlag", getEvaluateFlag())
            .append("score", getScore())
            .toString();
    }
}
