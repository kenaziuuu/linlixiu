package com.linlixiu.web.controller.repair;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.linlixiu.system.domain.RepairMessage;
import com.linlixiu.system.domain.RepairOrder;
import com.linlixiu.system.mapper.RepairMessageMapper;
import com.linlixiu.system.service.IRepairOrderService;

/**
 * 报修工单定时任务
 *
 * 由 Quartz 调度（在“系统监控—定时任务”中配置调用目标 repairTask.scanOvertime），
 * 每日扫描三类超时工单并打上超时标记，同时对仍未派单的工单提醒调度员。
 *
 * @author linlixiu
 * @date 2026-11-06
 */
@Component("repairTask")
public class RepairTaskJob
{
    private static final Logger log = LoggerFactory.getLogger(RepairTaskJob.class);

    @Autowired
    private IRepairOrderService repairOrderService;

    @Autowired
    private RepairMessageMapper repairMessageMapper;

    /**
     * 扫描超时工单：
     * 1）待派单超过 2 小时；
     * 2）待接单超过 2 小时；
     * 3）维修中超过 24 小时。
     * 命中的工单置 is_overtime = 1，供列表标红与统计口径使用。
     */
    public void scanOvertime()
    {
        int rows = repairOrderService.scanOvertimeOrders();
        log.info("报修工单超时扫描完成，本次标记 {} 条工单", rows);
    }

    /**
     * 待派单超时提醒：给仍未派单的超时工单补充一条系统消息记录，
     * 便于管理员在消息中心集中查看。
     */
    public void remindPending()
    {
        List<RepairOrder> list = repairOrderService.selectPendingOvertimeOrders();
        int count = 0;
        for (RepairOrder order : list)
        {
            RepairMessage message = new RepairMessage();
            message.setReceiverId(order.getResidentId());
            message.setTitle("报修工单等待派单时间较长");
            message.setContent("您的报修 " + order.getOrderNo() + " 尚未派单，社区已收到超时提醒，请耐心等待。");
            message.setMsgType("system");
            message.setOrderId(order.getOrderId());
            message.setIsRead(0);
            repairMessageMapper.insertRepairMessage(message);
            count++;
        }
        log.info("待派单超时提醒完成，共发送 {} 条消息", count);
    }
}
