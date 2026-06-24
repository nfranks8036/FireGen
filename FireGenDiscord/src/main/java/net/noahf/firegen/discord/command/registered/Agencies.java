package net.noahf.firegen.discord.command.registered;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.noahf.firegen.discord.command.Command;

public class Agencies extends Command {

    public Agencies() {
        super(
                "agencies", "View the list of agencies that serve the area."
        );
    }

    @Override
    public void command(SlashCommandInteractionEvent event) {
        event.reply("This command is not in service yet!").setEphemeral(true).queue();
    }

}
