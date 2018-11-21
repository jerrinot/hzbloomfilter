package info.jerrinot.hzbloomfilter;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;

public final class BloomFilter {
    private final IMap<String, com.google.common.hash.BloomFilter> map;
    private final String name;
    private final BloomFilterConfig config;

    public static BloomFilter newFilter(BloomFilterConfig config, HazelcastInstance instance) {
        return new BloomFilter(instance, config);
    }

    private BloomFilter(HazelcastInstance instance, BloomFilterConfig config) {
        this.map = instance.getMap(config.getBackingMap());
        this.name = config.getName();
        this.config = config;
    }

    public boolean put(int value) {
        boolean bitsChanged = (boolean) map.executeOnKey(name, new PutEP(value, config));
        return bitsChanged;
    }

    public boolean mightContain(int value) {
        boolean mightContain = (boolean) map.executeOnKey(name, new MightContainEP(value));
        return mightContain;
    }
}
