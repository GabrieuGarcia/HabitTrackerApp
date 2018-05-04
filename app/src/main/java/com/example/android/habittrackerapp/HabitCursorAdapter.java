package com.example.android.habittrackerapp;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.example.android.habittrackerapp.data.HabitContract.HabitEntry;

/**
 * Created by Gabriel on 01/05/2018.
 */

public class HabitCursorAdapter  extends CursorAdapter {

    /**
     * Constructs a new {@link HabitCursorAdapter}.
     *
     * @param context The context
     * @param c       The cursor from which to get the data.
     */
    public HabitCursorAdapter(Context context, Cursor c) {
        super(context, c, 0 /* flags */);
    }

    /**
     * Makes a new blank list item view. No data is set (or bound) to the views yet.
     *
     * @param context app context
     * @param cursor  The cursor from which to get the data. The cursor is already
     *                moved to the correct position.
     * @param parent  The parent to which the new view is attached to
     * @return the newly created list item view.
     */
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        // Inflate a list item view using the layout specified in list_item.xml
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    /**
     * This method binds the habit data (in the current row pointed to by cursor) to the given
     * list item layout. For example, the habit for the current habit can be set on the habit TextView
     * in the list item layout.
     *
     * @param view    Existing view, returned earlier by newView() method
     * @param context app context
     * @param cursor  The cursor from which to get the data. The cursor is already moved to the
     *                correct row.
     */
    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        // Find individual views that we want to modify in the list item layout
        TextView habitTextView = (TextView) view.findViewById(R.id.habit);

        // Find the columns of habit attributes that we're interested in
        int habitColumnIndex = cursor.getColumnIndex(HabitEntry.COLUMN_HABIT);

        // Read the habit attributes from the Cursor for the current habit
        String habitName = cursor.getString(habitColumnIndex);

        // If the habit breed is empty string or null, then use some default text
        // that says "Unknown breed", so the TextView isn't blank.

        // Update the TextViews with the attributes for the current habit
        habitTextView.setText(habitName);
    }

}
