package com.martdev.android.criminalintent.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.martdev.android.criminalintent.CrimePagerActivity;
import com.martdev.android.criminalintent.R;
import com.martdev.android.criminalintent.model.Crime;
import com.martdev.android.criminalintent.model.CrimeLab;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class CrimeListFragment extends Fragment {

    private RecyclerView mCrimeRecyclerView;
    private CrimeAdapter mAdapter;
    private int mAdapterClickedPosition = -1; //chapter 10a challenge

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_crime_list, container, false);

        mCrimeRecyclerView = v.findViewById(R.id.crime_recycler_view);
        mCrimeRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        updateUI();
        return v;
    }

    private void updateUI() {
        CrimeLab crimeLab = CrimeLab.get(getActivity());
        List<Crime> crimes = crimeLab.getCrimes();

        if (mAdapter == null) {
            mAdapter = new CrimeAdapter(crimes);
            mCrimeRecyclerView.setAdapter(mAdapter);
        } else {
            if (mAdapterClickedPosition < 0) { //chapter 10a challenge
                mAdapter.notifyDataSetChanged();
            } else {
                //chapter 10a challenge
                mAdapter.notifyItemChanged(mAdapterClickedPosition); //chapter 10a challenge
                mAdapterClickedPosition = -1;
            }
        }
    }

    //chapter 8 challenge
    private abstract class CrimeHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private Crime mCrime;
        private TextView mTitleTextView;
        private TextView mDateTextView;
        private ImageView mSolvedCrimeImage;

        CrimeHolder(LayoutInflater inflater, ViewGroup parent, int layout) {
            super(inflater.inflate(layout, parent, false));
            itemView.setOnClickListener(this);

            mTitleTextView = itemView.findViewById(R.id.crime_title);
            mDateTextView = itemView.findViewById(R.id.crime_date);
            mSolvedCrimeImage = itemView.findViewById(R.id.crime_solved);
        }

        @Override
        public void onClick(View view) {
            mAdapterClickedPosition = getAdapterPosition();//chapter 10a challenge
            Intent intent = CrimePagerActivity.newIntent(getActivity(), mCrime.getId());
            startActivity(intent);
        }

        public void bind(Crime crime) {
            mCrime = crime;
            mTitleTextView.setText(mCrime.getTitle());
            mDateTextView.setText(new SimpleDateFormat("EEEE, MMM, d, yyyy", Locale.getDefault()).format(mCrime.getDate()));//chapter 9 challenge
            mSolvedCrimeImage.setVisibility(mCrime.isSolved() ? View.VISIBLE : View.GONE);
        }
    }

    private class RegularCrimeHolder extends CrimeHolder { //chapter 8 challenge
        RegularCrimeHolder(LayoutInflater inflater, ViewGroup parent) {
            super(inflater, parent, R.layout.crime_list);
        }
    }

    private class SeriousCrimeHolder extends CrimeHolder { //chapter 8 challenge

        private Button mContactPolice;

        SeriousCrimeHolder(LayoutInflater inflater, ViewGroup parent) {
            super(inflater, parent, R.layout.serious_crime_list);
        }

        @Override
        public void bind(final Crime crime) {
            super.bind(crime);

            mContactPolice = itemView.findViewById(R.id.contact_police);
            mContactPolice.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(getActivity(), "Police contacted for " + crime.getTitle(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private class CrimeAdapter extends  RecyclerView.Adapter<CrimeHolder> {
        private List<Crime> mCrimes;

        CrimeAdapter(List<Crime> crimes) {
            mCrimes = crimes;
        }

        @NonNull
        @Override
        public CrimeHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
            return viewType == 1 ? new SeriousCrimeHolder(layoutInflater, parent)
                    : new RegularCrimeHolder(layoutInflater, parent);
        }

        @Override
        public void onBindViewHolder(@NonNull CrimeHolder holder, int position) {
            Crime crime = mCrimes.get(position);
            holder.bind(crime);
        }

        @Override
        public int getItemCount() {
            return mCrimes.size();
        }

        //Chapter's 8 challenge
        @Override
        public int getItemViewType(int position) {
            if (mCrimes.get(position).isRequirePolice()) {
                return 1;
            }
            return 0;
        }
    }
}
