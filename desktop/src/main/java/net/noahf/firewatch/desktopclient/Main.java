package net.noahf.firewatch.desktopclient;

import net.noahf.firewatch.common.FireGen;

import java.util.Arrays;

public class Main {

    public static final String MUNICIPALITY = "Roanoke County";

    public static FireGen firegen;
    public static JavaFXManager fx;

    public static void main(String[] args) {
        Main.firegen = FireGen.start(MUNICIPALITY);
        Main.fx = new JavaFXManager(args);
    }
}