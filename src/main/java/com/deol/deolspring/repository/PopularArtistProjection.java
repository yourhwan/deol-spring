package com.deol.deolspring.repository;

public interface PopularArtistProjection {
    String  getMemberArtistName();
    Integer getMemberArtistSeq();
    String  getProfileImageUrl();
    Long    getStreamSum();
}