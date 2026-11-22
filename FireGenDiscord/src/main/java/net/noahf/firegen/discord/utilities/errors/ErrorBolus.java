package net.noahf.firegen.discord.utilities.errors;

import net.bytebuddy.implementation.bytecode.Throw;
import net.noahf.firegen.discord.services.ServiceResponse;
import net.noahf.firegen.discord.web.api.ApiResponse;
import org.jetbrains.annotations.Nullable;

public record ErrorBolus(ErrorCode errorCode, String message, @Nullable ApiResponse.IncludeError error) {

    public static ErrorBolus of(ErrorCode code) {
        return of(code, code.getBriefExplanation());
    }

    public static ErrorBolus of(ErrorCode code, String message) {
        return of(code, message, (ApiResponse.IncludeError) null);
    }

    public static ErrorBolus of(ErrorCode code, String message, Throwable throwable) {
        return new ErrorBolus(code, message, new ApiResponse.IncludeError("An internal error has occurred.", throwable.getClass().getCanonicalName(), throwable.getMessage()));
    }

    public static ErrorBolus of(ErrorCode code, String message, @Nullable ApiResponse.IncludeError error) {
        return new ErrorBolus(code, message, error);
    }

}
