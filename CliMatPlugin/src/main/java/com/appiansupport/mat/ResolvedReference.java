package com.appiansupport.mat;

import java.util.Comparator;
import java.util.Objects;
import org.eclipse.mat.snapshot.ISnapshot;
import org.eclipse.mat.snapshot.model.IObject;
import org.eclipse.mat.snapshot.model.NamedReference;


/** A ResolvedReference is a NamedReference holding its IObject as a field.
 * ResolvedReferences help prevent complications and redundancy due to SnapshotExceptions from NamedReference.getObject() and NamedReference.getObjectId().
 */

public class ResolvedReference extends NamedReference {
  static final String JAVA_LOCAL_REF_TAG = "<Java Local>";
  static final String CLASS_REF_TAG = "<class>";
  public static final Comparator<ResolvedReference> COMPARE_BY_RETAINED_HEAP = Comparator.comparing(r -> r.getObject().getRetainedHeapSize(), Comparator.reverseOrder());
  public static final Comparator<ResolvedReference> COMPARE_BY_NAME = Comparator.comparing(r -> r.getObject().getDisplayName());

  private IObject object;

  public ResolvedReference(ISnapshot snapshot, IObject object, long address, String name) {
    super(snapshot, address, name);
    this.object = Objects.requireNonNull(object, "ResolvedReference must include a valid IObject.");
  }

  public ResolvedReference(ISnapshot snapshot, IObject object, NamedReference ref) {
    super(snapshot, ref.getObjectAddress(), ref.getName());
    this.object = Objects.requireNonNull(object, "ResolvedReference must include a valid IObject.");
  }

  public static String getReferenceName(ResolvedReference ref) {
    String refName = ref.getName();
    //"Java Local" tag is not useful.
    //"class" is in IObject Display Name, so don't print Class tag.
    if (!(refName.equals(JAVA_LOCAL_REF_TAG) || refName.equals(CLASS_REF_TAG))) {
      return refName;
    } else {
      return "";
    }
  }

  public static String getFullName(ResolvedReference ref) {
    String displayName = ref.getObject().getDisplayName();
    String refName = getReferenceName(ref);
    if (refName.isEmpty()) {
      return displayName;
    } else {
      return String.format("(%s) %s", refName, displayName);
    }
  }

  @Override public IObject getObject()  {
    return object;
  }

  @Override public int getObjectId()  {
    return object.getObjectId();
  }

  @Override public String toString() {
    return this.getName() + " " + object.getDisplayName();
  }
}
