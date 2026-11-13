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
import com.linlixiu.common.utils.poi.ExcelUtil;
import com.linlixiu.system.domain.RepairWorker;
import com.linlixiu.system.service.IRepairWorkerService;

/**
 * 维修工管理Controller
 *
 * 面向社区管理员：维护维修工档案（工号、技能、在岗状态），并查看其接单与评分统计。
 *
 * @author linlixiu
 * @date 2026-11-06
 */
@RestController
@RequestMapping("/repair/worker")
public class RepairWorkerController extends BaseController
{
    @Autowired
    private IRepairWorkerService repairWorkerService;

    /**
     * 查询维修工列表
     */
    @PreAuthorize("@ss.hasPermi('repair:worker:list')")
    @GetMapping("/list")
    public TableDataInfo list(RepairWorker repairWorker)
    {
        startPage();
        List<RepairWorker> list = repairWorkerService.selectRepairWorkerList(repairWorker);
        return getDataTable(list);
    }

    /**
     * 导出维修工列表
     */
    @PreAuthorize("@ss.hasPermi('repair:worker:list')")
    @Log(title = "维修工管理", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, RepairWorker repairWorker)
    {
        List<RepairWorker> list = repairWorkerService.selectRepairWorkerList(repairWorker);
        ExcelUtil<RepairWorker> util = new ExcelUtil<RepairWorker>(RepairWorker.class);
        util.exportExcel(response, list, "维修工数据");
    }

    /**
     * 获取维修工详细信息
     */
    @PreAuthorize("@ss.hasPermi('repair:worker:query')")
    @GetMapping(value = "/{workerId}")
    public AjaxResult getInfo(@PathVariable("workerId") Long workerId)
    {
        return success(repairWorkerService.selectRepairWorkerById(workerId));
    }

    /**
     * 查询可派单的维修工
     */
    @PreAuthorize("@ss.hasPermi('repair:worker:list')")
    @GetMapping("/available")
    public AjaxResult available(String skill)
    {
        return success(repairWorkerService.selectAvailableWorkers(skill));
    }

    /**
     * 新增维修工
     */
    @PreAuthorize("@ss.hasPermi('repair:worker:add')")
    @Log(title = "维修工管理", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody RepairWorker repairWorker)
    {
        return toAjax(repairWorkerService.insertRepairWorker(repairWorker));
    }

    /**
     * 修改维修工
     */
    @PreAuthorize("@ss.hasPermi('repair:worker:edit')")
    @Log(title = "维修工管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody RepairWorker repairWorker)
    {
        return toAjax(repairWorkerService.updateRepairWorker(repairWorker));
    }

    /**
     * 删除维修工
     */
    @PreAuthorize("@ss.hasPermi('repair:worker:remove')")
    @Log(title = "维修工管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{workerIds}")
    public AjaxResult remove(@PathVariable Long[] workerIds)
    {
        return toAjax(repairWorkerService.deleteRepairWorkerByIds(workerIds));
    }
}
