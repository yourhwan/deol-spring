// src/main/java/com/deol/deolspring/controller/SearchController.java
package com.deol.deolspring.controller;

import com.deol.deolspring.dto.SearchResultDto;
import com.deol.deolspring.service.SearchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:3000") // React 앱의 주소에 맞춰 수정
@RequiredArgsConstructor
@Tag(name = "Search", description = "통합 검색 API")
public class SearchController {

    private final SearchService searchService;

    /**
     * GET /api/search?query=키워드
     *
     * @param query - 검색 키워드
     * @return SearchResultDto (아티스트, 앨범, 트랙 결과)
     */
    @Operation(
            summary = "검색",
            description = "주어진 키워드로 아티스트, 앨범, 트랙을 통합 검색하여 결과를 반환합니다."
    )
    @GetMapping("/search")
    public SearchResultDto searchAll(
            @Parameter(description = "검색할 키워드", example = "V")
            @RequestParam("query") String query
    ) {
        return searchService.searchByKeyword(query);
    }
}
