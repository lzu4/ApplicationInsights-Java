package com.microsoft.applicationinsights.smoketest;

import java.util.List;

import com.microsoft.applicationinsights.internal.schemav2.Data;
import com.microsoft.applicationinsights.internal.schemav2.Envelope;
import com.microsoft.applicationinsights.internal.schemav2.RemoteDependencyData;
import com.microsoft.applicationinsights.internal.schemav2.RequestData;
import org.junit.Test;

import static com.microsoft.applicationinsights.smoketest.matchers.RequestDataMatchers.hasName;
import static org.hamcrest.Matchers.hasItem;
import static org.junit.Assert.*;

@UseAgent
public class JmsTest extends AiSmokeTest {

    @Test
    @TargetUri("/sendMessage")
    public void doMostBasicTest() throws Exception {
        List<Envelope> rdList = mockedIngestion.waitForItems("RequestData", 2);
        List<Envelope> rddList = mockedIngestion.waitForItemsInRequest("RemoteDependencyData", 2);

        Envelope rdEnvelope1 = rdList.get(0);
        Envelope rdEnvelope2 = rdList.get(1);
        Envelope rddEnvelope1 = rddList.get(0);
        Envelope rddEnvelope2 = rddList.get(1);

        RequestData rd1 = (RequestData) ((Data) rdEnvelope1.getData()).getBaseData();
        RequestData rd2 = (RequestData) ((Data) rdEnvelope2.getData()).getBaseData();
        RemoteDependencyData rdd1 =
                (RemoteDependencyData) ((Data) rddEnvelope1.getData()).getBaseData();
        RemoteDependencyData rdd2 =
                (RemoteDependencyData) ((Data) rddEnvelope2.getData()).getBaseData();

        assertEquals("GET /sendMessage", rd1.getName());
        assertEquals("HelloController.sendMessage", rdd2.getName());
        assertEquals("queue/message send", rdd1.getName());
        assertEquals("queue/message receive", rd2.getName());

        assertParentChild(rdd2.getId(), rdEnvelope1, rddEnvelope1);
        assertParentChild(rdd1.getId(), rddEnvelope2, rdEnvelope2);
    }

    private static void assertParentChild(
            String parentId, Envelope parentEnvelope, Envelope childEnvelope) {
        String operationId = parentEnvelope.getTags().get("ai.operation.id");

        assertNotNull(operationId);

        assertEquals(operationId, childEnvelope.getTags().get("ai.operation.id"));
        assertEquals(parentId, childEnvelope.getTags().get("ai.operation.parentId"));
    }
}
