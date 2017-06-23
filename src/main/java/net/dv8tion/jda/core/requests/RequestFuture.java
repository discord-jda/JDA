package net.dv8tion.jda.core.requests;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Future;

public interface RequestFuture<T> extends Future<T>, CompletionStage<T>
{
    /**
     * <b>This method is unsupported by the current implementation!</b>
     *
     * <p>{@inheritDoc}
     */
    @Override
    public CompletableFuture<T> toCompletableFuture();
}
