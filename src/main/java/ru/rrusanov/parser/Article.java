package ru.rrusanov.parser;

import java.util.Objects;
/**
 * @author Roman Rusanov
 * @version 0.1
 * @since 30.03.2019
 *
 * The class describe
 */
public class Article {

    private String url;

    private String topic;

    private String text;

    private String date;

    public Article(String url, String topic, String text, String date) {
        this.url = url;
        this.topic = topic;
        this.text = text;
        this.date = date;
    }

    public String getUrl() {
        return url;
    }

    public String getTopic() {
        return topic;
    }

    public String getText() {
        return text;
    }

    public String getDate() {
        return date;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setDate(String date) {
        this.date = date;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Article article = (Article) o;
        return url.equals(article.url) &&
                topic.equals(article.topic) &&
                text.equals(article.text) &&
                date.equals(article.date);
    }

    @Override
    public int hashCode() {
        return Objects.hash(url, topic, text, date);
    }

    @Override
    public String toString() {
        return "ru.rrusanov.parser.Article{" +
                "url='" + url + '\'' +
                ", topic='" + topic + '\'' +
                ", text='" + text + '\'' +
                ", date='" + date + '\'' +
                '}';
    }
}
