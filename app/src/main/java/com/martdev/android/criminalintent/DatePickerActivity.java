package com.martdev.android.criminalintent;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;

import com.martdev.android.criminalintent.fragment.DatePickerFragment;

import java.util.Date;

public class DatePickerActivity extends SingleFragmentActivity {
    private static final String EXTRA_DATE = "com.martdev.android.criminalintent.crime_date";

    public static Intent newIntent(Context packageContext, Date date) {
        Intent intent = new Intent(packageContext, DatePickerActivity.class);
        intent.putExtra(EXTRA_DATE, date);
        return intent;
    }

    @Override
    protected Fragment createFragment() {
        Date date = (Date) getIntent().getSerializableExtra(EXTRA_DATE);

        return DatePickerFragment.newInstance(date);
    }
}
