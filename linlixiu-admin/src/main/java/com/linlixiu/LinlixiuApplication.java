package com.linlixiu;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

/**
 * 启动程序
 * 
 * @author linlixiu
 */
@SpringBootApplication(exclude = { DataSourceAutoConfiguration.class },scanBasePackages = {"org.jeecg.modules.jmreport","com.linlixiu"})
public class LinlixiuApplication
{
    public static void main(String[] args)
    {
        // System.setProperty("spring.devtools.restart.enabled", "false");
        SpringApplication.run(LinlixiuApplication.class, args);
        System.out.println("\n" +
                "★ 邻里修 · 社区报修管理系统  启动成功 ★\n" +
                "\n" +
                "        /\\               邻里修 · Linlixiu\n" +
                "       /  \\              ==============================\n" +
                "      /    \\             社区报修管理系统\n" +
                "     /      \\\n" +
                "    /________\\           报修 -> 派单 -> 维修 -> 评价\n" +
                "    |        |\n" +
                "    |  ____  |           报修不跑腿 · 派单有响应\n" +
                "    | |    | |           进度看得见 · 服务有评价\n" +
                "    |_|____|_|\n");
    }
}
