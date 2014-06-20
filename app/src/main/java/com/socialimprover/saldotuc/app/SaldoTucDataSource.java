package com.socialimprover.saldotuc.app;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;

public class SaldoTucDataSource {

    private SQLiteDatabase mDatabase; // The actual DB!
    private SaldoTucHelper mSaldoTucHelper; // Helper class for creating and opening the DB

    public SaldoTucDataSource(Context context) {
        mSaldoTucHelper = new SaldoTucHelper(context);
    }

    /*
     * Open the db. Will create if it doesn't exist
     */
    public void open() throws SQLiteException {
        mDatabase = mSaldoTucHelper.getWritableDatabase();
    }

    /*
     * We always need to close our db connections
     */
    public void close() {
        mDatabase.close();
    }

     /*
     * CRUD operations!
     */
    public void insertCard(Card card) {
        ContentValues values = new ContentValues();
        values.put(SaldoTucHelper.COLUMN_NAME, card.getName());
        values.put(SaldoTucHelper.COLUMN_CARD, card.getNumber());
        values.put(SaldoTucHelper.COLUMN_PHONE, card.getPhone());
        values.put(SaldoTucHelper.COLUMN_HOUR, card.getHour());
        values.put(SaldoTucHelper.COLUMN_AMPM, card.getAmpm());

        if (card.getBalance() != null) {
            values.put(SaldoTucHelper.COLUMN_LAST_BALANCE, card.getBalance());
        }

        mDatabase.insert(SaldoTucHelper.TABLE_CARDS, null, values);
    }

    public Cursor selectAllCards() {
        return mDatabase.rawQuery("SELECT * FROM " + SaldoTucHelper.TABLE_CARDS + " ORDER BY LOWER(" + SaldoTucHelper.COLUMN_NAME + ")", null);
    }

    public Cursor selectCard(Integer id) {
        return mDatabase.rawQuery("SELECT * FROM " + SaldoTucHelper.TABLE_CARDS + " WHERE " + SaldoTucHelper.COLUMN_ID + " = " + id, null);
    }

    public int updateCard(Card card) {
        ContentValues values = new ContentValues();
        values.put(SaldoTucHelper.COLUMN_NAME, card.getName());
        values.put(SaldoTucHelper.COLUMN_CARD, card.getNumber());
        values.put(SaldoTucHelper.COLUMN_PHONE, card.getPhone());
        values.put(SaldoTucHelper.COLUMN_HOUR, card.getHour());
        values.put(SaldoTucHelper.COLUMN_AMPM, card.getAmpm());
        values.put(SaldoTucHelper.COLUMN_LAST_BALANCE, card.getBalance());

        return mDatabase.update(
            SaldoTucHelper.TABLE_CARDS, // table
            values, // values
            SaldoTucHelper.COLUMN_ID + " = ?",   // where clause
            new String[] { card.getId().toString() } // where params
        );
    }

}
