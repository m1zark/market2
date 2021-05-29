package com.m1zark.market.Utils.Logs;

import com.m1zark.market.Market;
import com.m1zark.market.Utils.Listing;
import lombok.Getter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Getter
public class Log {
    private int id;
    private String player;
    private Listing listing;
    private String timeStamp;
    private LogAction logAction;

    public Log(String player, Listing listing, String timeStamp, LogAction action) {
        this.player = player;
        this.listing = listing;
        this.timeStamp = timeStamp;
        this.logAction = action;
    }

    public void setId(int id) { this.id = id; }

    public LocalDateTime getTimeStampDate() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy '@' h:mm a z");
        return LocalDateTime.parse(this.timeStamp, formatter);
    }
}
