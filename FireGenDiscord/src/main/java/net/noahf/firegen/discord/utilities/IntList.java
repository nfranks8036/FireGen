package net.noahf.firegen.discord.utilities;

import lombok.Getter;
import net.dv8tion.jda.api.components.selections.SelectOption;
import net.noahf.firegen.api.incidents.units.Unit;
import net.noahf.firegen.discord.config.files.ConfigUnits;
import net.noahf.firegen.discord.incidents.structure.units.UnitImpl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;

@Getter
public class IntList {

    private final String key;
    private final List<Integer> integers;

    public IntList(String key, String parse) {
        if (!parse.contains("IntList")) {
            throw new IllegalArgumentException("Not a stringified IntList.");
        }

        String[] options = parse.substring("IntList[".length(), parse.length()-1).split("->");
        if (options.length != 2) {
            throw new IllegalArgumentException("Expected an arrow between values (e.g., '1->10').");
        }

        this.key = key;
        this.integers = new ArrayList<>(
                IntStream.range(Integer.parseInt(options[0]), Integer.parseInt(options[1])+1)
                        .boxed().toList()
        );
    }

    public boolean isInValue(int integer) {
        return integers.contains(integer);
    }

    @Override
    public String toString() {
        return "IntList{key=" + key + "}[" + String.join(",", integers.stream().map(Object::toString).toList()) + "]";
    }
}