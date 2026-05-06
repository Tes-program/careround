package com.careround.common.event;

/** Published on: TEAM_MEMBER_ADDED → careround.team.member-added */
public record TeamMemberAddedEvent(
        String medicalTeamId,
        String userId,
        String hospitalId
) {}
