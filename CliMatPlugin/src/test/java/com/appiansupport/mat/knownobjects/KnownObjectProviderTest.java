package com.appiansupport.mat.knownobjects;

import com.appiansupport.mat.utils.ThreadFinder;
import com.appiansupport.mat.resolvers.KnownObjectResolver;
import org.eclipse.mat.snapshot.ISnapshot;
import org.eclipse.mat.snapshot.model.IObject;
import org.eclipse.mat.util.IProgressListener;
import org.eclipse.mat.util.VoidProgressListener;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class KnownObjectProviderTest {
    private KnownObjectProvider knownObjectProvider;
    private ISnapshot snapshotMock;
    private IProgressListener listener;
    private ThreadFinder threadFinderMock;
    private List<KnownObjectResolver> resolvers;

    @BeforeEach
    void setupKOR() {
        snapshotMock = mock(ISnapshot.class);
        listener = new VoidProgressListener();
        threadFinderMock = mock(ThreadFinder.class);
        resolvers = new ArrayList<>();
        knownObjectProvider = new KnownObjectProvider(snapshotMock,listener,threadFinderMock,resolvers);
    }

    @Test
    void getKnownObject_NoResolvers_MakesUnknown() {
        IObject unknown = mock(IObject.class);
        KnownObject shouldBeUnkown = knownObjectProvider.getKnownObject(unknown);
        assertTrue(shouldBeUnkown instanceof UnknownObject);
    }

    @Test
    void getKnownObject_NoMatchingResolver_MakesUnknown() {
        TestFalseResolver testResolver = new TestFalseResolver();
        IObject testObject = mock(IObject.class);
        resolvers.add(testResolver);
        KnownObject shouldBeUnkown = knownObjectProvider.getKnownObject(testObject);
        assertTrue(shouldBeUnkown instanceof UnknownObject);
    }

    @Test
    void getKnownObject_MatchingResolver_MakesMatch() {
        TestKnownObjectResolver testResolver = new TestKnownObjectResolver();
        IObject testObject = mock(IObject.class);
        resolvers.add(testResolver);
        KnownObject shouldBeNull = knownObjectProvider.getKnownObject(testObject);
        assertNull(shouldBeNull);
    }

    @Test
    void getKnownObject_GetTwice_ConfirmCache() {
        IObject unknown = mock(IObject.class);
        KnownObject firstGet = knownObjectProvider.getKnownObject(unknown);
        KnownObject secondGet = knownObjectProvider.getKnownObject(unknown);
        assertSame(firstGet, secondGet);
    }

    private static class TestFalseResolver extends TestKnownObjectResolver {

        @Override
        public boolean resolve(IObject object) {
            return false;
        }

    }
}