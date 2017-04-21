/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2010, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.as.test.integration.jca.workmanager.distributed;

import javax.naming.NamingException;
import javax.resource.spi.work.Work;
import javax.resource.spi.work.WorkException;
import java.io.IOException;

import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.as.arquillian.api.ServerSetup;
import org.jboss.logging.Logger;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Tests distributed work manager and whether it really distributes work over multiple nodes. Test cases use two servers
 * both with a deployed resource adapter configured to use the DWM.
 *
 * Work is scheduled via a stateless ejb proxy. This allows us to schedule work from within the test, without the need
 * to marshall anything not serializable (such as the resource adapter).
 */
@ServerSetup(DwmAlwaysTestCase.DwmAlwaysServerSetupTask.class)
@RunWith(Arquillian.class)
@RunAsClient
public class DwmAlwaysTestCase extends AbstractDwmTestCase {

    private static Logger log = Logger.getLogger(DwmAlwaysTestCase.class.getCanonicalName());

    static class DwmAlwaysServerSetupTask extends DwmServerSetupTask {
        @Override
        protected Policy getPolicy() {
            return Policy.ALWAYS;
        }

        @Override
        protected Selector getSelector() {
            return Selector.FIRST_AVAILABLE;
        }
    }

    /**
     * Executes a long work instances on a single node and verifies that it took enough time and executed on the
     * expected node (the other one since Policy.ALWAYS)
     */
    @Test
    @InSequence(1)
    public void testDoWork() throws IOException, NamingException, WorkException, InterruptedException {
        log.info("Running testDoWork()");

        long startTime = System.currentTimeMillis();
        int doWorkAccepted = server2Proxy.getDoWorkAccepted();
        int distributedDoWorkAccepted = server1Proxy.getDistributedDoWorkAccepted();

        server1Proxy.doWork(new LongWork().setName("testDoWork-work1"));

        Assert.assertTrue("Expected time >= " + (startTime + LongWork.WORK_TIMEOUT) + ", actual: " + System.currentTimeMillis(),
                startTime + LongWork.WORK_TIMEOUT <= System.currentTimeMillis());

        logWorkStats();

        Assert.assertTrue("Expected doWorkAccepted = " + (doWorkAccepted + 1) + " but was: " + server2Proxy.getDoWorkAccepted(),
                server2Proxy.getDoWorkAccepted() == doWorkAccepted + 1);
        Assert.assertTrue("Expected distributedDoWorkAccepted = " + (distributedDoWorkAccepted + 1) + " but was: " + server1Proxy.getDistributedDoWorkAccepted(),
                server1Proxy.getDistributedDoWorkAccepted() == distributedDoWorkAccepted + 1);
    }

    /**
     * Submits a few (less than our max threads) long work instances and verifies that
     * {@link org.jboss.jca.core.api.workmanager.DistributedWorkManager#startWork(Work)} returns sooner than the time
     * needed for the work items to actually finish. Also verifies that the work was started on the expected node
     * (the other one since Policy.ALWAYS).
     */
    @Test
    @InSequence(11)
    public void testStartWork() throws IOException, NamingException, WorkException, InterruptedException {
        log.info("Running testStartWork()");

        long startTime = System.currentTimeMillis();
        int startWorkAccepted = server2Proxy.getStartWorkAccepted();
        int distributedStartWorkAccepted = server1Proxy.getDistributedStartWorkAccepted();

        server1Proxy.startWork(new LongWork().setName("testStartWork-work1"));

        Assert.assertTrue("Expected time < " + (startTime + LongWork.WORK_TIMEOUT) + ", actual: " + System.currentTimeMillis(),
                startTime + LongWork.WORK_TIMEOUT > System.currentTimeMillis());

        Thread.sleep(LongWork.WORK_TIMEOUT); // wait for the started work to finish, so it doesn't mess up our statistics for other tests
        logWorkStats();

        Assert.assertTrue("Expected startWorkAccepted = " + (startWorkAccepted + 1) + " but was: " + server2Proxy.getStartWorkAccepted(),
                server2Proxy.getStartWorkAccepted() == startWorkAccepted + 1);
        Assert.assertTrue("Expected distributedStartWorkAccepted = " + (distributedStartWorkAccepted + 1) + " but was: " + server1Proxy.getDistributedStartWorkAccepted(),
                server1Proxy.getDistributedStartWorkAccepted() == distributedStartWorkAccepted + 1);
    }

    /**
     * Schedules several (one more than our max threads) long work instances and verifies that
     * {@link org.jboss.jca.core.api.workmanager.DistributedWorkManager#scheduleWork(Work)} returns sooner than the time
     * needed for the work items to actually finish. Also verifies that work was executed on both nodes (Policy.ALWAYS
     * selects the other node first, then the first node is selected because we have Selector.MAX_FREE_THREADS).
     */
    @Test
    @InSequence(21)
    public void testScheduleWork() throws WorkException, InterruptedException {
        log.info("Running testScheduleWork()");

        long startTime = System.currentTimeMillis();
        int scheduleWorkAcceptedServer2 = server2Proxy.getScheduleWorkAccepted();
        int scheduleWorkAcceptedServer1 = server1Proxy.getScheduleWorkAccepted();
        int distributedScheduleWorkAccepted = server1Proxy.getDistributedScheduleWorkAccepted();

        for (int i = 0; i < SRT_MAX_THREADS + 1; i++) {
            server1Proxy.scheduleWork(new LongWork().setName("testScheduleWork-work" + (i + 1)));
        }

        Assert.assertTrue("Expected time < " + (startTime + LongWork.WORK_TIMEOUT) + ", actual: " + System.currentTimeMillis(),
                startTime + LongWork.WORK_TIMEOUT > System.currentTimeMillis());

        Thread.sleep(LongWork.WORK_TIMEOUT * 2 + 100); // wait for the scheduled work to start and finish, so it doesn't mess up our statistics for other tests
        logWorkStats();

        Assert.assertTrue("Expected scheduleWorkAccepted = " + (scheduleWorkAcceptedServer2 + SRT_MAX_THREADS + 1) + " but was: " + server2Proxy.getScheduleWorkAccepted(),
                (server2Proxy.getScheduleWorkAccepted() == scheduleWorkAcceptedServer2 + SRT_MAX_THREADS) &&
                (server1Proxy.getScheduleWorkAccepted() == scheduleWorkAcceptedServer1 + 1));
        Assert.assertTrue("Expected distributedScheduleWorkAccepted = " + (distributedScheduleWorkAccepted + SRT_MAX_THREADS + 1) + " but was: " + server1Proxy.getDistributedScheduleWorkAccepted(),
                server1Proxy.getDistributedScheduleWorkAccepted() == distributedScheduleWorkAccepted + SRT_MAX_THREADS + 1);
    }

    /**
     * Does a few instances of short work with {@code policy = ALWAYS} and expects that they will be executed on a
     * remote node.
     */
    @Test
    @InSequence(31)
    public void testAlwaysPolicy() throws IOException, NamingException, WorkException {
        log.info("Running testAlwaysPolicy()");

        int doWorkAccepted = server2Proxy.getDoWorkAccepted();

        server1Proxy.doWork(new ShortWork().setName("testAlwaysPolicy-work1"));
        server1Proxy.doWork(new ShortWork().setName("testAlwaysPolicy-work2"));

        logWorkStats();

        Assert.assertTrue("Expected doWorkAccepted = " + (doWorkAccepted + 2) + ", actual: " + server2Proxy.getDoWorkAccepted(),
                server2Proxy.getDoWorkAccepted() == doWorkAccepted + 2);
    }

    @Test
    @InSequence(101)
    public void logFinalStats() {
        log.info("Running logFinalStats()");
        logWorkStats();
    }
}
