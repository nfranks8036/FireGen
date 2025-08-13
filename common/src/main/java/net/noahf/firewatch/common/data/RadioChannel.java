package net.noahf.firewatch.common.data;

import net.noahf.firewatch.common.data.objects.StructureObject;

public class RadioChannel extends StructureObject {

    private final String radioChannel;

    RadioChannel(String radioChannel) {
        this.radioChannel = radioChannel;
    }

    @Override public String name() { return radioChannel; }
    @Override public String formatted() { return radioChannel; }

}