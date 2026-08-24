package com.iexceed.appzillonbanking.cagl.cob.utils;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

@Component
public class SequenceIdGeneratorService {

    private static final int DEFAULT_BLOCK_SIZE = 1;

    @PersistenceContext
    private EntityManager entityManager;

    private final Map<String, Deque<Long>> pools = new ConcurrentHashMap<>();
    private final Map<String, ReentrantLock> locks = new ConcurrentHashMap<>();

    public String nextValueAsString(String sequenceName) {
        return String.valueOf(nextValue(sequenceName));
    }

    public long nextValue(String sequenceName) {
        return nextValue(sequenceName, DEFAULT_BLOCK_SIZE);
    }

    public long nextValue(String sequenceName, int blockSize) {
        ReentrantLock lock = locks.computeIfAbsent(sequenceName, k -> new ReentrantLock());
        lock.lock();
        try {
            Deque<Long> pool = pools.computeIfAbsent(sequenceName, k -> new ArrayDeque<>());
            if (pool.isEmpty()) {
                pool.addAll(fetchBlock(sequenceName, blockSize));
            }
            return pool.poll();
        } finally {
            lock.unlock();
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    protected List<Long> fetchBlock(String sequenceName, int blockSize) {
        @SuppressWarnings("unchecked")
        List<Number> raw = entityManager.createNativeQuery("""
            SELECT nextval(CAST(:seq AS regclass)) FROM generate_series(1, :size)
            """)
                .setParameter("seq", sequenceName)
                .setParameter("size", blockSize)
                .getResultList();

        return raw.stream().map(Number::longValue).toList();
    }
}