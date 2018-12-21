package com.martdev.android.criminalintent.model;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.martdev.android.criminalintent.database.CrimeBaseHelper;
import com.martdev.android.criminalintent.database.CrimeCursorWrapper;
import com.martdev.android.criminalintent.database.CrimeDbSchema.CrimeTable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CrimeLab {
    private static CrimeLab sCrimeLab;

    private Context mContext;
    private SQLiteDatabase mDatabase;

    //the context helps in telling a particular component what is going on in an activity or application
    public static CrimeLab get(Context context) {
        if (sCrimeLab == null) {
            sCrimeLab = new CrimeLab(context);
        }
        return sCrimeLab;
    }

    private CrimeLab(Context context) {
        mContext = context.getApplicationContext();
        mDatabase = new CrimeBaseHelper(mContext).getWritableDatabase();
    }

    public void addCrime(Crime crime) {
        ContentValues values = getContentValues(crime);

        mDatabase.insert(CrimeTable.NAME, null, values);
    }

    public void deleteCrime(Crime crime) {
        String crimeId = crime.getId().toString();

        mDatabase.delete(CrimeTable.NAME, CrimeTable.Cols.UUID
                + " = ?", new String[] { crimeId });
    }

    public List<Crime> getCrimes() {
        List<Crime> crimes = new ArrayList<>();

        CrimeCursorWrapper cursor = query(null, null);

        try {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                crimes.add(cursor.getCrime());
                cursor.moveToNext();
            }
        } finally {
            cursor.close();
        }

        return crimes;
    }

    public Crime getCrime(UUID id) {
         CrimeCursorWrapper cursor = query(CrimeTable.Cols.UUID + " = ?",
                 new String[] { id.toString() });

         try {
             if (cursor.getCount() == 0) {
                 return null;
             }

             cursor.moveToFirst();
             return cursor.getCrime();
         } finally {
             cursor.close();
         }
    }

    public void updateCrime(Crime crime) {
        String uuidId = crime.getId().toString();
        ContentValues values = getContentValues(crime);

        mDatabase.update(CrimeTable.NAME, values, CrimeTable.Cols.UUID
                + " = ?", new String[] { uuidId });
    }

    private CrimeCursorWrapper query(String whereClause, String[] whereArgs) {
        Cursor cursor = mDatabase.query(
                CrimeTable.NAME,
                null,
                whereClause,
                whereArgs,
                null,
                null,
                null
        );

        return new CrimeCursorWrapper(cursor);
    }

    private ContentValues getContentValues(Crime crime) {
        ContentValues values = new ContentValues();
        values.put(CrimeTable.Cols.UUID, crime.getId().toString());
        values.put(CrimeTable.Cols.TITLE, crime.getTitle());
        values.put(CrimeTable.Cols.DATE, crime.getDate().getTime());
        values.put(CrimeTable.Cols.TIME, crime.getTime().getTime());
        values.put(CrimeTable.Cols.SOLVED, crime.isSolved());
        values.put(CrimeTable.Cols.SUSPECT, crime.getSuspect());
        values.put(CrimeTable.Cols.SUSPECT_NUMBER, crime.getSuspectNumber());

        return values;
    }
}
