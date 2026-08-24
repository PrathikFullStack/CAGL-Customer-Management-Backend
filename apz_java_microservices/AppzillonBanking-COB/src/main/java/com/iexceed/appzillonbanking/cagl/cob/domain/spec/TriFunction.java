package com.iexceed.appzillonbanking.cagl.cob.domain.spec;

@FunctionalInterface
public interface TriFunction<A, B, C, R> {
    R apply(A a, B b, C c);
}