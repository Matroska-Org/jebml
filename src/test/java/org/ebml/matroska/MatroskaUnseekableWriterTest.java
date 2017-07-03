package org.ebml.matroska;

import org.ebml.io.FileDataWriter;
import org.junit.Assert;

import java.io.File;

public class MatroskaUnseekableWriterTest extends MatroskaFileWriterTest {
    @Override
    protected FileDataWriter createDataWriter(File destination) throws Exception {
        return new FileDataWriter(destination.getPath()){
            @Override
            public boolean isSeekable() {
                return false;
            }

            @Override
            public long seek(long pos) {
                Assert.fail("Must not be called");
                return -1;
            }
        };
    }
}
