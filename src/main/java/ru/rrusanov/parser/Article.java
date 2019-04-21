package ru.rrusanov.parser;
import java.util.Objects;
/**
 * @author Roman Rusanov
 * @version 0.1
 * @since 30.03.2019
 *
 * The class describe Article instance.
 */
public class Article {
    /**
     * The filed contain url this article.
     */
    private String url;
    /**
     * The field contain topic this article.
     */
    private String topic;
    /**
     * The filed contain text this article.
     */
    private String text;
    /**
     * The field contain date this article.
     */
    private String date;
    /**
     * The constructor.
     * @param url url this article.
     * @param topic topic this article.
     * @param text text this article.
     * @param date date this article.
     */
    public Article(String url, String topic, String text, String date) {
        this.url = url;
        this.topic = topic;
        this.text = text;
        this.date = date;
    }
    /**
     * The getter.
     * @return String
     */
    public String getUrl() {
        return url;
    }
    /**
     * The getter.
     * @return String
     */
    public String getTopic() {
        return topic;
    }
    /**
     * The getter.
     * @return String
     */
    public String getText() {
        return text;
    }
    /**
     * The getter.
     * @return String
     */
    public String getDate() {
        return date;
    }
    /**
     * The setter.
     * @param url string.
     */
    public void setUrl(String url) {
        this.url = url;
    }
    /**
     * The setter.
     * @param topic string.
     */
    public void setTopic(String topic) {
        this.topic = topic;
    }
    /**
     * The setter.
     * @param text string.
     */
    public void setText(String text) {
        this.text = text;
    }
    /**
     * The setter.
     * @param date string.
     */
    public void setDate(String date) {
        this.date = date;
    }
    /**
     * The method check if two instance of article equals.
     * @param o another instance.
     * @return if equals return true, otherwise false.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Article article = (Article) o;
        return url.equals(article.url)
                && topic.equals(article.topic)
                && text.equals(article.text)
                && date.equals(article.date);
    }
    /**
     * The method generate hashCode for instance.
     * @return
     */
    @Override
    public int hashCode() {
        return Objects.hash(url, topic, text, date);
    }
    /**
     * Method describe correct view instance.
     * @return String with all fields article.
     */
    @Override
    public String toString() {
        return "ru.rrusanov.parser.Article{"
                + "url='" + url + '\''
                + ", topic='" + topic + '\''
                + ", text='" + text + '\''
                + ", date='" + date + '\''
                + '}';
    }
}
