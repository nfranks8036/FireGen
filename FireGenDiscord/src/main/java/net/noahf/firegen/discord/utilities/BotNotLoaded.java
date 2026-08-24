package net.noahf.firegen.discord.utilities;

public class BotNotLoaded extends RuntimeException {

    public BotNotLoaded() {
        super("The bot is still starting up and can't be accessed quite yet. Try again in a few moments!");
    }

}
