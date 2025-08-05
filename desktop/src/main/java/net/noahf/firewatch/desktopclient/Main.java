package net.noahf.firewatch.desktopclient;

import net.noahf.firewatch.common.FireGen;

import java.util.Arrays;

public class Main {

    public static FireGen firegen;
    public static JavaFXManager fx;

    public static void main(String[] args) {
        final String INCIDENT_STRUCTURE = "incident_structure.json";

        Main.firegen = new FireGen(INCIDENT_STRUCTURE);
        Main.fx = new JavaFXManager(args);


    }
}