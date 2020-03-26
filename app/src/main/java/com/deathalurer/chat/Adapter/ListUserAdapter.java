package com.deathalurer.chat.Adapter;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.deathalurer.chat.CircleTransform;
import com.deathalurer.chat.FriendList;
import com.deathalurer.chat.R;
import com.quickblox.chat.QBChatService;
import com.quickblox.content.QBContent;
import com.quickblox.content.model.QBFile;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.users.QBUsers;
import com.quickblox.users.model.QBUser;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Created by Abhinav Singh on 29,February,2020
 */
public class ListUserAdapter extends RecyclerView.Adapter<ListUserAdapter.ListUserViewHolder> {
    private Context mContext;
    private ArrayList<FriendList> mList;
    public AddUser userAdded;

    public ListUserAdapter(Context mContext, ArrayList<FriendList> mList,AddUser userAdded) {
        this.mContext = mContext;
        this.mList = mList;
        this.userAdded = userAdded;
    }


    @NonNull
    @Override
    public ListUserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        LayoutInflater inflater = LayoutInflater.from(mContext);
        view = inflater.inflate(R.layout.list_user_layout,parent,false);
        return new ListUserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ListUserViewHolder holder, final int position) {

        holder.name.setText(mList.get(position).getUser().getFullName());

        if(mList.get(position).getUser().getFileId()!=null) {
            int profileID = mList.get(position).getUser().getFileId();
            QBContent.getFile(profileID).performAsync(new QBEntityCallback<QBFile>() {
                @Override
                public void onSuccess(QBFile qbFile, Bundle bundle) {
                    String url = qbFile.getPublicUrl();
                    Glide.with(mContext).load(url).apply(RequestOptions.circleCropTransform()).
                            placeholder(R.drawable.profile_circle).into(holder.imageView);
                }

                @Override
                public void onError(QBResponseException e) {
                }
            });
        }

        holder.layout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(mList.get(position).isSelected())
                    {
                        holder.userSelected.setVisibility(View.INVISIBLE);
                        mList.get(position).setSelected(false);
                    }
                    else
                    {
                        holder.userSelected.setVisibility(View.VISIBLE);
                        mList.get(position).setSelected(true);
                    }
                    userAdded.UserSelected(mList.get(position));
                }
            });
        }

    @Override
    public int getItemCount() {
        Log.e("Adapter", "getItemCount: " +mList.size() );
        return mList.size();
    }

    class ListUserViewHolder extends RecyclerView.ViewHolder {
         TextView name;
         RelativeLayout layout;
         ImageView imageView,userSelected;
        public ListUserViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.listUserItemName);
            layout = itemView.findViewById(R.id.user_item_layout);
            imageView  = itemView.findViewById(R.id.listUserItemImage);
            userSelected = itemView.findViewById(R.id.userSelectedForGroup);
        }
    }

    public interface AddUser{
         void UserSelected(FriendList friendUser);
    }

}
