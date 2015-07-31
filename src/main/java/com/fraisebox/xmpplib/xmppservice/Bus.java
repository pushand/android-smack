package com.fraisebox.xmpplib.xmppservice;


import android.graphics.Bitmap;

import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.chat.Chat;
import org.jivesoftware.smack.packet.Message;

import java.util.ArrayList;

/**
 * Created by pushan on 22/07/15.
 */
public class Bus {

    private static Bus bus;
    private XmppLibListener xmppLibListener;
    private XmppServiceListener xmppServiceListener;

    private Bus(){

    }

    public static void registerLib(XmppLibListener xmppLibListener){
        if(bus==null)bus=new Bus();
        bus.xmppLibListener = xmppLibListener;
    }
    public static void registerService(XmppServiceListener xmppServiceListener){
        if(bus==null)bus=new Bus();
        bus.xmppServiceListener = xmppServiceListener;
    }

    public static void connectionProblem(String message, Throwable cause){
        bus.xmppServiceListener.connectionProblem(message,cause);
    }

    public static void incomingMessage(Chat chat, Message message) {
        bus.xmppServiceListener.incomingMessage(chat, message);
    }

    public static void buddies(ArrayList<String> buddies) {
        bus.xmppServiceListener.buddies(buddies);
    }

    public static void presenceChanged(String from, String presence, String status) {
        bus.xmppServiceListener.presenceChanged(from, presence, status);
    }

    public static void updatePresence(String presence, String status) {
        bus.xmppLibListener.updatePresence(presence, status);
    }

    public static AbstractXMPPConnection getConnection() {
        return bus.xmppLibListener.getConnection();
    }

    public static void incomingFile(Bitmap bitmap, String peer) {
        bus.xmppServiceListener.incomingImage(bitmap, peer);
    }
}
