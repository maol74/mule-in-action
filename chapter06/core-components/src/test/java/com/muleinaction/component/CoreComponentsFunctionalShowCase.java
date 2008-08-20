package com.muleinaction.component;

import org.mule.api.MuleMessage;
import org.mule.module.client.MuleClient;
import org.mule.tck.FunctionalTestCase;
import org.mule.transport.NullPayload;

import com.muleinaction.test.BuilderBean;
import com.muleinaction.test.StringTargetComponent;

/**
 * @author David Dossot (david@dossot.net)
 */
public class CoreComponentsFunctionalShowCase extends FunctionalTestCase {

    private StringTargetComponent target;

    private MuleClient muleClient;

    @Override
    protected String getConfigResources() {
        return "conf/core-components-config.xml";
    }

    @Override
    protected void doSetUp() throws Exception {
        setDisposeManagerPerSuite(true);

        super.doSetUp();

        muleClient = new MuleClient(muleContext);

        target =
                (StringTargetComponent) muleContext.getRegistry().lookupObject(
                        "StringTargetComponent");

        target.reset();
    }

    public void testExplicitBridge() throws Exception {
        doTestComponent("vm://ExplicitBridge.In", "Hello world!", true, false);
    }

    public void testImplicitBridge() throws Exception {
        doTestComponent("vm://ImplicitBridge.In", "Hello world!", true, false);
    }

    public void testEcho() throws Exception {
        doTestComponent("vm://Echo.In", "Hello world!", true, false);
    }

    public void testLog() throws Exception {
        doTestComponent("vm://Log.In", NullPayload.getInstance().toString(),
                false, false);
    }

    public void testNull() throws Exception {
        doTestComponent("vm://Null.In", NullPayload.getInstance().toString(),
                false, true);
    }

    public void testStatic() throws Exception {
        doTestComponent("vm://Static.In", "All I hear is static", true, false);
    }

    public void testBuilder() throws Exception {
        final BuilderBean bb = new BuilderBean();

        final MuleMessage response =
                muleClient.send("vm://Builder.In", bb, null);

        assertNotNull(response);
        assertSame(bb, response.getPayload());
        assertEquals(123, bb.getI().intValue());
        assertEquals("ABC", bb.getS());
    }

    private void doTestComponent(final String inboundUri,
            final String expectedResponse, final boolean expectedToReachTarget,
            final boolean willGetException) throws Exception {

        final MuleMessage response = muleClient.send(inboundUri, "Hello", null);

        assertNotNull(response);
        assertEquals(expectedResponse, response.getPayloadAsString());

        if (expectedToReachTarget) {
            assertEquals(expectedResponse, target.getValue());
        } else {
            assertNull(target.getValue());
        }

        assertEquals(willGetException, response.getExceptionPayload() != null);
    }

}