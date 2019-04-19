package ru.rrusanov.parser;

public class StartParser {

    public static void main(String[] args) {
        String properties = args.length == 0 ? "app.properties" : args[0];
        Config config = new Config(properties);
        String cronTime = config.getConfig().getProperty("cron.time");
        ParserScheduler scheduler = new ParserScheduler(cronTime, properties);
        scheduler.initScheduler();
    }
}
