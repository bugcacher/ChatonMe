package com.deathalurer.chat;

import com.quickblox.users.model.QBUser;

import java.util.ArrayList;

/**
 * Created by Abhinav Singh on 14,March,2020
 */
public class FriendList {
    private QBUser user;
    private boolean isSelected = false;


    public FriendList(QBUser user, boolean isSelected) {
        this.user = user;
        this.isSelected = isSelected;
    }

    public QBUser getUser() {
        return user;
    }

    public void setUser(QBUser user) {
        this.user = user;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }
}
