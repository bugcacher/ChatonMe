package com.deathalurer.chat;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;
import androidx.fragment.app.DialogFragment;

/**
 * Created by Abhinav Singh on 21,March,2020
 */
public class ChooseAvatarDialog extends AppCompatDialogFragment {
private TextView uploadImage;
private ImageView boy_one,boy_two,boy_three,boy_four,boy_five,
                    girl_one,girl_two,girl_three,girl_four;
private SendAvatarInterface sendAvatarInterface;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.avatar_layout,null);

        builder.setView(view);



        uploadImage = view.findViewById(R.id.uploadImageTextView);
        boy_five = view.findViewById(R.id.boy_five);
        boy_four = view.findViewById(R.id.boy_four);
        boy_one = view.findViewById(R.id.boy_one);
        boy_two = view.findViewById(R.id.boy_two);
        boy_three = view.findViewById(R.id.boy_three);
        girl_one = view.findViewById(R.id.girl_one);
        girl_two = view.findViewById(R.id.girl_two);
        girl_three = view.findViewById(R.id.girl_three);
        girl_four = view.findViewById(R.id.girl_four);

        selectImage();

        return  builder.create();
    }

    private void selectImage() {
        uploadImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendAvatarInterface.sendAvatarResponse(false,1);
            }
        });
        boy_one.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendAvatarInterface.sendAvatarResponse(true,getResourceId("boy_one"));
            }
        });
        boy_two.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendAvatarInterface.sendAvatarResponse(true,getResourceId("boy_two"));
            }
        });
        boy_three.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendAvatarInterface.sendAvatarResponse(true,getResourceId("boy_three"));
            }
        });
        boy_four.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendAvatarInterface.sendAvatarResponse(true,getResourceId("boy_four"));
            }
        });
        boy_five.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendAvatarInterface.sendAvatarResponse(true,getResourceId("boy_five"));
            }
        });
        girl_one.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendAvatarInterface.sendAvatarResponse(true,getResourceId("girl_one"));
            }
        });
        girl_two.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendAvatarInterface.sendAvatarResponse(true,getResourceId("girl_two"));
            }
        });
        girl_three.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendAvatarInterface.sendAvatarResponse(true,getResourceId("girl_three"));
            }
        });
        girl_four.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendAvatarInterface.sendAvatarResponse(true,getResourceId("girl_four"));
            }
        });

    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        sendAvatarInterface = (SendAvatarInterface) context;
    }

    public interface SendAvatarInterface{
        void sendAvatarResponse(boolean avatar, int id);
    }

    private int getResourceId(String resourceName){
        int id = getActivity().getResources().getIdentifier(resourceName,
                "drawable", getActivity().getPackageName());
        Log.e("_______", "getResourceId: "+id + R.drawable.boy_five);
        return id;
    }
}
