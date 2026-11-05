package net.noahf.firegen.discord.users;

import lombok.*;
import lombok.experimental.Accessors;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.User;
import net.noahf.firegen.api.Contributor;
import net.noahf.firegen.api.utilities.Descriptor;
import net.noahf.firegen.discord.utilities.Log;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@SuppressWarnings("UnusedReturnValue")
@NoArgsConstructor(access = AccessLevel.PROTECTED, force = true)
@Getter
@AllArgsConstructor
public class FireGenUser implements Contributor<User>, Descriptor {

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




    private final long id;

    private String name;

    private String displayName;

    private transient User userObject;

    @Accessors(fluent = true)
    private transient final boolean isFromJson;

    private final List<Permission> permissions = new ArrayList<>(List.of(Permission.DEFAULT));

    public boolean hasPermission(Permission permission, Permission... andPermission) {
        if (!this.permissions.contains(permission)) {
            return false;
        }
        if (andPermission.length < 1) {
            return true;
        }

        for (Permission p : andPermission) {
            if (!this.permissions.contains(p)) {
                return false;
            }
        }
        return true;
    }

    public void togglePermissions(Permission... permissions) {
        for (Permission p : permissions) {
            if (this.permissions.contains(p)) {
                this.permissions.remove(p);
            } else {
                this.permissions.add(p);
            }
        }
    }

    public boolean importIfNot(User user) {
        if (this.userObject != null) {
            return false;
        }

        this.userObject = user;
        this.name = user.getName();
        this.displayName = user.getEffectiveName();


        Log.info("Imported User: " + this.describe());
        return true;
    }

    public boolean importIfNot(JDA jda) {
        if (this.userObject != null) {
            return false;
        }

        User user = jda.retrieveUserById(this.id).complete();
        if (user == null) {
            throw new IllegalArgumentException("That user does not exist per Discord: " + this.id);
        }
        return this.importIfNot(user);
    }

    @Override
    @NotNull
    public String toString() {
        if (this.name == null) return "[Unknown User]";
        return this.name;
    }

    @Override
    public String describe() {
        return this.getName() + " [ID: " + this.getId() + ", display: " + this.getDisplayName() + ", permission count: " + this.getPermissions().size() + "]";
    }
}
