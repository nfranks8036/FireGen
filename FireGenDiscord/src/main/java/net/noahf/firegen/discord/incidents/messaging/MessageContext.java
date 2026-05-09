package net.noahf.firegen.discord.incidents.messaging;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.noahf.firegen.api.incidents.Incident;
import net.noahf.firegen.discord.incidents.structure.IncidentImpl;

@AllArgsConstructor
public class MessageContext {

    private final @Getter IncidentImpl incident;
    private final @Getter IncidentMessagingService service;

}
