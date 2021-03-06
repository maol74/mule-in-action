package com.muleinaction;

import org.mule.api.service.Service;
import org.mule.api.MuleMessage;
import org.mule.api.context.notification.EndpointMessageNotificationListener;
import org.mule.api.context.notification.ServerNotification;
import org.mule.tck.FunctionalTestCase;
import org.mule.module.client.MuleClient;
import org.mule.context.notification.EndpointMessageNotification;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.WildcardFileFilter;

import java.io.File;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.Iterator;

/**
 * @author John D'Emic (john.demic@gmail.com)
 */
public class HttpOutboundFunctionalTestCase extends FunctionalTestCase {

    private static String DEST_DIRECTORY = "./data/reports";

    private CountDownLatch latch = new CountDownLatch(1);

    @Override
    protected String getConfigResources() {
        return "conf/http-outbound-config.xml";
    }

    protected void doSetUp() throws Exception {
        super.doSetUp();

        for (Object o :  FileUtils.listFiles(new File(DEST_DIRECTORY), new String[]{"xml"}, false)) {
            File file = (File) o;
            file.delete();
        }

        muleContext.registerListener(new EndpointMessageNotificationListener() {
            public void onNotification(final ServerNotification notification) {
                if ("httpInboundService".equals(notification.getResourceIdentifier())) {
                    final EndpointMessageNotification messageNotification = (EndpointMessageNotification) notification;
                    if (messageNotification.getEndpoint().contains("reports")) {
                        latch.countDown();
                    }
                }
            }
        });
    }

    public void testCorrectlyInitialized() throws Exception {
        final Service service = muleContext.getRegistry().lookupService(
                "httpOutboundService");

        assertNotNull(service);
        assertEquals("httpOutboundModel", service.getModel().getName());
    }

    public void testMessageSentAndReceived() throws Exception {
        File destDir = new File(DEST_DIRECTORY);
        
        assertEquals(0, FileUtils.listFiles(destDir, new WildcardFileFilter("*.xml"), null).size());
        assertTrue("Message did not reach directory on time", latch.await(15, TimeUnit.SECONDS));
        waitForDirNotEmpty(destDir);
        assertEquals(1, FileUtils.listFiles(destDir, new WildcardFileFilter("*.xml"), null).size());
        File file = (File) FileUtils.listFiles(destDir,
                new WildcardFileFilter("*.xml"), null).toArray()[0];
        assertEquals(XML, FileUtils.readFileToString(file));
    }
    
    private void waitForDirNotEmpty(File destDir) throws Exception {
        while(FileUtils.listFiles(destDir, new WildcardFileFilter("*.xml"), null).size() == 0) {
            Thread.sleep(250);
        }
    }

    private static String XML = "<backup><host>esb01</host></backup>\n";

}
