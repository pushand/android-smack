package com.fraisebox.xmpplib.chat;

import java.util.List;

/**
 * Created by pushan on 18/07/15.
 */
public interface XmppListener {

    void buddies(List<String> buddies);

    void connectionFailed(String reason, Throwable cause);

}
