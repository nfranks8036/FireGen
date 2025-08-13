package net.noahf.firewatch.common.loader;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.noahf.firewatch.common.data.IncidentStructure;
import net.noahf.firewatch.common.data.IncidentType;
import net.noahf.firewatch.common.units.Agency;
import net.noahf.firewatch.common.units.AgencyManager;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class JsonImporter {

    public static final String STRUCTURE = "incident_structure.json";
    public static final String AGENCIES = "agencies.json";


    private final ClassLoader loader = this.getClass().getClassLoader();
    private final String folder;

    private IncidentStructure incidentStructure;
    private AgencyManager agencyManager;

    public JsonImporter(String folder) {
        this.folder = folder;
    }

    public IncidentStructure importedIncidentStructure() {
        if (this.incidentStructure != null) {
            return this.incidentStructure;
        }

        final String incidentStructure = folder + File.separator + STRUCTURE;
        try (
                InputStream stream = loader.getResourceAsStream(incidentStructure)
        ) {
            if (stream == null) {
                throw new FileNotFoundException(incidentStructure);
            }
            try (InputStreamReader input = new InputStreamReader(stream)
            ) {
                Gson gson = new GsonBuilder().setPrettyPrinting().create();

                IncidentStructure structure = gson.fromJson(input, IncidentStructure.class);

                stream.close();
                input.close();

                for (IncidentType type : structure.incidentTypes()) {
                    type.finalizeDeserialize();
                }

                return this.incidentStructure = structure;
            }
        } catch (Exception exception) {
            throw new RuntimeException("Failed to get a valid incident structure from json '" + incidentStructure + "': "+ exception, exception);
        }
    }

    public AgencyManager importedAgencyManager() {
        final String agencies = folder + File.separator + AGENCIES;
        try (
                InputStream stream = loader.getResourceAsStream(agencies)
        ) {
            if (stream == null) {
                throw new FileNotFoundException(agencies);
            }
            try (InputStreamReader input = new InputStreamReader(stream)
            ) {
                Gson gson = new GsonBuilder().setPrettyPrinting().create();

                List<Agency> agencyList = gson.fromJson(input, new TypeToken<ArrayList<Agency>>(){}.getType());

                stream.close();
                input.close();

                return this.agencyManager = new AgencyManager(agencyList);
            }
        } catch (Exception exception) {
            throw new RuntimeException("Failed to get a valid incident structure from json '" + incidentStructure + "': "+ exception, exception);
        }
    }

}