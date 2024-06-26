package com.appiansupport.mat.knownobjects;

import com.appiansupport.mat.utils.ThreadFinder;
import org.apache.commons.text.TextStringBuilder;
import org.eclipse.mat.snapshot.ISnapshot;
import org.eclipse.mat.snapshot.model.IObject;
import org.eclipse.mat.util.IProgressListener;

/**
 * UnknownObject is used for all IObjects which do not have a matchinbg KnownObject implementation.
 */
class UnknownObject extends KnownObject {
  private String gcRoots;

  UnknownObject(ISnapshot snapshot, IProgressListener listener, IObject object, ThreadFinder threadFinder) {
    super(snapshot, listener, object, threadFinder);
  }

  /**
   * Perform a general analysis, as no specific information about this IObject is known.
   */
  @Override public void getInfo() {
    if (isAnalyzed) {
      return;
    }
    getReferencedThreads();
    gcRoots = gcRootsHandler.printGcRoots();
    isAnalyzed = true;
  }

  /**
   * @return a General analysis of this UnknownObject
   */
  @Override public String printInfo() {
    getInfo();
    TextStringBuilder output = new TextStringBuilder();
    output.appendln("Object not recognized; looking for Threads in references:");
    output.append(printReferencedThreads());
    if (referencedThreads == null || referencedThreads.isEmpty()) {
      output.appendln("No Threads found");
      if(gcRoots != null) {
        output.appendNewLine();
        output.append(gcRoots);
      }
    }
    return output.toString();
  }
}
