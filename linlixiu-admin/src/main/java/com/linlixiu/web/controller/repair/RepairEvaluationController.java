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
import com.linlixiu.common.core.domain.model.LoginUser;
import com.linlixiu.common.utils.SecurityUtils;
import com.linlixiu.common.utils.poi.ExcelUtil;
import com.linlixiu.system.domain.RepairEvaluation;
import com.linlixiu.system.service.IRepairEvaluationService;

/**
 * 评价反馈Controller
 *
 * 面向居民与管理员：居民提交评价、查看自己的评价；管理员回复评价、查看评价汇总。
 *
 * @author linlixiu
 * @date 2026-11-06
 */
@RestController
@RequestMapping("/repair/evaluation")
public class RepairEvaluationController extends BaseController
{
    @Autowired
    private IRepairEvaluationService repairEvaluationService;

    /**
     * 查询评价列表
     */
    @PreAuthorize("@ss.hasPermi('repair:evaluation:list')")
    @GetMapping("/list")
    public TableDataInfo list(RepairEvaluation repairEvaluation)
    {
        // 非管理员、非调度员只能查看本人提交的评价
        LoginUser loginUser = SecurityUtils.getLoginUser();
        if (!loginUser.getPermissions().contains("*:*:*")
                && !loginUser.getPermissions().contains("repair:evaluation:reply"))
        {
            repairEvaluation.setResidentId(loginUser.getUserId());
            // 居民视角下把「已完工但还没评价」的工单也列出来，方便直接去评价
            repairEvaluation.getParams().put("includePending", "1");
        }
        startPage();
        List<RepairEvaluation> list = repairEvaluationService.selectRepairEvaluationList(repairEvaluation);
        return getDataTable(list);
    }

    /**
     * 导出评价列表
     */
    @PreAuthorize("@ss.hasPermi('repair:evaluation:list')")
    @Log(title = "评价反馈", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, RepairEvaluation repairEvaluation)
    {
        List<RepairEvaluation> list = repairEvaluationService.selectRepairEvaluationList(repairEvaluation);
        ExcelUtil<RepairEvaluation> util = new ExcelUtil<RepairEvaluation>(RepairEvaluation.class);
        util.exportExcel(response, list, "评价反馈数据");
    }

    /**
     * 获取评价详情
     */
    @PreAuthorize("@ss.hasPermi('repair:evaluation:query')")
    @GetMapping(value = "/{evaluationId}")
    public AjaxResult getInfo(@PathVariable("evaluationId") Long evaluationId)
    {
        return success(repairEvaluationService.selectRepairEvaluationById(evaluationId));
    }

    /**
     * 按工单查询评价
     */
    @PreAuthorize("@ss.hasPermi('repair:evaluation:query')")
    @GetMapping(value = "/order/{orderId}")
    public AjaxResult getByOrder(@PathVariable("orderId") Long orderId)
    {
        return success(repairEvaluationService.selectEvaluationByOrderId(orderId));
    }

    /**
     * 评价汇总（平均分、好评率等）
     */
    @PreAuthorize("@ss.hasPermi('repair:evaluation:list')")
    @GetMapping("/summary")
    public AjaxResult summary()
    {
        // 居民只看自己的评价统计，管理员与调度员看全社区
        LoginUser loginUser = SecurityUtils.getLoginUser();
        Long residentId = null;
        if (!loginUser.getPermissions().contains("*:*:*")
                && !loginUser.getPermissions().contains("repair:evaluation:reply"))
        {
            residentId = loginUser.getUserId();
        }
        return success(repairEvaluationService.selectScoreSummary(residentId));
    }

    /**
     * 提交评价
     */
    @PreAuthorize("@ss.hasPermi('repair:evaluation:add')")
    @Log(title = "评价反馈", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody RepairEvaluation repairEvaluation)
    {
        return toAjax(repairEvaluationService.insertRepairEvaluation(repairEvaluation));
    }

    /**
     * 回复评价
     */
    @PreAuthorize("@ss.hasPermi('repair:evaluation:reply')")
    @Log(title = "评价反馈", businessType = BusinessType.UPDATE)
    @PutMapping("/reply")
    public AjaxResult reply(@RequestBody RepairEvaluation body)
    {
        if (body.getEvaluationId() == null)
        {
            return error("缺少评价编号");
        }
        return toAjax(repairEvaluationService.replyEvaluation(body.getEvaluationId(), body.getReplyContent()));
    }

    /**
     * 删除评价
     */
    @PreAuthorize("@ss.hasPermi('repair:evaluation:remove')")
    @Log(title = "评价反馈", businessType = BusinessType.DELETE)
    @DeleteMapping("/{evaluationIds}")
    public AjaxResult remove(@PathVariable Long[] evaluationIds)
    {
        return toAjax(repairEvaluationService.deleteRepairEvaluationByIds(evaluationIds));
    }
}
