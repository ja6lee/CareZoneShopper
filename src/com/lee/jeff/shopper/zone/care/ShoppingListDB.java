package com.lee.jeff.shopper.zone.care;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class ShoppingListDB extends SQLiteOpenHelper {

    public static final String TABLE_SHOPPING_ITEMS = "shopping_items";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_CATEGORY = "category";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_UPDATED_AT = "createdat";
    public static final String COLUMN_CREATED_AT = "updatedat";

    private static final String DATABASE_NAME = "shoppinglistitems.db";
    private static final int DATABASE_VERSION = 1;

    // Database creation sql statement
    private static final String DATABASE_CREATE = "create table "
            + TABLE_SHOPPING_ITEMS + "(" + COLUMN_ID
            + " integer primary key, " + COLUMN_CATEGORY
            + " text not null," + COLUMN_NAME
            + " text not null," + COLUMN_CREATED_AT
            + " text not null," + COLUMN_UPDATED_AT
            + " text not null" + ");";

    public ShoppingListDB(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        database.execSQL(DATABASE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(ShoppingListDB.class.getName(),
                "Upgrading database from version " + oldVersion + " to "
                        + newVersion + ", which will destroy all old data");
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SHOPPING_ITEMS);
        onCreate(db);
    }

}