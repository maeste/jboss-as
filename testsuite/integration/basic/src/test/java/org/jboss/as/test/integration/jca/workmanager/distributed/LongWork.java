package org.jboss.as.test.integration.jca.workmanager.distributed;

import org.jboss.logging.Logger;

import javax.resource.spi.work.Work;
import java.io.Serializable;
import java.util.concurrent.TimeUnit;

public class LongWork implements Work, Serializable {
    private static final Logger log = Logger.getLogger(LongWork.class.getCanonicalName());

    private boolean quit = false;
    private long workTimeout = 5000L; // 5 seconds
    private long sleepStep = 100L; // 0.1 seconds
    private String name;

    public LongWork setName(String name) {
        this.name = name;
        return this;
    }

    @Override
    public void release() {
        quit = true;
    }

    @Override
    public void run() {
        long startTime = System.currentTimeMillis();
        long finishTime = startTime + workTimeout;

        log.info("Starting work " + name + " on node "
                + System.getProperty("jboss.node.name"));

        while (!quit) {
            try {
                Thread.sleep(sleepStep);
            } catch (InterruptedException e) {
                log.error("Was interrupted while waiting for work to finish", e);
            }
            if (System.currentTimeMillis() >= finishTime) quit = true;
        }

        log.info("Finishing work " + name + " after " +
                TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - startTime) + " seconds");
    }
}
