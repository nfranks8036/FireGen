package net.noahf.firegen.discord.services;

import net.noahf.firegen.api.Contributor;
import net.noahf.firegen.discord.utilities.Log;

public record ServiceResponse<T>(
        boolean success,
        T response
) {

    public static ServiceResponse<String> fail(Contributor<?> contributor, String response) {
        Log.error("User " + contributor.getName() + " encountered an error: '" + response + "'");
        return new ServiceResponse<>(false, response);
    }

    public static ServiceResponse<Void> failNone() { return new ServiceResponse<>(false, null); }

    public static ServiceResponse<String> success(String response) {
        return new ServiceResponse<>(true, response);
    }

    public static <T> ServiceResponse<T> success(T input) { return new ServiceResponse<>(true, input); }

    public static ServiceResponse<Void> successNone() {
        return new ServiceResponse<>(true, null);
    }

    public static ServiceResponse<?> returnAUnlessBFail(ServiceResponse<?> a, ServiceResponse<?> b) {
        if (!b.success()) return b;
        return a;
    }



    public boolean hasObject() {
        return response != null;
    }

    public boolean isResponseType(Class<?> clazz) {
        return response.getClass().isAssignableFrom(clazz);
    }

}
