package net.noahf.firegen.discord.users;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.*;
import lombok.experimental.Accessors;
import net.noahf.firegen.api.Contributor;
import net.noahf.firegen.api.utilities.Descriptor;
import net.noahf.firegen.discord.web.serializers.FireGenUserSerializer;
import org.jetbrains.annotations.NotNull;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@SuppressWarnings("UnusedReturnValue")
@NoArgsConstructor(access = AccessLevel.PROTECTED, force = true)
@Getter
@AllArgsConstructor
@JsonSerialize(using = FireGenUserSerializer.class)
public class FireGenUser<T> implements Contributor<T>, Descriptor, UserDetails {

    private final long id;

    protected String name;

    protected String loginUsername;

    protected transient @JsonIgnore T userObject;

    @Accessors(fluent = true)
    private transient final boolean isFromJson;

    private final String password;

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

    @Override
    public String describe() {
        return this.getUsername() + " [ID: " + this.getId() + ", display: " + this.getLoginUsername() + ", permission count: " + this.getPermissions().size() + "]";
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return this.permissions.stream()
                .map(s -> new SimpleGrantedAuthority(s.name()))
                .toList();
    }

    @Override
    public String getPassword() {
        return this.password;
    }

    @Override
    public String getUsername() {
        return this.name;
    }
}
