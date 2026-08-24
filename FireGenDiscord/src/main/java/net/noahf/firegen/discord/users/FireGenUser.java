package net.noahf.firegen.discord.users;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import net.dv8tion.jda.api.entities.User;
import net.noahf.firegen.api.Contributor;
import net.noahf.firegen.discord.incidents.structure.IncidentImpl;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@NoArgsConstructor(access = AccessLevel.PROTECTED, force = true)
@Getter
@RequiredArgsConstructor
public class FireGenUser implements Contributor<User> {

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

    private final String name;

    private final String displayName;

    private transient final User userObject;

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

    @Override
    @NotNull
    public String toString() {
        if (this.name == null) return "[Unknown User]";
        return this.name;
    }

}
