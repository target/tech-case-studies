package com.target.retail.product.service.behavior;

import java.util.function.Supplier;

@FunctionalInterface
public interface InducedBehavior {
    <T> T execute(Supplier<T> supplier);
}
