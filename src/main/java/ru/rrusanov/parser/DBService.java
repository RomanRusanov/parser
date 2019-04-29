package ru.rrusanov.parser;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.sql.ResultSet;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Properties;
/**
 * @author Roman Rusanov
 * @version 0.1
 * @since 27.03.2019
 */
public class DBService implements AutoCloseable {
    /**
     * The field contain instance connection to database.
     */
    private Connection connection;
    /**
     * Logger.
     */
    private static final Logger LOG = LogManager.getLogger(DBService.class.getName());
    /**
     * The constructor.
     * @param config Configuration(path to db, name file, url jdbc)
     */
    public DBService(Config config) {
        this.initConnectionToSQLiteDB(config.getConfig());
    }

    /**
     * Method create instance of connection of db if it not exist yet.
     *
     * @param values Configuration db(pathToDb + file.db, url db, username, password).
     * @return True if connection create. Otherwise false.
     */
    public boolean initConnectionToSQLiteDB(Properties values) {
        String url = values.getProperty("url")
                + values.getProperty("pathToDB")
                + values.getProperty("fileDB");
        try {
            this.connection = ConnectionRollback.create(DriverManager.getConnection(url));
            this.connection.setAutoCommit(false);
        } catch (SQLException e) {
            LOG.error(String.format("Connection by passed url: %s", url));
        }
        return this.connection != null;
    }

    /**
     * Closes this resource, relinquishing any underlying resources.
     * This method is invoked automatically on objects managed by the
     * {@code try}-with-resources statement.
     *
     * @throws Exception if this resource cannot be closed
     */
    @Override
    public void close() throws Exception {
        if (this.connection != null) {
            this.connection.close();
        }
    }

    /**
     * The method insert all article in passed collection in DB.
     * @param articleList collection with articles.
     */
    public void insertArticleListToDB(List<Article> articleList) {
        int numArticleInsertToDB = 0;
        for (Article article : articleList) {
            try (PreparedStatement ps = this.connection.prepareStatement(
                "insert into vacancy (name, text, link) values (?, ?, ?)")
            ) {
                String topic = article.getTopic();
                if (this.isArticleExistInBD(article.getTopic())) {
                    continue;
                }
                ps.setString(1, topic);
                ps.setString(2, article.getText());
                ps.setString(3, article.getUrl());
                numArticleInsertToDB =  numArticleInsertToDB + ps.executeUpdate();
                this.connection.commit();
            } catch (SQLException e) {
                try {
                    this.connection.rollback();
                } catch (SQLException e1) {
                    LOG.error(String.format(
                            "Error rollback transaction. After unsuccessfully insert to vacancy table. "
                            + "%n SQL Exception:%s", e1.toString())
                    );
                }
                LOG.error(String.format("Error insert list in vacancy table.%n SQL Exception:%s", e.toString()));

            }
        }
        LOG.info(String.format("Article(s) added to DB: %d", numArticleInsertToDB));
    }
    /**
     * The method check exist this article in DB.
     * @param topic string with topic.
     * @return If exist return true, otherwise false.
     */
    public boolean isArticleExistInBD(String topic) {
        boolean result = false;
        try (PreparedStatement ps = this.connection.prepareStatement(
                "select name from vacancy;")) {
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    if (rs.getString("name").equals(topic)) {
                        result = true;
                    }
                }
            } catch (Exception e) {
                LOG.error("Result set (get data from vacancy table)");
            }
        } catch (SQLException e) {
            LOG.error("SQL query (select all articles names from vacancy table)");
        }
        return result;
    }
}
