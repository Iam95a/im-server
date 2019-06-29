package com.chen.im;

import com.chen.common.redis.RedisKeys;
import com.chen.im.common.dto.SingleMessage;
import com.chen.im.common.dto.User;
import com.chen.im.common.protobuf.RequestMessageProto;
import com.chen.im.common.constant.Constant;
import com.chen.im.spring.service.msg.MsgService;
import com.chen.im.spring.service.redis.RedisService;
import com.chen.im.spring.service.user.UserService;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.CharsetUtil;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author : goldgreat
 * @Description :
 * @Date :  2019/6/11 14:33
 */
@ChannelHandler.Sharable
public class ImHandler extends ChannelInboundHandlerAdapter {
    private static Map<Integer, User> onlineMap = new ConcurrentHashMap();
    private static Map<String, User> channelMap = new ConcurrentHashMap<>();

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("客户端上线");
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("客户端掉线");
        User user = channelMap.get(ctx.channel().id().asShortText());
        onlineMap.remove(user.getUserId());
        channelMap.remove(ctx.channel().id().asShortText());
    }


    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
//
//        RequestMessageProto.RequestMessage message = (RequestMessageProto.RequestMessage) msg;
//
//        System.err.println("server:" + message.getMsgId());
//        ctx.writeAndFlush(message);
                MsgService msgService = AppContext.getContext().getBean(MsgService.class);
        if (msg instanceof RequestMessageProto.RequestMessage) {
            RequestMessageProto.RequestMessage requestMessage = (RequestMessageProto.RequestMessage) msg;
            String command = requestMessage.getCommand();
            if (command.equals(Constant.CMD_LOGIN)) {
                UserService userService = AppContext.getContext().getBean(UserService.class);
                RequestMessageProto.RequestMessage.User loginUser = requestMessage.getUser();
                String nickname = loginUser.getNickname();
                String password = loginUser.getPassword();
                userService.login(nickname, password, ctx.channel(), requestMessage);

            } else if (requestMessage.getCommand().equals(Constant.CMD_SINGLE)) {
                RequestMessageProto.RequestMessage.SingleMessage singleMessage = requestMessage.getSingleMessage();
                long receiverId = singleMessage.getReceiverId();
                User receiverUser = onlineMap.get(receiverId);
                if (receiverUser != null) {
                    if (receiverUser.getChannel().isOpen()) {
                        receiverUser.getChannel().writeAndFlush(requestMessage);
                    } else {
                        msgService.saveMsg(new SingleMessage(singleMessage));
                    }
                } else {
                    msgService.saveMsg(new SingleMessage(singleMessage));
                }
            }
        } else {
            System.out.println(msg);
        }
    }

    private long getId() {
        return AppContext.getContext().getBean(RedisService.class).incr(RedisKeys.USER_ID, 1);
    }

}
