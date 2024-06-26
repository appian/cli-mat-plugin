package com.appiansupport.mat.utils;

import com.appiansupport.mat.utils.internal.ObjectFetcher;
import org.eclipse.mat.SnapshotException;
import org.eclipse.mat.snapshot.ISnapshot;
import org.eclipse.mat.snapshot.SnapshotInfo;
import org.eclipse.mat.snapshot.model.IObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.*;

import static org.mockito.Mockito.*;

public class ThreadFinderTest {
  static ISnapshot snapshotMock;
  static SnapshotInfo infoMock;
  static ThreadFinder threadFinder;
  static ObjectFetcher objectFetcherMock;
  static final String EMPTY_TEST_FILE_PREFIX = "src/test/resources/empty.";
  static final String ONE_THREAD_TEST_FILE_PREFIX = "src/test/resources/one.";
  static final String ONE_THREAD_NO_STACK_TEST_FILE_PREFIX = "src/test/resources/one-nostack.";
  static final String TWO_THREADS_TEST_FILE_PREFIX = "src/test/resources/two.";
  static final String ONE_THREAD_TEST_FILE_STACK = "stack";
  static final String OOM_THREAD_TEST_FILE_PREFIX = "src/test/resources/one-oom.";
  static final int MOCK_THREAD_ID = 1;

  @BeforeEach
  void setupMocks() throws SnapshotException {
    snapshotMock = mock(ISnapshot.class);
    objectFetcherMock = mock(ObjectFetcher.class);
    when(objectFetcherMock.getIdFromHexAddress(anyString())).thenReturn(MOCK_THREAD_ID);
    threadFinder = new ThreadFinder(snapshotMock, objectFetcherMock);
  }

  void setupThreadsFile(String threadsFilePrefix) {
    infoMock = mock(SnapshotInfo.class);
    when(infoMock.getPrefix()).thenReturn(threadsFilePrefix);
    when(snapshotMock.getSnapshotInfo()).thenReturn(infoMock);
  }

  IObject setupSnapshotToResolveIObjects() throws SnapshotException {
    IObject objectMock = mock(IObject.class);
    when(snapshotMock.getObject(anyInt())).thenReturn(objectMock);
    return objectMock;
  }

  @Test
  void getAllThreadIds_oneThread_findThread() {
    setupThreadsFile(ONE_THREAD_TEST_FILE_PREFIX);
    Assertions.assertEquals(1, threadFinder.getAllThreadIds().size());
  }

  @Test
  void getAllThreadIds_oneThreadNoStack_findThread() {
    setupThreadsFile(ONE_THREAD_NO_STACK_TEST_FILE_PREFIX);
    Assertions.assertEquals(1, threadFinder.getAllThreadIds().size());
  }

  @Test
  void getAllThreadIds_twoThreads_findThreads() {
    setupThreadsFile(TWO_THREADS_TEST_FILE_PREFIX);
    Assertions.assertEquals(2, threadFinder.getAllThreadIds().size());
  }

  @Test
  void getAllThreadIds_NoFile_EmptyList() {
    //noFile
    setupThreadsFile("");
    System.setErr((new PrintStream(OutputStream.nullOutputStream())));
    Assertions.assertEquals(0, threadFinder.getAllThreadIds().size());
    System.setErr(System.out);
  }

  @Test
  void getStackTrace_validThread_findStack() {
    setupThreadsFile(ONE_THREAD_TEST_FILE_PREFIX);
    Assertions.assertEquals(ONE_THREAD_TEST_FILE_STACK, threadFinder.getStackTrace(MOCK_THREAD_ID).trim());
  }

  @Test
  void getStackTrace_validThreadNoStack_EmptyString() {
    setupThreadsFile(ONE_THREAD_NO_STACK_TEST_FILE_PREFIX);
    Assertions.assertTrue(threadFinder.getStackTrace(MOCK_THREAD_ID).isEmpty());
  }

  @Test
  void getOomThreadId_YesOom_FindOom() throws SnapshotException {
    setupThreadsFile(OOM_THREAD_TEST_FILE_PREFIX);
    setupSnapshotToResolveIObjects();
    Assertions.assertTrue(threadFinder.getOomThread().isPresent());
  }

  @Test
  void getOomThreadId_NoOom_Empty() {
    setupThreadsFile(ONE_THREAD_TEST_FILE_PREFIX);
    Assertions.assertEquals(Optional.empty(), threadFinder.getOomThread());
  }

  @Test
  void isThread_Yes_True() {
    setupThreadsFile(ONE_THREAD_TEST_FILE_PREFIX);
    Assertions.assertTrue(threadFinder.isThread(MOCK_THREAD_ID));
  }

  @Test
  void isThread_No_False() {
    final int NOT_THREAD_ID = -1;
    setupThreadsFile(ONE_THREAD_TEST_FILE_PREFIX);
    Assertions.assertFalse(threadFinder.isThread(NOT_THREAD_ID));
  }

  @Test
  void getAllThreads_NoThreads_EmptyList() {
    //noFile
    setupThreadsFile(EMPTY_TEST_FILE_PREFIX);
    Assertions.assertEquals(0,threadFinder.getAllThreads().size());
  }

  @Test
  void getAllThreads_OneThread_OneObject() throws SnapshotException {
    setupThreadsFile(ONE_THREAD_TEST_FILE_PREFIX);
    setupSnapshotToResolveIObjects();
    Assertions.assertEquals(1,threadFinder.getAllThreads().size());
  }

  @Test
  void getAllThreads_TwoThreads_TwoObjects() throws SnapshotException {
    setupThreadsFile(TWO_THREADS_TEST_FILE_PREFIX);
    setupSnapshotToResolveIObjects();
    Assertions.assertEquals(2,threadFinder.getAllThreads().size());
  }

  @Test
  void sortThreadsByHeapUsage_NoThreads_EmptyList() {
    setupThreadsFile(EMPTY_TEST_FILE_PREFIX);
    Assertions.assertTrue(threadFinder.sortThreadsByHeapUsage().isEmpty());
  }

  @Test
  void sortThreadsByHeapUsage_OneThread_OneThread() throws SnapshotException {
    setupThreadsFile(ONE_THREAD_TEST_FILE_PREFIX);
    IObject mockObject = setupSnapshotToResolveIObjects();
    List<IObject> result = threadFinder.sortThreadsByHeapUsage();
    Assertions.assertTrue(result.size() == 1 && result.get(0) == mockObject);
  }

  @Test
  void sortThreadsByHeapUsage_TwoThreads_Sorted() throws SnapshotException {
    setupThreadsFile(TWO_THREADS_TEST_FILE_PREFIX);
    IObject mockObjectMoreHeap = mock(IObject.class);
    when(mockObjectMoreHeap.getRetainedHeapSize()).thenReturn(2L);
    IObject mockObjectlessHeap = mock(IObject.class);
    when(mockObjectlessHeap.getRetainedHeapSize()).thenReturn(1L);

    when(snapshotMock.getObject(anyInt())).thenReturn(mockObjectlessHeap,mockObjectMoreHeap);
    List<IObject> result = threadFinder.sortThreadsByHeapUsage();
    Assertions.assertTrue(result.indexOf(mockObjectMoreHeap)==0 && result.indexOf(mockObjectlessHeap)==1);
  }
}