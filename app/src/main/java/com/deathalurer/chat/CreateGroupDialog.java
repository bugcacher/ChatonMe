package com.deathalurer.chat;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;

import static androidx.constraintlayout.widget.Constraints.TAG;

/**
 * Created by Abhinav Singh on 14,March,2020
 */
public class CreateGroupDialog extends AppCompatDialogFragment {
    public EditText groupName;
    TextView cancel,create;
    GroupNameInterface groupNameInterface;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.create_group_dialog_layout,null);
        builder.setView(view)
                .setTitle("Create Group");

        groupName = view.findViewById(R.id.groupNameEt);
        cancel = view.findViewById(R.id.cancelButton);
        create =  view.findViewById(R.id.create_button);

        create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String name = groupName.getText().toString();
                Log.d(TAG, "onClick: "+ name);
                groupNameInterface.sendGroupName(name);
                dismiss();
            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });

        return builder.create();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        groupNameInterface = (CreateGroupDialog.GroupNameInterface) context;
    }

    interface GroupNameInterface{
         void sendGroupName(String name);
    }

}
