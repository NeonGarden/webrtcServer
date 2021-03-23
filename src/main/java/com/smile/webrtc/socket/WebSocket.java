package com.smile.webrtc.socket;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.gson.Gson;
import com.smile.webrtc.config.Context;
import com.smile.webrtc.model.Message;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@ServerEndpoint("/")
public class WebSocket {

        /**
         *  与某个客户端的连接对话，需要通过它来给客户端发送消息
         */
        public Session session;

        /**
         * 标识当前连接客户端的用户名
         */
        public String username = "";


        /**
         * 标识当前连接客户端的房间
         */
        public String roomId = "";


        private HandleMessage handleMessage = new HandleMessage();


        @OnOpen
        public void OnOpen(Session session){
            this.session = session;
            log.info("[WebSocket] 连接成功");
//            log.info("[WebSocket] 连接成功，当前连接人数为：={}",webSocketSet.size());
        }


        @OnClose
        public void OnClose(){
            if (this.username != null){
                try {
                    handleMessage.sendLeaveRoom(this);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                removeUserNameAtRoomSet();
                Context.webSocketSet.remove(this.username);
                this.username = "";
                this.roomId = "";

                log.info("[WebSocket] 退出成功，当前连接人数为：={}",Context.webSocketSet.size());
            }
        }

        @OnMessage
        public void OnMessage(String messageStr, Session session) throws IOException {
            log.info("[WebSocket] 收到消息：{}",messageStr);
            //判断是否需要指定发送，具体规则自定义


            Gson gson = new Gson();
            Message message = gson.fromJson(messageStr ,Message.class);
            if (message == null){
                return;
            }
            switch(message.event){
                case "_login" : {
                    Map map = (Map) message.getData();
                    String username = (String) map.get("username");
                    boolean state =  handleMessage.login(username,this);
                }
                    break;
                case "_join" : {
                    Map map = (Map) message.getData();
                    this.roomId =  (String) map.get("roomId");;
                    saveUserNameAtRoomSet(this.username,this.roomId);
                    handleMessage.joinRoom(this.roomId, this);
                }
                    break;
                case  "_offer" :{
                    handleMessage.sendMsgToReceiver(message.receiver, messageStr);
                }
                    break;
                case "_answer":{
                    handleMessage.sendMsgToReceiver(message.receiver, messageStr);
                }
                    break;
                case  "_ice_candidate": {
                    handleMessage.sendMsgToReceiver(message.receiver, messageStr);
                }
                    break;
                case "_leave": {
                    handleMessage.sendLeaveRoom(this);
                    removeUserNameAtRoomSet();
                }
                    break;
                default :
                    handleMessage.sendGroupSendingWithoutSender(this.roomId,messageStr,this.username);

                    //语句
            }
        }

        /**
         * 存放房间的用户
         * */

        private void saveUserNameAtRoomSet(String username, String roomId) {
           ArrayList<String> usernames =Context.roomSet.get(roomId);
           if (usernames == null || usernames.isEmpty()) {
               usernames = new ArrayList<>();
               usernames.add(username);
           }else {
               if (!usernames.contains(username)) {
                   usernames.add(username);
               }
           }
           System.out.println("room人数："+usernames);
            Context.roomSet.put(roomId,usernames);
        }

        private void removeUserNameAtRoomSet(){
            ArrayList<String> usernames = Context.roomSet.get(this.roomId);
            if (!(usernames == null || usernames.isEmpty())) {
                if (usernames.contains(this.username) && this.username != null) {
                    usernames.remove(this.username);
                    if (usernames.size() == 0) {
                        Context.roomSet.remove(this.roomId);
                    }else{
                        Context.roomSet.put(this.roomId,usernames);
                    }
                }
            }
        }
}
