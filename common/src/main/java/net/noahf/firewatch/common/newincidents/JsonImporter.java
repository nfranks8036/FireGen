package net.noahf.firewatch.common.newincidents;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class JsonImporter {

    public static final String STRUCTURE = "incident_structure.json";
    public static final String AGENCIES = "agencies.json";

    private final String folder;

    private final IncidentStructure incidentStructure;

    public JsonImporter(String folder) {
        this.folder = folder;

        final ClassLoader loader = this.getClass().getClassLoader();
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

                structure.postDeserialize();

                this.incidentStructure = structure;
            }
        } catch (Exception exception) {
            throw new RuntimeException("Failed to get a valid incident structure from json '" + incidentStructure + "': "+ exception, exception);
        }


    }

}
