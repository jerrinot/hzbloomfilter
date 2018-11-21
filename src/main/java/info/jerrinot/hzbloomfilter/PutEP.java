package info.jerrinot.hzbloomfilter;

import com.google.common.hash.BloomFilter;
import com.google.common.hash.Funnels;
import com.hazelcast.map.EntryBackupProcessor;
import com.hazelcast.map.EntryProcessor;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.DataSerializable;

import java.io.IOException;
import java.util.Map;

final class PutEP implements EntryProcessor<String, BloomFilter<Integer>>, DataSerializable {
    private int value;
    private BloomFilterConfig config;

    PutEP() {
        //used during deserializaiton
    }

    PutEP(int value, BloomFilterConfig config) {
        this.value = value;
        this.config = config;
    }

    public Boolean process(Map.Entry<String, BloomFilter<Integer>> entry) {
        BloomFilter<Integer> filter = entry.getValue();
        if (filter == null) {
            // the filter does not exist yet. let's create it.
            int expectedInsertions = config.getExpectedInsertions();
            double fpp = config.getFpp();
            filter = BloomFilter.create(Funnels.integerFunnel(), expectedInsertions, fpp);
        }
        boolean result = filter.put(value);
        if (result) {
            entry.setValue(filter);
        }

        return result;
    }

    @Override
    public EntryBackupProcessor<String, BloomFilter<Integer>> getBackupProcessor() {
        return new BackupEntryProcessor(this);
    }

    @Override
    public void writeData(ObjectDataOutput out) throws IOException {
        assert config != null;

        out.writeInt(value);
        out.writeObject(config);
    }

    @Override
    public void readData(ObjectDataInput in) throws IOException {
        value = in.readInt();
        config = in.readObject();
    }

    static class BackupEntryProcessor implements EntryBackupProcessor<String, BloomFilter<Integer>>, DataSerializable {
        private PutEP putEP;

        BackupEntryProcessor() {
            //non-arg constructor used during deserilization
        }

        BackupEntryProcessor(PutEP putEP) {
            this.putEP = putEP;
        }

        @Override
        public void processBackup(Map.Entry<String, BloomFilter<Integer>> entry) {
            putEP.process(entry);
        }

        @Override
        public void writeData(ObjectDataOutput out) throws IOException {
            out.writeObject(putEP);
        }

        @Override
        public void readData(ObjectDataInput in) throws IOException {
            putEP = in.readObject();
        }
    }
}
