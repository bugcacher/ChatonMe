package com.deathalurer.chat;

import com.quickblox.chat.model.QBChatMessage;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Abhinav Singh on 05,March,2020
 */
public class QBChatHolder {
    private static QBChatHolder instance;
    private HashMap<String, ArrayList<QBChatMessage>> qbChatMessageArray;

    public static  synchronized QBChatHolder getInstance(){
        QBChatHolder qbChatHolder;
        synchronized (QBChatHolder.class){
            if(instance==null)
                instance = new QBChatHolder();
            qbChatHolder = instance;
        }
        return instance;
    }

    private QBChatHolder(){
        this.qbChatMessageArray = new HashMap<>();
    }

    public  void putMessages(String dialogId,ArrayList<QBChatMessage> qbChatMessages){
        this.qbChatMessageArray.put(dialogId, qbChatMessages);
    }

    public void putMessage(String dialogId,QBChatMessage qbChatMessage){
        ArrayList<QBChatMessage> list = this.qbChatMessageArray.get(dialogId);
        list.add(qbChatMessage);
        ArrayList<QBChatMessage> newList = new ArrayList<>(list.size());
        newList.addAll(list);
        putMessages(dialogId,newList);
    }

    public ArrayList<QBChatMessage> getChatMessageByDialogId(String dialogId){
        return (ArrayList<QBChatMessage>) this.qbChatMessageArray.get(dialogId);
    }
}
