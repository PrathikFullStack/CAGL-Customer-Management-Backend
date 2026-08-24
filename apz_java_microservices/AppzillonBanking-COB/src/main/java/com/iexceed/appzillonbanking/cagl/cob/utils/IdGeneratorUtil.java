package com.iexceed.appzillonbanking.cagl.cob.utils;

import com.iexceed.appzillonbanking.cagl.cob.constants.ApplicationConstants;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Generates application_id values shaped like the sample data
 * ("CO1928939192398129"): a fixed prefix followed by an epoch-millis
 * timestamp and a short random suffix, so IDs stay sortable and
 * collision-safe without needing a dedicated DB sequence call round-trip
 * per request.
 */
public final class IdGeneratorUtil {

    private static final SecureRandom RANDOM = new SecureRandom();

    private IdGeneratorUtil() {
    }

    public static String generateApplicationId(String branchId, String kmId) {
        final long timestamp = System.currentTimeMillis();
//        String firstFive = String.valueOf(timestamp).substring(0, 5);
        String branchNumber = branchId.replaceAll("\\D", "");
        String kmNumber = kmId.replaceAll("\\D", "");

        branchNumber = String.format("%d", Long.parseLong(branchNumber));
        kmNumber = String.format("%d", Long.parseLong(kmNumber));
        int random = ThreadLocalRandom.current().nextInt(100, 1000);

        return "CO" + branchNumber + kmNumber + timestamp + random;
    }
}
