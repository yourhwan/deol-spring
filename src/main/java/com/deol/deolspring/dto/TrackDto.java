package com.deol.deolspring.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class TrackDto {
    private Integer trackId;
    private String trackTitle; // 트랙 제목
    private MultipartFile trackFile; // 음원 파일
    private String trackLyrics; // 가사
    private String trackGenre; // 장르
    private String trackDuration; // 재생시간

}
