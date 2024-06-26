package com.appiansupport.example.resolvers;

import com.appiansupport.example.knownobjects.ExampleKnownObjectExtension;
import com.appiansupport.mat.utils.ThreadFinder;
import com.appiansupport.mat.knownobjects.KnownObjectProvider;
import com.appiansupport.mat.resolvers.KnownObjectResolver;
import org.eclipse.mat.snapshot.ISnapshot;
import org.eclipse.mat.snapshot.model.IObject;
import org.eclipse.mat.util.IProgressListener;

public class ExampleKnownObject implements KnownObjectResolver {

    @Override
    public boolean doBreakdownInDefaultReport() {
        return true;
    }

    @Override
    public String getClassName() {
        return "java.lang.Thread";
    }

    @Override
    public String getDisplayName() {
        return "Example Thread name";
    }

    public ExampleKnownObjectExtension init(ISnapshot snapshot, IProgressListener listener, IObject o, ThreadFinder t, KnownObjectProvider k) {
        return new ExampleKnownObjectExtension(snapshot,listener,o, t, k);
    }

    public boolean resolve(IObject object) {
        String regexToMatch = "java\\.lang\\.Thread @ 0x.+Example interesting thread name.*";
        return object.getDisplayName().matches(regexToMatch);
    }
}
