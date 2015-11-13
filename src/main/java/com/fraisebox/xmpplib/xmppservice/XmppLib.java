package com.fraisebox.xmpplib.xmppservice;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.text.TextUtils;

import com.fraisebox.xmpplib.chat.Buddy;
import com.fraisebox.xmpplib.chat.ChatAPI;
import com.fraisebox.xmpplib.chat.ChatNotificationHelper;
import com.fraisebox.xmpplib.chat.Friend;
import com.fraisebox.xmpplib.chat.XmppListener;
import com.fraisebox.xmpplib.database.Chats;
import com.fraisebox.xmpplib.database.DBHelper;

import org.jivesoftware.smack.chat.Chat;
import org.jivesoftware.smack.packet.Message;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by pushan on 09/07/15.
 */
public class XmppLib implements XmppServiceListener {

    private static XmppLib xmppLib;
    private final String username;
    private final String password;
    private final String serviceName;
    private final String host;
    private final int port;
    private List<String> buddies;
    private List<? extends ChatAPI> buddyList;
    private XmppListener xmppListener;
    private Context context;
    private String sipUser;
    private String appPackageName;
    private Intent xmppServiceIntent;

    private XmppLib(XmppLibBuilder builder) {
        this.username = builder.username;
        this.password = builder.password;
        this.serviceName = builder.serviceName;
        this.host = builder.host;
        this.port = builder.port;
        this.appPackageName = builder.packageName;
        Bus.registerService(this);
    }

    public void setXmppServiceIntent(Intent xmppServiceIntent) {
        this.xmppServiceIntent = xmppServiceIntent;
    }

    private static boolean isAuthorized() {
        return xmppLib != null;
    }

    public static boolean isConnnected() {
        return isAuthorized() && Bus.getConnection().isConnected();
    }

    public static boolean isAuthenticated() {
        return isAuthorized() && Bus.getConnection().isAuthenticated();
    }

    public static void sendMessage(String message, String to) {
        ChatAPI chatAPI = xmppLib.getBuddy(to);
        if (isAuthorized() && chatAPI != null) {
            Chats chats = DBHelper.getInstance().insertMessage(to, message, true);
            Buddy buddy = new Friend(message, to, chatAPI);
            buddy.sendMessage(Bus.getConnection(), chats);
        }
    }

    public static void sendFile(File file, String message, String to) {
        ChatAPI chatAPI = xmppLib.getBuddy(to);
        if (isAuthorized() && chatAPI != null) {
            Buddy buddy = new Friend(file, message, to, chatAPI);
            buddy.sendFile(Bus.getConnection());
        }
    }

    public static <T extends ChatAPI> void register(List<T> buddyList) {
        if (isAuthorized() && buddyList != null) {
            xmppLib.buddyList = buddyList;
        }
    }

    public static void updatePresence(String presence, String status) {
        Bus.updatePresence(presence, status);
    }


    public ChatAPI getBuddy(String participant) {
        if (buddyList != null) {
            for (ChatAPI chatAPI : buddyList) {
                ArrayList<String> sipId = chatAPI.getSipId();
                for (String sip : sipId) {
                    if (sip.equals(participant)) {
                        return chatAPI;
                    }
                }
            }
        }
        return null;
    }

    public String getUsername() {
        return username;
    }

    public String getAppPackageName() {
        return appPackageName;
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

    public List<? extends ChatAPI> getBuddyList() {
        return buddyList;
    }

    @Override
    public void incomingMessage(Chat chat, Message message) {
        ChatAPI chatAPI = getBuddy(chat.getParticipant().substring(0, chat.getParticipant().indexOf("/")));
        String msg = message.getBody();
        if (!TextUtils.isEmpty(msg)) {
            String sipId = chat.getParticipant().substring(0, chat.getParticipant().indexOf("/"));
            Chats chats = DBHelper.getInstance().insertMessage(sipId, msg, false);
            incomingMessage(chatAPI, chats);
        }
    }

    public static void incomingMessage(ChatAPI chatAPI, Chats chats) {
        if (chatAPI != null && xmppLib.sipUser != null && xmppLib.sipUser.equals(chats.getSipId())) {
            chatAPI.newMessage(chats);
        } else {
            ChatNotificationHelper.newMessageNotificaiton(xmppLib.context,
                    xmppLib.appPackageName, chats.getSipId(), chats.getMessage());
        }
    }

    @Override
    public void incomingImage(Bitmap bitmap, String peer) {
        ChatAPI chatAPI = getBuddy(peer.substring(0, peer.indexOf("/")));
        if (chatAPI != null) {
            if (bitmap != null)
                chatAPI.newImage(peer, bitmap);
        }
    }

    @Override
    public void buddies(List<String> buddies) {
        this.buddies = buddies;
        if (xmppListener != null) {
            xmppListener.buddies(buddies);
        }
    }

    @Override
    public void connectionProblem(String message, Throwable cause) {
        if (xmppListener != null) {
            xmppListener.connectionFailed(message, cause);
        }
    }

    @Override
    public void presenceChanged(String from, String presence, String status) {
        ChatAPI chatAPI = xmppLib.getBuddy(from.substring(0, from.indexOf("/")));
        if (chatAPI != null) {
            chatAPI.presenceChanged(presence);
            chatAPI.statusChanged(status);
        }
    }

    @Override
    public void context(Context context) {
        this.context = context;
    }

    public static void appInBackground(String sipUser) {
        xmppLib.sipUser = sipUser;
    }

    public static void stopXmpp() {
        xmppLib.stopService();
    }

    public static void clearNotification(String sipUserId) {
        ChatNotificationHelper.removeUnreadChats(sipUserId);
    }

    public static class XmppLibBuilder {
        private String username;
        private String password;
        private String serviceName;
        private String host;
        private String packageName;
        private Context context;
        private int port;


        public XmppLibBuilder packageName(String packageName) {
            this.packageName = packageName;
            return this;
        }

        public XmppLibBuilder context(Context context) {
            this.context = context;
            return this;
        }

        public XmppLibBuilder username(String username) {
            this.username = username;
            return this;
        }

        public XmppLibBuilder password(String password) {
            this.password = password;
            return this;
        }

        public XmppLibBuilder serviceName(String serviceName) {
            this.serviceName = serviceName;
            return this;
        }

        public XmppLibBuilder host(String host) {
            this.host = host;
            return this;
        }

        public XmppLibBuilder port(int port) {
            this.port = port;
            return this;
        }

        public void build(final XmppListener xmppListener) {
            if (xmppLib == null) {
                xmppLib = new XmppLib(this);
                if (xmppListener != null) {
                    xmppLib.xmppListener = xmppListener;
                    xmppLib.startService(context);
                }

            } else {
                xmppLib.xmppListener = xmppListener;
                xmppLib.xmppListener.buddies(xmppLib.buddies);
            }
        }

        public void build(final Intent intent) {
            xmppLib = new XmppLib(this);
            xmppLib.setXmppServiceIntent(intent);

        }
    }

    private void startService(Context context) {
        xmppServiceIntent = new Intent(context, XmppService.class);
        xmppServiceIntent.putExtra("username", getUsername());
        xmppServiceIntent.putExtra("password", getPassword());
        xmppServiceIntent.putExtra("host", getHost());
        xmppServiceIntent.putExtra("serviceName", getServiceName());
        xmppServiceIntent.putExtra("port", getPort());
        xmppServiceIntent.putExtra("packageName", getAppPackageName());
        xmppServiceIntent.setAction("start");
        context.startService(xmppServiceIntent);
    }

    private void stopService() {
        context.stopService(xmppServiceIntent);
        xmppLib = null;
    }
}
