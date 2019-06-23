package com.chen.im.spring.service.msg.impl;

import com.chen.common.redis.RedisKeys;
import com.chen.im.common.dto.SingleMessage;
import com.chen.im.spring.service.msg.MsgService;
import com.chen.im.spring.service.redis.RedisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MsgServiceImpl implements MsgService {
    @Autowired
    private RedisService redisService;

    @Override
    public long saveMsg(SingleMessage singleMessage) {
        if(singleMessage.getMsgId()==null){
            singleMessage.setMsgId(buildMsgId());
        }
        long receiverId = singleMessage.getReceiverId();
        redisService.lpush(RedisKeys.MSG + receiverId, singleMessage.toJson());
        return singleMessage.getMsgId();
    }

    @Override
    public long buildMsgId() {
        return redisService.incr(RedisKeys.MSG_ID, 1);
    }

    @Override
    public List<String> getMsgs(Long userId) {
        return redisService.lrange(RedisKeys.MSG + userId, 0, -1);
    }
}
