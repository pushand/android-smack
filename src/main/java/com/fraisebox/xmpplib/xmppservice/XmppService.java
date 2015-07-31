package com.fraisebox.xmpplib.xmppservice;

import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.fraisebox.xmpplib.chat.ConnectionException;
import com.fraisebox.xmpplib.chat.RosterListener;

import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.SmackConfiguration;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.chat.Chat;
import org.jivesoftware.smack.chat.ChatManager;
import org.jivesoftware.smack.chat.ChatManagerListener;
import org.jivesoftware.smack.chat.ChatMessageListener;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.roster.Roster;
import org.jivesoftware.smack.roster.RosterEntry;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.jivesoftware.smackx.filetransfer.FileTransferListener;
import org.jivesoftware.smackx.filetransfer.FileTransferManager;
import org.jivesoftware.smackx.filetransfer.FileTransferRequest;
import org.jivesoftware.smackx.filetransfer.IncomingFileTransfer;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by pushan on 08/07/15.
 */
public class XmppService extends Service implements XmppLibListener, RosterCallback {

    private AbstractXMPPConnection connection;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Bus.registerLib(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(intent.getAction().equals("start") && connection == null)
            startServiceThread(intent);
        return START_REDELIVER_INTENT;
    }

    private void startServiceThread(final Intent intent) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                connect(intent);
            }
        }).start();
    }

    private void connect(Intent intent){
        SmackConfiguration.DEBUG = true;
        XMPPTCPConnectionConfiguration config = XMPPTCPConnectionConfiguration.builder()
                .setUsernameAndPassword(intent.getStringExtra("username"), intent.getStringExtra("password"))
                .setServiceName(intent.getStringExtra("serviceName"))
                .setHost(intent.getStringExtra("host"))
                .setPort(intent.getIntExtra("port", 5222))
                .build();//"ip-172-31-44-94" ////ec2-52-26-65-0.us-west-2.compute.amazonaws.com
        connection = new XMPPTCPConnection(config);
        try {
            connection.connect();
            Log.d("Server", "connection status : " + connection.isConnected());
            if(connection.isConnected()){
                connection.login();
                if(connection.isAuthenticated()) {
                    Log.d("Server", "connection status : Authenticated");
                    updatePreferences(intent);
                    setPresence("Available", true);
                    getBuddies();
                    listenIncomingChat();
                    listenForIncomingFile();
                }else{
                    throw new ConnectionException("Authentication failed");
                }
            }else{
                throw new ConnectionException("Cannot connect to server");
            }
        } catch (SmackException | IOException | XMPPException | ConnectionException e) {
            Bus.connectionProblem(e.getMessage(), e.getCause());
        }
    }

    private void updatePreferences(Intent intent) {
        getSharedPreferences("lib",MODE_PRIVATE).edit().putString("user",intent.toUri(0)).apply();
    }

    private void setPresence(String status, boolean available) {
        // Create a new presence. Pass in false to indicate we're unavailable._
        Presence presence = new Presence(available ? Presence.Type.available : Presence.Type.unavailable);
        if(status!=null)presence.setStatus(status);
        try {
            connection.sendStanza(presence);
        } catch (SmackException.NotConnectedException e) {
            e.printStackTrace();
        }
    }

    private void getBuddies(){
        ArrayList<String> buddies;
        try {
            Thread.sleep(3000);
            buddies = new ArrayList<>();
            Roster roster = Roster.getInstanceFor(connection);
            Collection<RosterEntry> entries = roster.getEntries();
            for (RosterEntry entry : entries) {
                buddies.add(entry.getUser());
            }
            roster.addRosterListener(new RosterListener(this));
            Bus.buddies(buddies);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

   private void listenIncomingChat(){
       try {
           Thread.sleep(3000);
       } catch (InterruptedException e) {
           e.printStackTrace();
       }
       ChatManager chatManager = ChatManager.getInstanceFor(connection);
        chatManager.addChatListener(
                new ChatManagerListener() {
                    @Override
                    public void chatCreated(Chat chat, boolean createdLocally) {
                        if (!createdLocally)
                            chat.addMessageListener(new ChatMessageListener() {
                                @Override
                                public void processMessage(Chat chat, Message message) {
                                    Bus.incomingMessage(chat, message);
                                }
                            });
                    }
                });
    }

    private void listenForIncomingFile(){
        FileTransferManager fileTransferManager = FileTransferManager.getInstanceFor(connection);
        fileTransferManager.addFileTransferListener(new FileTransferListener() {
            @Override
            public void fileTransferRequest(FileTransferRequest request) {
                if(request.getMimeType().equals("png")) {
                    IncomingFileTransfer accept = request.accept();
                    try {
                        Bus.incomingFile(readStream(accept.recieveFile()), accept.getPeer());
                    } catch (SmackException e) {
                        e.printStackTrace();
                    } catch (XMPPException.XMPPErrorException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }


    @Override
    public void presenceChanged(String from, String presence, String status) {
        Bus.presenceChanged(from, presence, status);
    }

    @Override
    public void updatePresence(String presenceStatus, String status) {
        Presence presence = new Presence(Presence.Type.available);
        if(status!=null)presence.setStatus(status);
        try {
            connection.sendStanza(presence);
        } catch (SmackException.NotConnectedException e) {
            e.printStackTrace();
        }
        if(presence.equals("away")){
            presence.setMode(Presence.Mode.away);
        }else if(presence.equals("dnd")){
            presence.setMode(Presence.Mode.dnd);
        }else if(presence.equals("xa")){
            presence.setMode(Presence.Mode.xa);
        }else if(presence.equals("chat")){
            presence.setMode(Presence.Mode.chat);
        }else if(presence.equals("unavailable")){
            presence.setType(Presence.Type.unavailable);
        }if(status!=null)presence.setStatus(status);
        try {
            connection.sendStanza(presence);
        } catch (SmackException.NotConnectedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public AbstractXMPPConnection getConnection() {
        return connection;
    }

    private Bitmap readStream(InputStream inputStream) {
        if(inputStream!=null) {
           return BitmapFactory.decodeStream(inputStream);
        } else{
            return null;
        }
    }

    }
