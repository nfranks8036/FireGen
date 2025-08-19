package net.noahf.firewatch.common.shortcuts;

import java.util.function.Function;

public class ChainedMethod<P, N> {

    private ChainedMethod<?, P> previous;
    private Function<MethodContext<P>, MethodContext<N>> modify;

    public ChainedMethod(ChainedMethod<?, P> previous, Function<MethodContext<P>, MethodContext<N>> modify) {
        this.previous = previous;
        this.modify = modify;
    }



}
