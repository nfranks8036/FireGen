package net.noahf.firegen.discord.config.files;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.noahf.firegen.api.incidents.SystemMunicipality;
import net.noahf.firegen.api.utilities.FireGenVariables;
import net.noahf.firegen.discord.config.SingleObjectConfiguration;
import net.noahf.firegen.discord.incidents.SystemMunicipalityImpl;
import net.noahf.firegen.discord.utilities.JsonUtilities;
import net.noahf.firegen.discord.utilities.Log;

import static net.noahf.firegen.discord.utilities.JsonUtilities.asStr;

public class ConfigMunicipality extends SingleObjectConfiguration<SystemMunicipality> {

    public ConfigMunicipality(FireGenVariables vars) {
        super(vars, vars.municipality() + "/" + vars.municipalityFile());
    }

    @Override
    public void importObject(JsonElement e) {
        JsonObject main = e.getAsJsonObject();
        JsonObject state = JsonUtilities.element(main, "state", false).getAsJsonObject();

        this.set(new SystemMunicipalityImpl(
                asStr(main, "municipality"),
                asStr(main, "short"),
                asStr(main, "dispatch_name"),
                new SystemMunicipalityImpl.StateImpl(
                        asStr(state, "name"),
                        asStr(state, "abbreviation")
                )
        ));

        Log.info("Imported municipality " + this.get());
    }
}
