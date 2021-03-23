package com.smile.webrtc.socket;

import com.alibaba.fastjson.JSON;
import com.smile.webrtc.config.Context;
import com.smile.webrtc.model.*;

import javax.websocket.Session;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class HandleMessage {

    /**
     * 登录
     * */
    public boolean login(String username, WebSocket webSocket) throws IOException {
        boolean state = true;

        Context.webSocketSet.put(username,webSocket);
        webSocket.username = username;

        LoginResult loginResult = new LoginResult();
        loginResult.setState(state);
        Message message = new Message();
        message.setEvent("_logined");
        message.setData(loginResult);
        String msg = JSON.toJSONString(message);
        webSocket.session.getBasicRemote().sendText(msg);
        return state;
    }

    /**
     *进入房间----给进入房间者返回当前房间集合------给房间其他人发送通知有新的用户进入
     * */
    public void joinRoom(String roomId, WebSocket webSocket) throws IOException {
        webSocket.roomId = roomId;
        sendPeers(webSocket);
        sendNewPeer(roomId,webSocket.username);
    }

    /**
     * 返回当前房间集合
     * */
    public void sendPeers( WebSocket webSocket) throws IOException {
        ArrayList<String> usernames = Context.roomSet.get(webSocket.roomId);
        ArrayList<String> homePeers = new ArrayList<>();
        for (int i = 0; i < usernames.size(); i++) {
            if (!usernames.get(i).equals(webSocket.username)){
                homePeers.add(usernames.get(i));
            }
        }
        PeersResult peersResult = new PeersResult();
        peersResult.setPeers(homePeers);
        Message message = new Message();
        message.setEvent("_peers");
        message.setData(peersResult);
        String msg = JSON.toJSONString(message);
        webSocket.session.getBasicRemote().sendText(msg);
    }



    /**
     * 给房间其他人发送通知有新的用户进入
     * */
    public void sendNewPeer(String roomId, String username) throws IOException {
        NewPeerResult newPeerResult = new NewPeerResult();
        newPeerResult.setUsername(username);
        Message message = new Message();
        message.setEvent("_new_peer");
        message.setData(newPeerResult);
        String msg = JSON.toJSONString(message);
        sendGroupSendingWithoutSender(roomId,msg,username);
    }



    /**
     * 离开房间
     * */

    public void sendLeaveRoom( WebSocket webSocket) throws IOException {
        LeavePeerResult leavePeerResult  = new LeavePeerResult();
        leavePeerResult.setUsername(webSocket.username);
        Message message = new Message();
        message.setEvent("_leave_peer");
        message.setData(leavePeerResult);
        String msg = JSON.toJSONString(message);
        sendGroupSendingWithoutSender(webSocket.roomId,msg,webSocket.username);
    }

    /**
     * 单个发送消息
     * */

    public void sendMsgToReceiver(String receiver, String msg) throws IOException {
        WebSocket webSocket = Context.webSocketSet.get(receiver);
        if (webSocket != null) {
            webSocket.session.getBasicRemote().sendText(msg);
        }
    }


    /**
     * 房间内全部广播发送消息
     * */

    public void sendGroupSending(String roomId, String msg) throws IOException {
        ArrayList <String> usernames = Context.roomSet.get(roomId);
        if (usernames == null) {
            return;
        }
        for (int i = 0; i < usernames.size(); i++) {
            String username = usernames.get(i);
            sendMsgToReceiver(username, msg);
        }
    }

    /**
     * 房间内除了发送者全部广播发送消息
     * */
    public void sendGroupSendingWithoutSender(String roomId, String msg, String sender) throws IOException {
        ArrayList <String> usernames = Context.roomSet.get(roomId);
        if (usernames == null) {
            return;
        }
        for (int i = 0; i < usernames.size(); i++) {
            String username = usernames.get(i);
            if (sender != username) {
                sendMsgToReceiver(username, msg);
            }
        }
    }









}
