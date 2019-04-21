package ru.rrusanov.parser;
/**
 * The main point enter. Start the program. May take config from console or use default from app.properties.
 * @author Roman Rusanov
 * @version 0.1
 * @since 19.04.2019
 */
public class StartParser {
    /**
     * Main method.
     * @param args params passed from console.
     */
    public static void main(String[] args) {
        String properties = args.length == 0 ? "app.properties" : args[0];
        Config config = new Config(properties);
        String cronTime = config.getConfig().getProperty("cron.time");
        ParserScheduler scheduler = new ParserScheduler(cronTime, properties);
        scheduler.initScheduler();
    }
}
