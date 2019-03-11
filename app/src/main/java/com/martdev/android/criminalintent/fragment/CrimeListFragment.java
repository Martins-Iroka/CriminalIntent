package com.martdev.android.criminalintent.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.martdev.android.criminalintent.R;
import com.martdev.android.criminalintent.model.Crime;
import com.martdev.android.criminalintent.model.CrimeLab;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class CrimeListFragment extends Fragment {
    private static final String SAVE_SUBTITLE_VISIBILITY = "subtitle";

    private RecyclerView mCrimeRecyclerView;
    private CrimeAdapter mAdapter;
    private int mAdapterClickedPosition = -1; //chapter 10a challenge
    private boolean mSubtitleVisible;
    private LinearLayout mEmptyLayout;
    private Button mAddCrimeButton;
    private Callbacks mCallbacks;
    private DeleteCrimeCallback mDeleteCrime;

    public interface Callbacks {
        void onCrimeSelected(Crime crime);
    }

    public interface DeleteCrimeCallback {
        void onDeleteCrime(UUID crimeId);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mCallbacks = (Callbacks) context;
        mDeleteCrime = (DeleteCrimeCallback) context;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_crime_list, container, false);

        mCrimeRecyclerView = v.findViewById(R.id.crime_recycler_view);
        mCrimeRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        setCrimeRecyclerViewItemTouchListener();

        mEmptyLayout = v.findViewById(R.id.layout);
        mAddCrimeButton = v.findViewById(R.id.add_crime);
        mAddCrimeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Crime crime = new Crime();
                CrimeLab.get(getActivity()).addCrime(crime);
                mCallbacks.onCrimeSelected(crime);
            }
        });

        if (savedInstanceState != null) {
            mSubtitleVisible = savedInstanceState.getBoolean(SAVE_SUBTITLE_VISIBILITY);
        }

        updateUI();
        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        updateUI();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(SAVE_SUBTITLE_VISIBILITY, mSubtitleVisible);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallbacks = null;
        mDeleteCrime = null;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_crime_list, menu);

        MenuItem subtitle = menu.findItem(R.id.show_subtitle);
        if (mSubtitleVisible) {
            subtitle.setTitle(R.string.hide_subtitle);
        } else {
            subtitle.setTitle(R.string.show_subtitle);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.new_crime:
                Crime crime = new Crime();
                CrimeLab.get(getActivity()).addCrime(crime);
                mCallbacks.onCrimeSelected(crime);
                return true;
            case R.id.show_subtitle:
                mSubtitleVisible = !mSubtitleVisible;
                getActivity().invalidateOptionsMenu();
                updateSubtitle();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void updateSubtitle() {
        CrimeLab crimeLab = CrimeLab.get(getActivity());
        int crimeCount = crimeLab.getCrimes().size();
        String subtitle = getResources().getQuantityString(R.plurals.subtitle_plural, crimeCount, crimeCount);

        if (!mSubtitleVisible) {
            subtitle = null;
        }

        AppCompatActivity activity = (AppCompatActivity) getActivity();
        activity.getSupportActionBar().setSubtitle(subtitle);
    }

    public void updateUI() {
        CrimeLab crimeLab = CrimeLab.get(getActivity());
        List<Crime> crimes = crimeLab.getCrimes();

        mEmptyLayout.setVisibility((crimes.size() > 0 ? View.GONE : View.VISIBLE));

        if (mAdapter == null) {
            mAdapter = new CrimeAdapter(crimes);
            mCrimeRecyclerView.setAdapter(mAdapter);
        } else {
            if (mAdapterClickedPosition < 0) { //chapter 10a challenge
                mAdapter.setCrimes(crimes);
                mAdapter.notifyDataSetChanged();
            } else {
                //chapter 10a challenge
                mAdapter.setCrimes(crimes);
                mAdapter.notifyItemChanged(mAdapterClickedPosition); //chapter 10a challenge
                mAdapterClickedPosition = -1;
            }
        }

        updateSubtitle();
    }

    private void setCrimeRecyclerViewItemTouchListener() {
        ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper
                .SimpleCallback(0, ItemTouchHelper.LEFT|ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                Crime crime = mAdapter.mCrimes.get(position);
                mAdapter.mCrimes.remove(position);
                mAdapter.notifyItemRemoved(position);
                mDeleteCrime.onDeleteCrime(crime.getId());
            }
        };

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
        itemTouchHelper.attachToRecyclerView(mCrimeRecyclerView);
    }

    public void deleteCrime(UUID crimeId) {
        Crime crime = CrimeLab.get(getActivity()).getCrime(crimeId);
        CrimeLab.get(getActivity()).deleteCrime(crime);
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
            mCallbacks.onCrimeSelected(mCrime);
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

    public class CrimeAdapter extends RecyclerView.Adapter<CrimeHolder> {
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

        private void setCrimes(List<Crime> crimes) {
            mCrimes = crimes;
        }
    }
}
