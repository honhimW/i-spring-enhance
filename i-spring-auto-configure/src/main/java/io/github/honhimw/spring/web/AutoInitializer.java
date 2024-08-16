package io.github.honhimw.spring.web;

import jakarta.annotation.Nonnull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationListener;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author hon_him
 * @since 2024-07-25
 */

@Slf4j
public class AutoInitializer implements ApplicationContextAware {

    private final List<Map.Entry<String, Runnable>> tasks = new CopyOnWriteArrayList<>();

    public AutoInitializer addTask(String name, Runnable task) {
        tasks.add(Map.entry(name, task));
        return this;
    }

    @Override
    public void setApplicationContext(@Nonnull ApplicationContext applicationContext) throws BeansException {
        if (!tasks.isEmpty()) {
            log.info("There are ({}) Auto-Initialize tasks.", tasks.size());
            for (Map.Entry<String, Runnable> task : tasks) {
                String key = task.getKey();
                Runnable value = task.getValue();
                log.info("Initialize task: [{}]", key);
                value.run();
            }
        }
    }

}
