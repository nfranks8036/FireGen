package net.noahf.firegen.discord.utilities;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class ImmutablePair<A, B> {

    public static <F, S> ImmutablePair<F, S> of(F first, S second) {
        return new ImmutablePair<>(first, second);
    }

    private @Getter A firstElement;
    private @Getter B secondElement;

}
