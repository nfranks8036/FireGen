package net.noahf.firegen.discord.users;

import net.noahf.firegen.api.utilities.AutofilledCharSequence;
import org.jetbrains.annotations.NotNull;

public enum Permission implements AutofilledCharSequence {

    ADMIN,

    RELOAD_CONFIG,

    CHANGE_CALL_TYPE,

    CHANGE_DATE_TIME,

    CHANGE_LOCATION,

    USE_CUSTOM_LOCATION,

    CHANGE_UNITS,

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
