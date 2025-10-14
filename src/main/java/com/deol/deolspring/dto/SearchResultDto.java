// src/main/java/com/deol/deolspring/dto/SearchResultDto.java
package com.deol.deolspring.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class SearchResultDto {

    @Schema(description = "검색된 아티스트 목록")
    private List<SearchArtistDto> artists;

    @Schema(description = "검색된 앨범 목록")
    private List<AlbumSearchDto> albums;

    @Schema(description = "검색된 트랙 목록")
    private List<TrackSearchDto> tracks;
}
