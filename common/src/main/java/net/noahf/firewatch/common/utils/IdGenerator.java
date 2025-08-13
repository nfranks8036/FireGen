package net.noahf.firewatch.common.utils;

import java.util.Random;

public class IdGenerator {

    public static int generate() {
        return new Random(System.currentTimeMillis()).nextInt(10000, 99999);
    }

}
