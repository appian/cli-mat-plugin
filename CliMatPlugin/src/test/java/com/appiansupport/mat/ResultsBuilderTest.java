package com.appiansupport.mat;

import com.appiansupport.mat.knownobjects.KnownObject;
import com.appiansupport.mat.knownobjects.KnownObjectProvider;
import com.appiansupport.mat.knownobjects.TestKnownObjectResolver;
import com.appiansupport.mat.utils.ThreadFinder;
import com.appiansupport.mat.utils.internal.ThreadPrinter;
import com.appiansupport.mat.resolvers.KnownObjectResolver;
import com.appiansupport.mat.suspects.LeakSuspectPrinter;
import com.appiansupport.mat.suspects.LeakSuspectsFinder;
import com.appiansupport.mat.utils.internal.HeapSizer;
import com.appiansupport.mat.utils.internal.HeapTablePrinter;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ResultsBuilderTest {
    private HeapSizer heapSizerMock;
    private HeapTablePrinter tablePrinter;
    private KnownObjectProvider knownObjectProviderMock;
    private ResultsBuilder resultsBuilder;
    private LeakSuspectsFinder suspectFinderMock;
    private LeakSuspectPrinter leakSuspectPrinter;
    private ThreadFinder threadFinderMock;
    private ThreadPrinter threadPrinter;
    private final static String TEST_EMPTY_LIST_SIZE="0";

    void setupObjects() {
        setupObjectsWithResolvers(new ArrayList<>());
    }

    void setupObjectsWithResolvers(List<KnownObjectResolver> resolvers) {
        heapSizerMock = mock(HeapSizer.class);
        tablePrinter = new HeapTablePrinter(heapSizerMock);
        knownObjectProviderMock = mock(KnownObjectProvider.class);
        suspectFinderMock = mock(LeakSuspectsFinder.class);
        threadFinderMock = mock(ThreadFinder.class);
        threadPrinter = new ThreadPrinter(threadFinderMock, heapSizerMock);
        leakSuspectPrinter = mock(LeakSuspectPrinter.class);
        resultsBuilder = new ResultsBuilder(heapSizerMock, tablePrinter, knownObjectProviderMock, suspectFinderMock,
                threadFinderMock, threadPrinter, leakSuspectPrinter, resolvers);
    }


    @Test
    void printKnownObjectInfoWithHeaders_NullData_Null() {
        setupObjects();
        KnownObject knownObjectMock = mock(KnownObject.class);
        when(knownObjectMock.printInfo()).thenReturn(null);
        assertNull(resultsBuilder.printKnownObjectInfoWithHeaders(knownObjectMock));
    }

    @Test
    void printKnownObjectInfoWithHeaders_EmptyData_Null() {
        setupObjects();
        KnownObject knownObjectMock = mock(KnownObject.class);
        when(knownObjectMock.printInfo()).thenReturn("");
        assertNull(resultsBuilder.printKnownObjectInfoWithHeaders(knownObjectMock));
    }

    @Test
    void printTopKnownObjects_None_Empty() {
        setupObjects();
        assertTrue(resultsBuilder.printTopKnownObjects().isEmpty());
    }

    @Test
    void printAllDetailsOfTopKnownObject_NoObjects_Empty() {
        setupObjects();
        KnownObjectResolver resolverMock = mock(KnownObjectResolver.class);
        when(knownObjectProviderMock.getKnownObjectsFromResolver(resolverMock)).thenReturn(new ArrayList<>());
        assertTrue(resultsBuilder.printAllDetailsOfTopKnownObject(resolverMock).isEmpty());
    }

    @Test
    void printTopKnownObjectsCount_NoTopObjects_StillPrintThreads() {
        setupObjects();
        when(threadFinderMock.getAllThreads()).thenReturn(new ArrayList<>());
        String result = resultsBuilder.printTopKnownObjectsCount();
        Predicate<String> hasThreadData = s -> (s.contains(resultsBuilder.THREAD_COUNTS_HEADER) && s.contains(TEST_EMPTY_LIST_SIZE));
        assertTrue(result.lines().anyMatch(hasThreadData));
    }

    @Test
    void printTopKnownObjectsCount_OneResolverNoObjects_PrintIt() {
        List<KnownObjectResolver> resolvers = new ArrayList<>();
        KnownObjectResolver testResolver = new TestKnownObjectResolver();
        resolvers.add(testResolver);
        setupObjectsWithResolvers(resolvers);
        when(knownObjectProviderMock.getKnownObjectsFromResolver(testResolver)).thenReturn(new ArrayList<>());
        when(threadFinderMock.getAllThreads()).thenReturn(new ArrayList<>());
        Predicate<String> hasThreadData = s -> (s.contains(TestKnownObjectResolver.DISPLAY_NAME) && s.contains(TEST_EMPTY_LIST_SIZE));
        String result = resultsBuilder.printTopKnownObjectsCount();
        assertTrue(result.lines().anyMatch(hasThreadData));
    }

    @Test
    void printTopKnownObjectsCount_OneResolverOneObject_PrintIt() {
        List<KnownObjectResolver> resolvers = new ArrayList<>();
        KnownObjectResolver testResolver = new TestKnownObjectResolver();
        resolvers.add(testResolver);
        setupObjectsWithResolvers(resolvers);
        List<KnownObject> resolvedObjects = new ArrayList<>();
        resolvedObjects.add(mock(KnownObject.class));
        when(knownObjectProviderMock.getKnownObjectsFromResolver(testResolver)).thenReturn(resolvedObjects);
        when(threadFinderMock.getAllThreads()).thenReturn(new ArrayList<>());
        Predicate<String> hasResolver = s -> (s.contains(TestKnownObjectResolver.DISPLAY_NAME) && s.contains("1"));
        String result = resultsBuilder.printTopKnownObjectsCount();
        assertTrue(result.lines().anyMatch(hasResolver));
    }

    @Test
    void printTopKnownObjectsCount_TwoResolvers_PrintBoth() {
        List<KnownObjectResolver> resolvers = new ArrayList<>();
        KnownObjectResolver resolverWithNoObjects = new TestKnownObjectResolver();
        KnownObjectResolver resolverWithOneObject = new SecondTestResolver();
        resolvers.add(resolverWithNoObjects);
        resolvers.add(resolverWithOneObject);
        setupObjectsWithResolvers(resolvers);
        List<KnownObject> resolvedObjects = new ArrayList<>();
        resolvedObjects.add(mock(KnownObject.class));
        when(knownObjectProviderMock.getKnownObjectsFromResolver(resolverWithNoObjects)).thenReturn(new ArrayList<>());
        when(knownObjectProviderMock.getKnownObjectsFromResolver(resolverWithOneObject)).thenReturn(resolvedObjects);
        when(threadFinderMock.getAllThreads()).thenReturn(new ArrayList<>());
        Predicate<String> hasResolverZero = s -> (s.contains(TestKnownObjectResolver.DISPLAY_NAME) && s.contains("0"));
        Predicate<String> hasResolverOne = s -> (s.contains(SecondTestResolver.DISPLAY_NAME) && s.contains("1"));
        String result = resultsBuilder.printTopKnownObjectsCount();
        assertTrue(result.lines().anyMatch(hasResolverZero) && result.lines().anyMatch(hasResolverOne));
    }

    @Test
    void printTopKnownObjectsCount_TwoResolvers_PrintLargerFirst() {
        List<KnownObjectResolver> resolvers = new ArrayList<>();
        KnownObjectResolver resolverWithNoObjects = new TestKnownObjectResolver();
        KnownObjectResolver resolverWithOneObject = new SecondTestResolver();
        resolvers.add(resolverWithNoObjects);
        resolvers.add(resolverWithOneObject);
        setupObjectsWithResolvers(resolvers);
        List<KnownObject> resolvedObjects = new ArrayList<>();
        resolvedObjects.add(mock(KnownObject.class));
        when(knownObjectProviderMock.getKnownObjectsFromResolver(resolverWithNoObjects)).thenReturn(new ArrayList<>());
        when(knownObjectProviderMock.getKnownObjectsFromResolver(resolverWithOneObject)).thenReturn(resolvedObjects);
        when(threadFinderMock.getAllThreads()).thenReturn(new ArrayList<>());
        String result = resultsBuilder.printTopKnownObjectsCount();
        int resolverWithNone_Index = result.indexOf(TestKnownObjectResolver.DISPLAY_NAME);
        int resolverWithOne_Index = result.indexOf(SecondTestResolver.DISPLAY_NAME);
        assertTrue(resolverWithNone_Index>=0);
        assertTrue(resolverWithOne_Index>=0);
        assertTrue(resolverWithOne_Index<resolverWithNone_Index);
    }

    private static class SecondTestResolver extends TestKnownObjectResolver {
        final static String DISPLAY_NAME = "Another Resolver";

        @Override
        public String getDisplayName() {
            return DISPLAY_NAME;
        }
    }
}