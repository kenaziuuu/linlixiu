package com.linlixiu.system.domain;

import java.math.BigDecimal;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.linlixiu.common.annotation.Excel;
import com.linlixiu.common.core.domain.BaseEntity;

/**
 * 维修工档案对象 repair_worker
 * 
 * @author linlixiu
 * @date 2026-11-06
 */
public class RepairWorker extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 维修工ID */
    @Excel(name = "维修工ID")
    private Long workerId;

    /** 关联用户ID */
    private Long userId;

    /** 工号 */
    @Excel(name = "工号")
    private String workerNo;

    /** 姓名 */
    @Excel(name = "姓名")
    private String workerName;

    /** 手机号 */
    @Excel(name = "手机号")
    private String phone;

    /** 技能类型 */
    @Excel(name = "技能类型")
    private String skills;

    /** 在岗状态 */
    @Excel(name = "在岗状态", readConverterExp = "0=在岗,1=休息,2=离职")
    private String status;

    /** 累计接单 */
    @Excel(name = "累计接单")
    private Integer totalCount;

    /** 完成数 */
    @Excel(name = "完成数")
    private Integer finishCount;

    /** 平均评分 */
    @Excel(name = "平均评分")
    private BigDecimal avgScore;

    public void setWorkerId(Long workerId)
    {
        this.workerId = workerId;
    }

    public Long getWorkerId()
    {
        return workerId;
    }

    public void setUserId(Long userId)
    {
        this.userId = userId;
    }

    public Long getUserId()
    {
        return userId;
    }

    public void setWorkerNo(String workerNo)
    {
        this.workerNo = workerNo;
    }

    public String getWorkerNo()
    {
        return workerNo;
    }

    public void setWorkerName(String workerName)
    {
        this.workerName = workerName;
    }

    public String getWorkerName()
    {
        return workerName;
    }

    public void setPhone(String phone)
    {
        this.phone = phone;
    }

    public String getPhone()
    {
        return phone;
    }

    public void setSkills(String skills)
    {
        this.skills = skills;
    }

    public String getSkills()
    {
        return skills;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    public String getStatus()
    {
        return status;
    }

    public void setTotalCount(Integer totalCount)
    {
        this.totalCount = totalCount;
    }

    public Integer getTotalCount()
    {
        return totalCount;
    }

    public void setFinishCount(Integer finishCount)
    {
        this.finishCount = finishCount;
    }

    public Integer getFinishCount()
    {
        return finishCount;
    }

    public void setAvgScore(BigDecimal avgScore)
    {
        this.avgScore = avgScore;
    }

    public BigDecimal getAvgScore()
    {
        return avgScore;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
            .append("workerId", getWorkerId())
            .append("userId", getUserId())
            .append("workerNo", getWorkerNo())
            .append("workerName", getWorkerName())
            .append("phone", getPhone())
            .append("skills", getSkills())
            .append("status", getStatus())
            .append("totalCount", getTotalCount())
            .append("finishCount", getFinishCount())
            .append("avgScore", getAvgScore())
            .toString();
    }
}
