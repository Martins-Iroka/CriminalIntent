package com.martdev.android.criminalintent;

import android.support.v4.app.Fragment;

import com.martdev.android.criminalintent.fragment.CrimeListFragment;

public class CrimeListActivity extends SingleFragmentActivity {

    @Override
    public Fragment createFragment() {
        return new CrimeListFragment();
    }
}
