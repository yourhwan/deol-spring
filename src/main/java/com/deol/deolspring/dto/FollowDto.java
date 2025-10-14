package com.deol.deolspring.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class FollowDto {
    private Integer artistId;
    private String artistName;
    private String profileImage;
}
