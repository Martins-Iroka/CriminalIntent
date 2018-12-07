package com.martdev.android.criminalintent.model;

import android.annotation.TargetApi;
import android.content.Context;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class CrimeLab {
    private static CrimeLab sCrimeLab;

    //private List<Crime> mCrimes;
    private Map<UUID, Crime> mCrimes;//chapter 10b challenge

    //the context helps in telling a particular component what is going on in an activity or application
    public static CrimeLab get(Context context) {
        if (sCrimeLab == null) {
            sCrimeLab = new CrimeLab(context);
        }
        return sCrimeLab;
    }

    private CrimeLab(Context context) {
        mCrimes = new LinkedHashMap<>();//chapter 10b challenge
    }

    public void addCrime(Crime crime) {
        mCrimes.put(crime.getId(), crime);
    }

    public void deleteCrime(UUID crimeId) {
        mCrimes.remove(crimeId);
    }

    public List<Crime> getCrimes() {
        return new ArrayList<>(mCrimes.values());//chapter 10b challenge
    }

    public Crime getCrime(UUID id) {
        return mCrimes.get(id);//chapter 10b challenge
    }
}
