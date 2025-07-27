package net.noahf.firewatch.desktopclient.utils;

import java.util.function.Supplier;

public class SupplierUtils {

    public static <T> T tryGet(Supplier<T> tryValue) {
        return SupplierUtils.tryGet(tryValue, null);
    }

    public static <T> T tryGet(Supplier<T> tryValue, T def) {
        try {
            return tryValue.get();
        } catch (Exception exception) {
            return def;
        }
    }
}
