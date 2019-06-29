package com.chen.im.spring.service;

import com.alibaba.fastjson.JSONObject;
import com.chen.im.common.protobuf.RequestMessageProto;
import io.netty.channel.Channel;

import java.util.Map;

public class BaseService {
    public void sendSuccess(String cmd, Long msgId, Map<String, String> param, Channel channel) {
        try {
            System.out.println("给客户端发送成功消息");
            RequestMessageProto.RequestMessage.Builder builder = RequestMessageProto.RequestMessage.newBuilder();
            builder.setCommand(cmd);
            builder.setMsgId(msgId);
            builder.setCode(200);
            builder.setParams(JSONObject.toJSONString(param));
            channel.writeAndFlush(builder.build());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendFail(String cmd, Long msgId, Map<String, String> param, Channel channel) {
        try {
            System.out.println("给客户端发送失败消息");
            RequestMessageProto.RequestMessage.Builder builder = RequestMessageProto.RequestMessage.newBuilder();
            builder.setCommand(cmd);
            builder.setMsgId(msgId);
            builder.setParams(JSONObject.toJSONString(param));
            builder.setCode(400);
            channel.writeAndFlush(builder.build());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
