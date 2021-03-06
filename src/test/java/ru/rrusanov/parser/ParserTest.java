package ru.rrusanov.parser;
import org.hamcrest.CoreMatchers;
import org.hamcrest.Matchers;
import org.hamcrest.core.Is;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
/**
 * The class check behavior Parse class.
 */
public class ParserTest {
    /**
     * The field contain instance of test class.
     */
    private Parser parser;
    /**
     * The method execute before each test.
     */
    @Before
    public void setUp() {
        this.parser = new Parser();
    }
    /**
     * The test check method getTextArticle.
     */
    @Test
    public void whenParseTopicThenReturnText() {
        this.parser = new Parser();
        String expect = "Здесь будут писаться все описания действий модераторов по исправлению и удалению топиков, "
                + "а так же замечания участникам форума, согласно правилам форума \"Работа\".";
        String result = parser.getTextArticle("https://www.sql.ru/forum/269098/"
                + "soobshheniya-ot-moderatorov-zdes-vy-mozhete-uznat-prichiny-udaleniya-topikov");
        Assert.assertThat(result, Is.is(expect));
    }
    /**
     * The test check method findMatchCharSequence.
     */
    @Test()
    public void whenStringContainMatchPatternThenReturnTrue() {
        String[] topicsNotMatch = {"java script", "javascript"};
        Assert.assertThat(this.parser.findMatchCharSequence("The javascript lang", topicsNotMatch),
                Is.is(true));
        Assert.assertThat(this.parser.findMatchCharSequence("The java script lang", topicsNotMatch),
                Is.is(true));
        Assert.assertThat(this.parser.findMatchCharSequence("The java lang", topicsNotMatch),
                Is.is(false));
        Assert.assertThat(this.parser.findMatchCharSequence("shortStr", topicsNotMatch), Is.is(false));
    }
    /**
     * The test check method getMaxPageNumber.
     */
    @Test
    public void maxPageNumberMustBeGreater0AndLess3000() {
        int actual = this.parser.getMaxPageNumber("https://www.sql.ru/forum/job/");
        Assert.assertThat(actual, CoreMatchers.allOf(Matchers.greaterThan(0), Matchers.lessThan(3000)));
    }
    /**
     * The test check method compareStringDate.
     */
    @Test
    public void whenFirstDateLessSecondDateThenReturnFalse() {
        this.parser = new Parser();
        String date1 = "01 янв 99, 12:00";
        String date2 = "01 янв 99, 12:01";
        boolean result = parser.compareStringDate(date1, date2);
        Assert.assertThat(result, Is.is(false));
    }
}