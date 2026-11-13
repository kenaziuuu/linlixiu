package com.linlixiu.web.controller.repair;

import java.util.List;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.linlixiu.common.annotation.Log;
import com.linlixiu.common.core.controller.BaseController;
import com.linlixiu.common.core.domain.AjaxResult;
import com.linlixiu.common.core.page.TableDataInfo;
import com.linlixiu.common.enums.BusinessType;
import com.linlixiu.common.utils.SecurityUtils;
import com.linlixiu.common.utils.poi.ExcelUtil;
import com.linlixiu.system.domain.RepairOrder;
import com.linlixiu.system.domain.RepairWorker;
import com.linlixiu.system.service.IRepairOrderService;
import com.linlixiu.system.service.IRepairWorkerService;

/**
 * 维修任务Controller
 *
 * 面向维修工角色：查看指派给自己的工单、接单、更新进度、完工、退回。
 *
 * @author linlixiu
 * @date 2026-11-06
 */
@RestController
@RequestMapping("/repair/task")
public class RepairTaskController extends BaseController
{
    @Autowired
    private IRepairOrderService repairOrderService;

    @Autowired
    private IRepairWorkerService repairWorkerService;

    /**
     * 查询我的维修任务
     */
    @PreAuthorize("@ss.hasPermi('repair:task:list')")
    @GetMapping("/list")
    public TableDataInfo list(RepairOrder repairOrder)
    {
        repairOrder.setWorkerId(SecurityUtils.getUserId());
        startPage();
        List<RepairOrder> list = repairOrderService.selectRepairOrderList(repairOrder);
        return getDataTable(list);
    }

    /**
     * 导出我的维修任务
     */
    @PreAuthorize("@ss.hasPermi('repair:task:list')")
    @Log(title = "维修任务", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, RepairOrder repairOrder)
    {
        repairOrder.setWorkerId(SecurityUtils.getUserId());
        List<RepairOrder> list = repairOrderService.selectRepairOrderList(repairOrder);
        ExcelUtil<RepairOrder> util = new ExcelUtil<RepairOrder>(RepairOrder.class);
        util.exportExcel(response, list, "维修任务数据");
    }

    /**
     * 获取工单详情
     */
    @PreAuthorize("@ss.hasPermi('repair:task:list')")
    @GetMapping(value = "/{orderId}")
    public AjaxResult getInfo(@PathVariable("orderId") Long orderId)
    {
        return success(repairOrderService.selectRepairOrderByOrderId(orderId));
    }

    /**
     * 查询当前登录维修工的档案（含技能、评分）
     */
    @PreAuthorize("@ss.hasPermi('repair:task:list')")
    @GetMapping("/profile")
    public AjaxResult profile()
    {
        RepairWorker worker = repairWorkerService.selectRepairWorkerByUserId(SecurityUtils.getUserId());
        return success(worker);
    }

    /**
     * 接单
     */
    @PreAuthorize("@ss.hasPermi('repair:task:accept')")
    @Log(title = "维修任务", businessType = BusinessType.UPDATE)
    @PutMapping("/accept/{orderId}")
    public AjaxResult accept(@PathVariable("orderId") Long orderId)
    {
        return toAjax(repairOrderService.acceptOrder(orderId, SecurityUtils.getUserId()));
    }

    /**
     * 完工
     */
    @PreAuthorize("@ss.hasPermi('repair:task:finish')")
    @Log(title = "维修任务", businessType = BusinessType.UPDATE)
    @PutMapping("/finish")
    public AjaxResult finish(@RequestBody RepairOrder body)
    {
        if (body.getOrderId() == null)
        {
            return error("缺少工单编号");
        }
        return toAjax(repairOrderService.finishOrder(body.getOrderId(), body.getFinishResult(),
                SecurityUtils.getUserId()));
    }

    /**
     * 退回工单
     */
    @PreAuthorize("@ss.hasPermi('repair:task:return')")
    @Log(title = "维修任务", businessType = BusinessType.UPDATE)
    @PutMapping("/return")
    public AjaxResult returnOrder(@RequestBody RepairOrder body)
    {
        if (body.getOrderId() == null)
        {
            return error("缺少工单编号");
        }
        return toAjax(repairOrderService.returnOrder(body.getOrderId(), body.getCancelReason(),
                SecurityUtils.getUserId()));
    }

    /**
     * 追加维修进度
     */
    @PreAuthorize("@ss.hasPermi('repair:task:progress')")
    @Log(title = "维修任务", businessType = BusinessType.UPDATE)
    @PostMapping("/progress")
    public AjaxResult progress(@RequestBody RepairOrder body)
    {
        if (body.getOrderId() == null)
        {
            return error("缺少工单编号");
        }
        return toAjax(repairOrderService.addProgress(body.getOrderId(), body.getRemark(),
                SecurityUtils.getUserId()));
    }
}
