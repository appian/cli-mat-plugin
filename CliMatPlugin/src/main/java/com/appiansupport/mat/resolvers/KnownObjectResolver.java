package com.appiansupport.mat.resolvers;

import com.appiansupport.mat.utils.ThreadFinder;
import com.appiansupport.mat.knownobjects.KnownObject;
import com.appiansupport.mat.knownobjects.KnownObjectProvider;
import org.eclipse.mat.snapshot.ISnapshot;
import org.eclipse.mat.snapshot.model.IObject;
import org.eclipse.mat.util.IProgressListener;

public interface KnownObjectResolver {

    /**
     * @return Controls whether all instances of the resolved KnownObject should be detailed in the default report.
     */
    boolean doBreakdownInDefaultReport();

    /**
     * @return The class of the object which this resolver resolves. Used to efficiently find all instances of the KnownObject.
     */
    String getClassName();

    /**
     * @return The singular display/common name of the object which this resolver resolves.
     */
    String getDisplayName();

    /**
     * @param snapshot
     * @param listener
     * @param object
     * @param threadFinder
     * @param knownObjectProvider
     * @return A new Knownobject
     */
    KnownObject init(ISnapshot snapshot, IProgressListener listener, IObject object, ThreadFinder threadFinder, KnownObjectProvider knownObjectProvider);

    /**
     * @param object The object to check
     * @return True if this object is indeed an instance of the KnownObject subclass, else false.
     */
    boolean resolve(IObject object);
}
