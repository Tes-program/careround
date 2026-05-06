package com.careround.common.event;

/** Published on: TEAM_INVITE_SENT → careround.team.invite-sent */
public record TeamInviteSentEvent(
        String inviteId,
        String medicalTeamId,
        String invitedUserId,
        String invitedById,
        String hospitalId
) {}
