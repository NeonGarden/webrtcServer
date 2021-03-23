package com.smile.webrtc.config;


import com.smile.webrtc.socket.WebSocket;

import javax.websocket.Session;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

public class Context {
    /**
     *  用于存所有的连接服务的客户端，这个对象存储是安全的
     */
    public static ConcurrentHashMap<String, WebSocket> webSocketSet = new ConcurrentHashMap<>();

    public static ConcurrentHashMap<String, ArrayList<String>> roomSet = new ConcurrentHashMap<>();

    public static ConcurrentHashMap<String, WebSocket> getWebSocketSet() {
        return webSocketSet;
    }

    public static void setWebSocketSet(ConcurrentHashMap<String, WebSocket> webSocketSet) {
        Context.webSocketSet = webSocketSet;
    }

    public static ConcurrentHashMap<String, ArrayList<String>> getRoomSet() {
        return roomSet;
    }

    public static void setRoomSet(ConcurrentHashMap<String, ArrayList<String>> roomSet) {
        Context.roomSet = roomSet;
    }
}
