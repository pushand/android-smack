package com.fraisebox.xmpplib.xmppservice;

import org.jivesoftware.smack.AbstractXMPPConnection;

public interface XmppLibListener {
        void updatePresence(String presence, String status);
        AbstractXMPPConnection getConnection();
    }