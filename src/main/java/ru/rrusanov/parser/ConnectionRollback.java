package ru.rrusanov.parser;

import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Class create connection with rollback all commits.
 * It is used for integration test.
 *
 * @author Roman Rusanov
 * @version 0.1
 * @since 25.03.19
 */
public class ConnectionRollback {
    /**
     * Create connection with autocommit=false mode and rollback call, when connection is closed.
     * @param connection to db.
     * @return modified connection.
     * @throws SQLException possible exception.
     */
    public static Connection create(Connection connection) throws SQLException {
        connection.setAutoCommit(false);
        return (Connection) Proxy.newProxyInstance(
                ConnectionRollback.class.getClassLoader(),
                new Class[] {Connection.class},
                (proxy, method, args) -> {
                    Object rsl = null;
                    if ("close".equals(method.getName())) {
                        connection.rollback();
                        connection.close();
                    } else {
                        rsl = method.invoke(connection, args);
                    }
                    return rsl;
                }
        );
    }
}
