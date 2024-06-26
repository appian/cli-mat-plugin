package com.appiansupport.appianmat.resolvers;

import org.eclipse.mat.SnapshotException;
import org.eclipse.mat.snapshot.extension.IClassSpecificNameResolver;
import org.eclipse.mat.snapshot.extension.Subject;
import org.eclipse.mat.snapshot.model.IObject;

@Subject("com.appiancorp.suiteapi.knowledge.Document")
public class DocumentNameResolver implements IClassSpecificNameResolver {

  @Override public String resolve(IObject object) {
    try {
      Long docId = (Long) object.resolveValue("_id.value");
      IObject externalName = (IObject) object.resolveValue("_externalFilename");
      Integer size = (Integer) object.resolveValue("_size.value");
      String result = "";
      if (docId != null) {
        result += "#" + Long.toString(docId);
        if (externalName != null && externalName.getClassSpecificName() != null) {
          result +=": " + externalName.getClassSpecificName();
        }
      } else if (externalName != null && externalName.getClassSpecificName() != null) {
        result += externalName.getClassSpecificName();
      } else {
        return null;
      }
      if (size != null) {
        result += String.format(" (%,dB)",size);
      }
      return result.isEmpty() ? null : result;
    } catch (SnapshotException e) {
      System.err.println("Error resolving name of " + object.getDisplayName());
      return null;
    }
  }
}