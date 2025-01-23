package com.target.retail.data.services.service.behavior;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.Random;


public class BehaviorsTest {


    private static final double failingFailureRate = 0.5;

    private static final int slowResponseBehaviorMinimumDelay = 500;

    private static final int slowResponseBehaviorMaximumDelay = 2000;


    @Test
    public void testNormalBehavior() {
        Behaviors testInstance = new Behaviors(BehaviorType.NORMAL, failingFailureRate, slowResponseBehaviorMinimumDelay, slowResponseBehaviorMaximumDelay);

        try {
            long t1 = System.currentTimeMillis();
            for (int i = 10; i < 20; i++) {
                final int localVar = i;
                testInstance.getConfiguredBehavior().execute( () -> someRandomCodeForTheTest(localVar));
            }
            long t2 = System.currentTimeMillis();
            assertTrue((t2 - t1) < 1000,"Unexpectedly long time for the test: Time taken (ms) = "+(t2 - t1));
        }catch(Exception e) {
            fail("Unexpected exception for normal behavior",e );
        }

    }

    @Test
    public void testRandomFailureBehavior() {
        Behaviors testInstance = new Behaviors(BehaviorType.RANDOM_FAILURES, failingFailureRate, slowResponseBehaviorMinimumDelay, slowResponseBehaviorMaximumDelay);

        int failureCount = 0;
        int successCount = 0;
        for (int i = 10; i < 20; i++) {
            try {

                final int localVar = i;
                testInstance.getConfiguredBehavior().execute(() -> someRandomCodeForTheTest(localVar));
                successCount++;
            } catch (Exception e) {
                failureCount++;
            }
        }


        assertTrue(failureCount > 0 && successCount > 0, "Failures or success not seen for Random failure behavior. (success=" + successCount + ", failures=" + failureCount + ")");
    }


    @Test
    public void testSlowResponseBehavior() {
        Behaviors testInstance = new Behaviors(BehaviorType.SLOW_RESPONSE, failingFailureRate, slowResponseBehaviorMinimumDelay, slowResponseBehaviorMaximumDelay);
        long t1 = System.currentTimeMillis();
        testInstance.getConfiguredBehavior().execute( () -> someRandomCodeForTheTest(2231343)); // some random seed
        long t2 = System.currentTimeMillis();
        assertTrue((t2 - t1) >= slowResponseBehaviorMinimumDelay, "Unexpected response time : Time in ms = "+(t2 - t1));

    }

    private double someRandomCodeForTheTest(int i) {
        Random random = new Random(i);
        double d = random.nextGaussian();
        System.out.println("random.nextGaussian()::" + d);
        return d;
    }


}
