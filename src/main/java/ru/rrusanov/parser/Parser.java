package ru.rrusanov.parser;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.PersistJobDataAfterExecution;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
/**
 * @author Roman Rusanov
 * @version 0.1
 * @since 27.03.2019
 */
@PersistJobDataAfterExecution
public class Parser implements Job {

    private final List<Article> allMatchedArticle = new ArrayList<>();
    private final String[] topicsNotMatch = {"javascript", "java script", "Java Script", "JavaScript"};
    private final String[] topicsMatch = {"java", "Java", "JAVA"};
    private final Date dToday;
    private final Date dYesterday;
    private final SimpleDateFormat formatShort = new SimpleDateFormat("dd MMM yy");
    private final SimpleDateFormat formatFull = new SimpleDateFormat("dd MMM yy, HH:mm");
    private final String strToday;
    private final String strYesterday;
    private final Integer maxPageNumber;
    private boolean noMoreMatchedArticle;
    private String lastArticleDate = "01 янв 70, 00:00";
    private boolean firstStart = true;
    private boolean stopProcess = false;
    private String configFile;
    private DBService dbService;
    /**
     * Logger.
     */
    private static final Logger LOG = LogManager.getLogger(Parser.class.getName());
    /**
     * Version for Logger.
     */
    private int version = 1;

    /**
     * Default constructor.
     */
    public Parser() {
        this.maxPageNumber = getMaxPageNumber("http://www.sql.ru/forum/job/");
        this.noMoreMatchedArticle = false;
        this.dToday = new Date();
        this.strToday = this.formatShort.format(dToday);
        this.dYesterday = new Date(System.currentTimeMillis() - 86400000);
        this.strYesterday = this.formatShort.format(dYesterday);
    }

    public List<Article> getAllMatchedArticle() {
        return allMatchedArticle;
    }

    public boolean getFirstStart() {
        return firstStart;
    }

    public void setFirstStart(boolean firstStart) {
        this.firstStart = firstStart;
    }

    public String getLastArticleDate() {
        return lastArticleDate;
    }

    public void setLastArticleDate(String lastArticleDate) {
        this.lastArticleDate = lastArticleDate;
    }

    public String getConfigFile() {
        return configFile;
    }

    public void setConfigFile(String configFile) {
        this.configFile = configFile;
    }

    @Override
    public void execute(JobExecutionContext context) {
        JobDataMap dataMap = context.getJobDetail().getJobDataMap();
        List<Article> listArticleOnCurrentPage;
        for (int i = 1; i <= this.maxPageNumber && !noMoreMatchedArticle; i++) { //iterate by pages
            Elements allArticleOnPage = this.getAllArticleOnPage("http://www.sql.ru/forum/job/" + i);
            // on second start not fund new article.
            if (!this.firstStart && stopProcess) {
                break;
            }
            listArticleOnCurrentPage = this.parseCurrentPage(allArticleOnPage);
            this.allMatchedArticle.addAll(listArticleOnCurrentPage);
            LOG.info(String.format("Page http://www.sql.ru/forum/job/%d", i));
        }
        this.dbService = new DBService(new Config(this.configFile));
        this.dbService.insertArticleListToDB(this.allMatchedArticle);
        this.noMoreMatchedArticle = false;
        dataMap.put("firstStart", false);
        dataMap.put("lastArticleDate", this.lastArticleDate);
        LOG.info(String.format("LastArticleDate after page parse: %s", this.lastArticleDate));
    }

    public Document getDocFromUrl(String url) {
        Document currentPage = null;
        try {
            currentPage = Jsoup.connect(url).get();
        } catch (IOException e) {
            LOG.error(String.format(
                    "Error connect to site(%s). Version:%d%n Exception:%s", url, version, e.toString())
            );
        }
        return currentPage;
    }

    public int getMaxPageNumber(String site) {
        Document currentPage = this.getDocFromUrl(site);
        Elements sortOptions = currentPage.getElementsByAttributeValue("style", "text-align:left");
        return Integer.parseInt(sortOptions.first().child(10).text());
    }


    public boolean findMatchCharSequence(String strProcess, String[] pattern) {
        // check length strProcess and pattern String length. The pattern string must be shorter.
        for (String str : pattern) {
            if (str.length() > strProcess.length()) {
                return false;
            }
        }
        // find match char sequence
        boolean result = false;
        boolean flag; //by default chars not equals.
        for (String str : pattern) { // array pattern
            int counter = 0; // how many fined equals char
            int strCursor = 0;
            for (int j = 0; j < strProcess.length(); j++) { // array strProcess
                flag = false;
                // find equal char in strProcess and pattern.
                if (str.charAt(strCursor) == strProcess.charAt(j)) {
                    flag = true;
                    counter++;
                    strCursor++;
                    // check if pattern length equals funded chars when matched funded
                    if (counter == str.length()) {
                        result = true;
                        break;
                    }
                }
                if (!flag) { // if sequence not math, reset counter and cursor at start position pattern word.
                    strCursor = 0;
                    counter = 0;
                }
            }
            if (result) { // if find when break and return true.
                break;
            }
        }
        return result;
    }

    public Elements getAllArticleOnPage(String site) {
        Document currentPage = this.getDocFromUrl(site);
        Element pageContainer = currentPage.getElementById("page-container");
        Element contentWrapperForum = pageContainer.getElementById("content-wrapper-forum");
        Elements table = contentWrapperForum.getElementsByTag("table");
        return table.next(".forumTable");
    }

    public String getTextArticle(String urlArticle) {
        Document currentPage = this.getDocFromUrl(urlArticle);
        Elements msgBody = currentPage.getElementsByAttributeValue("class", "msgBody");
        return msgBody.first().nextElementSibling().text();
    }

    public List<Article> parseCurrentPage(Elements forumTable) {
        List<Article> result = new ArrayList<>();
        int counter = 0; // how many article sequent not matched by date.
        int counterOldDate = 0;
        String currYear = this.strToday.substring(this.strToday.length() - 2); // Current year
        String stringToCompare = String.format(" %s, ", currYear);
        Elements curr = forumTable.first().child(0).children();
        Iterator<Element> iterator = curr.iterator();
        iterator.next();
        while (iterator.hasNext()) {
            Elements c = iterator.next().children();
            String date = this.convertDate(c.last().text());
            //compare
            if (!this.firstStart && counterOldDate > 10) {
                this.stopProcess = true;
                break;
            }
            // check the field lastArticleDate must store the date of the most recent article.
            if (!this.compareStringDate(this.lastArticleDate, date)) {
                this.lastArticleDate = date;
                counterOldDate = 0;
            } else {
                counterOldDate++;
            }
            if (!date.contains(stringToCompare) && counter <= 4) {
                counter++;
                continue;
            }
            if (counter > 4) {
                this.noMoreMatchedArticle = true;
                break;
            }
            counter = 0;
            String topic = c.first().nextElementSibling().child(0).text();
            if (this.findMatchCharSequence(topic, this.topicsNotMatch) ||
                !this.findMatchCharSequence(topic, this.topicsMatch)) {
                continue;
            }
            String url = c.first().nextElementSibling().child(0).attributes().get("href");
            String text = this.getTextArticle(url);
            Article currentArticle = new Article(url, topic, text, date);
            if (!this.allMatchedArticle.contains(currentArticle)) {
                result.add(currentArticle);
            }
        }
        return result;
    }

    public String convertDate(String date) {
        String result = date;
        if (result.contains("сегодня")) {
            result = date.replace("сегодня", this.strToday);
        }
        if (result.contains("вчера")) {
            result = date.replace("вчера", this.strYesterday);
        }
        return result;
    }

    public boolean compareStringDate(String date1, String date2) {
        Date d1 = null;
        Date d2 = null;
        try {
            d1 = this.formatFull.parse(date1);
        } catch (ParseException e) {
            LOG.error(String.format(
                    "Error parse date(%s). Version:%d%n Exception:%s", date1, version, e.toString())
            );
        }
        try {
            d2 = this.formatFull.parse(date2);
        } catch (ParseException e) {
            LOG.error(String.format(
                    "Error parse date(%s). Version:%d%n Exception:%s", date2, version, e.toString())
            );
        }
        return d1.compareTo(d2) > 0;
    }
}
