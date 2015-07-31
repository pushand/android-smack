package com.fraisebox.xmpplib.xmppservice;

/**
 * Created by pushan on 19/07/15.
 */
public interface RosterCallback {

    void presenceChanged(String from, String presence, String status);

}
