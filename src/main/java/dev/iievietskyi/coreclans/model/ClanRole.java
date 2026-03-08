package dev.iievietskyi.coreclans.model;

public enum ClanRole {
    LEADER,
    CO_LEADER,
    OFFICER,
    MEMBER;

    public boolean canInvite() {
        return this == LEADER || this == CO_LEADER || this == OFFICER;
    }

    public boolean canManageMembers() {
        return this == LEADER || this == CO_LEADER;
    }

    public boolean canManageWar() {
        return this == LEADER || this == CO_LEADER || this == OFFICER;
    }
}
