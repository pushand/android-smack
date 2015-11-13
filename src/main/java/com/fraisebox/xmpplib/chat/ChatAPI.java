package com.fraisebox.xmpplib.chat;

import android.graphics.Bitmap;

import com.fraisebox.xmpplib.database.Chats;

import java.util.ArrayList;

/**
 * Created by pushan on 18/07/15.
 *
 */
public interface ChatAPI {

    void newMessage(Chats chats);

    void newImage(String sipId, Bitmap bitmap);

    void presenceChanged(String presence);

    void statusChanged(String status);

    ArrayList<String> getSipId();
}
