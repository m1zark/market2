package com.m1zark.market.Utils.Database;

import com.m1zark.market.Market;
import com.m1zark.market.Utils.Config.ConfigManager;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.TimeUnit;

public class DataSource extends SQLStatements {
    private static HikariConfig config = new HikariConfig();
    private static HikariDataSource ds;

    public DataSource(String mainTable, String logTable) {
        super(mainTable, logTable);
    }

    public static Connection getConnection() throws SQLException {
        return ds.getConnection();
    }

    public void shutdown() { if (ds != null) ds.close(); }

    static {
        if(ConfigManager.getStorageType().equalsIgnoreCase("h2")) {
            config.setDataSourceClassName("org.h2.jdbcx.JdbcDataSource");
            config.addDataSourceProperty("URL", "jdbc:h2:" + Market.getInstance().getConfigDir() + "/storage/data;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false;MODE=MSSQLServer");
        } else {
            config.setJdbcUrl("jdbc:mysql://" + ConfigManager.mysqlURL);
            config.setUsername(ConfigManager.mysqlUsername);
            config.setPassword(ConfigManager.mysqlPassword);

            config.addDataSourceProperty("alwaysSendSetIsolation", false);
            config.addDataSourceProperty("prepStmtCacheSqlLimit", 2048);
            config.addDataSourceProperty("prepStmtCacheSize", 250);
            config.addDataSourceProperty("cachePrepStmts", true);
            config.addDataSourceProperty("useServerPrepStmts", true);
            config.addDataSourceProperty("cacheCallableStmts", true);
            config.addDataSourceProperty("cacheServerConfiguration", true);
            config.addDataSourceProperty("elideSetAutoCommits", true);
            config.addDataSourceProperty("useLocalSessionState", true);
        }

        config.setPoolName("Market");
        config.setMaximumPoolSize(10);
        config.setMinimumIdle(10);
        config.setMaxLifetime(1800000);
        config.setConnectionTimeout(5000);
        config.setLeakDetectionThreshold(TimeUnit.SECONDS.toMillis(10));
        config.setConnectionTestQuery("/* Market ping */ SELECT 1");
        config.setInitializationFailTimeout(1);
        ds = new HikariDataSource(config);
    }
}
