package com.example.android.habittrackerapp.data;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.util.Log;

import com.example.android.habittrackerapp.data.HabitContract.HabitEntry;

/**
 * Created by Gabriel on 26/04/2018.
 */

/**
 * Database helper for Habits app. Manages database creation and version management.
 */
public class HabitDbHelper extends SQLiteOpenHelper {

    private static HabitDbHelper mDbHelper;

    /**
     * Name of the database file
     */
    private static final String DATABASE_NAME = "shelter.db";

    /**
     * Database version. If you change the database schema, you must increment the database version.
     */
    private static final int DATABASE_VERSION = 4;

    /**
     * URI matcher code for the content URI for the habits table
     */
    private static final int HABITS = 100;

    /**
     * URI matcher code for the content URI for a single habit in the habits table
     */
    private static final int HABIT_ID = 101;

    /**
     * UriMatcher object to match a content URI to a corresponding code.
     * The input passed into the constructor represents the code to return for the root URI.
     * It's common to use NO_MATCH as the input for this case.
     */
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    /**
     * Constructs a new instance of {@link HabitDbHelper}.
     *
     * @param context of the app
     */
    public HabitDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);

        // The calls to addURI() go here, for all of the content URI patterns that the provider
        // should recognize. All paths added to the UriMatcher have a corresponding code to return
        // when a match is found.

        // The content URI of the form "content://com.example.android.habits/habits" will map to the
        // integer code {@link #HABITS}. This URI is used to provide access to MULTIPLE rows
        // of the habits table.
        sUriMatcher.addURI(HabitContract.CONTENT_AUTHORITY, HabitContract.PATH_HABITS, HABITS);

        // The content URI of the form "content://com.example.android.habits/habits/#" will map to the
        // integer code {@link #HABIT_ID}. This URI is used to provide access to ONE single row
        // of the habits table.
        //
        // In this case, the "#" wildcard is used where "#" can be substituted for an integer.
        // For example, "content://com.example.android.habits/habits/3" matches, but
        // "content://com.example.android.habits/habits" (without a number at the end) doesn't match.
        sUriMatcher.addURI(HabitContract.CONTENT_AUTHORITY, HabitContract.PATH_HABITS+ "/#", HABIT_ID);
    }

    /**
     * This is called when the database is created for the first time.
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create a String that contains the SQL statement to create the habits table
        String SQL_CREATE_HABITS_TABLE = "CREATE TABLE " + HabitEntry.TABLE_HABIT + " ("
                + HabitEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + HabitEntry.COLUMN_HABIT + " TEXT NOT NULL,"
                + HabitEntry.COLUMN_IMPORTANCE + " INTEGER);";

        // Execute the SQL statement
        db.execSQL(SQL_CREATE_HABITS_TABLE);
    }

    /**
     * This is called when the database needs to be upgraded.
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE " + HabitEntry.TABLE_HABIT + ";");
        onCreate(db);
    }

    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        // Get readable database
        SQLiteDatabase database = mDbHelper.getReadableDatabase();

        // This cursor will hold the result of the query
        Cursor cursor;

        // Figure out if the URI matcher can match the URI to a specific code
        int match = sUriMatcher.match(uri);
        switch (match) {
            case HABITS:
                // For the HABITS code, query the habits table directly with the given
                // projection, selection, selection arguments, and sort order. The cursor
                // could contain multiple rows of the habits table.
                cursor = database.query(HabitEntry.TABLE_HABIT, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            case HABIT_ID:
                // For the HABIT_ID code, extract out the ID from the URI.
                // For an example URI such as "content://com.example.android.habits/habits/3",
                // the selection will be "_id=?" and the selection argument will be a
                // String array containing the actual ID of 3 in this case.
                //
                // For every "?" in the selection, we need to have an element in the selection
                // arguments that will fill in the "?". Since we have 1 question mark in the
                // selection, we have 1 String in the selection arguments' String array.
                selection = HabitEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};

                // This will perform a query on the habits table where the _id equals 3 to return a
                // Cursor containing that row of the table.
                cursor = database.query(HabitEntry.TABLE_HABIT, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown URI " + uri);
        }
        return cursor;
    }

    public int update(Uri uri, ContentValues contentValues, String selection,
                      String[] selectionArgs) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case HABITS:
                return updateHabit(uri, contentValues, selection, selectionArgs);
            case HABIT_ID:
                // For the HABIT_ID code, extract out the ID from the URI,
                // so we know which row to update. Selection will be "_id=?" and selection
                // arguments will be a String array containing the actual ID.
                selection = HabitEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                return updateHabit(uri, contentValues, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }
    }

    public long insert(Uri uri, ContentValues contentValues) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case HABITS:
                return insertHabit(uri, contentValues);
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }
    }

    /**
     * Insert a habit into the database with the given content values. Return the new content URI
     * for that specific row in the database.
     */
    public long insertHabit(Uri uri, ContentValues values) {
        // If the {@link HabitEntry#COLUMN_HABIT_NAME} key is present,
        // check that the name value is not null.
        if (!values.containsKey(HabitEntry.COLUMN_HABIT) ||                      //habit
                values.getAsString(HabitEntry.COLUMN_HABIT) == null) {
            throw new IllegalArgumentException("Habit requires a name");
        }

        // Otherwise, get writeable database to update the data
        SQLiteDatabase database = this.getWritableDatabase();

        // Perform the update on the database and get the number of rows affected
        long rowsUpdated = database.insert(HabitEntry.TABLE_HABIT, null, values); //habits

        //QUANTIDADE DE ITENS NA TABELA HABITS (VIZUALIZAR NO LOGCAT)
        System.out.println("DATABASE - " + database.rawQuery("SELECT * FROM habits", null).getCount());

        database.close();

        // If the ID is -1, then the insertion failed. Log an error and return null.
        if (rowsUpdated == -1) {
            Log.e("DB_ERROR", "Failed to insert row for " + uri);
        }

        return rowsUpdated;
    }

    /**
     * Update habits in the database with the given content values. Apply the changes to the rows
     * specified in the selection and selection arguments (which could be 0 or 1 or more habits).
     * Return the number of rows that were successfully updated.
     */
    public int updateHabit(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        // If the {@link HabitEntry#COLUMN_HABIT_NAME} key is present,
        // check that the name value is not null.
        if (values.containsKey(HabitEntry.COLUMN_HABIT)) {
            String name = values.getAsString(HabitEntry.COLUMN_HABIT);
            if (name == null) {
                throw new IllegalArgumentException("Habit requires a name");
            }
        }

        // No need to check the breed, any value is valid (including null).

        // If there are no values to update, then don't try to update the database
        if (values.size() == 0) {
            return 0;
        }

        // Otherwise, get writeable database to update the data
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // Perform the update on the database and get the number of rows affected
        int rowsUpdated = database.update(HabitEntry.TABLE_HABIT, values, selection, selectionArgs);

        // Return the number of rows updated
        return rowsUpdated;
    }

    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case HABITS:
                return HabitEntry.CONTENT_LIST_TYPE;
            case HABIT_ID:
                return HabitEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException("Unknown URI " + uri + " with match " + match);
        }
    }

    public Cursor sectAll(){
        // Otherwise, get writeable database to update the data
        SQLiteDatabase database = mDbHelper.getReadableDatabase();

        Cursor c = database.rawQuery("SELECT * FROM habits", null);

        return c;
    }

}