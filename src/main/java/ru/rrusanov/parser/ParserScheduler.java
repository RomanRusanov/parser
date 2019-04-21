package ru.rrusanov.parser;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.JobBuilder;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.CronScheduleBuilder;
/**
 * The class explain behavior for scheduler which the start method execute from Parser class.
 * The class need pass string for cron timer instance, and configFile that be stored in JobDataMap and after used
 * by Parser class to instance DBService.
 * @author Roman Rusanov
 * @version 0.1
 * @since 18.04.2019
 */
public class ParserScheduler {
    /**
     * The field contain string cronTimer.
     */
    private final String cronTime;
    /**
     * The field contain string configFile passed from StartParser class.
     */
    private final String configFile;
    /**
     * Constructor.
     * @param cronTime string for cron timer instance.
     * @param configFile string stored in JobDataMap.
     */
    public ParserScheduler(String cronTime, String configFile) {
        this.cronTime = cronTime;
        this.configFile = configFile;
    }
    /**
     * The method start scheduler.
     */
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
