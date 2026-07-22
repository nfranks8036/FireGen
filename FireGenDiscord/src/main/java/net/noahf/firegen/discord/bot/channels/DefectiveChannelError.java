package net.noahf.firegen.discord.bot.channels;

public class DefectiveChannelError extends RuntimeException {

    public DefectiveChannelError(long channelId) {
        super("The channel with id '" + channelId + "' does not exist or is not accessible by FireGen, it will not attempt to be accessed again (unless reload/restart).");
    }

}
