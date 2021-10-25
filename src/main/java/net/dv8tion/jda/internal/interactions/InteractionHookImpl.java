package net.dv8tion.jda.internal.interactions;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.interactions.ChannelInteractionHook;
import net.dv8tion.jda.api.interactions.Interaction;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.utils.MiscUtil;
import net.dv8tion.jda.internal.requests.restaction.TriggerRestAction;
import net.dv8tion.jda.internal.utils.JDALogger;

import javax.annotation.Nonnull;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.ReentrantLock;

public class InteractionHookImpl implements InteractionHook
{
    public static final String TIMEOUT_MESSAGE = "Timed out waiting for interaction acknowledgement";
    private final InteractionImpl interaction;
    private final List<TriggerRestAction<?>> readyCallbacks = new LinkedList<>();
    private final Future<?> timeoutHandle;
    private final ReentrantLock mutex = new ReentrantLock();
    private Exception exception;
    private boolean isReady;
    protected final JDA api;

    //This is used to give a proper error when an interaction is ack'd twice
    // By default, discord only responds with "unknown interaction" which is horrible UX so we add a check manually here
    private volatile boolean isAck;

    public InteractionHookImpl(@Nonnull InteractionImpl interaction, @Nonnull JDA api)
    {
        this.api = api;
        this.interaction = interaction;
        // 10 second timeout for our failure
        this.timeoutHandle = api.getGatewayPool().schedule(() -> this.fail(new TimeoutException(TIMEOUT_MESSAGE)), 10, TimeUnit.SECONDS);
    }

    public synchronized boolean ack()
    {
        boolean wasAck = isAck;
        this.isAck = true;
        return wasAck;
    }

    public synchronized boolean isAck()
    {
        return isAck;
    }

    public void ready()
    {
        MiscUtil.locked(mutex, () -> {
            timeoutHandle.cancel(false);
            isReady = true;
            readyCallbacks.forEach(TriggerRestAction::run);
        });
    }

    public void fail(Exception exception)
    {
        MiscUtil.locked(mutex, () -> {
            if (!isReady && this.exception == null)
            {
                this.exception = exception;
                if (!readyCallbacks.isEmpty()) // only log this if we even tried any responses
                {
                    if (exception instanceof TimeoutException)
                        JDALogger.getLog(ChannelInteractionHook.class).warn("Up to {} Interaction Followup Messages Timed out! Did you forget to acknowledge the interaction?", readyCallbacks.size());
                    readyCallbacks.forEach(callback -> callback.fail(exception));
                }
            }
        });
    }

    private <T extends TriggerRestAction<R>, R> T onReady(T runnable)
    {
        return MiscUtil.locked(mutex, () -> {
            if (isReady)
                runnable.run();
            else if (exception != null)
                runnable.fail(exception);
            else
                readyCallbacks.add(runnable);
            return runnable;
        });
    }

    @Nonnull
    @Override
    public Interaction getInteraction()
    {
        return interaction;
    }

    @Nonnull
    @Override
    public JDA getJDA()
    {
        return api;
    }
}
