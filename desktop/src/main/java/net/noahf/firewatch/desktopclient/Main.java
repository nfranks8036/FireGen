package net.noahf.firewatch.desktopclient;

import net.noahf.firewatch.common.FireGen;

import java.util.Arrays;

public class Main {

    public static FireGen firegen;
    public static JavaFXManager fx;

    public static void main(String[] args) {
        Main.firegen = new FireGen();
        Main.fx = new JavaFXManager(args);
    }
}