package net.noahf.firewatch.desktopclient;

import net.noahf.firewatch.common.FireGen;

import java.util.Arrays;

public class Main {

    public static FireGen firegen;
    public static JavaFXManager fx;

    public static void main(String[] args) {
        final String FOLDER = "Montgomery County";

        Main.firegen = new FireGen(FOLDER);
        Main.fx = new JavaFXManager(args);
    }
}