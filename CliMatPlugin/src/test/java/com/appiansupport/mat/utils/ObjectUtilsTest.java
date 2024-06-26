package com.appiansupport.mat.utils;

import org.eclipse.mat.SnapshotException;
import org.eclipse.mat.snapshot.model.IObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Optional;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ObjectUtilsTest {

    static void silenceStdErr(){
        System.setErr((new PrintStream(OutputStream.nullOutputStream())));
    }

    static void resumeStdErr(){
        System.setErr(System.out);
    }

    @Test
    // IObject.resolveValue() should never return a String according to the API, but we allow it for easier mock testing
    void getStringFromObjectResolution_String_ReturnIt() throws SnapshotException {
        String pathToResolve="test.path";
        String stringToReturn="StringObject";
        IObject objectMock = mock(IObject.class);
        when(objectMock.resolveValue(pathToResolve)).thenReturn(stringToReturn);
        Assertions.assertEquals(stringToReturn, ObjectUtils.getStringFromObjectResolution(objectMock,pathToResolve).get());
    }

    @Test
    void getStringFromObjectResolution_NullResolution_Empty() throws SnapshotException {
        String pathToResolve="test.path";
        IObject objectMock = mock(IObject.class);
        when(objectMock.resolveValue(pathToResolve)).thenReturn(null);
        Assertions.assertEquals(Optional.empty(), ObjectUtils.getStringFromObjectResolution(objectMock,pathToResolve));
    }

    @Test
    void getStringFromObjectResolution_SnapshotException_Empty() throws SnapshotException {
        silenceStdErr();
        String pathToResolve="test.path";
        IObject objectMock = mock(IObject.class);
        when(objectMock.resolveValue(pathToResolve)).thenThrow(SnapshotException.class);
        Assertions.assertEquals(Optional.empty(), ObjectUtils.getStringFromObjectResolution(objectMock,pathToResolve));
        resumeStdErr();
    }
}