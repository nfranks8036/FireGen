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

import java.util.ArrayList;
import java.util.List;

public class ReloadConfig extends Command {

    public ReloadConfig() {
        super("reload-config", "Reloads the configuration files of FireGen.",
                CommandFlags.include()
                        .permissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR))
                        .options(new OptionData[]{new OptionData(OptionType.STRING, "file", "Reload specific file.", false, true)})
                        .finish()
        );
    }

    @Override
    public void command(SlashCommandInteractionEvent event) {
        event.reply("This command is unfinished.").setEphemeral(true).queue();
    }

    @Override
    public List<String> autocomplete(CommandAutoCompleteInteractionEvent event, User user, String commandString, AutoCompleteQuery focused) {
        return new ArrayList<>();
    }
}
