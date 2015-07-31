package com.fraisebox.xmpplib.chat;

import org.jivesoftware.smack.AbstractXMPPConnection;

/**
 * Created by pushan on 18/07/15.
 *
 */
public abstract class Buddy {

    protected ChatAPI chatAPI;

    protected Buddy(ChatAPI chatAPI){
        this.chatAPI = chatAPI;
    }

    public abstract void sendMessage(AbstractXMPPConnection connection);

    public abstract void sendFile(AbstractXMPPConnection connection);
}
