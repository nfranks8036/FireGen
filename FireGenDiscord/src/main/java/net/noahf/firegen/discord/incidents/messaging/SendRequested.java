package net.noahf.firegen.discord.incidents.messaging;

import lombok.Getter;
import net.noahf.firegen.discord.utilities.Log;
import org.jetbrains.annotations.Nullable;

public class SendRequested {

    public static SendRequested createOrIgnore(@Nullable SendRequested old, long sendAfterMs, Runnable runTask) {
        if (old != null && !old.consumed) {
            return old;
        }
        return new SendRequested(
                sendAfterMs, runTask
        );
    }

    private @Getter long sendAt;
    private final Runnable runTask;
    private final Thread thread;
    private @Getter boolean consumed;

    SendRequested(long sendAfterMs, Runnable runTask) {
        this.sendAt = System.currentTimeMillis() + sendAfterMs;
        this.runTask = runTask;
        this.consumed = false;
        this.thread = this.generateThread();
        this.thread.start();
    }

    public void cancel() {
        this.consumed = true;
    }

    public void hardInterrupt() {
        this.consumed = true;
        this.thread.interrupt();
    }

    @SuppressWarnings("BusyWait")
    private Thread generateThread() {
        return new Thread(() -> {
            while (!this.consumed) {
                try {
                    if (System.currentTimeMillis() >= this.sendAt) {
                        this.runTask.run();
                        this.consumed = true;
                        Thread.currentThread().interrupt();
                        return;
                    }
                    Thread.sleep(1000);
                } catch (Exception e) {
                    e.printStackTrace(System.err);
                    Thread.currentThread().interrupt();
                }
            }
            Thread.currentThread().interrupt();
        });
    }

}
