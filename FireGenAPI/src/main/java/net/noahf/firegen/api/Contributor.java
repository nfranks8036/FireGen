package net.noahf.firegen.api;

import java.util.UUID;

public interface Contributor extends Identifiable {

    String getName();

    String getDisplayName();

    Object getUserObject();

}
