package net.noahf.firegen.discord.command.registered;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.noahf.firegen.discord.Main;
import net.noahf.firegen.discord.command.Command;
import net.noahf.firegen.discord.command.CommandFlags;
import net.noahf.firegen.discord.incidents.structure.IncidentImpl;
import net.noahf.firegen.discord.users.FireGenUser;
import net.noahf.firegen.discord.utilities.DiscordMessages;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class AdminCommand extends Command {

    public AdminCommand() {
        super(
                "admin-cmd", "Execute an admin command.",
                CommandFlags.include()
                        .permissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR))
                        .options(new OptionData[]{
                                new OptionData(OptionType.STRING, "command", "The command to execute", true, false),
                                new OptionData(OptionType.STRING, "parameter", "The parameter", false, false)
                        })
                        .finish()
        );
    }

    @Override
    public void command(SlashCommandInteractionEvent event) {
        if (!Main.users.hasPermission(event.getUser(), net.noahf.firegen.discord.users.Permission.ADMIN)) {
            DiscordMessages.error(event, "You don't have permission to use the admin command.");
            return;
        }

        OptionMapping commandMapping = event.getOption("command");
        if (commandMapping == null) {
            DiscordMessages.error(event, "Expected command."); return;
        }

        String command = commandMapping.getAsString();

        OptionMapping parameterMapping = event.getOption("parameter");
        @Nullable String parameter = parameterMapping != null ? parameterMapping.getAsString() : null;

        switch (command) {
            case "force-post" -> {
                if (parameter == null) {
                    DiscordMessages.error(event, "Expected parameter for 'force-post'.");
                    return;
                }

                IncidentImpl incident = (IncidentImpl) Main.incidents.getIncidentBy(Long.parseLong(parameter));
                if (incident == null) {
                    DiscordMessages.error(event, "Invalid parameter.");
                    return;
                }

                incident.admin_wipeMessages();
                incident.update();
                DiscordMessages.selfDestruct(event, 5, "Sent an update.");
            }
            case "see-saved-user" -> {
                User user = event.getUser();
                int type = 0;
                if (parameter != null) {
                    long who = Long.parseLong(parameter);
                    user = Main.JDA.getUserById(who);
                    type = 1;
                    if (user == null) {
                        user = Main.JDA.retrieveUserById(who).complete();
                        type = 2;
                    }
                }

                String typeString = switch (type) {
                    case 0 -> "SELF";
                    case 1 -> "CACHE";
                    case 2 -> "RETRIEVED";
                    default -> "CORRUPT";
                };

                FireGenUser fireGenUser = Main.users.getByDiscordNotNull(user);
                java.util.List<net.noahf.firegen.discord.users.Permission> permissions = fireGenUser.getPermissions();
                event.reply(
                        "User: `" + user.getName() + "` (`" + user.getIdLong() + "`)\n" +
                                "StoredUser: `" + fireGenUser.getName() + "` and `" + fireGenUser.getDisplayName() + "`\n" +
                                "Permissions: `" + String.join("`, `", permissions) + "`\n" +
                                "Permission size: `" + permissions.size() + "`/`" + net.noahf.firegen.discord.users.Permission.values().length + "`\n" +
                                "From JSON? `" + fireGenUser.isFromJson() + "`\n" +
                                "Obtained Type: `" +  typeString + "`"
                ).setEphemeral(true).queue();
            }
            default -> {
                DiscordMessages.error(event, "Invalid command.");
            }
        }
    }
}
