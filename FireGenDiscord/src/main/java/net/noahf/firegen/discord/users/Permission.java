package net.noahf.firegen.discord.users;

import net.noahf.firegen.api.utilities.AutofilledCharSequence;
import org.jetbrains.annotations.NotNull;

public enum Permission implements AutofilledCharSequence {

    ADMIN,

    CHANGE_CALL_TYPE,

    CHANGE_DATE_TIME,

    CHANGE_LOCATION,

    USE_CUSTOM_LOCATION,

    CHANGE_AGENCIES,

    NARRATIVE_ADD,

    NARRATIVE_HIDE,

    INCIDENT_CREATE,

    INCIDENT_CLOSE,

    INCIDENT_REOPEN,

    INCIDENT_PUBLISH,

    INCIDENT_UNPUBLISH,

    DEFAULT;

    @Override
    @NotNull
    public String toString() {
        return this.name();
    }
}
