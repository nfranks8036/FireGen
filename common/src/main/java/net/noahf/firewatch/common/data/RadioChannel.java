package net.noahf.firewatch.common.data;

import net.noahf.firewatch.common.data.objects.StructureObject;

public class RadioChannel extends StructureObject {

    private final String radioChannel;

    RadioChannel(String radioChannel) {
        this.radioChannel = radioChannel;
    }

    @Override public String getName() { return radioChannel; }
    @Override public String getFormatted() { return radioChannel; }

}