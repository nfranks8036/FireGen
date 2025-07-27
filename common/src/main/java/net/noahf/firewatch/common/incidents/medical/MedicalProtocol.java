package net.noahf.firewatch.common.incidents.medical;

public enum MedicalProtocol {

    ABDOMINAL_PAIN,
    ALLERGIC_REACTION,
    ANIMAL_BITE,
    ASSAULT,
    BACK_PAIN,
    DIFFICULTY_BREATHING,
    BURN_SUBJECT,
    HAZARDOUS_EXPOSURE,
    CARDIAC_ARREST,
    CHEST_PAIN,
    CHOKING,
    SEIZURES,
    DIABETIC_PROBLEMS,
    DROWNING,
    ELECTROCUTION,
    EYE_PROBLEM,
    FALLS,
    HEADACHE,
    HEART_PROBLEM,
    ENVIRONMENTAL_EXPOSURE,
    HEMORRHAGE,
    INDUSTRIAL_ACCIDENTS,
    OVERDOSE,
    PREGNANCY,
    PSYCHIATRIC_PROBLEM,
    SICK_PERSON,
    PENETRATING_TRAUMA,
    STROKE,
    MOTOR_VEHICLE_CRASH,
    TRAUMATIC_INJURY,
    SUBJECT_UNCONSCIOUS,
    UNKNOWN_MEDICAL_CALL,
    INTER_FACILITY_TRANSFER,
    AUTOMATIC_CRASH_NOTIFICATION,
    HCP_REFERRAL,
    EPIDEMIC_OR_OUTBREAK;

    private final String name;
    private final int protocol;

    MedicalProtocol() {
        this.name = this.name().toUpperCase().replace("_", " ");
        this.protocol = this.ordinal() + 1; // so it's not zero-based
    }

    public int protocol() { return this.protocol; }

    @Override
    public String toString() {
        return this.name;
    }
}
