package com.martdev.android.criminalintent.database;

import android.database.Cursor;
import android.database.CursorWrapper;

import com.martdev.android.criminalintent.database.CrimeDbSchema.CrimeTable;
import com.martdev.android.criminalintent.model.Crime;

import java.util.Date;
import java.util.UUID;

public class CrimeCursorWrapper extends CursorWrapper {
    public CrimeCursorWrapper(Cursor cursor) {
        super(cursor);
    }

    public Crime getCrime() {
        String uuid = getString(getColumnIndex(CrimeTable.Cols.UUID));
        String title = getString(getColumnIndex(CrimeTable.Cols.TITLE));
        long date = getLong(getColumnIndex(CrimeTable.Cols.DATE));
        long time = getLong(getColumnIndex(CrimeTable.Cols.TIME));
        int solved = getInt(getColumnIndex(CrimeTable.Cols.SOLVED));
        String suspect = getString(getColumnIndex(CrimeTable.Cols.SUSPECT));
        String suspect_number = getString(getColumnIndex(CrimeTable.Cols.SUSPECT_NUMBER));

        Crime crime = new Crime(UUID.fromString(uuid));
        crime.setTitle(title);
        crime.setDate(new Date(date));
        crime.setTime(new Date(time));
        crime.setSolved(solved != 0);
        crime.setSuspect(suspect);
        crime.setSuspectNumber(suspect_number);

        return crime;
    }
}
