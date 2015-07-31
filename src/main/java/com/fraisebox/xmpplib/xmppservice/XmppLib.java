package com.fraisebox.xmpplib.xmppservice;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;

import com.fraisebox.xmpplib.chat.Buddy;
import com.fraisebox.xmpplib.chat.XmppListener;
import com.fraisebox.xmpplib.chat.ChatAPI;
import com.fraisebox.xmpplib.chat.Friend;
import com.fraisebox.xmpplib.chat.RosterListener;

import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.chat.Chat;
import org.jivesoftware.smack.packet.Message;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by pushan on 09/07/15.
 *
 */
public class XmppLib implements XmppServiceListener{

    private static XmppLib xmppLib;
    private final String username;
    private final String password;
    private final String serviceName;
    private final String host;
    private final int port;
    private List<String> buddies;
    private List<? extends ChatAPI> buddyList;
    private XmppListener xmppListener;

    private XmppLib(XmppLibBuilder builder){
        this.username = builder.username;
        this.password = builder.password;
        this.serviceName = builder.serviceName;
        this.host = builder.host;
        this.port = builder.port;
    }

    private static boolean isAuthorized() {
        return xmppLib!=null;
    }

    public static boolean isConnnected(){
        return isAuthorized() && Bus.getConnection().isConnected();
    }

    public static boolean isAuthenticated(){
        return isAuthorized() && Bus.getConnection().isAuthenticated();
    }

    public static <T extends ChatAPI> void sendMessage(String message, String to, T t){
        if(isAuthorized()) {
            Buddy buddy = new Friend(message, to, t);
            buddy.sendMessage(Bus.getConnection());
        }
    }

    public static <T extends ChatAPI> void sendFile(File file, String message, String to, T t){
        if(isAuthorized()) {
            Buddy buddy = new Friend(file, message, to, t);
            buddy.sendFile(Bus.getConnection());
        }
    }

    public static<T extends ChatAPI> void register(List<T> buddyList){
        if(isAuthorized() && buddyList!=null) {
            xmppLib.buddyList = buddyList;
        }
    }

    public static void updatePresence(String presence, String status){
        Bus.updatePresence(presence, status);
    }



    public int getBuddy(String participant){
        participant = participant.substring(0,participant.indexOf("/"));
        return buddies.indexOf(participant);
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getServiceName() {
        return serviceName;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public List<? extends ChatAPI> getBuddyList(){
        return buddyList;
    }

    @Override
    public void incomingMessage(Chat chat, Message message) {
        int index = getBuddy(chat.getParticipant());
        if (index != -1) {
            String msg = message.getBody();
            if(!TextUtils.isEmpty(msg))
                buddyList.get(index).newMessage(msg);
        }
    }

    @Override
    public void incomingImage(Bitmap bitmap, String peer) {
        int index = getBuddy(peer);
        if (index != -1) {
            if(bitmap != null)
                buddyList.get(index).newImage(bitmap);
        }
    }

    @Override
    public void buddies(List<String> buddies) {
        this.buddies = buddies;
        xmppListener.buddies(buddies);
    }

    @Override
    public void connectionProblem(String message, Throwable cause) {
        xmppLib.xmppListener.connectionFailed(message,cause);
    }

    @Override
    public void presenceChanged(String from, String presence, String status) {
        int index = xmppLib.getBuddy(from);
        if(index!=-1){
            ChatAPI chatAPI = xmppLib.getBuddyList().get(index);
            chatAPI.presenceChanged(presence);
            chatAPI.statusChanged(status);
        }
    }

    public static class XmppLibBuilder{
        private String username;
        private String password;
        private String serviceName;
        private String host;
        private int port;

        public XmppLibBuilder username(String username){
            this.username = username;
            return this;
        }
        public XmppLibBuilder password(String password){
            this.password = password;
            return this;
        }
        public XmppLibBuilder serviceName(String serviceName){
            this.serviceName = serviceName;
            return this;
        }
        public XmppLibBuilder host(String host){
            this.host = host;
            return this;
        }
        public XmppLibBuilder port(int port){
            this.port = port;
            return this;
        }
        public void build(final XmppListener xmppListener){
            Context context = (Context) xmppListener;
            xmppLib =  new XmppLib(this);
            xmppLib.xmppListener = xmppListener;
            Intent intent = new Intent(context.getApplicationContext(),XmppService.class);
            intent.putExtra("username",xmppLib.getUsername());
            intent.putExtra("password",xmppLib.getPassword());
            intent.putExtra("host",xmppLib.getHost());
            intent.putExtra("serviceName", xmppLib.getServiceName());
            intent.putExtra("port", xmppLib.getPort());
            intent.setAction("start");
            Bus.registerService(xmppLib);
            context.startService(intent);

        }
    }
}
