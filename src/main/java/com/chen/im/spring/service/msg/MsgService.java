package com.chen.im.spring.service.msg;

import com.chen.im.common.dto.SingleMessage;

import java.util.List;

public interface MsgService {
    long saveMsg(SingleMessage singleMessage);

    List<String> getMsgs(Long userId);

    long buildMsgId();
}
