package net.noahf.firegen.discord.command.registered;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.buttons.Button;
import net.dv8tion.jda.api.components.selections.SelectOption;
import net.dv8tion.jda.api.components.selections.StringSelectMenu;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.noahf.firegen.api.incidents.SystemMunicipality;
import net.noahf.firegen.api.incidents.units.Agency;
import net.noahf.firegen.discord.Main;
import net.noahf.firegen.discord.bot.DiscordMessages;
import net.noahf.firegen.discord.command.Command;
import net.noahf.firegen.discord.config.ConfigManager;
import net.noahf.firegen.discord.config.files.ConfigMunicipality;
import net.noahf.firegen.discord.config.files.ConfigUnits;
import net.noahf.firegen.discord.incidents.structure.units.AgencyImpl;
import net.noahf.firegen.discord.utilities.Log;
import org.jspecify.annotations.NonNull;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static net.noahf.firegen.discord.users.FireGenUser.createId;

public class Agencies extends Command {

    public Agencies() {
        super(
                "agencies", "View the list of agencies that serve the area."
        );
    }

    @Override
    public void command(SlashCommandInteractionEvent event) {
        ConfigManager config = Main.config;

        List<Agency> agencies = new ArrayList<>(Main.config.get(ConfigUnits.class).getAgencies());
        SystemMunicipality local = config.get(ConfigMunicipality.class).get();
        event.replyEmbeds(new EmbedBuilder()
                .setColor(new Color(128, 252, 222))
                .setTitle("Agencies (" + agencies.size() + ")")
                .setDescription(
                        "These agencies serve " + local.getName() + ", " + local.getState().getAbbreviation() + ":\n\n" +
                        agencies.stream().map(a -> (AgencyImpl) a).map(a ->
                                "- " + (a.getEmoji() != null ? a.getEmoji().getFormatted() + " " : "") + a.getFormatted()
                                + " (`" + a.getShorthand() + "`)"
                                ).collect(Collectors.joining("\n"))
                )
                .build()
        ).setEphemeral(true)
                .setComponents(ActionRow.of(
                        StringSelectMenu.create(createId(event.getUser(), "agencyselectmenu"))
                                .addOptions(agencies.stream()
                                        .map(a -> (AgencyImpl)a)
                                        .map(a ->
                                                SelectOption.of(DiscordMessages.truncate("View: " + a.getTitle(), SelectOption.LABEL_MAX_LENGTH, "..."), createId(event.getUser(), "agencyselectmenu", a.getShorthand()))
                                                        .withEmoji(a.getEmoji())
                                                        .withDescription(a.getShorthand() + " (" + a.getType().getFullName() + ")")
                                        )
                                        .limit(StringSelectMenu.OPTIONS_MAX_AMOUNT)
                                        .toList()
                                )
                                .build()
                ))
                .queue();
    }

    public static class AgencyDetailsListener extends ListenerAdapter {
        @Override
        public void onStringSelectInteraction(@NonNull StringSelectInteractionEvent event) {
            String input = event.getSelectedOptions().getFirst().getValue();
            try {
                if (!input.startsWith("firegenuser")) {
                    return;
                }

                String command = input.split("-")[2];
                if (!command.equalsIgnoreCase("agencyselectmenu")) {
                    return;
                }

                String agencyStr = input.split("-")[3];
                ConfigUnits config = Main.config.get(ConfigUnits.class);
                Agency agency = config.agencyFromShorthand(agencyStr);
                if (agency == null)
                    throw new IllegalArgumentException("No agency exist by the shorthand '" + agencyStr + "'");

                event.replyEmbeds(AgencyInfo.agencyView(agency)).setEphemeral(true).queue();
            } catch (Exception exception) {
                Log.error("Button press error with input: \"" + input + "\"");
                DiscordMessages.error(event, "An error occurred while processing your Agency Detail View button press: " + exception, exception);
            }
        }
    }

}
