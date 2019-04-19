package ru.rrusanov.parser;

import org.quartz.*;

public class ParserScheduler {

    private final String cronTime;
    private final String configFile;

    public ParserScheduler(String cronTime, String configFile) {
        this.cronTime = cronTime;
        this.configFile = configFile;
    }

    public void initScheduler() {
        SchedulerFactory schedulerFactory = new org.quartz.impl.StdSchedulerFactory();
        Scheduler scheduler = null;
        try {
            scheduler = schedulerFactory.getScheduler();
        } catch (SchedulerException e) {
            e.printStackTrace();
        }
        // define the job and tie it to our ru.rrusanov.parser.Parser class
        JobDetail job = JobBuilder.newJob(Parser.class)
                .usingJobData("configFile", this.configFile)
                .withIdentity("parser", "group1")
                .build();
        // Trigger
        Trigger trigger = TriggerBuilder.newTrigger()
                .withIdentity("ParserTriggerEveryDay", "group1")
                .startNow()
                .withSchedule(CronScheduleBuilder.cronSchedule(this.cronTime))
                .build();
        // Tell quartz to schedule the job using our trigger
        try {
            scheduler.scheduleJob(job, trigger);
            scheduler.start();
        } catch (SchedulerException e) {
            e.printStackTrace();
        }
    }
}
