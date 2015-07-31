package com.fraisebox.xmpplib.chat;

import android.graphics.Bitmap;

/**
 * Created by pushan on 18/07/15.
 *
 */
public interface ChatAPI {

    void newMessage(String message);

    void newImage(Bitmap bitmap);

    void presenceChanged(String presence);

    void statusChanged(String status);
}
