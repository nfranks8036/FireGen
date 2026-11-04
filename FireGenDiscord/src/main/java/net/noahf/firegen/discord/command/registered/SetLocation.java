package net.noahf.firegen.discord.command.registered;

import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.AutoCompleteQuery;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.noahf.firegen.api.incidents.location.IncidentLocation;
import net.noahf.firegen.api.incidents.location.LocationField;
import net.noahf.firegen.api.incidents.location.LocationType;
import net.noahf.firegen.api.incidents.location.LocationVenue;
import net.noahf.firegen.discord.Main;
import net.noahf.firegen.discord.actions.registered.EditLocation;
import net.noahf.firegen.discord.actions.registered.EditMode;
import net.noahf.firegen.discord.bot.DiscordMessages;
import net.noahf.firegen.discord.command.Command;
import net.noahf.firegen.discord.command.CommandFlags;
import net.noahf.firegen.discord.config.files.ConfigVenues;
import net.noahf.firegen.discord.incidents.structure.IncidentImpl;
import net.noahf.firegen.discord.incidents.structure.location.IncidentLocationImpl;
import net.noahf.firegen.discord.utilities.MessageStatus;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import static net.noahf.firegen.discord.bot.DiscordMessages.truncate;

public class SetLocation extends Command {

    private static SubcommandData[] data;

    static {
        List<SubcommandData> subs = new ArrayList<>();
        for (LocationType type : LocationType.values()) {
            SubcommandData thisSub = new SubcommandData(type.getShortName(), type.getDescription());
            for (LocationField field : type.getFields()) {
                String id = field.getId();
                if (!id.equalsIgnoreCase("common-name"))
                    id = field.getId().substring(field.getId().indexOf("-") + 1);

                thisSub = thisSub
                        .addOption(OptionType.STRING, id,
                                truncate(field.getDescription(), 100, "..."),
                                field.isRequired(), id.equalsIgnoreCase("venue")
                        );
            }
            subs.add(thisSub);
        }

        data = subs.toArray(SubcommandData[]::new);
    }

    public SetLocation() {
        super("set-location", "Sets the location of the current incident to a specific spot.",
                CommandFlags.include()
                        .subcommandData(data)
                        .finish()
                );
    }

    @Override
    public void command(SlashCommandInteractionEvent event) {
        event.deferReply(true).queue();
        IncidentImpl incident = (IncidentImpl) EditMode.editIncidents.get(event.getUser());

        if (incident == null) {
            DiscordMessages.error(event, "You are not currently editing an incident. " +
                    "Press 'Edit Mode' of the incident of your choice."
            );
            return;
        }

        if (!incident.getStatus().isInProgress()) {
            DiscordMessages.error(event, "This incident is closed and cannot be edited.");
            return;
        }

        String locationTypeStr = event.getSubcommandName();
        LocationType locationType = LocationType.values().stream()
                .filter(t -> t.getShortName().equalsIgnoreCase(locationTypeStr))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Expected value location type, got '" + locationTypeStr + "'"));

        OptionMapping venueMapping = event.getOption(LocationField.VENUE.getId());
        @Nullable LocationVenue venue = null;
        if (venueMapping != null) {
            venue = Main.config.get(ConfigVenues.class).fromName(venueMapping.getAsString());

            if (venue == null) {
                DiscordMessages.error(event, "That venue does not exist: " + venueMapping.getAsString());
                return;
            }
        }

        OptionMapping commonNameMapping = event.getOption(LocationField.COMMON_NAME.getId());
        String commonName = null;
        if (commonNameMapping != null) {
            commonName = commonNameMapping.getAsString();
        }

        List<String> data = new LinkedList<>();
        for (OptionMapping mapping : event.getOptions()) {
            if (List.of(LocationField.VENUE.getId(), LocationField.COMMON_NAME.getId()).contains(mapping.getName())) {
                continue;
            }

            data.add(mapping.getAsString());
        }

        IncidentLocation location = new IncidentLocationImpl(data, locationType, commonName, venue);
        MessageStatus status = Main.actions.getAction(EditLocation.class).onSubmit(incident, event, locationType, location);
        if (status != MessageStatus.NONE) return;
        DiscordMessages.selfDestruct(event, 5, "__Set location to:__\n" + location.format("\n", null));
    }

    @Override
    public List<String> autocomplete(CommandAutoCompleteInteractionEvent event, User user, String commandString, AutoCompleteQuery focused) {
        if (focused.getName().equalsIgnoreCase("venue")) {
            return Main.config.get(ConfigVenues.class).get().stream().map(LocationVenue::getName).toList();
        }
        return null;
    }
}
