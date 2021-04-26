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
import java.text.DateFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
/**
 * The class parse site http://www.sql.ru/forum/job and all next pages by current year. And collect Article
 * to collection if article contain string in (topicMatched array), and ignore topics that exist in
 * (topicsNotMatch array).
 * @author Roman Rusanov
 * @version 0.1
 * @since 27.03.2019
 */
@PersistJobDataAfterExecution
public class Parser implements Job {
    /**
     * The field contain all find and matched articles on current page.
     */
    private final List<Article> allMatchedArticle = new ArrayList<>();
    /**
     * The field contain array that be checked in each find topic. And if this sting present, when topic ignored.
     */
    private final String[] topicsNotMatch = {"javascript", "java script", "Java Script", "JavaScript"};
    /**
     * The field contain array that be checked in each find topic. And if this sting present,
     * when that article be added to List<Article> allMatchedArticle.
     */
    private final String[] topicsMatch = {"java", "Java", "JAVA"};
    /**
     * The field contain full date format. That is necessary to compare date.
     */
    private final SimpleDateFormat formatFull;
    /**
     * The field contain today date in string format.
     */
    private final String strToday;
    /**
     * The field contain yesterday date in string format.
     */
    private final String strYesterday;
    /**
     * The field contain last page number in site sql.ru.
     */
    private final Integer maxPageNumber;
    /**
     * The field contain state when no matched article on current page by date field.
     */
    private boolean noMoreMatchedArticle;
    /**
     * The field contain last article date that be processed by parser.
     */
    private String lastArticleDate = "01 янв 70, 00:00";
    /**
     * The field contain if parser instance run first run. That state passed from one instance to next by use JobDataMap
     * in parser scheduler.
     */
    private boolean firstStart = true;
    /**
     * The filed contain state when not need parse another pages(old date).
     */
    private boolean stopProcess = false;
    /**
     * The field contain string with config file passed from StartParser class.
     */
    private String configFile;
    /**
     * Logger.
     */
    private static final Logger LOG = LogManager.getLogger(Parser.class.getName());
    /**
     * Default constructor.
     */
    public Parser() {
        this.maxPageNumber = getMaxPageNumber("http://www.sql.ru/forum/job/");
        this.noMoreMatchedArticle = false;
        Date dToday = new Date();
        this.formatFull = this.ruLocale("dd MMM yy, HH:mm");
        SimpleDateFormat formatShort = this.ruLocale("dd MMM yy");
        this.strToday = formatShort.format(dToday);
        Date dYesterday = new Date(System.currentTimeMillis() - 86400000);
        this.strYesterday = formatShort.format(dYesterday);
    }
    /**
     * The method create new instance with ru locale SimpleDateFormat.
     * @param format format string
     * @return instance SimpleDateFormat
     */
    public SimpleDateFormat ruLocale(String format) {
        Locale locale = new Locale("ru");
        DateFormatSymbols dfs = DateFormatSymbols.getInstance(locale);
        String[] months = {
                "января", "февраля", "марта", "апреля", "мая", "июня",
                "июля", "августа", "сентября", "октября", "ноября", "декабря"};
        String[] shortMonths = {
                "янв", "фев", "мар", "апр", "май", "июн",
                "июл", "авг", "сен", "окт", "ноя", "дек"};
        dfs.setMonths(months);
        dfs.setShortMonths(shortMonths);
        String[] weekdays = {"", "Воскресенье", "Понедельник", "Вторник", "Среда", "Четверг", "Пятница", "Суббота"};
        String[] shortWeekdays = {"", "вс", "пн", "вт", "ср", "чт", "пт", "сб"};
        dfs.setWeekdays(weekdays);
        dfs.setShortWeekdays(shortWeekdays);
        SimpleDateFormat sdf = new SimpleDateFormat(format, locale);
        sdf.setDateFormatSymbols(dfs);
        return sdf;
    }
    /**
     * The getter for field firstStart.
     * (Getter is necessary when instance parser run scheduler then scheduler set value from JobDataMap.
     * Data passed between instances from one parser to next)
     * @return boolean.
     */
    public boolean getFirstStart() {
        return firstStart;
    }
    /**
     * The setter for field firstStart.
     * (Getter is necessary when instance parser run scheduler then scheduler set value from JobDataMap.
     * Data passed between instances from one parser to next)
     * @param firstStart boolean.
     */
    public void setFirstStart(boolean firstStart) {
        this.firstStart = firstStart;
    }
    /**
     * The getter for field getLastArticleDate.
     * (Getter is necessary when instance parser run scheduler then scheduler set value from JobDataMap.
     * Data passed between instances from one parser to next)
     * @return String.
     */
    public String getLastArticleDate() {
        return lastArticleDate;
    }
    /**
     * The setter for field lastArticleDate.
     * (Getter is necessary when instance parser run scheduler then scheduler set value from JobDataMap.
     * Data passed between instances from one parser to next)
     * @param lastArticleDate String.
     */
    public void setLastArticleDate(String lastArticleDate) {
        this.lastArticleDate = lastArticleDate;
    }
    /**
     * The getter for field getConfigFile.
     * (Getter is necessary when instance parser run scheduler then scheduler set value from JobDataMap.
     * Data passed between instances from one parser to next)
     * @return String.
     */
    public String getConfigFile() {
        return configFile;
    }
    /**
     * The setter for field configFile.
     * (Getter is necessary when instance parser run scheduler then scheduler set value from JobDataMap.
     * Data passed between instances from one parser to next)
     * @param configFile String.
     */
    public void setConfigFile(String configFile) {
        this.configFile = configFile;
    }
    /**
     * The method execute when instance scheduler run jod parser.
     * @param context Data with scheduler params.
     */
    @Override
    public void execute(JobExecutionContext context) {
        JobDataMap dataMap = context.getJobDetail().getJobDataMap();
        List<Article> listArticleOnCurrentPage;
        for (int i = 1; i <= this.maxPageNumber && !noMoreMatchedArticle; i++) { //iterate by pages
            Elements allArticleOnPage = this.getAllArticleOnPage("http://www.sql.ru/forum/job/" + i);
            // on second start if not fund new article.
            if (!this.firstStart && stopProcess) {
                break;
            }
            listArticleOnCurrentPage = this.parseCurrentPage(allArticleOnPage);
            this.allMatchedArticle.addAll(listArticleOnCurrentPage);
            LOG.info(String.format("Page http://www.sql.ru/forum/job/%d", i));
        }
        /**
         * The field contain instance that provide DB connection, and insert data to DB.
         */
        DBService dbService = new DBService(new Config(this.configFile));
        dbService.insertArticleListToDB(this.allMatchedArticle);
        this.noMoreMatchedArticle = false;
        dataMap.put("firstStart", false);
        dataMap.put("lastArticleDate", this.lastArticleDate);
        LOG.info(String.format("LastArticleDate after page parse: %s", this.lastArticleDate));
    }
    /**
     * The method get Jsoup Document from string url.
     * @param url string with site.
     * @return Document.
     */
    public Document getDocFromUrl(String url) {
        Document currentPage = null;
        try {
            currentPage = Jsoup.connect(url).get();
        } catch (IOException e) {
            LOG.error(String.format(
                    "Error connect to site(%s).%n Exception:%s", url, e.toString())
            );
        }
        return currentPage;
    }
    /**
     * The method get number the last page from http://www.sql.ru/forum/job.
     * @param site string http://www.sql.ru/forum/job
     * @return int value.
     */
    public int getMaxPageNumber(String site) {
        Document currentPage = this.getDocFromUrl(site);
        Elements sortOptions = currentPage.getElementsByAttributeValue("style", "text-align:left");
        return Integer.parseInt(sortOptions.first().child(10).text());
    }
    /**
     * The method check if string contain find sequence.
     * @param strProcess String to process check.
     * @param pattern array Strings that be find in String to process.
     * @return boolean. If String to process contain one or more String from pattern, when return true.
     * Otherwise return false.
     */
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
    /**
     * The method get all article from current page.
     * @param site current page site url.
     * @return Jsoup Elements instance.
     */
    public Elements getAllArticleOnPage(String site) {
        Document currentPage = this.getDocFromUrl(site);
        Element pageContainer = currentPage.getElementById("page-container");
        Element contentWrapperForum = pageContainer.getElementById("content-wrapper-forum");
        Elements table = contentWrapperForum.getElementsByTag("table");
        return table.next(".forumTable");
    }
    /**
     * The method get text from url article.
     * @param urlArticle string url article.
     * @return String with text.
     */
    public String getTextArticle(String urlArticle) {
        Document currentPage = this.getDocFromUrl(urlArticle);
        Elements msgBody = currentPage.getElementsByAttributeValue("class", "msgBody");
        return msgBody.first().nextElementSibling().text();
    }
    /**
     * The method get from Jsoup Elements instance, the instance Article List with matched topics Article.
     * @param forumTable Jsoup Elements instance
     * @return List Article.
     */
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
            if (this.findMatchCharSequence(topic, this.topicsNotMatch)
                    || !this.findMatchCharSequence(topic, this.topicsMatch)) {
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
    /**
     * The method check if string contain words(сегодня, вчера) then cut that words and insert date format.
     * @param date string with date.
     * @return converted string.
     */
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
    /**
     * Compare two strings.
     * @param date1 first string date.
     * @param date2 second string date.
     * @return If first greater then second return true. Otherwise false.
     */
    public boolean compareStringDate(String date1, String date2) {
        Date d1 = null;
        Date d2 = null;
        try {
            d1 = this.formatFull.parse(date1);
        } catch (ParseException e) {
            LOG.error(String.format(
                    "Error parse date(%s).%n Exception:%s", date1, e.toString())
            );
        }
        try {
            d2 = this.formatFull.parse(date2);
        } catch (ParseException e) {
            LOG.error(String.format(
                    "Error parse date(%s).%n Exception:%s", date2, e.toString())
            );
        }
        return d1.compareTo(d2) > 0;
    }
}
