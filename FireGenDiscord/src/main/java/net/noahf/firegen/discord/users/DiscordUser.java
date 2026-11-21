package net.noahf.firegen.discord.users;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.User;
import net.noahf.firegen.discord.utilities.Log;

import java.util.Arrays;

public class DiscordUser extends FireGenUser<User> {


    public DiscordUser(long id, String name, String displayName, User userObject, boolean isFromJson) {
        super(id, name, displayName, userObject, isFromJson, null);
    }

    public static String createId(String command, String... additionalParameters) {
        String[] commands = Arrays.copyOf(additionalParameters, additionalParameters.length + 1);
        for (int i = commands.length - 1; i >= 1; i--) {
            commands[i] = commands[i - 1];
        }
        commands[0] = command;

        return String.format("firegenuser-generic-%s", String.join("-", commands));
    }

    public static String createId(User user, String command, String... additionalParameters) {
        if (additionalParameters == null || additionalParameters.length == 0) {
            return createInteractionIdString(user, command);
        }

        String[] commands = Arrays.copyOf(additionalParameters, additionalParameters.length + 1);
        for (int i = commands.length - 1; i >= 1; i--) {
            commands[i] = commands[i - 1];
        }
        commands[0] = command;
        // the name of the command has to come first
        return createInteractionIdString(user, commands);
    }

    private static String createInteractionIdString(User user, String... commands) {
        return String.format(
                "firegenuser-%s-%s",
                user.getId(), String.join("-", commands)
        );
    }

    public boolean importIfNot(User user) {
        if (this.userObject != null) {
            return false;
        }

        this.userObject = user;
        this.name = user.getName();

        Log.info("Imported User: " + this.describe());
        return true;
    }

    public boolean importIfNot(JDA jda) {
        if (this.userObject != null) {
            return false;
        }

        User user = jda.retrieveUserById(this.getId()).complete();
        if (user == null) {
            throw new IllegalArgumentException("That user does not exist per Discord: " + this.getId());
        }
        return this.importIfNot(user);
    }
}
