package net.noahf.firegen.discord.config.files;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.noahf.firegen.api.incidents.units.RadioChannel;
import net.noahf.firegen.api.utilities.FireGenVariables;
import net.noahf.firegen.discord.config.MultiObjectConfiguration;
import net.noahf.firegen.discord.incidents.structure.units.RadioChannelImpl;
import net.noahf.firegen.discord.utilities.Log;

import static net.noahf.firegen.discord.utilities.JsonUtilities.asInt;
import static net.noahf.firegen.discord.utilities.JsonUtilities.asStr;

public class ConfigRadioChannels extends MultiObjectConfiguration<RadioChannel> {

    public ConfigRadioChannels(FireGenVariables vars) {
        super(null, RadioChannel.class, vars.municipality() + "/" + vars.radioChannelsFile());
    }

    @Override
    public void importObject(JsonElement e) {
        JsonArray array = e.getAsJsonArray();
        for (JsonElement element : array.asList()) {
            JsonObject object = element.getAsJsonObject();

            String name = asStr(object, "name");
            String alphaTag = asStr(object, "alpha_tag");
            int talkgroupId = asInt(object, "talkgroup_id");

            RadioChannel channel = new RadioChannelImpl(name, alphaTag, talkgroupId);

            this.add(channel);
        }

        Log.info("Imported " + this.count() + " radio channels.");
    }
}
