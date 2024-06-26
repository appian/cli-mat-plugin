package com.appiansupport.mat.suspects;

import com.appiansupport.mat.constants.Messages;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.appiansupport.mat.utils.internal.HeapSizer;
import org.eclipse.mat.SnapshotException;
import org.eclipse.mat.snapshot.ClassHistogramRecord;
import org.eclipse.mat.snapshot.Histogram;
import org.eclipse.mat.snapshot.ISnapshot;
import org.eclipse.mat.snapshot.model.IClass;
import org.eclipse.mat.snapshot.model.IClassLoader;
import org.eclipse.mat.snapshot.model.IObject;
import org.eclipse.mat.util.IProgressListener;

public class LeakSuspectsFinder {
  public static final int SUSPECT_THRESHOLD_PERCENT = 20;
  public static final double BIG_DROP_RATIO = 0.7;
  //Todo: Make these configurable or constants elsewhere
  private static final int MAX_DEPTH = 1000;
  private final ISnapshot snapshot;
  private final IProgressListener listener;
  private final HeapSizer heapSizer;
  private SuspectClassRecord[] suspectClasses;
  private final long suspectThreshold;
  private final Map<String,Long> suspectObjectClassToSize;
  private boolean haveAnalyzed;

  private List<IObject> suspectObjects;
  private final HashMap<Integer, Integer> idToAccumulationPoint;

  public LeakSuspectsFinder(ISnapshot snapshot, IProgressListener listener, HeapSizer heapSizer) {
    this.snapshot = snapshot;
    this.listener = listener;
    this.heapSizer = heapSizer;
    long totalHeap = snapshot.getSnapshotInfo().getUsedHeapSize();
    suspectThreshold = SUSPECT_THRESHOLD_PERCENT * totalHeap / 100;
    suspectObjectClassToSize = new HashMap<>(4);
    idToAccumulationPoint = new HashMap<>(4);
    haveAnalyzed = false;
  }

  private SuspectClassRecord[] findSuspectClasses() throws SnapshotException {
    SuspectClassBuilder builder = new SuspectClassBuilder(snapshot, listener, heapSizer, BIG_DROP_RATIO);
    ArrayList<ClassHistogramRecord> suspiciousClassList = new ArrayList<>();
    int[] topDominators = snapshot.getImmediateDominatedIds(-1);
    try {
      listener.subTask(Messages.FindLeaksQuery_SearchingGroupsOfObjects);
      Histogram parentHistogram = builder.groupByClasses(topDominators);

      ClassHistogramRecord[] classRecords = parentHistogram.getClassHistogramRecords().toArray(new ClassHistogramRecord[0]);
      Arrays.sort(classRecords, Histogram.reverseComparator(Histogram.COMPARATOR_FOR_RETAINEDHEAPSIZE));

      int k = 0;
      while (k < classRecords.length && classRecords[k].getRetainedHeapSize() > suspectThreshold) {
        long classRetained = classRecords[k].getRetainedHeapSize();
        // (Eclipse) avoid showing class-suspect for something found on object level
        // DD: Do add the suspect Class if its total size is SUSPECT_THRESHOLD_PERCENT greater than the sum of suspect objects
        Long suspectObjectsOfClassRetained = suspectObjectClassToSize.get(classRecords[k].getLabel());
        if (suspectObjectsOfClassRetained == null || (classRetained - suspectObjectsOfClassRetained > suspectThreshold)) {
          suspiciousClassList.add(classRecords[k]);
        }
        k++;
      }

      if (!suspiciousClassList.isEmpty()) {
        suspectClasses = builder.buildSuspectClassRecords(suspiciousClassList);
      }

      if (listener.isCanceled()) {
        throw new IProgressListener.OperationCanceledException();
      }
    } catch (SnapshotException snapshotException) {
      listener.sendUserMessage(IProgressListener.Severity.ERROR, "Error fetching suspect classes: ", snapshotException);
    }
    return suspectClasses;
  }

  private List<IObject> findSuspectObjects() throws SnapshotException {
    if (suspectObjects != null) {
      return suspectObjects;
    }
    int[] topDominators = snapshot.getImmediateDominatedIds(-1);

    listener.subTask(Messages.FindLeaksQuery_SearchingSingleObjects);
    int i = 0;
    while (i < topDominators.length && snapshot.getRetainedHeapSize(topDominators[i]) > suspectThreshold) {
      int topDominator = topDominators[i];
      long dominatorRetainedHeap = snapshot.getRetainedHeapSize(topDominator);
      if (suspectObjects == null) {
        suspectObjects = new ArrayList<>();
      }
      IObject suspiciousObject = snapshot.getObject(topDominator);
      suspectObjects.add(suspiciousObject);
      //Keep track of the total size of suspects by Class, to determine if suspect Class should also be reported.
      suspectObjectClassToSize.merge(snapshot.getClassOf(topDominator).getName(),dominatorRetainedHeap,Long::sum);
      addSuspectObjectAccumulationPoint(topDominators[i]);
      i++;
    }
    return suspectObjects;
  }

  private void addSuspectObjectAccumulationPoint(int bigObjectId) throws SnapshotException {
    int dominator = bigObjectId;
    double dominatorRetainedSize = snapshot.getRetainedHeapSize(dominator);
    int[] dominated = snapshot.getImmediateDominatedIds(dominator);

    int depth = 0;
    while (dominated != null && dominated.length != 0 && depth < MAX_DEPTH) {
      double dominatedRetainedSize = snapshot.getRetainedHeapSize(dominated[0]);
      if (dominatedRetainedSize / dominatorRetainedSize < BIG_DROP_RATIO) {
        if(dominator != bigObjectId) {
          idToAccumulationPoint.put(bigObjectId, dominator);
        }
        return;
      }

      dominatorRetainedSize = dominatedRetainedSize;
      dominator = dominated[0];
      dominated = snapshot.getImmediateDominatedIds(dominator);
      depth++;
    }

    if (dominated == null || dominated.length == 0) {
      idToAccumulationPoint.put(bigObjectId, dominator);
    }
  }

  public String printAccumulationPointInfo(IObject object) {
    Integer accumulationPointId = idToAccumulationPoint.get(object.getObjectId());
    if (accumulationPointId == null) {
      return null;
    } else {
      try {
        IObject accumulationObject = snapshot.getObject(accumulationPointId);
        return "Heap is accumulated in " + printAccumulationPointDetails(accumulationObject);
      } catch (SnapshotException snapshotException) {
        listener.sendUserMessage(
          IProgressListener.Severity.ERROR,
          String.format("Unable to fetch Object for accumulation point %d of object %s", accumulationPointId, object.getDisplayName()),
          snapshotException);
        return null;
      }
    }
  }

  private String printAccumulationPointDetails(IObject accumulationObject) {
    int accumulationPointId = accumulationObject.getObjectId();
    if (snapshot.isClassLoader(accumulationPointId)) {
      IClassLoader accPointClassloader = (IClassLoader) accumulationObject;

      String classloaderName = getClassLoaderName(accPointClassloader);
      return String.format(Messages.LeakHunterQuery_Msg_AccumulatedBy, classloaderName, accumulationObject.getRetainedHeapSize());
    } else if (snapshot.isClass(accumulationPointId)) {
      IClass clazz = (IClass) accumulationObject;
      return String.format(Messages.LeakHunterQuery_Msg_AccumulatedByLoadedBy, clazz.getName(), accumulationObject.getRetainedHeapSize());
    } else {
      String className = accumulationObject.getClazz().getName();
      return String.format(Messages.LeakHunterQuery_Msg_AccumulatedByInstance, className, accumulationObject.getRetainedHeapSize());
    }
  }

  private String getClassLoaderName(IObject classLoader) {
    if (classLoader.getObjectAddress() == 0) {
      return "<system class loader>";
    } else {
      String name = classLoader.getClassSpecificName();
      if (name == null) {
        name = classLoader.getTechnicalName();
      }
      return name;
    }
  }

  public List<IObject> getSuspectObjects() {
    analyze();
    return suspectObjects == null ? null : new ArrayList<>(suspectObjects);
  }

  public SuspectClassRecord[] getSuspectClasses() {
    analyze();
    return suspectClasses == null ? null : suspectClasses.clone();
  }

  //Analyze suspect Classes & objects together because they depend on each other
  private void analyze() {
    if (!haveAnalyzed) {
      try {
        findSuspectObjects();
      } catch (SnapshotException snapshotException) {
        listener.sendUserMessage(IProgressListener.Severity.ERROR, "Error finding suspect objects", snapshotException);
      }
      try {
        findSuspectClasses();
      } catch (SnapshotException snapshotException) {
        listener.sendUserMessage(IProgressListener.Severity.ERROR, "Error finding suspect classes", snapshotException);
      }
      haveAnalyzed = true;
    }
  }
}