package com.careround.search.dto;

import java.util.List;

public record SearchGroupResponse(
        String type,
        List<SearchResultResponse> results
) {}
