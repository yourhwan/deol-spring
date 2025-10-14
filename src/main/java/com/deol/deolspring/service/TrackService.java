package com.deol.deolspring.service;

import com.deol.deolspring.repository.TrackRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TrackService {

    private final TrackRepository trackRepository;

    // 트랙 재생수 증가
    @Transactional
    public void incrementStreamCount(Integer trackId) {
        trackRepository.incrementStreamCount(trackId);
    }
}

