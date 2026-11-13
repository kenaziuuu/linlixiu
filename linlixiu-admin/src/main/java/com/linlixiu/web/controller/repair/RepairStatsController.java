package com.linlixiu.web.controller.repair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.linlixiu.common.core.controller.BaseController;
import com.linlixiu.common.core.domain.AjaxResult;
import com.linlixiu.common.utils.SecurityUtils;
import com.linlixiu.system.domain.SysNotice;
import com.linlixiu.system.service.IRepairEvaluationService;
import com.linlixiu.system.service.IRepairMessageService;
import com.linlixiu.system.service.IRepairOrderService;
import com.linlixiu.system.service.ISysNoticeService;

/**
 * 报修统计Controller
 *
 * 面向社区管理员：报修量、完成率、类型分布、趋势与维修工绩效。
 *
 * @author linlixiu
 * @date 2026-11-06
 */
@RestController
@RequestMapping("/repair/stats")
public class RepairStatsController extends BaseController
{
    @Autowired
    private IRepairOrderService repairOrderService;

    @Autowired
    private IRepairEvaluationService repairEvaluationService;

    @Autowired
    private IRepairMessageService repairMessageService;

    @Autowired
    private ISysNoticeService noticeService;

    /**
     * 首页公告栏：最新 5 条已发布公告
     * 
     * 所有登录用户都可访问，避免普通角色因缺少 system:notice:list 权限而看不到公告。
     */
    @GetMapping("/notices")
    public AjaxResult notices()
    {
        SysNotice query = new SysNotice();
        List<SysNotice> list = noticeService.selectNoticeList(query);
        List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
        if (list != null)
        {
            // 只取已发布（status = 0）的，按发布时间倒序，最多 5 条
            List<SysNotice> valid = new ArrayList<SysNotice>();
            for (SysNotice n : list)
            {
                if ("0".equals(n.getStatus()))
                {
                    valid.add(n);
                }
            }
            valid.sort((a, b) -> {
                if (a.getCreateTime() == null && b.getCreateTime() == null)
                {
                    return 0;
                }
                if (a.getCreateTime() == null)
                {
                    return 1;
                }
                if (b.getCreateTime() == null)
                {
                    return -1;
                }
                return b.getCreateTime().compareTo(a.getCreateTime());
            });
            for (int i = 0; i < valid.size() && i < 5; i++)
            {
                SysNotice n = valid.get(i);
                Map<String, Object> item = new HashMap<String, Object>();
                item.put("noticeId", n.getNoticeId());
                item.put("noticeTitle", n.getNoticeTitle());
                item.put("noticeType", n.getNoticeType());
                item.put("noticeContent", n.getNoticeContent());
                item.put("createTime", n.getCreateTime());
                result.add(item);
            }
        }
        return success(result);
    }

    /**
     * 首页快捷入口红点数据
     * 
     * 所有登录用户都可访问，按当前账号返回各自的待办数量。
     */
    @GetMapping("/badges")
    public AjaxResult badges()
    {
        Long userId = SecurityUtils.getUserId();
        Map<String, Object> data = new HashMap<String, Object>();
        data.put("unread", repairMessageService.selectUnreadCount(userId));
        data.put("pendingTask", repairOrderService.countWorkerPending(userId));
        data.put("pendingEvaluation", repairOrderService.countResidentPendingEvaluation(userId));
        return success(data);
    }

    /**
     * 统计概览
     */
    @PreAuthorize("@ss.hasPermi('repair:stats:view')")
    @GetMapping("/overview")
    public AjaxResult overview()
    {
        return success(repairOrderService.selectOverview());
    }

    /**
     * 按报修类型统计
     */
    @PreAuthorize("@ss.hasPermi('repair:stats:view')")
    @GetMapping("/byType")
    public AjaxResult byType()
    {
        return success(repairOrderService.selectCountByType());
    }

    /**
     * 按工单状态统计
     */
    @PreAuthorize("@ss.hasPermi('repair:stats:view')")
    @GetMapping("/byStatus")
    public AjaxResult byStatus()
    {
        return success(repairOrderService.selectCountByStatus());
    }

    /**
     * 近 N 天报修趋势
     */
    @PreAuthorize("@ss.hasPermi('repair:stats:view')")
    @GetMapping("/trend")
    public AjaxResult trend(@RequestParam(value = "days", defaultValue = "7") Integer days)
    {
        return success(repairOrderService.selectTrend(days));
    }

    /**
     * 维修工绩效排行
     */
    @PreAuthorize("@ss.hasPermi('repair:stats:view')")
    @GetMapping("/workerRank")
    public AjaxResult workerRank(@RequestParam(value = "limit", defaultValue = "5") Integer limit)
    {
        return success(repairOrderService.selectWorkerRank(limit));
    }

    /**
     * 评价汇总
     */
    @PreAuthorize("@ss.hasPermi('repair:stats:view')")
    @GetMapping("/scoreSummary")
    public AjaxResult scoreSummary()
    {
        // 该接口有 repair:stats:view 权限，属于管理端统计，固定统计全社区
        return success(repairEvaluationService.selectScoreSummary(null));
    }
}
