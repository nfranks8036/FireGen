package net.noahf.firegen.discord.utilities;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@Getter
public class ImmutablePair<A, B> {

    public static <F, S> ImmutablePair<F, S> of(F first, S second) {
        return new ImmutablePair<>(first, second);
    }

    public static <I> List<ImmutablePair<Integer, I>> withIndices(List<I> items) {
        List<ImmutablePair<Integer, I>> result = new ArrayList<>();
        for (int i = 0; i < items.size(); i++) {
            result.add(of(i, items.get(i)));
        }
        return result;
    }

    private @Getter A firstElement;
    private @Getter B secondElement;

}
