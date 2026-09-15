package net.noahf.firegen.discord.users;

public class SystemUser extends FireGenUser {

    public static SystemUser get() { return new SystemUser(); }

    private SystemUser() {
        super(
                0, "(SYSTEM)", "System", null, false
        );
    }

}
