package com.deathalurer.chat;

import android.app.Activity;
import android.app.Dialog;
import android.view.Window;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.DrawableImageViewTarget;

/**
 * Created by Abhinav Singh on 09,March,2020
 */
public class CustomDialogUpload {
    Activity activity;
    Dialog dialog;

    public CustomDialogUpload(Activity activity) {
        this.activity = activity;
    }

    public void showDialog(){
        dialog = new Dialog(activity);
        dialog.setCancelable(false);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.custom_dialog_loader);

        ImageView imageView = dialog.findViewById(R.id.custom_loading_imageView);
        Glide.with(activity)
                .load(R.drawable.loading)
                .placeholder(R.drawable.upload_loader)
                .centerCrop()
                .into(new DrawableImageViewTarget(imageView));

        dialog.show();
    }
    public void  hideDialog(){
        dialog.dismiss();
    }
}
