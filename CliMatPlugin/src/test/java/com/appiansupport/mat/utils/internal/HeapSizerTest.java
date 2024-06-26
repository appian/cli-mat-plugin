package com.appiansupport.mat.utils.internal;

import org.eclipse.mat.snapshot.ISnapshot;
import org.eclipse.mat.util.IProgressListener;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.mock;

class HeapSizerTest {
    private static HeapSizer heapSizer;
    private final long TEST_VAL = 1024L;

    @BeforeAll
    static void setupTests(){
        ISnapshot snapshotMock = mock(ISnapshot.class);
        IProgressListener listenerMock = mock(IProgressListener.class);
        ObjectFetcher objectFetcher = new ObjectFetcher(snapshotMock);
        heapSizer = new HeapSizer(snapshotMock,objectFetcher);
    }

    @Test
    void getXmxFromJvmProps_NullInput_Npe(){
        Assertions.assertThrows(NullPointerException.class,()->heapSizer.getXmxFromJvmProps(null));
    }

    @Test
    void getXmxFromJvmProps_NoMatch_Null(){
        Assertions.assertNull(heapSizer.getXmxFromJvmProps("-Xms1024m"));
    }

    @Test
    void getXmxFromJvmProps_EmptyInput_Null(){
        Assertions.assertNull(heapSizer.getXmxFromJvmProps(""));
    }

    @Test
    void getXmxFromJvmProps_MatchNoUnit_ReturnsLongBytes(){
        Assertions.assertEquals(TEST_VAL,heapSizer.getXmxFromJvmProps("-Xmx"+TEST_VAL));
    }

    @Test
    void getXmxFromJvmProps_TwoMatchesNoUnit_ReturnsLongBytes(){
        Assertions.assertEquals(TEST_VAL,heapSizer.getXmxFromJvmProps("-Xmx"+TEST_VAL+"---"+"Xmx"+TEST_VAL));
    }

    @Test
    void getXmxFromJvmProps_Matchkb_ReturnsLongBytes(){
        Assertions.assertEquals(TEST_VAL,heapSizer.getXmxFromJvmProps("-Xmx1k"));
    }

    @Test
    void getXmxFromJvmProps_MatchKb_ReturnsLongBytes(){
        Assertions.assertEquals(TEST_VAL,heapSizer.getXmxFromJvmProps("-Xmx1K"));
    }

    @Test
    void getXmxFromJvmProps_Matchmb_ReturnsLongBytes(){
        Assertions.assertEquals(TEST_VAL*TEST_VAL,heapSizer.getXmxFromJvmProps("-Xmx1m"));
    }

    @Test
    void getXmxFromJvmProps_MatchMb_ReturnsLongBytes(){
        Assertions.assertEquals(TEST_VAL*TEST_VAL,heapSizer.getXmxFromJvmProps("-Xmx1M"));
    }

    @Test
    void getXmxFromJvmProps_Matchgb_ReturnsLongBytes(){
        Assertions.assertEquals(TEST_VAL*TEST_VAL*TEST_VAL,heapSizer.getXmxFromJvmProps("-Xmx1g"));
    }

    @Test
    void getXmxFromJvmProps_MatchGb_ReturnsLongBytes(){
        Assertions.assertEquals(TEST_VAL*TEST_VAL*TEST_VAL,heapSizer.getXmxFromJvmProps("-Xmx1G"));
    }
}