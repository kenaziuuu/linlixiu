package com.linlixiu.quartz.util;

import org.quartz.JobExecutionContext;
import com.linlixiu.quartz.domain.SysJob;

/**
 * 定时任务处理（允许并发执行）
 * 
 * @author linlixiu
 *
 */
public class QuartzJobExecution extends AbstractQuartzJob
{
    @Override
    protected void doExecute(JobExecutionContext context, SysJob sysJob) throws Exception
    {
        JobInvokeUtil.invokeMethod(sysJob);
    }
}
