package info.jerrinot.hzbloomfilter;

import com.google.common.hash.BloomFilter;
import com.hazelcast.core.ReadOnly;
import com.hazelcast.map.EntryBackupProcessor;
import com.hazelcast.map.EntryProcessor;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.DataSerializable;

import java.io.IOException;
import java.util.Map;

final class MightContainEP implements EntryProcessor<String, BloomFilter<Integer>>, ReadOnly, DataSerializable {
    private int value;

    MightContainEP() {
        // used during deserialization
    }

    MightContainEP(int value) {
        this.value = value;
    }

    @Override
    public Boolean process(Map.Entry<String, BloomFilter<Integer>> entry) {
        BloomFilter<Integer> filter = entry.getValue();
        if (filter == null) {
            // the filter does not exist -> it's empty
            return false;
        }
        return filter.mightContain(value);
    }

    @Override
    public EntryBackupProcessor<String, BloomFilter<Integer>> getBackupProcessor() {
        // this is read-only operation - we have no backup to execute
        return null;
    }

    @Override
    public void writeData(ObjectDataOutput out) throws IOException {
        out.writeInt(value);
    }

    @Override
    public void readData(ObjectDataInput in) throws IOException {
        value = in.readInt();
    }
}
