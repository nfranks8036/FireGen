package net.noahf.firewatch.common.incidents.medical;

public enum MedicalProtocol {

    ABDOMINAL_PAIN ("Abdominal Pain / Problems"),
    ALLERGIC_REACTION ("Allergic Reaction / Envenomations"),
    ANIMAL_BITE ("Animal Bite / Attack"),
    ASSAULT ("Assault / Sexual Assault / Stun Gun"),
    BACK_PAIN ("Back Pain"),
    BREATHING_PROBLEM ("Breathing Problem"),
    BURN_SUBJECT ("Burns / Explosions"),
    HAZARDOUS_EXPOSURE ("Hazardous Exposure"),
    CARDIAC_ARREST ("Cardiac or Respiratory Arrest"),
    CHEST_PAIN ("Chest Pain"),
    CHOKING ("Choking"),
    SEIZURES ("Convulsions / Seizures"),
    DIABETIC_PROBLEMS ("Diabetic Problems"),
    DROWNING ("Drowning"),
    ELECTROCUTION ("Electrocution / Lightning"),
    EYE_PROBLEM ("Eye Problems"),
    FALLS ("Falls"),
    HEADACHE ("Headache"),
    HEART_PROBLEM ("Heart Problems"),
    ENVIRONMENTAL_EXPOSURE ("Heat / Cold Exposure"),
    HEMORRHAGE ("Hemorrhage / Lacerations"),
    ENTRAPMENTS ("Entrapments"),
    OVERDOSE ("Overdose / Poisoning"),
    PREGNANCY ("Pregnancy / Childbirth / Miscarriage"),
    PSYCHIATRIC_PROBLEM ("Psychiatric / Suicide Attempt"),
    SICK_PERSON ("Sick Person"),
    PENETRATING_TRAUMA ("Stab / Gunshot / Penetrating Trauma"),
    STROKE ("Stroke / Transient Ischemic Attack"),
    MOTOR_VEHICLE_CRASH ("Motor Vehicle Crash"),
    TRAUMATIC_INJURY ("Traumatic Injury"),
    SUBJECT_UNCONSCIOUS ("Unconscious / Fainting"),
    UNKNOWN_MEDICAL_PROBLEM ("Unknown Medical Problem"),
    INTER_FACILITY_TRANSFER ("Inter-Facility Transfer / Palliative Care"),
    AUTOMATIC_CRASH_NOTIFICATION ("Automatic Crash Notification"),
    HCP_REFERRAL ("HCP Referral (UK)"),
    OUTBREAK ("Pandemic / Epidemic / Outbreak");

    private final String name;
    private final String description;
    private final int protocol;

    MedicalProtocol(String description) {
        this.name = this.name().toUpperCase().replace("_", " ");
        this.description = description;
        this.protocol = this.ordinal() + 1; // so it's not zero-based
    }

    public int protocol() { return this.protocol; }

    public String description() { return this.description; }

    @Override
    public String toString() {
        return this.name;
    }
}
