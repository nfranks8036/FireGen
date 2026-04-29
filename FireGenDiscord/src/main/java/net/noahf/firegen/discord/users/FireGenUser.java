package net.noahf.firegen.discord.users;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import net.dv8tion.jda.api.entities.User;
import net.noahf.firegen.api.Contributor;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

@Getter
@RequiredArgsConstructor
public class FireGenUser implements Contributor<User> {

    private final long id;
    private final String name;
    private final String displayName;
    private final User userObject;
    private @Accessors(fluent = true) final boolean isFromJson;

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
        return this.name;
    }
}
