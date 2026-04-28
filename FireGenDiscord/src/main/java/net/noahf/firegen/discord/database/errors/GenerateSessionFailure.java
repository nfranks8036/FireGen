package net.noahf.firegen.discord.database.errors;

public class GenerateSessionFailure extends RuntimeException {

    public GenerateSessionFailure(Throwable cause) {
        super("Failed to generate a Hibernate database session!", cause);
    }

}
