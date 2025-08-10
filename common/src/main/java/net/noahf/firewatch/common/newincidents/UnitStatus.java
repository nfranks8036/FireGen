package net.noahf.firewatch.common.newincidents;

import net.noahf.firewatch.common.newincidents.objects.StructureObject;

import java.util.regex.Pattern;

public class UnitStatus implements StructureObject {

    private String name;
    private String narrative;


    @Override public String getName() { return this.name; }
    @Override public String getFormatted() {
        return Pattern.compile("\\b[a-z]")
                .matcher(this.getName().toLowerCase().replaceAll("_", " "))
                .replaceAll(mr -> mr.group().toUpperCase());
    }

    public String getNarrative(String parameter) {
        if (!this.narrative.contains("{0}") || parameter == null || parameter.isBlank()) {
            return this.narrative;
        }
        return this.narrative.replace("{0}", parameter);
    }

}
