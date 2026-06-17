package net.noahf.firegen.discord.command.registered;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.AutoCompleteQuery;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.noahf.firegen.discord.command.Command;
import net.noahf.firegen.discord.command.CommandFlags;
import net.noahf.firegen.discord.incidents.structure.IncidentImpl;

import java.lang.reflect.Constructor;
import java.util.List;

public class AdminCreate extends Command {

    public AdminCreate() {
        super("admin-create", "Create objects on the fly.",

                CommandFlags.include()
                        .options(new OptionData[]{
                                new OptionData(OptionType.STRING, "object", "The object type.", true, true),
                                new OptionData(OptionType.STRING, "parameters", "The parameters to initialize the object.", true, false)
                        })
                        .permissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR))
                        .finish());
    }



    @Override
    public void command(SlashCommandInteractionEvent event) {

    }

    @Override
    public List<String> autocomplete(CommandAutoCompleteInteractionEvent event, User user, String commandString, AutoCompleteQuery focused) {
        return null;
    }

    private static class AdminCreateObject<T> {
     
    }
}
