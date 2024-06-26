package com.appiansupport.mat.utils;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import org.apache.commons.text.TextStringBuilder;
import org.eclipse.mat.SnapshotException;

import org.eclipse.mat.snapshot.model.IObject;
import org.eclipse.mat.snapshot.model.NamedReference;

public class ObjectUtils {

  public static final Comparator<IObject> compareIObjectByDisplayName = Comparator.nullsLast(Comparator.comparing(IObject::getDisplayName));
  public static final Comparator<IObject> compareIObjectByClassSpecificName = Comparator.nullsLast(Comparator.comparing(IObject::getClassSpecificName));
  public static final Comparator<IObject> compareIObjectByRetainedHeap = Comparator.comparing(IObject::getRetainedHeapSize);
  public static final Comparator<IObject> compareIObjectByRetainedHeapDescending = Comparator.comparing(IObject::getRetainedHeapSize).reversed();

  /**
   * Optional wrapper around IObject.resolveValue() for Strings.
   * @param object the IObject to resolve a path of
   * @param pathToResolve The path to resolve, within outoing references of this object. See Eclipse MAT IObject docs for more details.
   * @return The String, if found
   */
  public static Optional<String> getStringFromObjectResolution(IObject object, String pathToResolve) {
    try {
      // Will either be null, a primitive, or an ObjectReference (IObject)
      Object resolvedObject = object.resolveValue(pathToResolve);
      if (resolvedObject == null) {
        return Optional.empty();
      //Helper for mock testing, otherwise should be impossible
      } else if (resolvedObject instanceof String) {
        return Optional.of((String) resolvedObject);
      } else {
        IObject resolvedIObject = (IObject) resolvedObject;
        return Optional.of(resolvedIObject.getClassSpecificName());
      }
    } catch (SnapshotException snapshotException) {
      System.err.printf("Error resolving path %s of object %s%n", pathToResolve, object.getDisplayName());
      snapshotException.printStackTrace();
      return Optional.empty();
    }
  }

  /**
   * Print all Strings, line-separated, of a given IObject
   * @param object the IObject to search
   * @return All Strings found in outgoing references of the IObject
   */
  public static String printObjectStrings(IObject object) {
    TextStringBuilder output = new TextStringBuilder();
    List<NamedReference> outboundReferences = object.getOutboundReferences();
    Set<String> sortedStringSet = new TreeSet<>();
    for (NamedReference namedReference : outboundReferences) {
      try {
        IObject currObject = namedReference.getObject();
        //Print Strings only
        if (currObject.getTechnicalName().startsWith("java.lang.String")) {
          String classSpecificName = currObject.getClassSpecificName();
          //Handling String[] gracefully, which have a null Class-specific name
          String classSpecificOrDisplayNameIfNull = (classSpecificName == null) ? currObject.getDisplayName() : classSpecificName;
          sortedStringSet.add(classSpecificOrDisplayNameIfNull);
        }
      } catch (SnapshotException snapshotException) {
        System.err.println("Error while trying to read object " + namedReference.toString());
        snapshotException.printStackTrace();
      }
    }
    if(!sortedStringSet.isEmpty()){
      output.appendln(PrintUtils.printNote("All Strings of Object"));
    }
    sortedStringSet.forEach(output::appendln);
    return output.toString();
  }

}
