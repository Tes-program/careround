package com.careround.ai.dto;

public record ClinicalNoteContent(
        String subjective,
        String objective,
        String assessment,
        String plan
) {}
