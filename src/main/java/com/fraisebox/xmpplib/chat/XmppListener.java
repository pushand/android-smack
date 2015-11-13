package com.fraisebox.xmpplib.chat;

import java.util.List;

/**
 * Created by pushan on 18/07/15.
 */
public interface XmppListener {

    /**
     * Fired when new connection is established
     * or
     * When new buddy list is sent from app implementing this listener
     * */
    void buddies(List<String> buddies);

    void connectionFailed(String reason, Throwable cause);

}
