package com.m1zark.market.Utils.Database;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.m1zark.market.Market;
import com.m1zark.market.MarketInfo;
import com.m1zark.market.Utils.Config.ConfigManager;
import com.m1zark.market.Utils.Listing;
import com.m1zark.market.Utils.Logs.Log;
import org.spongepowered.api.text.Text;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class SQLStatements {
    private String mainTable;
    private String logTable;

    public SQLStatements(String mainTable, String logTable) {
        this.mainTable = mainTable;
        this.logTable = logTable;
    }

    public void createTables() {
        try(Connection connection = DataSource.getConnection()) {
            try(PreparedStatement statement = connection.prepareStatement("CREATE TABLE IF NOT EXISTS `" + this.mainTable + "` (ID INTEGER NOT NULL AUTO_INCREMENT, SellerUUID CHAR(36) NOT NULL, Item LONGTEXT, Quantity Integer, Price MEDIUMTEXT, Ends MEDIUMTEXT, PRIMARY KEY(ID))")) {
                statement.executeUpdate();
            }

            try(PreparedStatement statement = connection.prepareStatement("CREATE TABLE IF NOT EXISTS `" + this.logTable + "` (ID INTEGER NOT NULL AUTO_INCREMENT, Log LONGTEXT, PRIMARY KEY(ID))")) {
                statement.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void clearTables() {
        try(Connection connection = DataSource.getConnection()) {
            try(PreparedStatement statement = connection.prepareStatement("TRUNCATE TABLE `" + this.mainTable + "`")) {
                statement.executeUpdate();
            }
        } catch(SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean addListing(Listing listing) {
        try(Connection connection = DataSource.getConnection()) {
            try(PreparedStatement statement = connection.prepareStatement("INSERT INTO `" + this.mainTable + "` (SellerUUID, Item, Quantity, Price, Ends) VALUES (?, ?, ?, ?, ?)")) {
                statement.setString(1, listing.getSellerUUID().toString());
                statement.setString(2, listing.getItem());
                statement.setInt(3, listing.getQuantity());
                statement.setString(4, listing.getPrice());
                statement.setString(5, listing.getExpiresRaw());
                statement.executeUpdate();
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public void removeListing(int id) {
        try(Connection connection = DataSource.getConnection()) {
            try(PreparedStatement query = connection.prepareStatement("DELETE FROM `" + this.mainTable + "` WHERE ID='" + id + "'")) {
                query.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updateListing(int id, boolean admin) {
        try(Connection connection = DataSource.getConnection()) {
            long expires = Market.getInstance().getConfig().getListingsTime();
            Instant date = (admin) ? Instant.now().minusSeconds(60) : Instant.now().plusSeconds(expires);

            try(PreparedStatement query = connection.prepareStatement("UPDATE `" + this.mainTable + "` SET Ends='" + date.toString() + "' WHERE ID='" + id + "'")) {
                query.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<Listing> getAllListings() {
        ArrayList<Listing> listings = new ArrayList<>();

        try(Connection connection = DataSource.getConnection()) {
            try(ResultSet results = connection.prepareStatement("SELECT * FROM `" + this.mainTable + "` ORDER BY ENDS ASC").executeQuery()) {
                while(results.next()) {
                    listings.add(new Listing(results.getInt("ID"), results.getString("Item"), UUID.fromString(results.getString("SellerUUID")), results.getString("Price"), results.getInt("Quantity"), results.getString("Ends")));
                }
            }

            return listings;
        } catch (SQLException e) {
            e.printStackTrace();
            return Lists.newArrayList();
        }
    }


    public void addNewLog(Log log) {
        Gson gson = new Gson();
        try(Connection connection = DataSource.getConnection()) {
            try(PreparedStatement statement = connection.prepareStatement("INSERT INTO `" + this.logTable + "` (Log) VALUES (?)")) {
                statement.setString(1, gson.toJson(log));
                statement.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void removeLog(int id) {
        try(Connection connection = DataSource.getConnection()) {
            try(PreparedStatement query = connection.prepareStatement("DELETE FROM `" + this.logTable + "` WHERE ID='" + id + "'")) {
                query.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void removeAllLogs() {
        try(Connection connection = DataSource.getConnection()) {
            try(PreparedStatement statement = connection.prepareStatement("TRUNCATE TABLE `" + this.logTable + "`")) {
                statement.executeUpdate();
            }
        } catch(SQLException e) {
            e.printStackTrace();
        }
    }

    public List<Log> getAllLogs() {
        Gson gson = new Gson();
        ArrayList<Log> logs = new ArrayList<>();

        try(Connection connection = DataSource.getConnection()) {
            try(ResultSet results = connection.prepareStatement("SELECT * FROM `" + this.logTable + "` ORDER BY ID DESC").executeQuery()) {
                while (results.next()) {
                    Log log = gson.fromJson(results.getString("Log"), Log.class);
                    log.setId(results.getInt("ID"));
                    if(!this.checkExpired(log)) logs.add(log);
                }
            }

            return logs;
        } catch (SQLException e) {
            e.printStackTrace();
            return Lists.newArrayList();
        }
    }

    private boolean checkExpired(Log log) {
        SimpleDateFormat ft = new SimpleDateFormat("MM/dd/yyyy @ h:mm a z");
        try {
            Date date = new Date(ft.parse(log.getTimeStamp()).getTime());
            long daysElapsed = ChronoUnit.DAYS.between(date.toInstant() , Instant.now());

            if(daysElapsed >= ConfigManager.getRemoveExpiredLogs()) {
                removeLog(log.getId());
                return true;
            }
            return false;
        } catch (ParseException e) {
            e.printStackTrace();
            return false;
        }
    }
}
