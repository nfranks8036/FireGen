package net.noahf.firegen.discord.users;

public class SystemUser extends FireGenUser<SystemUser> {

    public static SystemUser get() { return new SystemUser(); }

    private SystemUser() {
        super(
                0, "", "System", null, false, null
        );
    }

}
