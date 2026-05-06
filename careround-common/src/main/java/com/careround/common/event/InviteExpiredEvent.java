package com.careround.common.event;

/** Published on: INVITE_EXPIRED → careround.invite.expired */
public record InviteExpiredEvent(
        String inviteId,
        String medicalTeamId,
        String invitedUserId,
        String hospitalId
) {}
