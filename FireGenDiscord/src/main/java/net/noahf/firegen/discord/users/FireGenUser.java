package net.noahf.firegen.discord.users;

import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import net.dv8tion.jda.api.entities.User;
import net.noahf.firegen.api.Contributor;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor(access = AccessLevel.PROTECTED, force = true)
@Getter
@RequiredArgsConstructor
@Entity
public class FireGenUser implements Contributor<User> {

    @Id
    private final long id;

    private final String name;

    private final String displayName;

    private transient final User userObject;

    @Accessors(fluent = true)
    private transient final boolean isFromJson;

    @ElementCollection
    @Enumerated
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
