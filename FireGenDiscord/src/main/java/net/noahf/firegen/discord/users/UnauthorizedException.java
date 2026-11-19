package net.noahf.firegen.discord.users;

public class UnauthorizedException extends RuntimeException {

    public UnauthorizedException() {
        super("The username and password do not match to a user.");
    }

}
