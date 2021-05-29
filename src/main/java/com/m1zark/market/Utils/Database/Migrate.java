package com.m1zark.market.Utils.Database;

import com.m1zark.market.Market;
import com.m1zark.market.Utils.Config.ConfigManager;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.service.sql.SqlService;

import javax.sql.DataSource;
import java.io.File;
import java.sql.*;

public class Migrate {
    private static Connection h2Connection = null;
    private static DataSource h2Source = null;

    private static Connection mysqlConnection = null;
    private static DataSource mysqlSource = null;

    private static String MAIN_TABLE = "Market_Listing";

    private static void connectH2() {
        try {
            if (h2Source == null) {
                h2Source = Sponge.getServiceManager().provide(SqlService.class).get().getDataSource("jdbc:h2:" + Market.getInstance().getConfigDir().toString() + File.separator + "/storage/data");
            }
            h2Connection = h2Source.getConnection();
        } catch(SQLException e) {
            e.printStackTrace();
        }
    }

    private static void connectMYSQL() {
        try {
            if (mysqlSource == null) {
                mysqlSource = Sponge.getServiceManager().provide(SqlService.class).get().getDataSource("jdbc:mysql://" + ConfigManager.mysqlUsername + ":" + ConfigManager.mysqlPassword + "@" + ConfigManager.mysqlURL);
            }
            mysqlConnection = mysqlSource.getConnection();
        } catch(SQLException e) {
            e.printStackTrace();
        }
    }

    public static void migrateH2() {
        connectH2();
        connectMYSQL();

        try {
            h2Connection.prepareStatement("TRUNCATE TABLE `" + MAIN_TABLE + "`").executeUpdate();

            final PreparedStatement insertStatement = h2Connection.prepareStatement("INSERT INTO `" + MAIN_TABLE + "` (SellerUUID, Item, Quantity, Price, Ends) VALUES (?, ?, ?, ?, ?)");

            final Statement statement1 = mysqlConnection.createStatement();
            final ResultSet resultSet = statement1.executeQuery("SELECT SellerUUID, Item, Quantity, Price, Ends FROM `" + MAIN_TABLE + "`");
            while (resultSet.next()) {
                insertStatement.clearParameters();
                insertStatement.setString(1, resultSet.getString("SellerUUID"));
                insertStatement.setString(2, resultSet.getString("Item"));
                insertStatement.setInt(3, resultSet.getInt("Quantity"));
                insertStatement.setString(4, resultSet.getString("Price"));
                insertStatement.setString(5, resultSet.getString("Ends"));
                insertStatement.executeUpdate();
            }

            mysqlConnection.close();
            h2Connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void migrateSQL() {
        connectH2();
        connectMYSQL();

        try {
            mysqlConnection.prepareStatement("TRUNCATE TABLE `" + MAIN_TABLE + "`").executeUpdate();

            final PreparedStatement insertStatement = mysqlConnection.prepareStatement("INSERT INTO `" + MAIN_TABLE + "` (SellerUUID, Item, Quantity, Price, Ends) VALUES (?, ?, ?, ?, ?)");

            final Statement statement1 = h2Connection.createStatement();
            final ResultSet resultSet = statement1.executeQuery("SELECT SellerUUID, Item, Quantity, Price, Ends FROM `" + MAIN_TABLE + "`");
            while (resultSet.next()) {
                insertStatement.clearParameters();
                insertStatement.setString(1, resultSet.getString("SellerUUID"));
                insertStatement.setString(2, resultSet.getString("Item"));
                insertStatement.setInt(3, resultSet.getInt("Quantity"));
                insertStatement.setString(4, resultSet.getString("Price"));
                insertStatement.setString(5, resultSet.getString("Ends"));
                insertStatement.executeUpdate();
            }

            mysqlConnection.close();
            h2Connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
