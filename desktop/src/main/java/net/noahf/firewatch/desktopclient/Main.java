package net.noahf.firewatch.desktopclient;

import net.noahf.firewatch.common.FireGen;

public class Main {

    public static FireGen fireGen;
    public static JavaFXManager fx;

    public static void main(String[] args) {
        Main.fireGen = new FireGen();
        Main.fx = new JavaFXManager(args);
    }
}