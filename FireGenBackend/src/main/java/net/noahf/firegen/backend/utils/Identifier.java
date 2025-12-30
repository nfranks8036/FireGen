package net.noahf.firegen.backend.utils;

import java.util.Calendar;

public class Identifier {

    public static Identifier from(String formatted) {
        int year;
        int id;
        if (formatted.contains("-")) {
            String[] splitId = formatted.split("-");
            year = Integer.parseInt(splitId[0]);
            id = Integer.parseInt(splitId[1]);
        } else {
            year = Calendar.getInstance().get(Calendar.YEAR);
            id = Integer.parseInt(formatted);
        }
        return new Identifier(year, id);
    }

    private final int year;
    private final int id;

    public Identifier(int year, int id) {
        this.year = year;
        this.id = id;
    }

    public int year() { return this.year; }
    public int id() { return this.id; }

    public String format() {
        return this.year() + "-" + String.format("%08d", this.id());
    }

}
