package com.linlixiu.web.controller.repair;

import java.util.List;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
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
import com.linlixiu.system.domain.RepairProgress;
import com.linlixiu.system.service.IRepairOrderService;
import com.linlixiu.system.service.IRepairProgressService;

/**
 * 居民报修Controller
 *
 * 面向居民角色：提交报修、查看本人报修、取消报修、查看处理进度。
 *
 * @author linlixiu
 * @date 2026-11-06
 */
@RestController
@RequestMapping("/repair/order")
public class RepairOrderController extends BaseController
{
    @Autowired
    private IRepairOrderService repairOrderService;

    @Autowired
    private IRepairProgressService repairProgressService;

    /**
     * 查询我的报修列表（强制按当前登录居民过滤）
     */
    @PreAuthorize("@ss.hasPermi('repair:order:list')")
    @GetMapping("/list")
    public TableDataInfo list(RepairOrder repairOrder)
    {
        repairOrder.setResidentId(SecurityUtils.getUserId());
        startPage();
        List<RepairOrder> list = repairOrderService.selectRepairOrderList(repairOrder);
        return getDataTable(list);
    }

    /**
     * 导出我的报修列表
     */
    @PreAuthorize("@ss.hasPermi('repair:order:export')")
    @Log(title = "报修工单", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, RepairOrder repairOrder)
    {
        repairOrder.setResidentId(SecurityUtils.getUserId());
        List<RepairOrder> list = repairOrderService.selectRepairOrderList(repairOrder);
        ExcelUtil<RepairOrder> util = new ExcelUtil<RepairOrder>(RepairOrder.class);
        util.exportExcel(response, list, "我的报修数据");
    }

    /**
     * 获取报修工单详细信息
     */
    @PreAuthorize("@ss.hasPermi('repair:order:query')")
    @GetMapping(value = "/{orderId}")
    public AjaxResult getInfo(@PathVariable("orderId") Long orderId)
    {
        return success(repairOrderService.selectRepairOrderByOrderId(orderId));
    }

    /**
     * 查询工单的处理进度（时间线）
     */
    @PreAuthorize("@ss.hasPermi('repair:order:query')")
    @GetMapping(value = "/progress/{orderId}")
    public AjaxResult progress(@PathVariable("orderId") Long orderId)
    {
        RepairProgress query = new RepairProgress();
        query.setOrderId(orderId);
        return success(repairProgressService.selectRepairProgressList(query));
    }

    /**
     * 新增报修工单
     */
    @PreAuthorize("@ss.hasPermi('repair:order:add')")
    @Log(title = "报修工单", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody RepairOrder repairOrder)
    {
        return toAjax(repairOrderService.insertRepairOrder(repairOrder));
    }

    /**
     * 修改报修工单（仅待派单状态可改）
     */
    @PreAuthorize("@ss.hasPermi('repair:order:edit')")
    @Log(title = "报修工单", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody RepairOrder repairOrder)
    {
        RepairOrder old = repairOrderService.selectRepairOrderByOrderId(repairOrder.getOrderId());
        if (old == null)
        {
            return error("报修工单不存在");
        }
        if (!SecurityUtils.getUserId().equals(old.getResidentId()))
        {
            return error("只能修改本人提交的报修");
        }
        if (!"pending".equals(old.getStatus()))
        {
            return error("工单已进入派单流程，不能修改");
        }
        return toAjax(repairOrderService.updateRepairOrder(repairOrder));
    }

    /**
     * 取消报修
     */
    @PreAuthorize("@ss.hasPermi('repair:order:edit')")
    @Log(title = "报修工单", businessType = BusinessType.UPDATE)
    @PutMapping("/cancel/{orderId}")
    public AjaxResult cancel(@PathVariable("orderId") Long orderId, @RequestBody(required = false) RepairOrder body)
    {
        String reason = body == null ? "" : body.getCancelReason();
        return toAjax(repairOrderService.cancelOrder(orderId, reason, SecurityUtils.getUserId()));
    }

    /**
     * 删除报修工单
     */
    @PreAuthorize("@ss.hasPermi('repair:order:remove')")
    @Log(title = "报修工单", businessType = BusinessType.DELETE)
    @DeleteMapping("/{orderIds}")
    public AjaxResult remove(@PathVariable Long[] orderIds)
    {
        return toAjax(repairOrderService.deleteRepairOrderByOrderIds(orderIds));
    }
}
