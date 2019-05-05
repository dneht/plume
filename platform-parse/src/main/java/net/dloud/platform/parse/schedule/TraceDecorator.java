package net.dloud.platform.parse.schedule;

import net.dloud.platform.parse.context.LocalContext;
import org.springframework.core.task.TaskDecorator;

/**
 * @author QuDasheng
 * @create 2019-05-03 18:00
 **/
public class TraceDecorator implements TaskDecorator {

    @Override
    public Runnable decorate(Runnable runnable) {
        final LocalContext local = LocalContext.load();
        return () -> {
            try {
                LocalContext.set(local);
                runnable.run();
            } finally {
                LocalContext.remove();
            }
        };
    }
}
