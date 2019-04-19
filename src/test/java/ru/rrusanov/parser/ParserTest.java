package ru.rrusanov.parser;

import org.hamcrest.CoreMatchers;
import org.hamcrest.Matchers;
import org.hamcrest.core.Is;
import org.jsoup.select.Elements;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import java.util.List;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.Matchers.lessThan;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.Matchers.greaterThan;

public class ParserTest {

    private Parser parser;

    @Before
    public void setUp() {
        this.parser = new Parser();
    }

    @Test
    public void getAllArticleOnPage() {

    }

    @Test
    public void getTextArticle() {
        this.parser = new Parser();
        String expect = "Здесь будут писаться все описания действий модераторов по исправлению и удалению топиков, "
                + "а так же замечания участникам форума, согласно правилам форума \"Работа\".";
        String result = parser.getTextArticle("https://www.sql.ru/forum/269098/"
                + "soobshheniya-ot-moderatorov-zdes-vy-mozhete-uznat-prichiny-udaleniya-topikov");
        Assert.assertThat(result, Is.is(expect));
    }

    @Test
    public void parseCurrentPage() {
        Parser parser = new Parser();
        Elements allArticle = parser.getAllArticleOnPage("https://www.sql.ru/forum/job/2");
        List<Article> listArticle = parser.parseCurrentPage(allArticle);
        System.out.println(listArticle);
    }

    @Test
    public void convertDate() {

    }

    @Test
    public void getDocFromUrl() {
    }

    @Test()
    public void findMatchCharSequence() {
        String[] topicsNotMatch = {"java script", "javascript"};
        Assert.assertThat(this.parser.findMatchCharSequence("The javascript lang", topicsNotMatch),
                Is.is(true));
        Assert.assertThat(this.parser.findMatchCharSequence("The java script lang", topicsNotMatch),
                Is.is(true));
        Assert.assertThat(this.parser.findMatchCharSequence("The java lang", topicsNotMatch),
                Is.is(false));
        Assert.assertThat(this.parser.findMatchCharSequence("shortStr", topicsNotMatch), Is.is(false));
    }

    @Test
    public void getMaxPageNumber() {
        int actual = this.parser.getMaxPageNumber("https://www.sql.ru/forum/job/");
        Assert.assertThat(actual, CoreMatchers.allOf(Matchers.greaterThan(0), Matchers.lessThan(3000)));
    }

    @Test
    public void compareStringDate() {
        this.parser = new Parser();
        String date1 = "01 янв 99, 12:00";
        String date2 = "01 янв 99, 12:01";
        boolean result = parser.compareStringDate(date1, date2);
        Assert.assertThat(result, Is.is(false));
    }
}