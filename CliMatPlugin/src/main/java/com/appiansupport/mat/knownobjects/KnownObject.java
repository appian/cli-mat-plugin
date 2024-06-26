package com.appiansupport.mat.knownobjects;

import com.appiansupport.mat.utils.PrintUtils;
import com.appiansupport.mat.utils.ThreadFinder;
import com.appiansupport.mat.utils.internal.GcRootsHandler;
import com.appiansupport.mat.utils.ReferenceFinder;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.text.TextStringBuilder;
import org.eclipse.mat.snapshot.ISnapshot;
import org.eclipse.mat.snapshot.model.IObject;
import org.eclipse.mat.util.IProgressListener;

/**
 * KnownObject defines custom analysis and printing behavior for a specific common object.
 */
public abstract class KnownObject {
  protected boolean isAnalyzed;
  protected final ISnapshot snapshot;
  protected final IProgressListener listener;
  protected final ThreadFinder threadFinder;
  protected GcRootsHandler gcRootsHandler;
  protected Map<String, Set<String>> keyResults;
  protected Set<IObject> referencedThreads;
  protected static final String REFERENCED_THREADS_KEY = "Referenced Thread";
  public final IObject thisObject;

  public KnownObject(ISnapshot snapshot, IProgressListener listener, IObject object, ThreadFinder threadFinder) {
    this.thisObject = Objects.requireNonNull(object);
    this.snapshot = Objects.requireNonNull(snapshot);
    this.listener = Objects.requireNonNull(listener);
    this.threadFinder = threadFinder;
    gcRootsHandler = new GcRootsHandler(snapshot,object);
    keyResults= new HashMap<>(8);
  }

  /**
   * getInfo() generates all relevant information about the object, to be printed in printInfo().
   */
  public abstract void getInfo();

  /**
   * @return A string representing all relevant information acquired via getInfo();
   * printInfo() should always call getInfo() to do the work of traversing the Object as needed.
   */
  public abstract String printInfo();

  /**
   * Prints all referenced threads identified in getReferencedThreads()
   * @return
   */
  public String printReferencedThreads() {
    getReferencedThreads();
    TextStringBuilder output = new TextStringBuilder();
    for (IObject referencingThread : referencedThreads) {
      output.appendln("Found Thread referencing Object: " + referencingThread.getDisplayName());
    }
    return output.toString();
  }

  /**
   * Searches incoming references, outgoing references, and GC Roots for Thread objects. Override to provide custom Thread searching capabilities.
   * @return A set of Thread IObjects found.
   */
  public Set<IObject> getReferencedThreads() {
    if (referencedThreads == null) {
      ReferenceFinder referenceFinder = new ReferenceFinder(snapshot, listener);
      referencedThreads = referenceFinder.findMatchingReferencesBothDirections(thisObject, threadFinder.getAllThreads());
      gcRootsHandler.getThreadsFromGcRoots(threadFinder).ifPresent(referencedThreads::addAll);
      Set<String> referencedThreadNames = referencedThreads.stream().map(IObject::getDisplayName).collect(Collectors.toSet());
      addKeyResults(REFERENCED_THREADS_KEY, referencedThreadNames);
    }
    return new HashSet<>(referencedThreads);
  }

  protected Map<String,Set<String>> addKeyResults(String k, Set<String> val) {
    keyResults.merge(k, new HashSet<>(val), (oldSet, newSet)-> {
      newSet.addAll(oldSet);
      return newSet;
    });
    return keyResults;
}

  protected Map<String,Set<String>> addKeyResult(String k, String val) {
    Set<String> valueToAdd = new HashSet<>(2);
    valueToAdd.add(val);
    return addKeyResults(k, valueToAdd);
  }

  /**
   * @return the IObject which this KnownObject represents
   */
  public IObject getObject() {
    return thisObject;
  }

  /**
   * Suggested objects appear as additional options in the CLI. Override to add custom CLI flows when this object is navigated to.
   * @return the Set of IObjects suggested for further analysis.
   */
  public Set<IObject> getSuggestedObjects() {
    getInfo();
    return new HashSet<>(referencedThreads);
  }

  /**
   * Key results represent the most important fields of this KnownObject. Add Key Results within getInfo().
   * @return the map of Key Results found in this KnownObject.
   */
  public Map<String,Set<String>> getKeyResults() {
    getInfo();
    return new HashMap<>(keyResults);
  }

  /**
   * Print a single Key Result value
   * @param key The Key Result Key to print
   * @return The Key Result value
   */
  public String printKeyResult(String key) {
    Set<String> values = keyResults.get(key);
    return PrintUtils.printPluralizedCommaSeparatedRow(key, values);
  }
}
