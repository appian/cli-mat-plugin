package com.appiansupport.example.knownobjects;

import com.appiansupport.mat.utils.ThreadFinder;
import com.appiansupport.mat.knownobjects.KnownObject;
import com.appiansupport.mat.knownobjects.KnownObjectProvider;

import java.util.Optional;
import org.apache.commons.text.TextStringBuilder;
import org.eclipse.mat.snapshot.ISnapshot;
import org.eclipse.mat.snapshot.model.IObject;
import org.eclipse.mat.util.IProgressListener;

public class ExampleKnownObjectExtension extends KnownObject {
  private String objectNameOfInterest;

  public ExampleKnownObjectExtension(ISnapshot snapshot, IProgressListener listener, IObject object, ThreadFinder threadFinder, KnownObjectProvider knownObjectProvider) {
    super(snapshot, listener, object, threadFinder, knownObjectProvider);
  }

  public void getInfo() {
      getName();
      getReferencedThreads();
  }

  @Override
  public String printInfo() {
    getInfo();
    TextStringBuilder output = new TextStringBuilder();
    getName().ifPresent(s -> output.appendln("Name: " + s));
    output.append(printReferencedThreads());
    return output.toString();
  }

  //We know that the String name of this object lives in an Outgoing Reference 'name', so we fetch that directly
  public Optional<String> getName() {
    if (objectNameOfInterest != null) {
      return Optional.of(objectNameOfInterest);
    } else {
      Optional<String> exampleNameOfInterest = ObjectUtils.getStringFromObjectResolution(thisObject, "name.value");
      objectNameOfInterest = expressionName.orElse(null);
      return Optional.ofNullable(objectNameOfInterest);
    }
  }
}
