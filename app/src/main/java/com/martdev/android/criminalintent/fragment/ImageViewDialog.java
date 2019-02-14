package com.martdev.android.criminalintent.fragment;

import android.app.Dialog;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;

import com.martdev.android.criminalintent.R;

public class ImageViewDialog extends DialogFragment {
    private static final String ARG_PHOTO = "bitmap";

    public static ImageViewDialog newInstance(Bitmap bitmap) {
        Bundle arg = new Bundle();
        arg.putParcelable(ARG_PHOTO, bitmap);

        ImageViewDialog dialog = new ImageViewDialog();
        dialog.setArguments(arg);

        return dialog;
    }

    private ImageView mPhotoView;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Bitmap bitmap = getArguments().getParcelable(ARG_PHOTO);

        View view = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_image_view, null);

        mPhotoView = view.findViewById(R.id.zoom_image);
        mPhotoView.setImageBitmap(bitmap);

        return new AlertDialog.Builder(getActivity())
                .setView(view)
                .create();
    }
}
