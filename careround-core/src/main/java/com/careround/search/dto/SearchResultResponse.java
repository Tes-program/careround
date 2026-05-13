package com.careround.search.dto;

public record SearchResultResponse(
        String type,
        String id,
        String title,
        String subtitle,
        String routeTarget
) {}
