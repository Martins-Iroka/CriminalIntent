package com.martdev.android.criminalintent;

import android.content.Intent;
import android.support.v4.app.Fragment;

import com.martdev.android.criminalintent.fragment.CrimeFragment;
import com.martdev.android.criminalintent.fragment.CrimeListFragment;
import com.martdev.android.criminalintent.model.Crime;

import java.util.UUID;

public class CrimeListActivity extends SingleFragmentActivity
        implements CrimeListFragment.Callbacks, CrimeListFragment.DeleteCrimeCallback,CrimeFragment.Callbacks {

    @Override
    public Fragment createFragment() {
        return new CrimeListFragment();
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_master_detail;
    }

    @Override
    public void onCrimeSelected(Crime crime) {
        if (findViewById(R.id.detail_fragment_container) == null) {
            Intent intent = CrimePagerActivity.newIntent(this, crime.getId());
            startActivity(intent);
        } else {
            Fragment newDetail = CrimeFragment.newInstance(crime.getId());
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.detail_fragment_container, newDetail)
                    .commit();
        }
    }

    @Override
    public void onCrimeUpdated(Crime crime) {
        CrimeListFragment listFragment = (CrimeListFragment)
                getSupportFragmentManager().findFragmentById(R.id.fragment_container);
        listFragment.updateUI();
    }

    @Override
    public void onDeleteCrime(UUID crimeId) {
        CrimeFragment crimeFragment = (CrimeFragment) getSupportFragmentManager().findFragmentById(R.id.detail_fragment_container);
        CrimeListFragment crimeListFragment = (CrimeListFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_container);

        crimeListFragment.deleteCrime(crimeId);
        crimeListFragment.updateUI();
        if (!(crimeFragment == null))
            crimeListFragment.getActivity().getSupportFragmentManager().beginTransaction().remove(crimeFragment).commit();
    }
}
