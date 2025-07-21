package net.noahf.firewatch.common.geolocation;

public enum State {

    ALABAMA ("AL"),
    ALASKA ("AK"),
    ARIZONA ("AZ"),
    ARKANSAS ("AR"),
    CALIFORNIA ("CA"),
    COLORADO ("CO"),
    CONNECTICUT ("CT"),
    DELAWARE ("DE"),
    FLORIDA ("FL"),
    GEORGIA ("GA"),
    HAWAII ("HI"),
    IDAHO ("ID"),
    ILLINOIS ("IL"),
    INDIANA ("IN"),
    IOWA ("IA"),
    KANSAS ("KS"),
    KENTUCKY ("KY"),
    LOUISIANA ("LA"),
    MAINE ("ME"),
    MARYLAND ("MD"),
    MASSACHUSETTS ("MA"),
    MICHIGAN ("MI"),
    MINNESOTA ("MN"),
    MISSISSIPPI ("MS"),
    MISSOURI ("MO"),
    MONTANA ("MT"),
    NEBRASKA ("NE"),
    NEVADA ("NV"),
    NEW_HAMPSHIRE ("NH"),
    NEW_JERSEY ("NJ"),
    NEW_MEXICO ("NM"),
    NEW_YORK ("NY"),
    NORTH_CAROLINA ("NC"),
    NORTH_DAKOTA ("ND"),
    OHIO ("OH"),
    OKLAHOMA ("OK"),
    OREGON ("OR"),
    PENNSYLVANIA ("PA"),
    RHODE_ISLAND ("RI"),
    SOUTH_CAROLINA ("SC"),
    SOUTH_DAKOTA ("SD"),
    TENNESSEE ("TN"),
    TEXAS ("TX"),
    UTAH ("UT"),
    VERMONT ("VT"),
    VIRGINIA ("VA"),
    WASHINGTON ("WA"),
    WEST_VIRGINIA ("WV"),
    WISCONSIN ("WI"),
    WYOMING ("WY");

    private final String abbreviation;

    State(String abbreviation) {
        this.abbreviation = abbreviation;
    }

    public String abbreviate() {
        return this.abbreviation;
    }

    @Override
    public String toString() {
        return this.name().replace("_", " ");
    }

    public static String[] asFormattedStrings() {
        String[] incidents = new String[State.values().length];
        for (int i = 0; i < incidents.length; i++) {
            incidents[i] = State.values()[i].toString();
        }
        return incidents;
    }

}
