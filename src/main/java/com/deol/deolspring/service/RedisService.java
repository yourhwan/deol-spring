package com.deol.deolspring.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class RedisService {

    private final StringRedisTemplate stringRedisTemplate;

    // key로 Redis에 저장된 데이터 가져오는 메소드
    public String getData(String key) {
        return stringRedisTemplate.opsForValue().get(key);
    }

    // key-value를 제한시간을 두어 저장하는 메소드
    public void setDataExpire(String key, String value, long duration) {
        stringRedisTemplate.opsForValue().set(key, value, Duration.ofSeconds(duration));
    }

    // key에 해당하는 데이터를 가져오고 그 데이터가 value와 일치하는지 확인, 일치하면 redis에서 해당 key-value 쌍을 삭제하고 true 반환
    public boolean checkData(String key, String value) {
        String data = getData(key);
        if (data != null && data.equals(value)) {
            deleteData(key);
            return true;
        }
        return false;
    }

    // redis에서 key 값에 해당하는 데이터를 삭제
    public void deleteData(String key) {
        stringRedisTemplate.delete(key);
    }
}

