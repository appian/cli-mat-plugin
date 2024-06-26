package com.appiansupport.mat.utils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.eclipse.mat.SnapshotException;
import org.eclipse.mat.snapshot.ISnapshot;
import org.eclipse.mat.snapshot.model.IObject;
import org.eclipse.mat.util.IProgressListener;

/**
 * Utility class for searching references (incoming, outgoing, or both) of IObjects.
 */
public class ReferenceFinder {
  private ISnapshot snapshot;
  private IProgressListener listener;

  public ReferenceFinder(ISnapshot snapshot, IProgressListener listener){
    this.snapshot = snapshot;
    this.listener = listener;
  }

  private boolean doesMatchDisplayName(IObject object, Pattern pattern) {
    String objectDisplayName = object.getDisplayName();
    Matcher matcher = pattern.matcher(objectDisplayName);
    return matcher.find();
  }

  /**
   * @param objects the List of IObjects to search
   * @param pattern The Pattern to check against the Objects' display name
   * @return The list of IObjects matching pattern
   */
  public List<IObject> searchObjectsByName(List<? extends IObject> objects, Pattern pattern) {
    List<IObject> matchingObjects = new ArrayList<>();
    for (IObject object : objects) {
      if (doesMatchDisplayName(object, pattern)) {
        matchingObjects.add(object);
      }
    }
    return matchingObjects;
  }

  private Set<IObject> findMatchingReferencesByName(IObject sourceObject, int[] referenceIds, Pattern pattern) {
    Set<IObject> foundObjects = new HashSet<>();
    for (int referenceId : referenceIds) {
      try {
        IObject referenceObject = snapshot.getObject(referenceId);
        if (doesMatchDisplayName(referenceObject, pattern)) {
          foundObjects.add(referenceObject);
        }
      } catch (SnapshotException snapshotException) {
        String snapshotExceptionMessage = String.format("Unable to process reference %s%s", referenceId, sourceObject == null ? "" : " of Object " + sourceObject.getDisplayName());
        listener.sendUserMessage(IProgressListener.Severity.ERROR, snapshotExceptionMessage, snapshotException);
      }
    }
    return foundObjects;
  }

  private Set<IObject> findMatchingReferences(IObject sourceObject, int[] referenceIds, List<IObject> objectsToMatch) {
    Set<IObject> foundObjects = new HashSet<>();
    for (int referenceId : referenceIds) {
      try {
        IObject referenceObject = snapshot.getObject(referenceId);
        if (objectsToMatch == null || objectsToMatch.isEmpty()) {
          foundObjects.add(referenceObject);
        } else {
          if (objectsToMatch.contains(referenceObject)) {
            foundObjects.add(referenceObject);
          }
        }
      } catch (SnapshotException snapshotException) {
        String snapshotExceptionMessage = String.format("Unable to process reference %s%s", referenceId, sourceObject == null ? "" : " of Object " + sourceObject.getDisplayName());
        listener.sendUserMessage(IProgressListener.Severity.ERROR, snapshotExceptionMessage, snapshotException);
      }
    }
    return foundObjects;
  }

  /**
   * @param sourceObject The IObject to search
   * @return The Set of matching which are both outgoing references of sourceObject and are in objectsToMatch
   */
  public Set<IObject> findMatchingOutgoingReferences(IObject sourceObject, List<IObject> objectsToMatch) {
    Set<IObject> foundObjects = new HashSet<>();
    try {
      int[] outgoingReferenceIds = snapshot.getOutboundReferentIds(sourceObject.getObjectId());
      foundObjects = findMatchingReferences(sourceObject, outgoingReferenceIds, objectsToMatch);
    } catch (SnapshotException snapshotExceptionOuter) {
      listener.sendUserMessage(IProgressListener.Severity.ERROR, "Unable to get outgoing references of Object: " + sourceObject.getDisplayName(), snapshotExceptionOuter);
    }
    return foundObjects;
  }

  /**
   * @param sourceObject The IObject to search
   * @return The Set of matching which are both outgoing references of sourceObject and are in objectToMatch
   */
  public Set<IObject> findMatchingOutgoingReferences(IObject sourceObject, IObject objectToMatch) {
    ArrayList<IObject> objectsToMatch = new ArrayList<>();
    if (objectToMatch != null) {
      objectsToMatch.add(objectToMatch);
    }
    return findMatchingOutgoingReferences(sourceObject, objectsToMatch);
  }

  /**
   * @param sourceObject The IObject to search
   * @param patternToMatch The Regex pattern to match the name of references
   * @return The Set of matching IObjects
   */
  public Set<IObject> findMatchingOutgoingReferencesByName(IObject sourceObject, Pattern patternToMatch) {
    Set<IObject> foundObjects = new HashSet<>();
    try {
      int[] outgoingReferenceIds = snapshot.getOutboundReferentIds(sourceObject.getObjectId());
      foundObjects = findMatchingReferencesByName(sourceObject, outgoingReferenceIds, patternToMatch);
    } catch (SnapshotException snapshotExceptionOuter) {
      listener.sendUserMessage(IProgressListener.Severity.ERROR, "Unable to get outgoing references of Object: " + sourceObject.getDisplayName(), snapshotExceptionOuter);
    }
    return foundObjects;
  }

  /**
   * @param sourceObject The IObject to search
   * @param objectsToMatch The master List of Objects search for
   * @return The Set ot of IObjects which are both incoming references of sourceObject and are in objectsToMatch
   */
  public Set<IObject> findMatchingIncomingReferences(IObject sourceObject, List<IObject> objectsToMatch) {
    Set<IObject> foundObjects = new HashSet<>();
    try {
      int[] incomingReferenceIds = snapshot.getInboundRefererIds(sourceObject.getObjectId());
      foundObjects = findMatchingReferences(sourceObject, incomingReferenceIds, objectsToMatch);
    } catch (SnapshotException snapshotExceptionOuter) {
      listener.sendUserMessage(IProgressListener.Severity.ERROR, "Unable to get incoming references of Object: " + sourceObject.getDisplayName(), snapshotExceptionOuter);
    }
    return foundObjects;
  }

  /**
   * @param sourceObject The IObject to search
   * @return The Set of matching which are both incoming references of sourceObject and are in objectToMatch
   */
  public Set<IObject> findMatchingIncomingReferences(IObject sourceObject, IObject objectToMatch) {
    List<IObject> objectsToMatch = new ArrayList<>();
    if (objectToMatch != null) {
      objectsToMatch.add(objectToMatch);
    }
    return findMatchingIncomingReferences(sourceObject, objectsToMatch);
  }

  /**
   * @param sourceObject The IObject to search
   * @param patternToMatch The Regex pattern to match the name of references
   * @return The Set of matching IObjects
   */
  public Set<IObject> findMatchingIncomingReferencesByName(IObject sourceObject, Pattern patternToMatch) {
    Set<IObject> foundObjects = new HashSet<>();
    try {
      int[] incomingReferenceIds = snapshot.getInboundRefererIds(sourceObject.getObjectId());
      foundObjects = findMatchingReferencesByName(sourceObject, incomingReferenceIds, patternToMatch);
    } catch (SnapshotException snapshotExceptionOuter) {
      listener.sendUserMessage(IProgressListener.Severity.ERROR, "Unable to get incoming references of Object: " + sourceObject.getDisplayName(), snapshotExceptionOuter);
    }
    return foundObjects;
  }

  /**
   * @param sourceObject The IObject to search
   * @param objectsToMatch The master List of Objects search for
   * @return The Set ot of IObjects which are both references of sourceObject and are in objectsToMatch
   */
  public Set<IObject> findMatchingReferencesBothDirections(IObject sourceObject, List<IObject> objectsToMatch) {
    Set<IObject> incomingMatches = findMatchingIncomingReferences(sourceObject, objectsToMatch);
    Set<IObject> outgoingMatches = findMatchingOutgoingReferences(sourceObject, objectsToMatch);
    incomingMatches.addAll(outgoingMatches);
    return incomingMatches;
  }

  /**
   * @param sourceObject The IObject to search
   * @return The Set of matching which are both references of sourceObject and are in objectToMatch
   */
  public Set<IObject> findMatchingReferencesBothDirections(IObject sourceObject, IObject objectToMatch) {
    Set<IObject> incomingMatches = findMatchingIncomingReferences(sourceObject, objectToMatch);
    Set<IObject> outgoingMatches = findMatchingOutgoingReferences(sourceObject, objectToMatch);
    incomingMatches.addAll(outgoingMatches);
    return incomingMatches;
  }

  /**
   * @param sourceObject The IObject to search
   * @param patternToMatch The Regex pattern to match the name of references
   * @return The Set of matching IObjects
   */
  public Set<IObject> findMatchingReferencesBothDirectionsByName(IObject sourceObject, Pattern patternToMatch) {
    Set<IObject> incomingMatches = findMatchingIncomingReferencesByName(sourceObject, patternToMatch);
    Set<IObject> outgoingMatches = findMatchingOutgoingReferencesByName(sourceObject, patternToMatch);
    incomingMatches.addAll(outgoingMatches);
    return incomingMatches;
  }

  private Set<IObject> convertReferenceIdsToObjects(IObject sourceObject, int[] referenceIds) {
    Set<IObject> foundObjects = new HashSet<>();
    for (int referenceId : referenceIds) {
      try {
        IObject referenceObject = snapshot.getObject(referenceId);
        foundObjects.add(referenceObject);
      } catch (SnapshotException snapshotException) {
        String snapshotExceptionMessage = String.format("Unable to process reference %s%s", referenceId, sourceObject == null ? "" : " of Object " + sourceObject.getDisplayName());
        listener.sendUserMessage(IProgressListener.Severity.ERROR, snapshotExceptionMessage, snapshotException);
      }
    }
    return foundObjects;
  }

  /**
   * @param sourceObject The IObject to search
   * @return the Set of outgoing IObjects
   */
  public Set<IObject> findOutgoingReferences(IObject sourceObject) {
    Set<IObject> foundObjects = new HashSet<>();
    try {
      int[] outgoingReferenceIds = snapshot.getOutboundReferentIds(sourceObject.getObjectId());
      foundObjects = convertReferenceIdsToObjects(sourceObject, outgoingReferenceIds);
    } catch (SnapshotException snapshotExceptionOuter) {
      listener.sendUserMessage(IProgressListener.Severity.ERROR, "Unable to get outgoing references of Object: " + sourceObject.getDisplayName(), snapshotExceptionOuter);
    }
    return foundObjects;
  }

  /**
   * @param sourceObject The IObject to search
   * @return the Set of incoming IObjects
   */
  public Set<IObject> findIncomingReferences(IObject sourceObject) {
    Set<IObject> foundObjects = new HashSet<>();
    try {
      int[] incomingReferenceIds = snapshot.getInboundRefererIds(sourceObject.getObjectId());
      foundObjects = convertReferenceIdsToObjects(sourceObject, incomingReferenceIds);
    } catch (SnapshotException snapshotExceptionOuter) {
      listener.sendUserMessage(IProgressListener.Severity.ERROR, "Unable to get outgoing references of Object: " + sourceObject.getDisplayName(), snapshotExceptionOuter);
    }
    return foundObjects;
  }
}
