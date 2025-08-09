package net.noahf.firewatch.common.newincidents;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.JsonAdapter;
import net.noahf.firewatch.common.newincidents.lists.StructureList;
import net.noahf.firewatch.common.newincidents.lists.StructureObject;
import net.noahf.firewatch.common.newincidents.medical.EmsMedical;
import net.noahf.firewatch.common.newincidents.medical.EmsMedicalDeserializer;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class IncidentStructure implements StructureObject {

    public static IncidentStructure create(String file) {
        Class<IncidentStructure> ic = IncidentStructure.class;
        try (
                InputStream stream = ic.getClassLoader().getResourceAsStream(file)
        ) {
            if (stream == null) {
                throw new FileNotFoundException(file);
            }
            try (InputStreamReader input = new InputStreamReader(stream)
            ) {
                Gson gson = new GsonBuilder().setPrettyPrinting().create();

                IncidentStructure structure = gson.fromJson(input, IncidentStructure.class);

                stream.close();
                input.close();

                structure.postDeserialize();

                return structure;
            }
        } catch (Exception exception) {
            throw new RuntimeException("Failed to get a valid incident structure from json '" + file + "': "+ exception, exception);
        }
    }

    private String municipality;

    private List<IncidentType> incident_types;

    private List<UnitStatus> unit_statuses;

    private List<UnitType> unit_types;

    private List<String> caller_types;

    @JsonAdapter(EmsMedicalDeserializer.class)
    private EmsMedical ems_medical;

    private List<String> agency_types;



    @Override
    public String getName() {
        return this.municipality;
    }

    @Override
    public String getFormatted() {
        return this.municipality.replace("_", " ");
    }


    public String getMunicipality() { return this.municipality; }

    public StructureList<IncidentType> getIncidentTypes() { return new StructureList<>(this.incident_types); }

    public StructureList<UnitStatus> getUnitStatuses() { return new StructureList<>(this.unit_statuses); }

    public List<String> getCallerTypes() { return new ArrayList<>(this.caller_types); }

    public EmsMedical getEmsMedical() { return this.ems_medical; }

    public List<String> getAgencyTypes() { return new ArrayList<>(this.agency_types); }

    public StructureList<IncidentPriority> getIncidentPriorities() { return new StructureList<>(IncidentType.allPriorities); }


    private void postDeserialize() {
        for (IncidentType type : this.incident_types) {
            type.postDeserialize();
        }
    }
}
