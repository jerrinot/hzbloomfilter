package info.jerrinot.hzbloomfilter;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.DataSerializable;

import java.io.IOException;

public final class BloomFilterConfig implements DataSerializable {
    private String name;
    private String backingMap;
    private int expectedInsertions;
    private double fpp;

    BloomFilterConfig() {
        // used during deserialization
    }

    public BloomFilterConfig(String name, String backingMap, int expectedInsertions, double fpp) {
        this.name = name;
        this.backingMap = backingMap;
        this.expectedInsertions = expectedInsertions;
        this.fpp = fpp;
    }

    public int getExpectedInsertions() {
        return expectedInsertions;
    }

    public double getFpp() {
        return fpp;
    }

    public String getName() {
        return name;
    }

    public String getBackingMap() {
        return backingMap;
    }

    @Override
    public void writeData(ObjectDataOutput out) throws IOException {
        out.writeUTF(name);
        out.writeUTF(backingMap);
        out.writeInt(expectedInsertions);
        out.writeDouble(fpp);
    }

    @Override
    public void readData(ObjectDataInput in) throws IOException {
        name = in.readUTF();
        backingMap = in.readUTF();
        expectedInsertions = in.readInt();
        fpp = in.readDouble();
    }
}
