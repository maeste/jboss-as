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
 * both with a deployed resource adapter configured to use the DWM. Tests with WATERMARK policy.
 */
@ServerSetup(DwmWatermarkTestCase.DwmWatermarkServerSetupTask.class)
@RunWith(Arquillian.class)
@RunAsClient
public class DwmWatermarkTestCase extends AbstractDwmTestCase {

    private static Logger log = Logger.getLogger(DwmWatermarkTestCase.class.getCanonicalName());

    private static final int WATERMARK_MAX_THREADS = 2;

    static class DwmWatermarkServerSetupTask extends AbstractDwmTestCase.DwmServerSetupTask {
        @Override
        protected Policy getPolicy() {
            return Policy.WATERMARK;
        }

        @Override
        protected Selector getSelector() {
            return Selector.MAX_FREE_THREADS;
        }

        @Override
        protected int getWatermarkPolicyOption() {
            return WATERMARK_MAX_THREADS - 1;
        }

        @Override
        protected int getSrtMaxThreads() {
            return WATERMARK_MAX_THREADS;
        }

        @Override
        protected int getSrtQueueLength() {
            return WATERMARK_MAX_THREADS;
        }
    }

    /**
     * Executes a long work instances on a single node and verifies that it took enough time and executed on the
     * expected node (this one since Policy.WATERMARK)
     */
    @Test
    @InSequence(1)
    public void testDoWork() throws IOException, NamingException, WorkException, InterruptedException {
        log.info("Running testDoWork()");

        long startTime = System.currentTimeMillis();
        int doWorkAccepted = server1Proxy.getDoWorkAccepted();
        int distributedDoWorkAccepted = server2Proxy.getDistributedDoWorkAccepted();

        server1Proxy.doWork(new LongWork().setName("testDoWork-work1"));

        Assert.assertTrue("Expected time >= " + (startTime + LongWork.WORK_TIMEOUT) + ", actual: " + System.currentTimeMillis(),
                startTime + LongWork.WORK_TIMEOUT <= System.currentTimeMillis());

        logWorkStats();

        Assert.assertTrue("Expected doWorkAccepted = " + (doWorkAccepted + 1) + " but was: " + server1Proxy.getDoWorkAccepted(),
                server1Proxy.getDoWorkAccepted() == doWorkAccepted + 1);
        Assert.assertTrue("Expected distributedDoWorkAccepted = " + (distributedDoWorkAccepted + 1) + " but was: " + server2Proxy.getDistributedDoWorkAccepted(),
                server2Proxy.getDistributedDoWorkAccepted() == distributedDoWorkAccepted + 1);
    }

    /**
     * Schedules several (one more than our max threads) long work instances and verifies that
     * {@link org.jboss.jca.core.api.workmanager.DistributedWorkManager#scheduleWork(Work)} returns sooner than the time
     * needed for the work items to actually finish.
     *
     * Also verifies that work was executed on both nodes (Policy.WATERMARK will select the local node once, then we hit
     * the watermark limit, and the other node is selected.
     */
    @Test
    @InSequence(11)
    public void testWatermarkPolicy() throws WorkException, InterruptedException {
        log.info("Running testWatermarkPolicy()");

        long startTime = System.currentTimeMillis();
        int scheduleWorkAcceptedServer1 = server1Proxy.getScheduleWorkAccepted();
        int scheduleWorkAcceptedServer2 = server2Proxy.getScheduleWorkAccepted();
        int distributedScheduleWorkAccepted = server1Proxy.getDistributedScheduleWorkAccepted();

        for (int i = 0; i < WATERMARK_MAX_THREADS; i++) {
            server1Proxy.scheduleWork(new LongWork().setName("testWatermarkPolicy-work" + (i + 1)));
        }

        Assert.assertTrue("Expected time < " + (startTime + LongWork.WORK_TIMEOUT) + ", actual: " + System.currentTimeMillis(),
                startTime + LongWork.WORK_TIMEOUT > System.currentTimeMillis());

        Thread.sleep(LongWork.WORK_TIMEOUT * 2 + 100); // wait for the scheduled work to start and finish, so it doesn't mess up our statistics for other tests
        logWorkStats();

        Assert.assertTrue("Expected scheduleWorkAccepted = "
                        + (scheduleWorkAcceptedServer1 + 1) + "/" + (scheduleWorkAcceptedServer2 + 1)
                        + " but was: " + server1Proxy.getScheduleWorkAccepted() + "/" + server2Proxy.getScheduleWorkAccepted(),
                (server1Proxy.getScheduleWorkAccepted() == scheduleWorkAcceptedServer1 + 1) &&
                        (server2Proxy.getScheduleWorkAccepted() == scheduleWorkAcceptedServer2 + 1));
        Assert.assertTrue("Expected distributedScheduleWorkAccepted = " + (distributedScheduleWorkAccepted + WATERMARK_MAX_THREADS)
                        + " but was: " + server2Proxy.getDistributedScheduleWorkAccepted(),
                server2Proxy.getDistributedScheduleWorkAccepted() == distributedScheduleWorkAccepted + WATERMARK_MAX_THREADS);
    }

    @Test
    @InSequence(101)
    public void logFinalStats() {
        log.info("Running logFinalStats()");
        logWorkStats();
    }
}
