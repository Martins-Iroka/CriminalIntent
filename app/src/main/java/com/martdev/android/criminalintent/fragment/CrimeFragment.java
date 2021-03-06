package com.martdev.android.criminalintent.fragment;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.ShareCompat.IntentBuilder;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.martdev.android.criminalintent.DatePickerActivity;
import com.martdev.android.criminalintent.R;
import com.martdev.android.criminalintent.model.Crime;
import com.martdev.android.criminalintent.model.CrimeLab;
import com.martdev.android.criminalintent.utils.PictureUtils;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import static android.widget.CompoundButton.*;

public class CrimeFragment extends Fragment {
    private static final String ARG_CRIME_ID = "crime_id";
    private static final String TIME_DIALOG = "DialogTime";
    private static final String IMAGE_VIEW_DIALOG = "ImageDialog";
    private static final String[] CONTACT_PERMISSION = { //chapter 15b solution
            Manifest.permission.READ_CONTACTS
    };

    private static final int REQUEST_DATE = 0;
    private static final int REQUEST_TIME = 1;
    private static final int REQUEST_CONTACT = 2;
    private static final int REQUEST_PHOTO = 3;
    private static final int REQUEST_CONTACT_PERMISSION = 3;

    private Crime mCrime;
    private File mPhotoFile;
    private Bitmap mBitmap;
    private EditText mTitleField;
    private Button mDateButton, mTimeButton, mSuspectButton, mSendReport, mCallSuspect;
    private ImageButton mPhotoButton;
    private ImageView mPhotoView;
    private CheckBox mSolvedCheckBox;
    private String mSuspectId;
    private Callbacks mCallbacks;

    public interface Callbacks {
        void onCrimeUpdated(Crime crime);
    }

    public static CrimeFragment newInstance(UUID id) {
        Bundle args = new Bundle();
        args.putSerializable(ARG_CRIME_ID, id);

        CrimeFragment fragment = new CrimeFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mCallbacks = (Callbacks) context;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        UUID crimeId = (UUID) getArguments().getSerializable(ARG_CRIME_ID);
        mCrime = CrimeLab.get(getActivity()).getCrime(crimeId);
        mPhotoFile = CrimeLab.get(getActivity()).getPhotoFile(mCrime);
    }

    @Override
    public void onPause() {
        super.onPause();

        CrimeLab.get(getActivity()).updateCrime(mCrime);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallbacks = null;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_crime, container, false);

        mTitleField = view.findViewById(R.id.crime_title);
        mTitleField.setText(mCrime.getTitle());
        mTitleField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mCrime.setTitle(s.toString());
                updateCrime();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        mDateButton = view.findViewById(R.id.crime_date);
        updateDate();
        mDateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = DatePickerActivity.newIntent(getActivity(), mCrime.getDate());
                startActivityForResult(intent, REQUEST_DATE);
            }
        });

        mTimeButton = view.findViewById(R.id.crime_time);
        updateTime();
        mTimeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentManager manager = getFragmentManager();
                TimePickerFragment pickerFragment = TimePickerFragment.newInstance(mCrime.getTime());
                pickerFragment.setTargetFragment(CrimeFragment.this, REQUEST_TIME);
                pickerFragment.show(manager, TIME_DIALOG);
            }
        });

        mSolvedCheckBox = view.findViewById(R.id.crime_solved);
        mSolvedCheckBox.setChecked(mCrime.isSolved());
        mSolvedCheckBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mCrime.setSolved(isChecked);
                updateCrime();
            }
        });

        final Intent pickContact = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
        mSuspectButton = view.findViewById(R.id.crime_suspect);
        mSuspectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityForResult(pickContact, REQUEST_CONTACT);
            }
        });

        mSendReport = view.findViewById(R.id.crime_report);
        mSendReport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //chapter 15a challenge
                Intent intent = IntentBuilder.from(getActivity()).setType("text/plain")
                        .setText(getCrimeReport())
                        .setSubject(getString(R.string.crime_report_subject))
                        .setChooserTitle(R.string.send_report)
                        .createChooserIntent();
                startActivity(intent);
            }
        });

        final Intent phoneNumber = new Intent(Intent.ACTION_DIAL);
        mCallSuspect = view.findViewById(R.id.call_suspect);
        mCallSuspect.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri suspectNumber = Uri.parse("tel:" + mCrime.getSuspectNumber());
                phoneNumber.setData(suspectNumber);
                startActivity(phoneNumber);
            }
        });

        PackageManager packageManager = getActivity().getPackageManager();
        if (packageManager.resolveActivity(pickContact, PackageManager.MATCH_DEFAULT_ONLY) == null) {
            mSuspectButton.setEnabled(false);
        }

        mPhotoButton = view.findViewById(R.id.crime_camera);
        final Intent captureImage = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        boolean canTakePhoto = mPhotoFile != null &&
                captureImage.resolveActivity(packageManager) != null;
        mPhotoButton.setEnabled(canTakePhoto);

        mPhotoButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri uri = FileProvider.getUriForFile(getActivity(),
                        "com.martdev.android.criminalintent.fileprovider",
                        mPhotoFile);
                captureImage.putExtra(MediaStore.EXTRA_OUTPUT, uri);

                List<ResolveInfo> cameraActivity = getActivity().getPackageManager()
                        .queryIntentActivities(captureImage, PackageManager.MATCH_DEFAULT_ONLY);

                for (ResolveInfo activity : cameraActivity) {
                    getActivity().grantUriPermission(activity.activityInfo.packageName,
                            uri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                }

                startActivityForResult(captureImage, REQUEST_PHOTO);
            }
        });

        mPhotoView = view.findViewById(R.id.crime_photo);
        mPhotoView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager manager = getFragmentManager();
                ImageViewDialog dialog = ImageViewDialog.newInstance(mBitmap);
                dialog.show(manager, IMAGE_VIEW_DIALOG);
            }
        });

        ViewTreeObserver observer = mPhotoView.getViewTreeObserver();
        if (observer.isAlive()) {
            observer.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    mPhotoView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    mPhotoView.getMeasuredWidth();
                    mPhotoView.getMeasuredHeight();

                    updatePhotoView();
                }
            });
        }


        updatePhotoView();

        if (mCrime.getSuspect() != null) {
            mSuspectButton.setText(mCrime.getSuspect());
        }

        return view;
    }

    private void updatePhotoView() {
        if (mPhotoFile == null || !mPhotoFile.exists()) {
            mPhotoView.setImageDrawable(null);
        } else {
            mBitmap = PictureUtils.getScaledBitmap(mPhotoFile.getPath(), getActivity());
            mPhotoView.setImageBitmap(mBitmap);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_crime_delete, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.delete_crime:
                Crime crime = CrimeLab.get(getActivity()).getCrime(mCrime.getId());
                CrimeLab.get(getActivity()).deleteCrime(crime);
                getActivity().finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK) {
            return;
        }

        if (requestCode == REQUEST_DATE) {
            Date date = DatePickerFragment.setDate(data);
            mCrime.setDate(date);
            updateCrime();
            updateDate();
        }

        else if (requestCode == REQUEST_TIME) {
            Date time = (Date) data.getSerializableExtra(TimePickerFragment.EXTRA_CRIME_TIME);
            mCrime.setTime(time);
            updateCrime();
            updateTime();
        }

        else if (requestCode == REQUEST_CONTACT && data != null) {
            Uri contactUri = data.getData();

            String[] queryField = {
                    ContactsContract.Contacts._ID,
                    ContactsContract.Contacts.DISPLAY_NAME
            };

            Cursor c = getActivity().getContentResolver().query(contactUri, queryField,
                    null, null, null);
            Log.d("contactUri Cursor dump", DatabaseUtils.dumpCursorToString(c));

            try {

                if (c.getCount() == 0) {
                    return;
                }

                c.moveToFirst();
                mSuspectId = c.getString(0);
                String suspect = c.getString(1);
                mCrime.setSuspect(suspect);
                updateCrime();
                mSuspectButton.setText(suspect);
            } finally {
                c.close();
            }

            if (hasContactPermission()) {
                updateSuspectNumber();
            } else {
                requestPermissions(CONTACT_PERMISSION, REQUEST_CONTACT_PERMISSION);
            }
        }

        else if (requestCode == REQUEST_PHOTO) {
            Uri uri = FileProvider.getUriForFile(getActivity(),
                    "com.martdev.android.criminalintent.fileprovider",
                    mPhotoFile);

            getActivity().revokeUriPermission(uri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

            updateCrime();
            updatePhotoView();
        }
    }

    private void updateCrime() {
        CrimeLab.get(getActivity()).updateCrime(mCrime);
        mCallbacks.onCrimeUpdated(mCrime);
    }
    private void updateTime() {
        mTimeButton.setText(DateFormat.getTimeInstance(DateFormat.SHORT).format(mCrime.getTime()));
    }

    private void updateDate() {
        mDateButton.setText(new SimpleDateFormat("EEEE, MMM, d, yyyy", Locale.getDefault()).format(mCrime.getDate()));
    }

    private String getCrimeReport() {
        String solvedCrime;
        if (mCrime.isSolved()) {
            solvedCrime = getString(R.string.crime_report_solved);
        } else {
            solvedCrime = getString(R.string.crime_report_unsolved);
        }

        String dateString = "EEE MMM dd";
        String dateFormat = android.text.format.DateFormat.format(dateString, mCrime.getDate()).toString();

        String suspect = mCrime.getSuspect();
        if (suspect == null) {
            suspect = getString(R.string.crime_report_no_suspect);
        } else {
            suspect = getString(R.string.crime_report_suspect, suspect);
        }

        return getString(R.string.crime_report, mCrime.getTitle(), dateFormat, solvedCrime, suspect);
    }

    private String getSuspectNumber(String contactId) {
        String suspectNumber = null;

        Uri contactUri = CommonDataKinds.Phone.CONTENT_URI;

        String[] queryFields = {
                ContactsContract.Data.CONTACT_ID,
                CommonDataKinds.Phone.NUMBER,
                CommonDataKinds.Phone.TYPE
        };

        //this indicates which row to return
        String selectionClause = ContactsContract.Data.CONTACT_ID + " = ?";

        String[] selectionArgs = { contactId };

        Cursor c = getActivity().getContentResolver().query(contactUri, queryFields, selectionClause,
                selectionArgs, null);
        Log.d("phoneUri Cursor dump", DatabaseUtils.dumpCursorToString(c));

        try {
            if (c.getCount() == 0) {
                return null;
            }

            c.moveToFirst();
            while (!c.isAfterLast()) {
                int phoneType = c.getInt(c.getColumnIndex(CommonDataKinds.Phone.TYPE));
                if (phoneType == CommonDataKinds.Phone.TYPE_MOBILE | phoneType == CommonDataKinds.Phone.TYPE_WORK) {
                    suspectNumber = c.getString(c.getColumnIndex(CommonDataKinds.Phone.DATA));
                }
                c.moveToNext();
            }
        } finally {
            c.close();
        }

        return suspectNumber;
    }

    private void updateSuspectNumber() {
        String suspectNumber = getSuspectNumber(mSuspectId);
        mCrime.setSuspectNumber(suspectNumber);
    }

    private boolean hasContactPermission() {
        int result = ContextCompat.checkSelfPermission(getActivity(), CONTACT_PERMISSION[0]);
        return result == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permission, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CONTACT_PERMISSION:
                if (hasContactPermission()) {
                    updateSuspectNumber();
                }
                default:
                    super.onRequestPermissionsResult(requestCode, permission, grantResults);
        }
    }
}
