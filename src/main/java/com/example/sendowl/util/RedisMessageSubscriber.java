package com.example.sendowl.util;


import com.example.sendowl.entity.RedisBoard;
import com.example.sendowl.repository.RedisBoardRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.StringTokenizer;

// MessageListener를 이용하여 redis에서 pub되는 메세지는 받는다.
public class RedisMessageSubscriber implements MessageListener {

    @Autowired
    private RedisBoardRepository redisBoardRepository;

    @Override
    public void onMessage(Message message, byte[] bytes) { // Callback for processing received objects through Redis.

        String body = new String(message.getBody(), StandardCharsets.UTF_8);
        String channel = new String(message.getChannel(), StandardCharsets.UTF_8);

        if(channel.split(":")[1].matches("expired")){
            String[] bodys = body.split(":");
            String key = bodys[0];
            String tag = bodys[1];
            String id = bodys[2];

            // 만료된 경우 DB에 조회수를 증가시키는 코드 추가하기
            System.out.println("find!!!!");
            System.out.println("body:"+ body);
            System.out.println("channel:"+ channel);
            System.out.println("!!"+key + tag + id);

            // 사용완료한 데이터를 제거한다.
            Long idL = Long.parseLong(id);
            System.out.println("get:" + redisBoardRepository.findById(1L).get().toString());
            redisBoardRepository.deleteById(1L);

            return;
        }
        else{
            return;
        }
   }
}

