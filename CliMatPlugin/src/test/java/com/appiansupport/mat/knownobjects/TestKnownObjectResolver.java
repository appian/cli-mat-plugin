package com.appiansupport.mat.knownobjects;

import com.appiansupport.mat.utils.ThreadFinder;
import com.appiansupport.mat.resolvers.KnownObjectResolver;
import org.eclipse.mat.snapshot.ISnapshot;
import org.eclipse.mat.snapshot.model.IObject;
import org.eclipse.mat.util.IProgressListener;

public class TestKnownObjectResolver implements KnownObjectResolver {
    public final static String DISPLAY_NAME = "Test Resolver";

    @Override
    public boolean resolve(IObject object) {
        return true;
    }

    @Override
    public boolean doBreakdownInDefaultReport() {
        return false;
    }

    @Override
    public String getClassName() {
        return null;
    }

    @Override
    public String getDisplayName() {
        return DISPLAY_NAME;
    }

    @Override
    public KnownObject init(ISnapshot snapshot, IProgressListener listener, IObject object, ThreadFinder threadFinder, KnownObjectProvider knownObjectProvider) {
        return null;
    }
}
