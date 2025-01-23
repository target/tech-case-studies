package com.target.retail.data.services.service.behavior;

import java.util.function.Supplier;

@FunctionalInterface
public interface InducedBehavior {
    <T> T execute(Supplier<T> supplier);
}
