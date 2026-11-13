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
import com.linlixiu.system.domain.RepairMessage;
import com.linlixiu.system.domain.RepairOrder;
import com.linlixiu.system.domain.RepairWorker;
import com.linlixiu.system.service.IRepairMessageService;
import com.linlixiu.system.service.IRepairOrderService;
import com.linlixiu.system.service.IRepairWorkerService;

/**
 * 工单派单Controller
 *
 * 面向调度员角色：查看全部工单、把待派单工单指派给维修工、改派、催办。
 *
 * @author linlixiu
 * @date 2026-11-06
 */
@RestController
@RequestMapping("/repair/dispatch")
public class RepairDispatchController extends BaseController
{
    @Autowired
    private IRepairOrderService repairOrderService;

    @Autowired
    private IRepairWorkerService repairWorkerService;

    @Autowired
    private IRepairMessageService repairMessageService;

    /**
     * 查询工单列表（调度视角，可按状态筛选）
     */
    @PreAuthorize("@ss.hasPermi('repair:dispatch:list')")
    @GetMapping("/list")
    public TableDataInfo list(RepairOrder repairOrder)
    {
        startPage();
        List<RepairOrder> list = repairOrderService.selectRepairOrderList(repairOrder);
        return getDataTable(list);
    }

    /**
     * 导出工单列表
     */
    @PreAuthorize("@ss.hasPermi('repair:dispatch:list')")
    @Log(title = "报修派单", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, RepairOrder repairOrder)
    {
        List<RepairOrder> list = repairOrderService.selectRepairOrderList(repairOrder);
        ExcelUtil<RepairOrder> util = new ExcelUtil<RepairOrder>(RepairOrder.class);
        util.exportExcel(response, list, "报修工单数据");
    }

    /**
     * 获取工单详情
     */
    @PreAuthorize("@ss.hasPermi('repair:dispatch:list')")
    @GetMapping(value = "/{orderId}")
    public AjaxResult getInfo(@PathVariable("orderId") Long orderId)
    {
        return success(repairOrderService.selectRepairOrderByOrderId(orderId));
    }

    /**
     * 查询可派单的维修工（按报修类型匹配技能）
     */
    @PreAuthorize("@ss.hasPermi('repair:dispatch:list')")
    @GetMapping("/workers")
    public AjaxResult workers(String skill)
    {
        List<RepairWorker> list = repairWorkerService.selectAvailableWorkers(skill);
        return success(list);
    }

    /**
     * 派单 / 改派
     */
    @PreAuthorize("@ss.hasPermi('repair:dispatch:assign')")
    @Log(title = "报修派单", businessType = BusinessType.UPDATE)
    @PutMapping("/assign")
    public AjaxResult assign(@RequestBody RepairOrder body)
    {
        if (body.getOrderId() == null || body.getWorkerId() == null)
        {
            return error("请选择工单与维修工");
        }
        return toAjax(repairOrderService.dispatchOrder(body.getOrderId(), body.getWorkerId(), body.getRemark()));
    }

    /**
     * 催办：给当前维修工推送一条催办消息
     */
    @PreAuthorize("@ss.hasPermi('repair:dispatch:urge')")
    @Log(title = "报修派单", businessType = BusinessType.UPDATE)
    @PutMapping("/urge/{orderId}")
    public AjaxResult urge(@PathVariable("orderId") Long orderId)
    {
        RepairOrder order = repairOrderService.selectRepairOrderByOrderId(orderId);
        if (order == null)
        {
            return error("报修工单不存在");
        }
        if (order.getWorkerId() == null)
        {
            return error("该工单尚未派单，无法催办");
        }
        RepairMessage message = new RepairMessage();
        message.setReceiverId(order.getWorkerId());
        message.setTitle("工单催办提醒");
        message.setContent("工单 " + order.getOrderNo() + "（" + order.getTitle() + "）已超时，请尽快处理。");
        message.setMsgType("urge");
        message.setOrderId(orderId);
        message.setIsRead(0);
        repairMessageService.insertRepairMessage(message);
        repairOrderService.addProgress(orderId, "调度员催办：请尽快处理该工单", SecurityUtils.getUserId());
        return success();
    }
}
