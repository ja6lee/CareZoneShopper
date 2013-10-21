package com.lee.jeff.shopper.zone.care;


public class ShoppingListItem {
    private long id;
    private String category, name, createdDate, updatedDate;

    public ShoppingListItem() {}

    public ShoppingListItem(long id, String category, String name, String createdAt, String updatedAt) {
        this.id = id;
        this.category = category;
        this.name = name;
        this.createdDate = createdAt;
        this.updatedDate = updatedAt;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getCategory() {
        return category;
    }

    public String getName() {
        return name;
    }

    public String getCreatedAt() {
        return createdDate;
    }

    public String getUpdatedAt() {
        return updatedDate;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setCreatedAt(String date) {
        this.createdDate = date;
    }

    public void setUpdatedAt(String date) {
        this.updatedDate = date;
    }

    // no longer used -- was used when not using custom adapter
    @Override
    public String toString() {
        return "Category: " + category + "\nName:" + name;
    }
}