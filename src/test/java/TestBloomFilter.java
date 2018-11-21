import com.google.common.collect.Sets;
import com.hazelcast.config.Config;
import com.hazelcast.config.InMemoryFormat;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import info.jerrinot.hzbloomfilter.BloomFilter;
import info.jerrinot.hzbloomfilter.BloomFilterConfig;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

public class TestBloomFilter {
    private static final String FILTER_NAME = "myFilter";
    private static final String BACKING_MAP_NAME = "myMap";
    private static final int EXPECTED_INSERTIONS = 10_000;
    private static final double FPP = 0.01;

    private static BloomFilterConfig FILTER_CONFIG = new BloomFilterConfig(FILTER_NAME, BACKING_MAP_NAME, EXPECTED_INSERTIONS, FPP);

    private HazelcastInstance instance1;
    private HazelcastInstance instance2;


    @Before
    public void setUp() {
        instance1 = Hazelcast.newHazelcastInstance(newConfig());
        instance2 = Hazelcast.newHazelcastInstance(newConfig());
    }

    private Config newConfig() {
        Config config = new Config();

        // let's use the OBJECT memory format - we are doing many small modifications to the filter.
        // performance of the BINARY format wouldnt be great due expensive serialization/deserialization
        config.getMapConfig(BACKING_MAP_NAME).setInMemoryFormat(InMemoryFormat.OBJECT);
        return config;
    }

    @After
    public void tearDown() {
        instance1.shutdown();
        instance2.shutdown();
    }

    @Test
    public void smokeTest() {
        int keySpace = 1_000_000;
        int keyCount = 10_000;

        Set<Integer> keys = generateKeys(keySpace, keyCount);

        //inserting values by using first Hazelcast instance
        insertValues(instance1, keys);

        //checking error rate by using another instance
        checkErrorRate(instance2, keySpace, keys);
    }

    private static void checkErrorRate(HazelcastInstance instance, int keySpace, Set<Integer> keys) {
        System.out.println("Validating error rate");
        BloomFilter filter2 = BloomFilter.newFilter(FILTER_CONFIG, instance);
        int falsePositive = 0;
        int falseNegative = 0;
        for (int key = 0; key < keySpace; key++) {
            if (key % 10000 == 0) {
                System.out.println("At " + key);
            }

            boolean expectedOutcome = keys.contains(key);
            boolean actualOutcome = filter2.mightContain(key);

            if (expectedOutcome != actualOutcome) {
                if (actualOutcome) {
                    falsePositive++;
                } else {
                    falseNegative++;
                }
            }
        }
        System.out.println("False positives: " + falsePositive + ", False negatives: " + falseNegative);
    }

    private static void insertValues(HazelcastInstance instance, Set<Integer> values) {
        System.out.println("Inserting " + values.size() + " values");
        BloomFilter filter1 = BloomFilter.newFilter(FILTER_CONFIG, instance);
        for (Integer key : values) {
            filter1.put(key);
        }
    }

    private static Set<Integer> generateKeys(int keySpace, int keyCount) {
        Set<Integer> keys = Sets.newHashSetWithExpectedSize(keyCount);
        while (keys.size() != keyCount) {
            keys.add(ThreadLocalRandom.current().nextInt(keySpace));
        }
        return keys;
    }
}
