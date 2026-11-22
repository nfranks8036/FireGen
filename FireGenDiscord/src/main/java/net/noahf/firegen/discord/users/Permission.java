package net.noahf.firegen.discord.users;

import net.noahf.firegen.api.AutofilledCharSequence;
import org.jetbrains.annotations.NotNull;

public enum Permission implements AutofilledCharSequence {

    ADMIN,

    RELOAD_CONFIG,

    CHANGE_CALL_TYPE,

    CHANGE_DATE_TIME,

    CHANGE_LOCATION,

    CHANGE_CROSS_STREETS,

    USE_CUSTOM_LOCATION,

    CHANGE_TAGS,

    CHANGE_UNITS,

    ASSOCIATE_LINKS,

    NARRATIVE_ADD,

    NARRATIVE_HIDE,

    INCIDENT_CREATE,

    INCIDENT_STATUS,

    INCIDENT_PUBLISH,

    DEFAULT;

    @Override
    @NotNull
    public String toString() {
        return this.name();
    }
}
