package net.noahf.firegen.discord.utilities.errors;

import lombok.Getter;

@Getter
public class FireGenError extends RuntimeException {

    private final ErrorCode errorCode;

    public FireGenError(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public FireGenError(ErrorCode errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

}
