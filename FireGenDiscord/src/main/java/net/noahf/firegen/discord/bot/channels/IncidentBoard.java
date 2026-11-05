package net.noahf.firegen.discord.bot.channels;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.components.MessageTopLevelComponent;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.noahf.firegen.api.incidents.Incident;
import net.noahf.firegen.discord.Main;
import net.noahf.firegen.discord.incidents.IncidentManager;
import net.noahf.firegen.discord.incidents.structure.IncidentImpl;
import net.noahf.firegen.discord.utilities.Log;
import net.noahf.firegen.discord.utilities.Time;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class IncidentBoard {

    private final ChannelManager channels;
    private final List<Message> messages;

    public IncidentBoard(ChannelManager channels) {
        this.channels = channels;
        this.messages = new ArrayList<>();

        this.sendInitial();
    }

    public void sendInitial() {
        List<TextChannel> channels = new ArrayList<>(this.channels.getFor(ChannelRole.INCIDENT_BOARD, null));

        Log.info("Sending incident board to " + channels.size() + " channels.");
        for (TextChannel channel : channels) {
            try {
                if (channel == null) {
                    Log.warn("INFO BOARD - Can't send a message here. This channel does not exist! Marked for removal.");
                    continue;
                }

                this.messages.add(channel.sendMessageEmbeds(getEmbed(null)).complete());
            } catch (Exception exception) {
                Log.error("INFO BOARD - Can't send message to " + (channel != null ? channel.getName() : null)
                        + ": " + exception, exception);
            }
        }
    }

    public void sendEdited() {
        if (this.messages.isEmpty()) {
            throw new IllegalStateException("Expected messages to exist, found none.");
        }

        IncidentManager manager = Main.incidents;
        List<Incident> incidents = manager.getIncidents();
        for (Message message : this.messages) {
            try {
                List<MessageTopLevelComponent> components = new ArrayList<>();
//                components.add(StringSelectMenu.create(FireGenUser.createId("editincident"))
//
//                );

                message.editMessageEmbeds(getEmbed(incidents)).setComponents(components).queue(null, (t) -> {
                    Log.warn("Info board message in #" + message.getChannel().getName() + " in " +
                            message.getGuild().getName() + " does not exist. Removing from the list " +
                            "(possibly deleted by staff).", t);
                    this.messages.remove(message);
                });
            } catch (Exception exception) {
                Log.error("Can't edit info board message: " + exception, exception);
            }
        }
    }

    private MessageEmbed getEmbed(@Nullable List<Incident> incidents) {
        IncidentManager manager = Main.incidents;

        EmbedBuilder builder = new EmbedBuilder()
                .setTitle("ACTIVE INCIDENT BOARD")
                .setDescription("Represents the list of active incidents right now. Note: This is updated by " +
                        "volunteers manually, so it will not always be 100% up-to-date.");

        if (incidents != null && manager != null && !incidents.isEmpty()) {
            this.fetchIncidents(incidents, manager).forEach(builder::addField);
        }  else {
            builder = builder.setFooter("No active incidents.");
        }

        return builder.build();
    }

    private List<MessageEmbed.Field> fetchIncidents(List<Incident> incidents, IncidentManager manager) {
        List<MessageEmbed.Field> fields = new ArrayList<>();
        for (Incident iIncident : incidents) {
            IncidentImpl incident = (IncidentImpl) iIncident;
            if (!incident.getStatus().isInProgress()) {
                continue;
            }

            String time = incident.getTime().formatTimeShort(Main.config.getFireGenVariables());
            String location = incident.getLocation().format(", ", null);
            String units = incident.getUnitAssignments().stream()
                    .map(u -> u.getUnit().getShorthand())
                    .collect(Collectors.joining(", "));
            MessageEmbed.Field field = new MessageEmbed.Field(
                    "\uD83D\uDEA8 `" + time + "` | **__" + incident.getType().getSelectedName() + "__**",
                    "\uD83D\uDCCD " + location + "\n" +
                            "\uD83D\uDE91 " + units,
                    true
            );

            fields.add(field);
        }
        if (fields.isEmpty()) {
            fields.add(new MessageEmbed.Field(
                    "No Active Incidents", "*Returned from FireGen as of <t:" + Time.getUnix() + ":T>*", true
            ));
        }
        return fields;
    }


}
