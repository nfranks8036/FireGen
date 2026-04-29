package net.noahf.firegen.discord.utilities;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public abstract class Manager<T extends Manager<?>> {

    private final Class<T> clazz;
    private final String name;

}
