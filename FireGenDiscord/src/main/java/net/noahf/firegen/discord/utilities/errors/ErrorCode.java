package net.noahf.firegen.discord.utilities.errors;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter @AllArgsConstructor
public enum ErrorCode {

    NO_PERMISSION("The signed in user does not have permission to perform the action."),

    NOT_CURRENTLY_EDITING("The signed in user is not currently in editing mode for their target."),

    UNIT_ATTEMPT_SET_ZERO_UNITS("The signed in user attempted to set the unit status of zero units."),

    UNIT_ALREADY_ASSIGNED_TO_OTHER_INCIDENT("The requested unit is already attached to another incident."),

    UNIT_HAS_CONTEXT_PREVENT_ADD("The requested unit cannot be added to incidents because it has additional requirements."),

    INCIDENT_TYPE_NOT_VALID("The written incident type is not a valid incident type."),

    NARRATIVE_ID_NOT_EXIST("The narrative entry with ID requested does not exist.");


    private final String briefExplanation;

}
