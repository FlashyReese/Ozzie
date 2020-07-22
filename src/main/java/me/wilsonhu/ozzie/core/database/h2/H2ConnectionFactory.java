package me.wilsonhu.ozzie.core.database.h2;

import org.h2.jdbcx.JdbcDataSource;

import java.sql.Connection;
import java.sql.SQLException;

public class H2ConnectionFactory {

    public H2ConnectionFactory() throws SQLException {
        JdbcDataSource ds = new JdbcDataSource();
        ds.setURL("jdbc:h2:˜/test");
        ds.setUser("sa");
        ds.setPassword("sa");
        Connection conn = ds.getConnection();
    }
}
