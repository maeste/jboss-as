package org.jboss.as.test.integration.jca.workmanager.distributed;

import org.jboss.logging.Logger;

import javax.resource.spi.work.Work;
import java.io.Serializable;

public class ShortWork implements Work, Serializable {
    private static final Logger log = Logger.getLogger(LongWork.class.getCanonicalName());

    private String name;

    public ShortWork setName(String name) {
        this.name = name;
        return this;
    }

    @Override
    public void release() {
        // do nothing
    }

    @Override
    public void run() {
        log.info("Started and finished work " + name + " on node "
                + System.getProperty("jboss.node.name"));
    }
}
