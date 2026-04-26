package net.noahf.firegen.api.utilities;

import org.jetbrains.annotations.NotNull;

import java.util.stream.IntStream;

public interface AutofilledCharSequence extends CharSequence {

    @NotNull
    @Override
    String toString();

    @Override
    default int length() {
        return this.toString().length();
    }

    @Override
    default char charAt(int index) {
        return this.toString().charAt(index);
    }

    @Override
    default boolean isEmpty() {
        return this.toString().isEmpty();
    }

    @NotNull
    @Override
    default CharSequence subSequence(int start, int end) {
        return this.toString().subSequence(start, end);
    }

    @NotNull
    @Override
    default IntStream chars() {
        return this.toString().chars();
    }

    @NotNull
    @Override
    default IntStream codePoints() {
        return this.toString().codePoints();
    }
}
