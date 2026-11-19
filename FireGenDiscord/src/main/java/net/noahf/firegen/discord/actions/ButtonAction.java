package net.noahf.firegen.discord.actions;

import net.dv8tion.jda.api.events.interaction.GenericInteractionCreateEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.noahf.firegen.discord.users.FireGenUser;

/**
 * Represents an action involving a {@link net.dv8tion.jda.api.components.buttons.Button JDA button}
 */
public interface ButtonAction extends FireGenAction {

    void execute(ActionsContext ctx, FireGenUser<?> user, ButtonInteractionEvent event);

    @Override
    default void execute(ActionsContext ctx, FireGenUser<?> user, GenericInteractionCreateEvent event) {
        this.execute(ctx, user, (ButtonInteractionEvent) event);
    }
}
