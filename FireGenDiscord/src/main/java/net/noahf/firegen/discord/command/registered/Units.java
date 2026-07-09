package net.noahf.firegen.discord.command.registered;

import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.buttons.Button;
import net.dv8tion.jda.api.entities.ClientType;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.noahf.firegen.api.incidents.units.UnitAssignment;
import net.noahf.firegen.discord.Main;
import net.noahf.firegen.discord.bot.DiscordMessages;
import net.noahf.firegen.discord.bot.MessageGenericData;
import net.noahf.firegen.discord.bot.UnitsResponseType;
import net.noahf.firegen.discord.command.Command;
import net.noahf.firegen.discord.utilities.Log;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class Units extends Command {

    public static final String BLANK_EMOJI = " ".repeat(7);

    public static final int MAX_UNIT_ROWS_TABLE = 12;
    public static final int MAX_UNIT_ROWS_EMBED = 32;

    private static final Map<Long, UnitsResponseType> userIdToViewType = new ConcurrentHashMap<>();

    public Units() {
        super("units", "View the list of units that were most recently active on calls.");
    }

    @Override
    public void command(SlashCommandInteractionEvent event) {
        MessageGenericData reply = createReply(event.getMember());
        event.reply(reply.asCreate())
                .setEphemeral(true)
                .setComponents(
                        ActionRow.of(
                                Button.primary("firegenuser-" + event.getUser().getIdLong() + "-refreshunits", "Refresh"),
                                Button.primary("firegenuser-" + event.getUser().getIdLong() + "-changetype", "Change to " + reply.guessType().nextOnList().getDescriptor())
                        )
                )
                .queue();
    }

    public static MessageGenericData createReply(Member user) {
        Set<UnitAssignment> assignments = Main.incidents.getAssignments();;

        UnitsResponseType type = UnitsResponseType.TABLE;

        if (user != null) {
            UnitsResponseType requested = userIdToViewType.getOrDefault(user.getIdLong(), UnitsResponseType.NOT_SET);

            if (requested == UnitsResponseType.NOT_SET) {
                if (user.getOnlineStatus(ClientType.MOBILE) == OnlineStatus.ONLINE) {
                    requested = UnitsResponseType.EMBED;
                }

                userIdToViewType.put(user.getIdLong(), type);
            }

            type = requested;
        }

        if (assignments.isEmpty()) {
            return type.applyNone();
        }

        return type.applyData(assignments);
    }

    public static class UnitsRefreshButtonDetector extends ListenerAdapter {

        @Override
        public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {
            String id = event.getComponentId();
            User user = event.getUser();

            if (!id.startsWith("firegenuser")
                    || !(id.endsWith("refreshunits") || id.endsWith("changetype"))
            ) {
                return;
            }

            Log.info(user.getName() + " (" + user.getIdLong() + ") pressed button '" + id + "'");
            UnitsResponseType currentView = userIdToViewType.getOrDefault(user.getIdLong(), UnitsResponseType.TABLE);
            if (id.endsWith("changetype")) {
                currentView = currentView.nextOnList();
                userIdToViewType.put(user.getIdLong(), currentView);
            }

            String changeText = "Change to " + currentView.nextOnList().getDescriptor();

            try {
                event.editMessage(" ").setEmbeds(new ArrayList<>())
                                .applyData(createReply(event.getMember()).asEdit())
                        .setComponents(
                                ActionRow.of(
                                        Button.secondary("firegenuser-" + event.getUser().getIdLong() + "-refreshunits", "Refresh").asDisabled(),
                                        Button.secondary("firegenuser-" + event.getUser().getIdLong() + "-changetype", changeText).asDisabled(),
                                        Button.danger("firegenuser-wait", "(Wait 5 seconds)").asDisabled()
                                )
                        )
                        .complete().editOriginalComponents(
                                ActionRow.of(
                                        Button.primary("firegenuser-" + event.getUser().getIdLong() + "-refreshunits", "Refresh").asEnabled(),
                                        Button.primary("firegenuser-" + event.getUser().getIdLong() + "-changetype", changeText).asEnabled()
                                )
                        ).completeAfter(5, TimeUnit.SECONDS);
            } catch (Exception exception) {
                DiscordMessages.error(event, "An error occurred processing your button press", exception);
            }
        }
    }

}
