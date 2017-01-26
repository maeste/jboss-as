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

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.container.test.api.TargetsContainer;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.as.arquillian.api.ServerSetup;
import org.jboss.as.arquillian.api.ServerSetupTask;
import org.jboss.as.arquillian.container.ManagementClient;
import org.jboss.as.controller.client.ModelControllerClient;
import org.jboss.as.test.integration.jca.rar.DistributedConnection1;
import org.jboss.as.test.integration.management.util.MgmtOperationException;
import org.jboss.as.test.integration.management.util.ModelUtil;
import org.jboss.as.test.shared.ServerReload;
import org.jboss.as.test.shared.TestSuiteEnvironment;
import org.jboss.dmr.ModelNode;
import org.jboss.logging.Logger;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.ResourceAdapterArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.wildfly.test.api.Authentication;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Properties;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.*;

/**
 * TODO: doc, make sure to include the description of the EJB mechanism of admin object lookup and why that's so
 */
@ServerSetup(DwmTest.DwmServerSetupTask.class)
@RunWith(Arquillian.class)
@RunAsClient
public class DwmTest {

    private static Logger log = Logger.getLogger(DwmTest.class.getCanonicalName());

    private static final String DEFAULT_DWM_NAME = "newdwm";
    private static final String DEFAULT_CONTEXT_NAME = "customContext1";
    private static final ModelNode DEFAULT_DWM_ADDRESS = new ModelNode()
            .add(SUBSYSTEM, "jca")
            .add("distributed-workmanager", DEFAULT_DWM_NAME);
    private static final String DEPLOYMENT_0 =  "deployment-0";
    private static final String DEPLOYMENT_1 =  "deployment-1";
    private static final String CONTAINER_0 = "container-0";
    private static final String CONTAINER_1 = "container-1";

    private enum Policy {
        ALWAYS,
        NEVER,
        WATERMARK
    }

    private enum Selector {
        FIRST_AVAILABLE,
        MAX_FREE_THREADS,
        PING_TIME
    }

    private static ModelControllerClient client1;
    private static ModelControllerClient client2;

    static {
        client1 = createClient1();
        try {
            client2 = createClient2();
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
    }

    private static ModelControllerClient createClient1() {
        return TestSuiteEnvironment.getModelControllerClient();
    }

    private static ModelControllerClient createClient2() throws UnknownHostException {
        return ModelControllerClient.Factory.create(InetAddress.getByName(TestSuiteEnvironment.getServerAddress()),
                TestSuiteEnvironment.getServerPort() + 100,
                Authentication.getCallbackHandler());
    }

    @Deployment(name = DEPLOYMENT_0)
    @TargetsContainer(CONTAINER_0)
    public static Archive<?> deploy0 () {
        return createDeployment();
    }

    @Deployment(name = DEPLOYMENT_1)
    @TargetsContainer(CONTAINER_1)
    public static Archive<?> deploy1 () {
        return createDeployment();
    }

    private static Archive<?> createDeployment() {
        JavaArchive jar = createLibJar();
        ResourceAdapterArchive rar = createResourceAdapterArchive();
        JavaArchive ejbJar = createEjbJar();

        EnterpriseArchive ear = ShrinkWrap.create(EnterpriseArchive.class, "dwmtest.ear");
        ear.addAsLibrary(jar).addAsModule(rar).addAsModule(ejbJar);
        ear.addAsManifestResource(DwmTest.class.getPackage(), "application.xml", "application.xml");

        return ear;
    }

    private static JavaArchive createLibJar() {
        JavaArchive jar = ShrinkWrap.create(JavaArchive.class, "lib.jar");
        jar.addClass(DwmTest.class)
                .addClass(LongWork.class).addClass(ShortWork.class)
                .addPackage(DistributedConnection1.class.getPackage());
        jar.addAsManifestResource(new StringAsset("Dependencies: javax.inject.api,org.jboss.as.connector,"
                + "org.jboss.as.controller,org.jboss.dmr,org.jboss.as.cli,org.jboss.staxmapper,"
                + "org.jboss.ironjacamar.impl\n"), "MANIFEST.MF");
        return jar;
    }

    private static ResourceAdapterArchive createResourceAdapterArchive() {
        ResourceAdapterArchive rar = ShrinkWrap.create(ResourceAdapterArchive.class, "dwm.rar");
        rar.addAsManifestResource(DwmTest.class.getPackage(), "ra-distributed.xml", "ra.xml")
                .addAsManifestResource(DwmTest.class.getPackage(), "ironjacamar-distributed-1.xml",
                        "ironjacamar.xml")
                .addAsManifestResource(
                        new StringAsset(
                                "Dependencies: org.jboss.as.controller-client,org.jboss.dmr,org.jboss.as.cli,org.jboss.as.connector \n"),
                        "MANIFEST.MF");
        return rar;
    }

    private static JavaArchive createEjbJar() {
        JavaArchive ejbJar = ShrinkWrap.create(JavaArchive.class, "ejb.jar");
        ejbJar.addClass(DwmAdminObjectEjb.class).addClass(DwmAdminObjectEjbImpl.class);
        ejbJar.addAsManifestResource(DwmTest.class.getPackage(), "ejb-jar.xml", "ejb-jar.xml")
                .addAsManifestResource(new StringAsset("Dependencies: org.jboss.ironjacamar.api"), "MANIFEST.MF");
        return ejbJar;
    }

    static class DwmServerSetupTask implements ServerSetupTask {
        @Override
        public void setup(ManagementClient managementClient, String containerId) throws Exception {
            log.info("Setting up " + containerId);
            setUpServer(containerId);
        }

        @Override
        public void tearDown(ManagementClient managementClient, String containerId) throws Exception {
            log.info("Tearing down " + containerId);
            tearDownServer(containerId);
        }

        private static void setUpServer(String containerId) throws Exception {
            ModelControllerClient mcc = CONTAINER_0.equals(containerId) ? client1 : client2;
            int serverPort = CONTAINER_0.equals(containerId) ? TestSuiteEnvironment.getServerPort() : TestSuiteEnvironment.getServerPort() + 100;

            ModelNode addBasicDwm = addBasicDwm();
            ModelNode setUpPolicy = setUpPolicy(Policy.ALWAYS);
            ModelNode setUpSelector = setUpSelector(Selector.MAX_FREE_THREADS);
            ModelNode setUpShortRunningThreads = setUpShortRunningThreads(10, 10);

            ModelNode compositeOp = ModelUtil.createCompositeNode(
                    new ModelNode[] {addBasicDwm, setUpPolicy, setUpSelector, setUpShortRunningThreads});
            ModelNode result = mcc.execute(compositeOp);
            log.info("Setting up Dwm: " + result);

            result = mcc.execute(setUpCustomContext());
            log.info("Setting up CustomContext: " + result);

            ServerReload.executeReloadAndWaitForCompletion(mcc, 60000, false,
                    TestSuiteEnvironment.getServerAddress(), serverPort);
        }

        private static void tearDownServer(String containerId) throws Exception {
            ModelControllerClient mcc = CONTAINER_0.equals(containerId) ? client1 : client2;
            int serverPort = CONTAINER_0.equals(containerId) ? TestSuiteEnvironment.getServerPort() : TestSuiteEnvironment.getServerPort() + 100;

            ModelNode removeDwm = new ModelNode();
            removeDwm.get(OP_ADDR).set(DEFAULT_DWM_ADDRESS);
            removeDwm.get(OP).set(REMOVE);

            ModelNode removeContext = new ModelNode();
            removeContext.get(OP_ADDR).set((new ModelNode())
                    .add(SUBSYSTEM, "jca")
                    .add("bootstrap-context", DEFAULT_CONTEXT_NAME));
            removeContext.get(OP).set(REMOVE);

            ModelNode compositeOp = ModelUtil.createCompositeNode(
                    new ModelNode[] { removeDwm, removeContext });
            mcc.execute(compositeOp);
            ServerReload.executeReloadAndWaitForCompletion(mcc, 60000, false,
                    TestSuiteEnvironment.getServerAddress(), serverPort);
        }

        private static ModelNode addBasicDwm() throws IOException, MgmtOperationException {
            ModelNode setUpDwm = new ModelNode();

            setUpDwm.get(OP_ADDR).set(DEFAULT_DWM_ADDRESS);
            setUpDwm.get(OP).set(ADD);
            setUpDwm.get(NAME).set(DEFAULT_DWM_NAME);

            return setUpDwm;
        }

        private static ModelNode setUpPolicy(Policy policy) throws IOException, MgmtOperationException {
            ModelNode setUpPolicy = new ModelNode();

            setUpPolicy.get(OP_ADDR).set(DEFAULT_DWM_ADDRESS);
            setUpPolicy.get(OP).set(WRITE_ATTRIBUTE_OPERATION);
            setUpPolicy.get(NAME).set("policy");
            setUpPolicy.get(VALUE).set(policy.toString());

            return setUpPolicy;
        }

        private static ModelNode setUpSelector(Selector selector) throws IOException, MgmtOperationException {
            ModelNode setUpSelector = new ModelNode();

            setUpSelector.get(OP_ADDR).set(DEFAULT_DWM_ADDRESS);
            setUpSelector.get(OP).set(WRITE_ATTRIBUTE_OPERATION);
            setUpSelector.get(NAME).set("selector");
            setUpSelector.get(VALUE).set(selector.toString());

            return setUpSelector;
        }

        private static ModelNode setUpShortRunningThreads(int maxThreads, int queueLength) throws IOException, MgmtOperationException {
            ModelNode setUpSrt = new ModelNode();

            // the thread pool name must be the same as the DWM it belongs to
            setUpSrt.get(OP_ADDR).set(DEFAULT_DWM_ADDRESS.clone().add("short-running-threads", DEFAULT_DWM_NAME));
            setUpSrt.get(OP).set(ADD);
            setUpSrt.get(MAX_THREADS).set(maxThreads);
            setUpSrt.get("queue-length").set(queueLength);

            return setUpSrt;
        }

        private static ModelNode setUpCustomContext() {
            ModelNode setUpCustomContext = new ModelNode();

            setUpCustomContext.get(OP_ADDR).set(new ModelNode()
                    .add(SUBSYSTEM, "jca")
                    .add("bootstrap-context", DEFAULT_CONTEXT_NAME));
            setUpCustomContext.get(OP).set(ADD);
            setUpCustomContext.get(NAME).set(DEFAULT_CONTEXT_NAME);
            setUpCustomContext.get("workmanager").set(DEFAULT_DWM_NAME);

            return setUpCustomContext;
        }
    }

    @Test
    public void testDwmSetup() throws Exception {
        log.info("Started testDwmSetup");

        ModelNode result = readAttribute(client1, DEFAULT_DWM_ADDRESS, "name");
        log.info("Name of the default Dwm: " + result);

        ModelNode readResource1 = new ModelNode();
        readResource1.get(OP_ADDR).set(DEFAULT_DWM_ADDRESS);
        readResource1.get(OP).set(READ_RESOURCE_OPERATION);

        log.info("Dwm resource: " + client1.execute(readResource1));

        result = readAttribute(client1, DEFAULT_DWM_ADDRESS, "selector");
        log.info("Selector of the default Dwm: " + result);

        result = readAttribute(client1, DEFAULT_DWM_ADDRESS, "policy");
        log.info("Policy of the default Dwm: " + result);

        DwmAdminObjectEjb server1Proxy = lookupAdminObject(TestSuiteEnvironment.getServerAddress(), "8080");
        Assert.assertNotNull(server1Proxy);
        DwmAdminObjectEjb server2Proxy = lookupAdminObject(TestSuiteEnvironment.getServerAddress(), "8180");
        Assert.assertNotNull(server2Proxy);
        log.info("Attempting to obtain statistics");
        log.info("isDoWorkDistributionEnabled: " + server1Proxy.isDoWorkDistributionEnabled());
        log.info("doWorkAccepted: " + server1Proxy.getDoWorkAccepted());

        int workIteration = 30;
        log.info("submitting " + workIteration + " long and " + workIteration + " short work instances (server 1)");
        for (int i = 0; i < workIteration; i++) {
            log.info("Starting long work " + i);
            server1Proxy.scheduleWork(new LongWork().setName("longWorkS1" + i));
            log.info("Starting short work " + i);
            server1Proxy.scheduleWork(new ShortWork().setName("shortWorkS1" + i));
        }
        log.info("work scheduled");

        logWorkStats(server1Proxy, server2Proxy);
        Thread.sleep(20000);
        logWorkStats(server1Proxy, server2Proxy);

        workIteration = 15;
        log.info("submitting " + workIteration + " long and " + workIteration + " short work instances (server2)");
        for (int i = 0; i < workIteration; i++) {
            log.info("Starting long work " + i);
            server2Proxy.scheduleWork(new LongWork().setName("longWorkS2" + i));
            log.info("Starting short work " + i);
            server2Proxy.scheduleWork(new ShortWork().setName("shortWorkS2" + i));
        }
        log.info("work scheduled");

        logWorkStats(server1Proxy, server2Proxy);
        Thread.sleep(20000);
        logWorkStats(server1Proxy, server2Proxy);

        log.info("Finished testDwmSetup");
    }

    private DwmAdminObjectEjb lookupAdminObject(String address, String port) throws NamingException {
        Properties properties = new Properties();
        properties.put(Context.INITIAL_CONTEXT_FACTORY, "org.jboss.naming.remote.client.InitialContextFactory");
        properties.put(Context.PROVIDER_URL, String.format("%s%s:%s", "http-remoting://", address, port));
        Context context = new InitialContext(properties);

        String ejbExportedName = String.format("%s/%s/%s!%s", "dwm-ejb-application", "dwm-ejb-module",
                DwmAdminObjectEjbImpl.class.getSimpleName(), DwmAdminObjectEjb.class.getCanonicalName());
        return (DwmAdminObjectEjb) context.lookup(ejbExportedName);
    }

    private void logWorkStats(DwmAdminObjectEjb server1Proxy, DwmAdminObjectEjb server2Proxy) {
        log.info("doWorkAccepted (server 1): " + server1Proxy.getDoWorkAccepted());
        log.info("doWorkAccepted (server 2): " + server2Proxy.getDoWorkAccepted());
        log.info("doWorkRejected (server 1): " + server1Proxy.getDoWorkRejected());
        log.info("doWorkRejected (server 2): " + server2Proxy.getDoWorkRejected());

        log.info("startWorkAccepted (server 1): " + server1Proxy.getStartWorkAccepted());
        log.info("startWorkAccepted (server 2): " + server2Proxy.getStartWorkAccepted());
        log.info("startWorkRejected (server 1): " + server1Proxy.getStartWorkRejected());
        log.info("startWorkRejected (server 2): " + server2Proxy.getStartWorkRejected());

        log.info("scheduleWorkAccepted (server 1): " + server1Proxy.getScheduleWorkAccepted());
        log.info("scheduleWorkAccepted (server 2): " + server2Proxy.getScheduleWorkAccepted());
        log.info("scheduleWorkRejected (server 1): " + server1Proxy.getScheduleWorkRejected());
        log.info("scheduleWorkRejected (server 2): " + server2Proxy.getScheduleWorkRejected());
    }

    private ModelNode readAttribute(ModelControllerClient mcc, ModelNode address, String attributeName) throws Exception {
        ModelNode op = new ModelNode();
        op.get(OP).set(READ_ATTRIBUTE_OPERATION);
        op.get(NAME).set(attributeName);
        op.get(OP_ADDR).set(address);
        return mcc.execute(op);
    }
}
