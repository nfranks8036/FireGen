package net.noahf.firegen.discord.users;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.Getter;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.User;
import net.noahf.firegen.api.Contributor;
import net.noahf.firegen.api.utilities.FireGenVariables;
import net.noahf.firegen.discord.Main;
import net.noahf.firegen.discord.config.ConfigManager;
import net.noahf.firegen.discord.incidents.IncidentManager;
import net.noahf.firegen.discord.utilities.JsonUtilities;
import net.noahf.firegen.discord.utilities.Log;
import net.noahf.firegen.discord.utilities.Manager;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

@Getter
public class UserManager extends Manager<UserManager> {

    private final List<FireGenUser> users;

    public UserManager(JDA jda, ConfigManager config) {
        super(UserManager.class, "Users");

        this.users = new ArrayList<>();
        this.importUsers(jda, config);
    }

    public void addUser(FireGenUser user) {
        this.users.add(user);
    }

    public void addUser(Contributor<User> contributor) {
        this.users.add(new FireGenUser(
                contributor.getId(), contributor.getName(), contributor.getDisplayName(), contributor.getUserObject(),
                false
        ));
    }

    public @Nullable FireGenUser getById(long id) {
        for (FireGenUser user : this.users) {
            if (user.getId() == id) {
                return user;
            }
        }
        return null;
    }

    public @Nullable FireGenUser getByDiscord(User user) {
        return this.getById(user.getIdLong());
    }

    public FireGenUser getByDiscordNotNull(User user) {
        FireGenUser u = this.getByDiscord(user);
        if (u != null) {
            return u;
        }
        u = new FireGenUser(user.getIdLong(), user.getName(), user.getEffectiveName(), user, false);
        this.users.add(u);
        return u;
    }

    public boolean hasPermission(User user, Permission permission, Permission... andPermissions) {
        FireGenUser fireGenUser = this.getByDiscord(user);
        if (fireGenUser == null) {
            return false;
        }

        return fireGenUser.hasPermission(permission, andPermissions);
    }





    private void importUsers(JDA jda, ConfigManager config) {
        FireGenVariables vars = config.getFireGenVariables();
        JsonUtilities.stream(Main.bot, null, vars.usersFile(), (e) -> {
            Log.info("Importing and searching for users...");
            long start = System.currentTimeMillis();
            JsonArray array = e.getAsJsonArray();
            for (JsonElement element : array.asList()) {
                JsonObject userObject = element.getAsJsonObject();

                long id = userObject.get("id").getAsLong();
                Permission[] permissions = this.getPermissionArray(userObject.get("permissions").getAsJsonArray());
                User discordUser = jda.getUserById(id);

                if (discordUser == null) {
                    discordUser = jda.retrieveUserById(id).complete();
                }

                if (discordUser == null) {
                    throw new IllegalArgumentException("Expected to find Discord account associated with '" + id + "'" +
                            ", but could not find one!"
                    );
                }

                FireGenUser user = new FireGenUser(
                        id, discordUser.getName(), discordUser.getEffectiveName(), discordUser, true
                );
                user.togglePermissions(permissions);
                user.togglePermissions(Permission.DEFAULT);

                this.addUser(user);
            }

            Log.info("Imported " + this.users.size() + " users in " + (System.currentTimeMillis() - start) + "ms.");
        });
    }

    private Permission[] getPermissionArray(JsonArray array) {
        List<JsonElement> elements = array.asList();
        Permission[] permissions = new Permission[elements.size()];
        for (int i = 0; i < permissions.length; i++) {
            String name = elements.get(i).getAsString();
            if (name.equalsIgnoreCase("*")) {
                return Permission.values();
            }

            permissions[i] = this.getPermissionFromName(name);
        }
        return permissions;
    }

    private Permission getPermissionFromName(String name) {
        Permission returned = null;
        try {
            returned = Permission.valueOf(name);
        } catch (IllegalArgumentException illegalArgumentException) {
            Log.warn("No permission exists by the name '" + name + "', check your users file and try again.");
        }
        return returned;
    }
}
