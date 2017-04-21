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
@ServerSetup(DwmNeverTestCase.DwmNeverServerSetupTask.class)
@RunWith(Arquillian.class)
@RunAsClient
public class DwmNeverTestCase extends AbstractDwmTestCase {

    private static Logger log = Logger.getLogger(DwmNeverTestCase.class.getCanonicalName());

    static class DwmNeverServerSetupTask extends AbstractDwmTestCase.DwmServerSetupTask {
        @Override
        protected Policy getPolicy() {
            return Policy.NEVER;
        }

        @Override
        protected Selector getSelector() {
            return Selector.MAX_FREE_THREADS;
        }
    }

    /**
     * Schedules several (one more than our max threads) long work instances and verifies that
     * {@link org.jboss.jca.core.api.workmanager.DistributedWorkManager#scheduleWork(Work)} returns sooner than the time
     * needed for the work items to actually finish.
     *
     * Also verifies that work was executed on the local node (Policy.NEVER only selects the local node, regardless of
     * the selector).
     */
    @Test
    @InSequence(1)
    public void testScheduleWork() throws WorkException, InterruptedException {
        log.info("Running testScheduleWork()");

        long startTime = System.currentTimeMillis();
        int scheduleWorkAcceptedServer1 = server1Proxy.getScheduleWorkAccepted();
        int scheduleWorkAcceptedServer2 = server2Proxy.getScheduleWorkAccepted();
        int distributedScheduleWorkAccepted = server2Proxy.getDistributedScheduleWorkAccepted();

        for (int i = 0; i < SRT_MAX_THREADS + 1; i++) {
            server1Proxy.scheduleWork(new LongWork().setName("testScheduleWork-work" + (i + 1)));
        }

        Assert.assertTrue("Expected time < " + (startTime + LongWork.WORK_TIMEOUT) + ", actual: " + System.currentTimeMillis(),
                startTime + LongWork.WORK_TIMEOUT > System.currentTimeMillis());

        Thread.sleep(LongWork.WORK_TIMEOUT * 2 + 100); // wait for the scheduled work to start and finish, so it doesn't mess up our statistics for other tests
        logWorkStats();

        Assert.assertTrue("Expected scheduleWorkAccepted = " + (scheduleWorkAcceptedServer1 + SRT_MAX_THREADS + 1) + " but was: " + server1Proxy.getScheduleWorkAccepted(),
                (server1Proxy.getScheduleWorkAccepted() == scheduleWorkAcceptedServer1 + SRT_MAX_THREADS + 1) &&
                        (server2Proxy.getScheduleWorkAccepted() == scheduleWorkAcceptedServer2));
        Assert.assertTrue("Expected distributedScheduleWorkAccepted = " + (distributedScheduleWorkAccepted + SRT_MAX_THREADS + 1) + " but was: " + server2Proxy.getDistributedScheduleWorkAccepted(),
                server2Proxy.getDistributedScheduleWorkAccepted() == distributedScheduleWorkAccepted + SRT_MAX_THREADS + 1);
    }

    /**
     * Does a few instances of short work with {@code policy = NEVER} and expects that they will be executed on the node
     * where started.
     */
    @Test
    @InSequence(11)
    public void testNeverPolicy() throws IOException, NamingException, WorkException {
        log.info("Running testAlwaysPolicy()");

        int doWorkAccepted = server1Proxy.getDoWorkAccepted();

        for (int i = 0; i < 10; i++) {
            server1Proxy.doWork(new ShortWork().setName("testNeverPolicy-work" + (i + 1)));
        }

        logWorkStats();

        Assert.assertTrue("Expected doWorkAccepted = " + (doWorkAccepted + 10) + ", actual: " + server1Proxy.getDoWorkAccepted(),
                server1Proxy.getDoWorkAccepted() == doWorkAccepted + 10);
    }

    @Test
    @InSequence(101)
    public void logFinalStats() {
        log.info("Running logFinalStats()");
        logWorkStats();
    }
}
