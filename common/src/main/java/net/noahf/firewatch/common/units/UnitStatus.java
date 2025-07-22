package net.noahf.firewatch.common.units;

import java.util.function.Function;
import java.util.regex.Pattern;

public enum UnitStatus {

    OUT_OF_SERVICE ((str) -> "OOS"),

    IN_SERVICE ((str) -> "clear"),

    RESPONDING ((str) -> "responding"),

    ON_SCENE ((str) -> "on-scene"),

    TRANSPORTING_SECONDARY ((str) -> "transporting " + str),

    ARRIVED_SECONDARY ((str) -> "arrived " + str);

    private final Function<String, String> narrativeSuffix;

    UnitStatus(Function<String, String> narrativeSuffix) {
        this.narrativeSuffix = narrativeSuffix;
    }

    public String narrativeSuffix(String input) { return this.narrativeSuffix.apply(input); }

    @Override
    public String toString() {
        return Pattern.compile("\\b[a-z]")
                .matcher(this.name().toLowerCase().replaceAll("_", " "))
                .replaceAll(mr -> mr.group().toUpperCase());
    }
}
