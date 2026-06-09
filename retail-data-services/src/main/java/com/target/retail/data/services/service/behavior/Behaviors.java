package com.target.retail.data.services.service.behavior;

import com.target.retail.data.services.exception.InducedFailureException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.function.Supplier;

@Component
public class Behaviors {

    @Value("${behaviors.random-failing.failure-rate:.05}")
    private double failingBehaviorFailureRate;

    @Value("${behaviors.slow-response.min-delay-ms:1000}")
    private int slowResponseBehaviorMinimumDelay;

    @Value("${behaviors.slow-response.max-delay-ms:10000}")
    private int slowResponseBehaviorMaximumDelay;

    @Value("${DEFAULT_BEHAVIOR:NORMAL}")
    private BehaviorType configuredDefaultBehavior;

    private final Map<String,InducedBehavior> behaviorMap;

    private Random random;

    public Behaviors() {

        random = new Random();
        behaviorMap = new HashMap<>();
        behaviorMap.put(BehaviorType.NORMAL.name(), this::callWithNoInducedBehavior);
        behaviorMap.put(BehaviorType.SLOW_RESPONSE.name(), this::callWithSlowResponse);
        behaviorMap.put(BehaviorType.RANDOM_FAILURES.name(), this::callWithRandomFailures);
    }

    public Behaviors(BehaviorType defaultBehavior, double failingBehaviorFailureRate, int slowResponseBehaviorMinimumDelay, int slowResponseBehaviorMaximumDelay) {
        this();
        this.configuredDefaultBehavior = defaultBehavior;
        this.failingBehaviorFailureRate = failingBehaviorFailureRate;
        this.slowResponseBehaviorMinimumDelay = slowResponseBehaviorMinimumDelay;
        this.slowResponseBehaviorMaximumDelay = slowResponseBehaviorMaximumDelay;
    }

    public Behaviors(BehaviorType defaultBehavior, double failingBehaviorFailureRate, int slowResponseBehaviorMinimumDelay, int slowResponseBehaviorMaximumDelay, Random random) {
        this(defaultBehavior, failingBehaviorFailureRate, slowResponseBehaviorMinimumDelay, slowResponseBehaviorMaximumDelay);
        this.random = random;
    }

    public InducedBehavior getConfiguredBehavior() {
       return getBehavior(configuredDefaultBehavior)
               .orElseThrow(() ->   new RuntimeException("default behaviour not configured"));
    }

    public Optional<InducedBehavior> getBehavior(BehaviorType type) {
        return Optional.ofNullable(behaviorMap.get(type.name()));
    }

    private <T> T callWithSlowResponse(Supplier<T> supplier) {
        try {
            int delay = random.nextInt(slowResponseBehaviorMinimumDelay, slowResponseBehaviorMaximumDelay);
            Thread.sleep(delay);
        }catch(InterruptedException interruptedException) {
            //hmmm ? // I miss Kotlin! :)
        }
        return supplier.get();
    }

    private <T> T callWithNoInducedBehavior(Supplier<T> supplier) {
        return supplier.get();
    }

    private <T> T callWithRandomFailures(Supplier<T> supplier) {

        if(random.nextDouble() < failingBehaviorFailureRate) {
            throw new InducedFailureException("Failure to call the service. Please try later");
        }
        return supplier.get();
    }

}
