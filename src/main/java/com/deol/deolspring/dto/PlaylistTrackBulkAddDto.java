package com.deol.deolspring.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlaylistTrackBulkAddDto {
    private Integer playlistId;
    private List<Integer> trackIds;
}
