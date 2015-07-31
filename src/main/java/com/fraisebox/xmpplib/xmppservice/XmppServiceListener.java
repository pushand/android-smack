package com.fraisebox.xmpplib.xmppservice;

import android.graphics.Bitmap;

import org.jivesoftware.smack.chat.Chat;
import org.jivesoftware.smack.packet.Message;

import java.util.List;

/**
 * Created by pushan on 19/07/15.
 */
public interface XmppServiceListener {

    void incomingMessage(Chat chat, Message message);
    void incomingImage(Bitmap bitmap, String peer);
    void buddies(List<String> buddies);
    void connectionProblem(String message, Throwable cause);
    void presenceChanged(String from, String presence, String status);
}
