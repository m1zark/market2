package com.m1zark.market.Utils.Config;

public enum Tokens {
    Max("max"),
    Player("player"),
    Balance("balance"),
    Expires("expires"),
    Price("price"),
    Quantity("quantity"),
    Buyer("buyer"),
    Count("count"),
    Item("item"),
    MaxPrice("maxprice"),
    Seller("seller"),
    Page("page");

    private String token;

    private Tokens(String token) {
        this.token = token;
    }

    public String getToken() {
        return this.token;
    }
}
