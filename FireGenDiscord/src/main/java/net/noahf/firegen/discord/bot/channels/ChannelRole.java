package net.noahf.firegen.discord.bot.channels;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor @Getter
public enum ChannelRole {

    RECEIVER(true),

    ADMIN(true),

    INCIDENT_BOARD(false);

    private final boolean perIncident;

}
