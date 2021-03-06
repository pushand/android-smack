package com.fraisebox.xmpplib.chat;

import com.fraisebox.xmpplib.database.Chats;
import com.fraisebox.xmpplib.database.DBHelper;
import com.fraisebox.xmpplib.xmppservice.XmppLib;

import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.chat.Chat;
import org.jivesoftware.smack.chat.ChatManager;
import org.jivesoftware.smack.chat.ChatMessageListener;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smackx.filetransfer.FileTransferManager;
import org.jivesoftware.smackx.filetransfer.OutgoingFileTransfer;

import java.io.File;

/**
 * Created by pushan on 18/07/15.
 *
 */
public class Friend extends Buddy{

    private String message;
    private String to;
    private File file;

    public Friend(String message, String to, ChatAPI chatAPI) {
        super(chatAPI);
        this.message = message;
        this.to = to;
    }

    public Friend(File file, String message,  String to, ChatAPI chatAPI) {
        super(chatAPI);
        this.to = to;
        this.file = file;
        this.message = message;

    }

    @Override
    public void sendMessage(AbstractXMPPConnection connection, Chats chats) {
        ChatManager chatmanager = ChatManager.getInstanceFor(connection);
        Chat newChat = chatmanager.createChat(to, new ChatMessageListener() {
            public void processMessage(Chat chat, Message message) {
                System.out.println("Received message: " + message);
                String sipId = chat.getParticipant();
                Chats chats = DBHelper.getInstance().insertMessage(sipId, message.getBody(), false);
                XmppLib.incomingMessage(chatAPI, chats);
            }
        });
        try {
            newChat.sendMessage(message);
        }
        catch (SmackException.NotConnectedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void sendFile(AbstractXMPPConnection connection) {
        FileTransferManager fileTransferManager = FileTransferManager.getInstanceFor(connection);
        OutgoingFileTransfer outgoingFileTransfer = fileTransferManager.createOutgoingFileTransfer(to);
        try {
            outgoingFileTransfer.sendFile(file,message);
        } catch (SmackException e) {
            e.printStackTrace();
        }
    }
}
