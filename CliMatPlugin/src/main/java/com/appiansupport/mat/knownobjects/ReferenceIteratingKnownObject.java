package com.appiansupport.mat.knownobjects;

import com.appiansupport.mat.utils.PrintUtils;
import com.appiansupport.mat.utils.ThreadFinder;
import org.apache.commons.text.TextStringBuilder;
import org.eclipse.mat.SnapshotException;
import org.eclipse.mat.snapshot.ISnapshot;
import org.eclipse.mat.snapshot.model.IObject;
import org.eclipse.mat.snapshot.model.NamedReference;
import org.eclipse.mat.util.IProgressListener;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * A helper example KnownObject implementation which iterates all outgoing references of the IObject.
 * Add Consumers to the classNameToHandler map for every Class of interest to provide custom analysis logic by Class.
 */
public class ReferenceIteratingKnownObject extends KnownObject {
    Map<String, Consumer<IObject>> classNameToHandler;

    public ReferenceIteratingKnownObject(ISnapshot snapshot, IProgressListener listener, IObject object, ThreadFinder threadFinder) {
        super(snapshot, listener, object, threadFinder);
        classNameToHandler = new HashMap<>();
        //classNameToHandler.add("class.of.interest", this::handleFoundClassOfInterest);
    }

    /**
     * Analyze a given IObject using a matching Consumer in classNameToHandler, if one exists
     * @param currObject the IObject to analyze
     */
    public void getInfoFromObject(IObject currObject) {
        Consumer<IObject> c = classNameToHandler.get(currObject.getClazz().getName());
        if (c != null) {
            c.accept(currObject);
        }
    }

    /**
     * Iterate all outgoing references of the object, and perform custom analyses on each via getInfoFromObject().
     */
    @Override public void getInfo() {
        if (!isAnalyzed) {
            List<NamedReference> outboundReferences = thisObject.getOutboundReferences();
            for (NamedReference namedReference : outboundReferences) {
                try {
                    IObject currObject = namedReference.getObject();
                    getInfoFromObject(currObject);
                } catch (SnapshotException snapshotException) {
                    listener.sendUserMessage(IProgressListener.Severity.ERROR, String.format("Unable to fetch reference %s %s from thread %s", namedReference.getName(), namedReference, thisObject.getDisplayName()), snapshotException);
                }
            }
            isAnalyzed = true;
        }
    }

    /**
     * Print all key results and referenced threads of this KnownObject.
     * @return The complete analysis of this KnownObject
     */
    @Override public String printInfo() {
        getInfo();
        TextStringBuilder output = new TextStringBuilder();

        for (String key : keyResults.keySet()) {
            Set<String> kr = keyResults.get(key);
            output.append(PrintUtils.printPluralizedCommaSeparatedRowLine(key, kr));
        }

        if (!referencedThreads.isEmpty()) {
            List<String> referencedThreadNames = referencedThreads.stream().map(IObject::getDisplayName).collect(Collectors.toList());
            output.append(PrintUtils.printPluralizedCommaSeparatedRowLine(REFERENCED_THREADS_KEY, referencedThreadNames));
        }

        return output.toString();
    }

    protected void addHandler(String s, Consumer<IObject> c) {
        classNameToHandler.put(s,c);
    }
}
