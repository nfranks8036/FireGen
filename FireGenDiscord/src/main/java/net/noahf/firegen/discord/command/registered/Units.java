package net.noahf.firegen.discord.command.registered;

import kotlin.Pair;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.buttons.Button;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback;
import net.noahf.firegen.api.incidents.Incident;
import net.noahf.firegen.api.incidents.units.AssignmentEvent;
import net.noahf.firegen.api.incidents.units.Unit;
import net.noahf.firegen.api.incidents.units.UnitAssignment;
import net.noahf.firegen.api.utilities.FireGenVariables;
import net.noahf.firegen.discord.Main;
import net.noahf.firegen.discord.command.Command;
import net.noahf.firegen.discord.incidents.structure.location.IncidentLocationImpl;
import net.noahf.firegen.discord.incidents.structure.units.AssignmentStatusImpl;
import net.noahf.firegen.discord.utilities.DiscordMessages;
import net.noahf.firegen.discord.utilities.ImmutablePair;
import net.noahf.firegen.discord.utilities.Log;
import net.noahf.firegen.discord.utilities.Time;
import net.noahf.firegen.discord.utilities.ansi.AnsiTableBuilder;
import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Units extends Command {

    public Units() {
        super("units", "View the list of units that serve the area.");
    }

    @Override
    public void command(SlashCommandInteractionEvent event) {
        event.reply(createAnsiReply())
                .setEphemeral(true)
                .setComponents(
                        ActionRow.of(
                                Button.primary("firegenuser-" + event.getUser().getIdLong() + "-refreshunits", "Refresh")
                        )
                )
                .queue();
    }

    public static String createAnsiReply() {
        Set<UnitAssignment> assignments = Main.incidents.getAssignments();;

        if (assignments.isEmpty()) {
            return "```diff\n- No units have been assigned to any incidents right now. Try again later. -```\n"
                    + "(as of <t:" + (Time.getUnix()) + ":R>)"
                    ;
        }

        AnsiTableBuilder table = new AnsiTableBuilder()
                .header("time", "unit", "status", "incident type", "incident location")
                .disallowDuplicateOnColumn(1);
        for (UnitAssignment assignment : assignments) {
            Unit unit = assignment.getUnit();
            Incident incident = assignment.getIncident();
            AssignmentEvent e = assignment.getLatestAssignment();
            LocalDateTime time = e.timestamp();
            AssignmentStatusImpl status = (AssignmentStatusImpl) e.status();

            table = table
                    .row(time.toEpochSecond(ZoneOffset.UTC), status.getAnsiColor(),
                            time.format(DateTimeFormatter.ofPattern(Main.incidents.getFireGenVariables().longTimeFormat())),
                            unit.getShorthand(),
                            status.getName(),
                            incident.getType().toString(),
                            ((IncidentLocationImpl)incident.getLocation()).getRequiredData(null)
                    );
        }

        ImmutablePair<String, Integer> returned = table.build(25);
        int amount = returned.getSecondElement();

        return "Returned `" + (amount >= 25 ? "25`/`25" : amount) + "` unit status" + (amount == 1 ? "" : "es") + " from FireGen as of <t:" + (Time.getUnix()) + ":R>." +
                "\n```ansi\n" + returned.getFirstElement() + "```";
    }

    public static class UnitsRefreshButtonDetector extends ListenerAdapter {

        @Override
        public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {
            String id = event.getComponentId();
            User user = event.getUser();

            if (!id.startsWith("firegenuser") || !id.endsWith("refreshunits")) {
                return;
            }

            Log.info(user.getName() + " (" + user.getIdLong() + ") pressed button '" + id + "'");

            try {
                event.editMessage(createAnsiReply())
                        .setComponents(
                                ActionRow.of(
                                        Button.secondary("firegenuser-" + event.getUser().getIdLong() + "-refreshunits", "Refresh (Wait 5s)").asDisabled()
                                )
                        )
                        .complete().editOriginalComponents(
                                ActionRow.of(
                                        Button.primary("firegenuser-" + event.getUser().getIdLong() + "-refreshunits", "Refresh").asEnabled()
                                )
                        ).completeAfter(5, TimeUnit.SECONDS);
                ;
            } catch (Exception exception) {
                DiscordMessages.error(event, "An error occurred processing your button press", exception);
            }
        }
    }

}
