package net.noahf.firegen.discord.bot.channels;

import lombok.Getter;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.noahf.firegen.api.incidents.Incident;
import net.noahf.firegen.api.utilities.AutofilledCharSequence;
import net.noahf.firegen.discord.utilities.Log;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;

@Getter
public class FireGenChannel implements AutofilledCharSequence {

    private final long channelId;
    private final TextChannel channel;
    private final ChannelRole role;
    private final @Nullable List<ChannelConditional> conditionals;

    public FireGenChannel(JDA jda, long channelId, ChannelRole role, @Nullable List<ChannelConditional> conditionals) {
        this.channelId = channelId;
        this.channel = jda.getTextChannelById(channelId);
        if (this.channel == null) {
            throw new DefectiveChannelError(this.channelId);
        }

        this.role = role;
        this.conditionals = conditionals;
    }

    public boolean evaluateConditionals(Incident incident) {
        if (conditionals == null || conditionals.isEmpty()) {
            return true;
        }

        try {
            for (ChannelConditional conditional : conditionals) {
                boolean response = conditional.evaluate(incident);
                if (!response) {
                    return false;
                }
            }
            return true;
        } catch (Exception exception) {
            Log.warn("An error occurred while evaluating conditionals for incident #" + incident.getFormattedId() + " and channel " + this.channelId + ": " + exception, exception);
            Log.warn("This error will make the conditional default to 'false' so no message will be sent!");
            exception.printStackTrace(System.err);
            return false;
        }
    }

    @Override @NotNull
    public String toString() {
        return channel.getName() + " [id=" + channel.getId() + "]";
    }
}
