package net.noahf.firegen.discord.actions;

import net.dv8tion.jda.api.events.interaction.GenericInteractionCreateEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.noahf.firegen.discord.users.FireGenUser;

/**
 * Represents an action involving a {@link StringDropdownAction JDA dropdown action}
 */
public interface StringDropdownAction extends FireGenAction {

    void execute(ActionsContext ctx, FireGenUser user, StringSelectInteractionEvent event);

    @Override
    default void execute(ActionsContext ctx, FireGenUser user, GenericInteractionCreateEvent event) {
        this.execute(ctx, user, (StringSelectInteractionEvent) event);
    }

}
