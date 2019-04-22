/**
 * @author Roman Rusanov
 * @version 0.1
 * @since 27.03.2019
 *
 * Article.java The class describe Article instance.
 *  public String getUrl() The getter.
 *  public String getTopic() The getter.
 *  public String getText() The getter.
 *  public String getDate() The getter.
 *  public void setUrl(String url) The setter.
 *  public void setTopic(String topic) The setter.
 *  public void setText(String text) The setter.
 *  public void setDate(String date) The setter.
 *  public boolean equals(Object o) The method check if two instance of article equals.
 *  public int hashCode() The method generate hashCode for instance.
 *  public String toString() Method describe correct view instance.
 *
 * Config.java Class contain configuration connection to SQLite db, cron scheduler parameters.
 *  public Properties getConfig() Getter for properties.
 *  public void init(String configFile) The method load properties from file example (app.properties).
 *
 * ConnectionRollback.java Class create connection with rollback all commits.
 *  public static Connection create(Connection connection)
 *   Create connection with autocommit=false mode and rollback call, when connection is closed.
 *
 * DBService.java The field contain instance connection to database.
 *  public boolean initConnectionToSQLiteDB(Properties values) Method create instance of
 *   connection of db if it not exist yet.
 *  public void close() Closes this resource, relinquishing any underlying resources.
 *  public void createTable() The method create table vacancy in DB.
 *  public void insertArticleListToDB(List<Article> articleList) The method insert all article in
 *   passed collection in DB.
 *  public boolean isArticleExistInBD(String topic) The method check exist this article in DB.
 *
 * Parser.java The class parse site http://www.sql.ru/forum/job and all next pages by current year.
 *  public boolean getFirstStart() The getter for field firstStart.
 *  public void setFirstStart(boolean firstStart) The setter for field firstStart.
 *  public String getLastArticleDate() The getter for field getLastArticleDate.
 *  public void setLastArticleDate(String lastArticleDate) The setter for field lastArticleDate.
 *  public String getConfigFile() The getter for field getConfigFile.
 *  public void setConfigFile(String configFile) The setter for field configFile.
 *  public void execute(JobExecutionContext context) The method execute when instance scheduler run jod parser.
 *  public Document getDocFromUrl(String url) The method get Jsoup Document from string url.
 *  public int getMaxPageNumber(String site) The method get number the last page from http://www.sql.ru/forum/job.
 *  public boolean findMatchCharSequence(String strProcess, String[] pattern) The method check if
 *   string contain find sequence.
 *  public Elements getAllArticleOnPage(String site) The method get all article from current page.
 *  public String getTextArticle(String urlArticle) The method get text from url article.
 *  public List<Article> parseCurrentPage(Elements forumTable) The method get from Jsoup Elements instance,
 *   the instance Article List with matched topics Article.
 *  public String convertDate(String date) The method check if string contain words(сегодня, вчера)
 *   then cut that words and insert date format.
 *  public boolean compareStringDate(String date1, String date2) Compare two strings.
 *
 *  ParserScheduler.java The class explain behavior for scheduler which the start method execute from Parser class.
 *   public void initScheduler() The method start scheduler.
 *
 *  StartParser.java The main point enter.
 *   public static void main(String[] args) Main method.
 */
package ru.rrusanov.parser;