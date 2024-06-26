package com.appiansupport.mat.suspects;

import com.appiansupport.mat.constants.Messages;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.appiansupport.mat.utils.ObjectUtils;
import com.appiansupport.mat.utils.internal.HeapSizer;
import com.appiansupport.mat.utils.internal.ObjectFetcher;
import org.eclipse.mat.SnapshotException;
import org.eclipse.mat.snapshot.ClassHistogramRecord;
import org.eclipse.mat.snapshot.ClassLoaderHistogramRecord;
import org.eclipse.mat.snapshot.Histogram;
import org.eclipse.mat.snapshot.IMultiplePathsFromGCRootsComputer;
import org.eclipse.mat.snapshot.ISnapshot;
import org.eclipse.mat.snapshot.MultiplePathsFromGCRootsRecord;
import org.eclipse.mat.snapshot.model.GCRootInfo;
import org.eclipse.mat.snapshot.model.IClass;
import org.eclipse.mat.snapshot.model.IObject;
import org.eclipse.mat.util.IProgressListener;
import org.eclipse.mat.util.MessageUtil;

class SuspectClassBuilder {

  private static final Set<String> REFERENCE_FIELD_SET = new HashSet<>(Collections.singletonList("referent"));
  private static final Set<String> UNFINALIZED_REFERENCE_FIELD_SET = new HashSet<>(Collections.singletonList("<" + GCRootInfo.getTypeAsString(GCRootInfo.Type.UNFINALIZED) + ">"));
  private static final double SUSPECT_GROUP_PARENT_THRESHOLD_PERCENT = 0.2;
  private static final int MAX_CLASS_INSTANCES_TO_SEARCH = 1000;

  private final ISnapshot snapshot;
  private final IProgressListener listener;
  private final HeapSizer heapSizer;
  private final double bigDropRatio;

  SuspectClassBuilder(ISnapshot snapshot, IProgressListener listener, HeapSizer heapSizer, double bigDropRatio) {
    this.snapshot = snapshot;
    this.listener = listener;
    this.heapSizer = heapSizer;
    this.bigDropRatio = bigDropRatio;
  }

  //Is grouping by classloader worthwhile?
  Histogram groupByClasses(int[] dominated) throws SnapshotException {
    Histogram histogram = snapshot.getHistogram(dominated, listener);

    if (listener.isCanceled()) {
      throw new IProgressListener.OperationCanceledException();
    }
    Collection<ClassHistogramRecord> records = histogram.getClassHistogramRecords();
    records.forEach(heapSizer::calculateClassRetained);
    Collection<ClassLoaderHistogramRecord> loaderRecords = histogram.getClassLoaderHistogramRecords();
    for (ClassLoaderHistogramRecord record : loaderRecords) {
      long retainedSize = 0;
      for (ClassHistogramRecord classRecord : record.getClassHistogramRecords()) {
        retainedSize += classRecord.getRetainedHeapSize();
      }

      record.setRetainedHeapSize(retainedSize);
    }
    return histogram;
  }

  SuspectClassRecord[] buildSuspectClassRecords(ArrayList<? extends ClassHistogramRecord> suspiciousClasses) throws SnapshotException {
    SuspectClassRecord[] suspectRecords = new SuspectClassRecord[suspiciousClasses.size()];
    int suspectIndex = 0;
    for (ClassHistogramRecord record : suspiciousClasses) {
      if (listener.isCanceled()) {
        throw new IProgressListener.OperationCanceledException();
      }
      suspectRecords[suspectIndex++] = buildSuspectClassRecord(record);
    }
    return suspectRecords;
  }

  private SuspectClassRecord buildSuspectClassRecord(ClassHistogramRecord record) throws SnapshotException {
    List<IObject> classObjects = new ObjectFetcher(snapshot).getObjectsFromClassRecord(record);
    classObjects.sort(ObjectUtils.compareIObjectByRetainedHeapDescending);
    if (classObjects.size() > MAX_CLASS_INSTANCES_TO_SEARCH) {
      listener.sendUserMessage(IProgressListener.Severity.INFO, MessageUtil.format(
          Messages.FindLeaksQuery_TooManySuspects,
          classObjects.size(),
          MAX_CLASS_INSTANCES_TO_SEARCH),
          null
      );
    }
    int[] topRecordInstanceIds = classObjects.stream()
        .limit(MAX_CLASS_INSTANCES_TO_SEARCH)
        .mapToInt(IObject::getObjectId)
        .toArray();
    List<SuspectParent> commonParents = getCommonParents(topRecordInstanceIds);
    return new SuspectClassRecord(record, classObjects, commonParents);
  }

  private List<SuspectParent> getCommonParents(int[] objectIds) throws SnapshotException {
    //All this method does is call MultiplePathsFromGCRootsComputerImpl constructor
    IMultiplePathsFromGCRootsComputer comp = snapshot.getMultiplePathsFromGCRoots(objectIds, getAvoidedFields());

    //returns one record for each group of paths starting from the same GC root
    MultiplePathsFromGCRootsRecord[] records = comp.getPathsByGCRoot(listener);
    List<SuspectParent> commonParents = new ArrayList<>();

    if (listener.isCanceled()) {
      throw new IProgressListener.OperationCanceledException();
    }
    if (records.length > 0) {
      int numPaths = comp.getAllPaths(listener).length;
      int diff = objectIds.length - numPaths;
      if (diff > 0) {
        listener.sendUserMessage(IProgressListener.Severity.INFO, MessageUtil.format(Messages.FindLeaksQuery_PathNotFound, diff, objectIds.length), null);
      }
      setRetainedSizesForPaths(records, snapshot);
      Arrays.sort(records, MultiplePathsFromGCRootsRecord.getComparatorByNumberOfReferencedObjects());

      MultiplePathsFromGCRootsRecord parentRecord = records[0];

      //min # of objects referenced by parentRecord to be considered for commonPath and accPoint
      int threshold = (int) (SUSPECT_GROUP_PARENT_THRESHOLD_PERCENT * objectIds.length);

      while (parentRecord.getCount() > threshold) {
        try {
          IObject parentObject = snapshot.getObject(parentRecord.getObjectId());
          SuspectParent pathParent = new SuspectParent(parentObject, (double) parentRecord.getCount() / (double) objectIds.length);
          commonParents.add(pathParent);

          MultiplePathsFromGCRootsRecord[] children = parentRecord.nextLevel();
          if (children == null || children.length == 0) {
            // reached the end - report the parent as it is big enough
            break;
          }
          setRetainedSizesForPaths(children, snapshot);
          Arrays.sort(children, MultiplePathsFromGCRootsRecord.getComparatorByNumberOfReferencedObjects());

          if ((double) children[0].getReferencedRetainedSize() / (double) parentRecord.getReferencedRetainedSize() < bigDropRatio) {
            // there is a big drop here - return the parent
            break;
          }

          // no big drop - take the biggest child and try again
          parentRecord = children[0];
        } catch (SnapshotException snapshotException) {
          listener.sendUserMessage(IProgressListener.Severity.ERROR, String.format("Unable to fetch object %s level %s", parentRecord.getObjectId(), parentRecord.getLevel()), snapshotException);
        }
      }
    }
    return commonParents;
  }

  private void setRetainedSizesForPaths(MultiplePathsFromGCRootsRecord[] records, ISnapshot snapshot) {
    for (MultiplePathsFromGCRootsRecord rec : records) {
      // (Eclipse) Get the "end" objects for each path.
      // This is equal to getting all the paths and returning their element [0]
      int[] referencedObjects = rec.getReferencedObjects();
      long retained = 0;
      for (int objectId : referencedObjects) {
        try {
          retained += snapshot.getRetainedHeapSize(objectId);
        } catch (SnapshotException snapshotException) {
          listener.sendUserMessage(IProgressListener.Severity.ERROR, "Unable to get retained Heap size of object " + objectId, snapshotException);
        }
      }
      rec.setReferencedRetainedSize(retained);
    }
  }

  //From Eclipse
  private Map<IClass, Set<String>> getAvoidedFields() throws SnapshotException {
    // (Eclipse) calculate the shortest paths to all
    // avoid weak paths
    Map<IClass, Set<String>> classToAvoidedFields = new HashMap<>();
    Collection<IClass> classes = snapshot.getClassesByName("java.lang.ref.WeakReference", true);
    if (classes != null) {
      for (IClass clazz : classes) {
        classToAvoidedFields.put(clazz, REFERENCE_FIELD_SET);
      }
    }
    // (Eclipse) Unfinalized objects from J9
    classes = snapshot.getClassesByName("java.lang.Runtime", false);
    if (classes != null) {
      for (IClass clazz : classes) {
        classToAvoidedFields.put(clazz, UNFINALIZED_REFERENCE_FIELD_SET);
      }
    }
    return classToAvoidedFields;
  }
}

