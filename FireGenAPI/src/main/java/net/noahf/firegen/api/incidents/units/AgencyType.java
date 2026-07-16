package net.noahf.firegen.api.incidents.units;

import net.noahf.firegen.api.utilities.StringSelectors;

import java.util.List;

public enum AgencyType implements StringSelectors {

    FIRE,

    EMS,

    FIRE_EMS,

    EMS_BY_AIR,

    POLICE,

    SHERIFF,

    SECURITY,

    STATE_POLICE,

    MENTAL_HEALTH_ASSISTANCE,

    TRANSIT,

    MEDICAL_EXAMINER,

    PUBLIC_WORKS,

    UTILITY_COMPANY,

    NATIONAL_GUARD,

    FEDERAL_EMERGENCY_MANAGEMENT_AGENCY,

    ANIMAL_CONTROL,

    FUNERAL_SERVICE,

    TOWING,

    DISPATCH,

    OTHER;


    @Override
    public List<String> asStringSelectors() {
        return List.of(name());
    }

}
