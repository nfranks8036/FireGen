package net.noahf.firewatch.common.incidents;

public enum CallerType {

    INDIVIDUAL,

    PASSERBY,

    LAW_ENFORCEMENT,

    ALARM_COMPANY,

    SEE_NARRATIVE;

    @Override
    public String toString() {
        return this.name().replace("_", " ");
    }

    public static String[] asFormattedStrings() {
        String[] incidents = new String[CallerType.values().length];
        for (int i = 0; i < incidents.length; i++) {
            incidents[i] = CallerType.values()[i].toString();
        }
        return incidents;
    }

}
