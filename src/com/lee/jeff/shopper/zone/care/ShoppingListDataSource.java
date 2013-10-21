package com.lee.jeff.shopper.zone.care;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class ShoppingListDataSource {

    // Database fields
    private SQLiteDatabase database;
    private ShoppingListDB dbHelper;
    private String[] allColumns = { ShoppingListDB.COLUMN_ID,
            ShoppingListDB.COLUMN_CATEGORY, ShoppingListDB.COLUMN_NAME, ShoppingListDB.COLUMN_CREATED_AT,
            ShoppingListDB.COLUMN_UPDATED_AT };

    public ShoppingListDataSource(Context context) {
        dbHelper = new ShoppingListDB(context);
    }

    public void open() throws SQLException {
        database = dbHelper.getWritableDatabase();
    }

    public void close() {
        dbHelper.close();
    }

    // gets the shopping item in the db with id
    public ShoppingListItem getShoppingListItemWithId(long id) {
        Cursor cursor = database.query(ShoppingListDB.TABLE_SHOPPING_ITEMS,
                allColumns, ShoppingListDB.COLUMN_ID + " = " + id, null,
                null, null, null);
        ShoppingListItem item = null;
        if (cursor != null && cursor.getCount() != 0) {
            cursor.moveToFirst();
            item = cursorToShoppingListItem(cursor);
            cursor.close();
        }
        return item;
    }

    // creates a shopping list item
    public ShoppingListItem createShoppingListItem(long id, String category, String name, String createdAt, String updatedAt) {
        ContentValues values = new ContentValues();
        values.put(ShoppingListDB.COLUMN_ID, id);
        values.put(ShoppingListDB.COLUMN_CATEGORY, category);
        values.put(ShoppingListDB.COLUMN_NAME, name);
        values.put(ShoppingListDB.COLUMN_CREATED_AT, createdAt);
        values.put(ShoppingListDB.COLUMN_UPDATED_AT, updatedAt);
        long insertId = database.insert(ShoppingListDB.TABLE_SHOPPING_ITEMS, null,
                values);
        Cursor cursor = database.query(ShoppingListDB.TABLE_SHOPPING_ITEMS,
                allColumns, ShoppingListDB.COLUMN_ID + " = " + insertId, null,
                null, null, null);
        cursor.moveToFirst();
        ShoppingListItem newItem = cursorToShoppingListItem(cursor);
        cursor.close();
        return newItem;
    }

    // updates entry in database
    public void updateShoppingListItem(long id, String category, String name, String createdAt, String updatedAt) {
        ContentValues values = new ContentValues();
        values.put(ShoppingListDB.COLUMN_CATEGORY, category);
        values.put(ShoppingListDB.COLUMN_NAME, name);
        values.put(ShoppingListDB.COLUMN_CREATED_AT, createdAt);
        values.put(ShoppingListDB.COLUMN_UPDATED_AT, updatedAt);
        int rowsAffected = database.update(ShoppingListDB.TABLE_SHOPPING_ITEMS, values, ShoppingListDB.COLUMN_ID + "=" + id, null);
    }

    // deletes an item from the database
    public void deleteShoppingListItem(ShoppingListItem item) {
        long id = item.getId();
        database.delete(ShoppingListDB.TABLE_SHOPPING_ITEMS, ShoppingListDB.COLUMN_ID
                + " = " + id, null);
    }

    // gets all shopping items from the db
    public ArrayList<ShoppingListItem> getAllShoppingListItems() {
        ArrayList<ShoppingListItem> items = new ArrayList<ShoppingListItem>();

        Cursor cursor = database.query(ShoppingListDB.TABLE_SHOPPING_ITEMS,
                allColumns, null, null, null, null, null);

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            ShoppingListItem item = cursorToShoppingListItem(cursor);
            items.add(item);
            cursor.moveToNext();
        }
        cursor.close();
        return items;
    }

    // converts the cursor to a shopping list item
    private ShoppingListItem cursorToShoppingListItem(Cursor cursor) {
        ShoppingListItem item = new ShoppingListItem();
        item.setId(cursor.getLong(0));
        item.setCategory(cursor.getString(1));
        item.setName(cursor.getString(2));
        item.setCreatedAt(cursor.getString(3));
        item.setUpdatedAt(cursor.getString(4));
        return item;
    }
}