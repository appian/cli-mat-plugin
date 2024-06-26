package com.appiansupport.mat.utils.internal;

import org.eclipse.mat.SnapshotException;
import org.eclipse.mat.snapshot.ClassHistogramRecord;
import org.eclipse.mat.snapshot.ISnapshot;
import org.eclipse.mat.snapshot.model.IClass;
import org.eclipse.mat.snapshot.model.IObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.*;
import java.util.regex.Pattern;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ObjectFetcherTest {
    static ISnapshot snapshotMock;
    static ObjectFetcher objectFetcher;
    final String TEST_ADDRESS = "0x1";
    final String TEST_ADDRESS_NOHEX = "0x1";
    final long TEST_LONG = 1L;
    final Pattern TEST_PATTERN = Pattern.compile("");


    @BeforeEach
    void setupTests(){
        snapshotMock = mock(ISnapshot.class);
        objectFetcher = new ObjectFetcher(snapshotMock);
    }

    @Test
    void getObjectFromHexAddress_Object_ReturnIt() throws SnapshotException {
        IObject objectMock = mock(IObject.class);
        when(snapshotMock.getObject(anyInt())).thenReturn(objectMock);
        assertEquals(objectMock,objectFetcher.getObjectFromHexAddress(TEST_ADDRESS));
    }

    @Test
    void getObjectFromHexAddress_NullObject_Null() throws SnapshotException {
        when(snapshotMock.getObject(anyInt())).thenReturn(null);
        assertNull(objectFetcher.getObjectFromHexAddress(TEST_ADDRESS));
    }

    @Test
    void getIdFromHexAddress_Null_Npe() {
        assertThrows(NullPointerException.class, () -> objectFetcher.getIdFromHexAddress(null));
    }

    @Test
    void getIdFromHexAddress_ValidAddress_ReturnId() throws SnapshotException {
        final int GOAL_ID = 7;
        when(snapshotMock.mapAddressToId(TEST_LONG)).thenReturn(GOAL_ID);
        assertEquals(GOAL_ID, objectFetcher.getIdFromHexAddress(TEST_ADDRESS));
    }

    @Test
    void getIdFromHexAddress_no0x_ReturnId() throws SnapshotException {
        final int GOAL_ID = 7;
        when(snapshotMock.mapAddressToId(TEST_LONG)).thenReturn(GOAL_ID);
        assertEquals(GOAL_ID, objectFetcher.getIdFromHexAddress(TEST_ADDRESS_NOHEX));
    }

    @Test
    void getIdFromHexAddress_BadHex_Nfe() throws SnapshotException {
        final String NOT_HEX = "z";
        assertThrows(NumberFormatException.class,() -> objectFetcher.getIdFromHexAddress(NOT_HEX));
    }

    @Test
    void getObjectIdsByClass_SnapshotException_ThrowIt() throws SnapshotException {
        when(snapshotMock.getClassesByName(TEST_PATTERN,false)).thenThrow(SnapshotException.class);
        assertThrows(SnapshotException.class, () -> objectFetcher.getObjectIdsByClass(TEST_PATTERN));
    }

    @Test
    void getObjectIdsByClass_NullClasses_EmptyArr() throws SnapshotException {
        when(snapshotMock.getClassesByName(TEST_PATTERN,false)).thenReturn(null);
        assertEquals(0, objectFetcher.getObjectIdsByClass(TEST_PATTERN).length);
    }

    @Test
    void getObjectIdsByClass_NoObjects_EmptyArr() throws SnapshotException {
        IClass classMock = mock(IClass.class);
        when(classMock.getObjectIds()).thenReturn(new int[0]);
        Collection<IClass> testClassList = Collections.singletonList(classMock);
        when(snapshotMock.getClassesByName(TEST_PATTERN,false)).thenReturn(testClassList);
        assertEquals(0, objectFetcher.getObjectIdsByClass(TEST_PATTERN).length);
    }


    @Test
    void getObjectIdsByClass_OneMatch_InArr() throws SnapshotException {
        final int TEST_ID_1 = 1;
        int[] testIds = {TEST_ID_1};
        IClass classMock = mock(IClass.class);
        when(classMock.getObjectIds()).thenReturn(testIds);
        Collection<IClass> testClassList = Collections.singletonList(classMock);
        when(snapshotMock.getClassesByName(TEST_PATTERN,false)).thenReturn(testClassList);
        int[] testResult = objectFetcher.getObjectIdsByClass(TEST_PATTERN);
        assertTrue(testResult.length == 1 && testResult[0] == TEST_ID_1);
    }

    @Test
    void getObjectIdsByClass_TwoMatchesOneClass_BothInArr() throws SnapshotException {
        final int TEST_ID_1 = 1;
        final int TEST_ID_2 = 2;
        int[] testIds = {TEST_ID_1,TEST_ID_2};
        IClass classMock = mock(IClass.class);
        when(classMock.getObjectIds()).thenReturn(testIds);
        Collection<IClass> testClassList = Collections.singletonList(classMock);
        when(snapshotMock.getClassesByName(TEST_PATTERN,false)).thenReturn(testClassList);
        int[] testResult = objectFetcher.getObjectIdsByClass(TEST_PATTERN);
        assertTrue(testResult.length == 2 && testResult[0] == TEST_ID_1 && testResult[1] == TEST_ID_2 );
    }

    @Test
    void getObjectIdsByClass_TwoMatchesTwoClasses_BothInArr() throws SnapshotException {
        final int TEST_VAL_1 = 1;
        final int TEST_VAL_2 = 2;
        int[] testIds1 = {TEST_VAL_1};
        int[] testIds2 = {TEST_VAL_2};
        IClass classMock1 = mock(IClass.class);
        when(classMock1.getObjectIds()).thenReturn(testIds1);
        IClass classMock2 = mock(IClass.class);
        when(classMock2.getObjectIds()).thenReturn(testIds2);
        Collection<IClass> testClassList = new ArrayList<>();
        testClassList.add(classMock1);
        testClassList.add(classMock2);
        when(snapshotMock.getClassesByName(TEST_PATTERN,false)).thenReturn(testClassList);
        int[] resultIds = objectFetcher.getObjectIdsByClass(TEST_PATTERN);
        assertTrue(resultIds.length == 2 && Arrays.stream(resultIds).allMatch(x -> x == TEST_VAL_1 || x == TEST_VAL_2));
    }

    @Test
    void getObjectsByClass_SnapshotException_ThrowIt() throws SnapshotException {
        when(snapshotMock.getClassesByName(TEST_PATTERN,false)).thenThrow(SnapshotException.class);
        assertThrows(SnapshotException.class, () -> objectFetcher.getObjectsByClass(TEST_PATTERN));
    }

    @Test
    void getObjectsByClass_NoMatches_EmptyList() throws SnapshotException {
        IClass classMock = mock(IClass.class);
        when(classMock.getObjectIds()).thenReturn(new int[0]);
        Collection<IClass> testClassList = Collections.singletonList(classMock);
        when(snapshotMock.getClassesByName(TEST_PATTERN,false)).thenReturn(testClassList);
        assertEquals(0, objectFetcher.getObjectsByClass(TEST_PATTERN).size());
    }


    @Test
    void getObjectsFromClassRecord_Null_Npe()  {
        assertThrows(NullPointerException.class, () -> objectFetcher.getObjectsFromClassRecord(null));
    }

    @Test
    void getObjectsFromClassRecord_SnapshotException_EmptyList() throws SnapshotException {
        final int TEST_ID = 1;
        ClassHistogramRecord recordMock = mock(ClassHistogramRecord.class);
        when(recordMock.getObjectIds()).thenReturn(new int[]{TEST_ID});
        when(snapshotMock.getObject(TEST_ID)).thenThrow(SnapshotException.class);
        System.setErr((new PrintStream(OutputStream.nullOutputStream())));
        assertTrue(objectFetcher.getObjectsFromClassRecord(recordMock).isEmpty());
        System.setErr(System.out);
    }

    @Test
    void getObjectsFromClassRecord_OneId_ReturnIt() throws SnapshotException {
        final int TEST_ID = 1;
        ClassHistogramRecord recordMock = mock(ClassHistogramRecord.class);
        when(recordMock.getObjectIds()).thenReturn(new int[]{TEST_ID});
        IObject objectMock = mock(IObject.class);
        when(snapshotMock.getObject(TEST_ID)).thenReturn(objectMock);
        List<IObject> testResult = objectFetcher.getObjectsFromClassRecord(recordMock);
        assertTrue(testResult.size() == 1 && testResult.get(0) == objectMock);
    }

    @Test
    void getObjectsFromClassRecord_TwoIds_ReturnBoth() throws SnapshotException {
        final int TEST_ID = 1;
        ClassHistogramRecord recordMock = mock(ClassHistogramRecord.class);
        when(recordMock.getObjectIds()).thenReturn(new int[]{TEST_ID,TEST_ID});
        IObject objectMock1 = mock(IObject.class);
        IObject objectMock2 = mock(IObject.class);
        when(snapshotMock.getObject(TEST_ID)).thenReturn(objectMock1).thenReturn(objectMock2);
        List<IObject> testResult = objectFetcher.getObjectsFromClassRecord(recordMock);
        assertTrue(testResult.size() == 2 && testResult.contains(objectMock1) && testResult.contains(objectMock2));
    }
}