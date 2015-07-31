package com.fraisebox.xmpplib.chat;

import com.fraisebox.xmpplib.xmppservice.RosterCallback;
import com.fraisebox.xmpplib.xmppservice.XmppLib;

import org.jivesoftware.smack.packet.Presence;

import java.util.Collection;

/**
 * Created by pushan on 19/07/15.
 */
public class RosterListener implements org.jivesoftware.smack.roster.RosterListener{

    private RosterCallback rosterCallback;

    public RosterListener(RosterCallback rosterCallback){
        this.rosterCallback = rosterCallback;
    }

    @Override
    public void entriesAdded(Collection<String> addresses) {

    }

    @Override
    public void entriesUpdated(Collection<String> addresses) {

    }

    @Override
    public void entriesDeleted(Collection<String> addresses) {

    }

    @Override
    public void presenceChanged(Presence presence) {
        System.out.println("Presence changed: " + presence.getFrom() + " " + presence);
        String presenceStatus = null;
        if(presence.getMode().equals(Presence.Mode.away)){
            presenceStatus = "away";
        }else if(presence.getMode().equals(Presence.Mode.dnd)){
            presenceStatus = "dnd";
        }else if(presence.getMode().equals(Presence.Mode.xa)){
            presenceStatus = "extended away";
        }else if(presence.getMode().equals(Presence.Mode.chat)){
            presenceStatus = "chat";
        }else if(presence.getType().equals(Presence.Type.unavailable)){
            presenceStatus = "Offline";
        }else{
            presenceStatus = "Online";
        }
        rosterCallback.presenceChanged(presence.getFrom(),presenceStatus,presence.getStatus());
    }


}
