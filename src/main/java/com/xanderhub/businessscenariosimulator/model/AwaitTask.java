package com.xanderhub.businessscenariosimulator.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.Entity;
import javax.persistence.Transient;
import java.util.concurrent.TimeUnit;

@Entity
@Getter
@SuperBuilder
@NoArgsConstructor
public class AwaitTask extends Task {

    @Transient
    private final Logger LOG = LoggerFactory.getLogger(AwaitTask.class);
    private Long duration;

    @Override
    protected void execute() {
        if (duration != null)
            try {
                TimeUnit.SECONDS.sleep(duration);
            } catch (InterruptedException e) {
                LOG.debug("Task - {} - shut down", getName());
                setIsStopped(true);
            }
    }
}
