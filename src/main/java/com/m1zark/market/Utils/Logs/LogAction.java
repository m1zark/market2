package com.m1zark.market.Utils.Logs;

public enum LogAction {
    All,
    Addition,   // The action specified when a user inputs something into the listings
    Removal,    // The action specified when a user removes something from the listings
    Buy,        // The action specified when a user receives the contents of a lot element
    Sell,       // The action specified when a user has their listing sold on the market
}
