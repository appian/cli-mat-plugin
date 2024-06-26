package com.appiansupport.mat.utils.internal;

import java.util.Collections;
import java.util.Optional;

import com.appiansupport.mat.utils.ThreadFinder;
import org.eclipse.mat.snapshot.model.IObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ThreadPrinterTest {
    private HeapSizer heapSizerMock;
    private ThreadFinder threadFinderMock;
    private ThreadPrinter threadPrinter;

    @BeforeEach
    void setupObjects() {
        threadFinderMock = mock(ThreadFinder.class);
        heapSizerMock = mock(HeapSizer.class);
        threadPrinter = new ThreadPrinter(threadFinderMock, heapSizerMock);
    }

    @Test
    void printOomThreadInfo_NoOom_EmptyString() {
        when(threadFinderMock.getOomThread()).thenReturn(Optional.empty());
        Assertions.assertTrue(threadPrinter.printOomThreadInfo().isEmpty());
    }

    @Test
    void printOomThreadInfo_YesOom_SomeString() {
        IObject mockOomObject = mock(IObject.class);
        when(threadFinderMock.getOomThread()).thenReturn(Optional.of(mockOomObject));
        when(heapSizerMock.printObjectHeapUsage(mockOomObject)).thenReturn("1");
        when(heapSizerMock.getUsedHeapDecimal()).thenReturn(Optional.empty());
        Assertions.assertFalse(threadPrinter.printOomThreadInfo().isEmpty());
    }

    @Test
    void shouldPrintOomExplosionTip_EmptyHeap_False(){
        when(heapSizerMock.getUsedHeapDecimal()).thenReturn(Optional.empty());
        Assertions.assertFalse(threadPrinter.shouldPrintOomExplosionTip());
    }

    @Test
    void shouldPrintOomExplosionTip_HighHeap_False(){
        when(heapSizerMock.getUsedHeapDecimal()).thenReturn(Optional.of(1D));
        Assertions.assertFalse(threadPrinter.shouldPrintOomExplosionTip());
    }

    @Test
    void shouldPrintOomExplosionTip_LowHeap_True(){
        when(heapSizerMock.getUsedHeapDecimal()).thenReturn(Optional.of(0D));
        Assertions.assertTrue(threadPrinter.shouldPrintOomExplosionTip());
    }

    @Test
    void printThreadStatistics_NoThreads_NotEmpty(){
        when(threadFinderMock.getAllThreads()).thenReturn(Collections.emptyList());
        when(threadFinderMock.sortThreadsByHeapUsage()).thenReturn(Collections.emptyList());
        Assertions.assertFalse(threadPrinter.printThreadStatistics().isEmpty());
    }
}