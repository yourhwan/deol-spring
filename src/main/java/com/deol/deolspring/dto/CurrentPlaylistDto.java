package com.deol.deolspring.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor  // ✅ 기본 생성자 추가
@AllArgsConstructor // ✅ 기존 생성자도 그대로 유지
public class CurrentPlaylistDto {
    private Integer currentPlaylistId;
    private List<TrackDetailDto> tracks;  // 현재 재생목록에 포함된 트랙들
    private Integer memberSeq;  // 추가: 현재 재생목록을 가진 유저의 memberSeq

}

