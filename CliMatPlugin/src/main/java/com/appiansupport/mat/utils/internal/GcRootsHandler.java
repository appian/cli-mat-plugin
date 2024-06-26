package com.appiansupport.mat.utils.internal;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.appiansupport.mat.utils.PrintUtils;
import com.appiansupport.mat.utils.ThreadFinder;
import org.eclipse.mat.SnapshotException;
import org.eclipse.mat.snapshot.IPathsFromGCRootsComputer;
import org.eclipse.mat.snapshot.ISnapshot;
import org.eclipse.mat.snapshot.model.IObject;


public class GcRootsHandler {
  private ISnapshot snapshot;
  private IObject object;
  private int objectId;
  private boolean haveSearched;
  private List<IObject> gcRoots;

  public GcRootsHandler(ISnapshot snapshot, IObject object) {
    this.snapshot = snapshot;
    this.object = object;
    this.objectId = object.getObjectId();
  }

  private List<IObject> getShortestGcRootsPath() {
    if (haveSearched) {
      return gcRoots;
    } else {
      try {
        IPathsFromGCRootsComputer pathsComputer = snapshot.getPathsFromGCRoots(objectId, null);
        if (pathsComputer == null) {
          haveSearched = true;
          return null;
        }
        int[] gcRootIds = pathsComputer.getNextShortestPath();
        if (gcRootIds == null || gcRootIds.length == 0) {
          haveSearched = true;
          return null;
        } else {
          List<IObject> gcRootObjects = new ArrayList<>();
          for (int i = gcRootIds.length - 1; i > -1; i--) {
            try {
              gcRootObjects.add(snapshot.getObject(gcRootIds[i]));
            } catch (SnapshotException snapshotExceptionInner) {
              System.err.printf("Error fetching GC Root %d of %s): %s", gcRootIds[i], object, snapshotExceptionInner.toString());
              haveSearched = true;
              return null;
            }
          }
          haveSearched = true;
          return gcRoots = gcRootObjects;
        }
      } catch (SnapshotException snapshotExceptionOuter) {
        System.err.printf("Error fetching GC roots of object %s: %s", object.getDisplayName(), snapshotExceptionOuter.toString());
        haveSearched = true;
        return null;
      }
    }
  }

  public Optional<List<IObject>> getGcRoots() {
    return Optional.ofNullable(getShortestGcRootsPath());
  }

  public String printGcRoots() {
    if (!haveSearched) {
      getShortestGcRootsPath();
    }
    if (gcRoots == null || gcRoots.isEmpty()) {
      return "";
    } else {
      if (gcRoots.size() == 1) {
        //should always be true, but just in case
        if (gcRoots.get(0).getObjectId() == object.getObjectId()) {
          return "";
        }
      }
      return PrintUtils.printNestedList((gcRoots.stream().map(IObject::getDisplayName).collect(Collectors.toList())));
    }
  }

  public Optional<Set<IObject>> getThreadsFromGcRoots(ThreadFinder threadFinder) {
    List<IObject> gcRoots = getShortestGcRootsPath();
    if (gcRoots != null) {
      Set<IObject> foundThreads = new HashSet<>();
      for (IObject gcRoot : gcRoots) {
        int id = gcRoot.getObjectId();
        if (id != object.getObjectId() && threadFinder.isThread(id)) {
          foundThreads.add(gcRoot);
        }
      }
      return Optional.of(foundThreads);
    } else {
      return Optional.empty();
    }
  }
}
