package com.appiansupport.mat.knownobjects;

import com.appiansupport.mat.utils.ThreadFinder;
import com.appiansupport.mat.resolvers.KnownObjectResolver;
import com.appiansupport.mat.utils.internal.ObjectFetcher;
import org.eclipse.mat.SnapshotException;
import org.eclipse.mat.snapshot.ISnapshot;
import org.eclipse.mat.snapshot.model.IObject;
import org.eclipse.mat.util.IProgressListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Provider and cache of KnownObject instances.
 */
public class KnownObjectProvider {
    private final ISnapshot snapshot;
    private final IProgressListener listener;
    private final ThreadFinder threadFinder;
    private final HashMap<IObject, KnownObject> objectToKnownObject;
    private final List<KnownObjectResolver> knownObjectResolvers;

    public KnownObjectProvider(ISnapshot snapshot, IProgressListener listener, ThreadFinder threadFinder,List<KnownObjectResolver> resolvers) {
        this.snapshot = snapshot;
        this.listener = listener;
        this.threadFinder = threadFinder;
        objectToKnownObject = new HashMap<>();
        knownObjectResolvers = resolvers;
    }

    private KnownObject createKnownObject(IObject object) {
        if(knownObjectResolvers != null) {
            for (KnownObjectResolver resolver : knownObjectResolvers) {
                if(resolver.resolve(object)) {
                    return resolver.init(snapshot,listener,object,threadFinder, this);
                }
            }
        }
        return new UnknownObject(snapshot, listener, object, threadFinder);
    }

    /**
     * @param object The IObject to convert to KnownObject
     * @return The cached KnownObject if present, else a new KnownObject.
     */
    public KnownObject getKnownObject(IObject object) {
        return objectToKnownObject.computeIfAbsent(object, this::createKnownObject);
    }

    /**
     * @param resolver The KnownObjectResolver to fetch objects from
     * @return A List of all KnownObjects which this resolver resolves.
     */
    public List<KnownObject> getKnownObjectsFromResolver(KnownObjectResolver resolver) {
        List<IObject> matchingObjects = getObjectsFromResolver(resolver);
        // Get each matching KnownObject from the cache, else, init it.
        return matchingObjects.stream()
                .filter(resolver::resolve)
                .map(outer -> objectToKnownObject.computeIfAbsent(outer,inner -> resolver.init(snapshot,listener,inner,threadFinder, this)))
                .collect(Collectors.toList());
    }

    private List<IObject> getObjectsFromResolver(KnownObjectResolver resolver) {
        String targetClassName = resolver.getClassName();
        ObjectFetcher objectFetcher = new ObjectFetcher(snapshot);
        List<IObject> matchingObjects = new ArrayList<>();
        try {
             matchingObjects = objectFetcher.getObjectsByClass(Pattern.compile(targetClassName));
        } catch (SnapshotException snapshotException) {
            System.err.printf("Unable to resolve objects of class %s%n", targetClassName);
            snapshotException.printStackTrace();
        }
        return matchingObjects;
    }
}
