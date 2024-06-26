package com.appiansupport.mat.suspects;

import com.appiansupport.mat.constants.Messages;
import com.appiansupport.mat.knownobjects.KnownObject;
import com.appiansupport.mat.knownobjects.KnownObjectProvider;
import com.appiansupport.mat.utils.*;
import com.appiansupport.mat.utils.internal.HeapSizer;
import com.appiansupport.mat.utils.internal.HeapTablePrinter;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.TextStringBuilder;
import org.eclipse.mat.snapshot.ClassHistogramRecord;
import org.eclipse.mat.snapshot.model.IObject;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class LeakSuspectPrinter {
  private final LeakSuspectsFinder leakSuspectsFinder;
  private final KnownObjectProvider knownObjectProvider;
  private final HeapTablePrinter tablePrinter;
  private final HeapSizer heapSizer;
  private boolean haveFetchedSuspects;
  private List<IObject> suspectObjects;
  private SuspectClassRecord[] suspectClasses;
  private static final int NUM_SUSPECT_CLASS_INSTANCES_TO_REPORT = 10;

  public LeakSuspectPrinter(LeakSuspectsFinder leakSuspectsFinder, KnownObjectProvider knownObjectProvider,
                            HeapTablePrinter tablePrinter, HeapSizer heapSizer) {
    this.leakSuspectsFinder = leakSuspectsFinder;
    this.knownObjectProvider = knownObjectProvider;
    this.tablePrinter = tablePrinter;
    this.heapSizer = heapSizer;
    this.haveFetchedSuspects=false;
  }

  public String printLeakSuspectsReport(boolean printSuspectStrings) {
    if(!haveFetchedSuspects) {
      suspectObjects = leakSuspectsFinder.getSuspectObjects();
      suspectClasses = leakSuspectsFinder.getSuspectClasses();
      haveFetchedSuspects=true;
    }
    TextStringBuilder leakSuspectsOutput = new TextStringBuilder();
    leakSuspectsOutput.appendNewLine();
    leakSuspectsOutput.appendln(PrintUtils.printSection("Leak Suspects"));
    leakSuspectsOutput.appendNewLine();
    if (!(suspectObjects == null && suspectClasses == null)) {
      leakSuspectsOutput.append(printSuspectOverviewSection());
      if (suspectObjects != null) {
        leakSuspectsOutput.append(printSuspectObjectsSection(printSuspectStrings));
      }
      if (suspectClasses != null) {
        leakSuspectsOutput.append(printSuspectClassesSection());
      }
    } else {
      leakSuspectsOutput.appendln("No suspects found.");
      leakSuspectsOutput.appendln("Tip: Look for the Thread that threw the OutOfMemoryError " + "or a high volume of Threads.");
      leakSuspectsOutput.appendNewLine();
    }
    return leakSuspectsOutput.toString();
  }

  private String printSuspectOverviewSection() {
    TextStringBuilder suspectOverviewOutput = new TextStringBuilder();
    if (suspectObjects != null) {
      suspectOverviewOutput.appendln(tablePrinter.printObjectsTableWithHeapAndPercent(suspectObjects, "Suspect Objects"));
    }
    if (suspectClasses != null) {
      List<ClassHistogramRecord> suspectClassList = Arrays.stream(suspectClasses).map(SuspectClassRecord::histogramRecord).collect(Collectors.toList());
      suspectOverviewOutput.appendln(tablePrinter.printClassTableWithHeapAndPercent(suspectClassList, "Suspect Classes"));
    }
    return suspectOverviewOutput.toString();
  }

  private String printSuspectObjectsSection(boolean printStrings) {
    if (suspectObjects == null) {
      return null;
    } else {
      TextStringBuilder suspectObjectsOutput = new TextStringBuilder();
      if(suspectObjects.size()>1){
        suspectObjectsOutput.appendln(printKeyResultsOfObjects(suspectObjects, "Suspect Objects"));
      }
      for (int i = 0; i < suspectObjects.size(); i++) {
        IObject suspect = suspectObjects.get(i);
        suspectObjectsOutput.appendln(PrintUtils.printHeader(String.format("Suspect object %d details", i + 1)));
        suspectObjectsOutput.appendNewLine();
        suspectObjectsOutput.appendln(printSingleSuspectObjectInfo(suspect, printStrings));
      }
      return suspectObjectsOutput.toString();
    }
  }

  private String printSuspectClassesSection() {
    if (suspectClasses == null) {
      return null;
    } else {
      TextStringBuilder suspectClassesOutput = new TextStringBuilder();
      for (int i = 0 ; i < suspectClasses.length ; i++) {
        SuspectClassRecord suspectClass = suspectClasses[i];
        suspectClassesOutput.appendln(PrintUtils.printHeader(String.format("Suspect Class %d details", i + 1)));
        suspectClassesOutput.appendNewLine();
        suspectClassesOutput.appendln(printSingleSuspectClassInfo(suspectClass));
      }
      return suspectClassesOutput.toString();
    }
  }

  private String printSingleSuspectClassInfo(SuspectClassRecord sus) {
    ClassHistogramRecord suspectRecord = sus.histogramRecord();
    TextStringBuilder output = new TextStringBuilder();
    output.appendln(String.format("%d instances of %s%n", suspectRecord.getObjectIds().length, suspectRecord.getLabel()));
    List<SuspectParent> commonParents = sus.commonParents();
    int nestingLevel = 0;
    if (commonParents.isEmpty()) {
      output.appendln("No common path of Class instances found");
      output.appendNewLine();
    } else {
      output.appendln("Common path of Class instances:");
      for (SuspectParent parent : commonParents) {
        output.appendln("%s%s (references %.2f%% of top objects searched)%n", StringUtils.repeat(Messages.NESTED_PATH_INDENTATION, nestingLevel++), parent.object().getDisplayName(), parent.referenceRatio() * 100);
      }
    }
    List<IObject> objects = sus.classObjects();
    int rowCount = Math.min(objects.size(), NUM_SUSPECT_CLASS_INSTANCES_TO_REPORT);
    final String tableHeader = String.format("Top %d Class Instances", rowCount);
    output.appendln(tablePrinter.printObjectsTableWithHeapAndPercent(objects.subList(0, rowCount), tableHeader));
    output.appendln(printKeyResultsOfObjects(objects.subList(0, rowCount), tableHeader));
    return output.toString();
  }

  public String printSingleSuspectObjectInfo(IObject targetObject, boolean printStrings) {
    TextStringBuilder output = new TextStringBuilder();
    output.appendln(PrintUtils.printHeader("Object: " + targetObject.getDisplayName()));
    output.appendln(heapSizer.printObjectHeapUsage(targetObject));
    String accumulationPointInfo = leakSuspectsFinder.printAccumulationPointInfo(targetObject);
    if (accumulationPointInfo != null) {
      output.appendln(accumulationPointInfo);
    }
    output.appendNewLine();
    KnownObject knownObject = knownObjectProvider.getKnownObject(targetObject);
    output.appendln(knownObject.printInfo());
    if (printStrings) {
      output.append(ObjectUtils.printObjectStrings(targetObject));
    }
    return output.toString();
  }

  public String printKeyResultsOfObjects(List<? extends IObject> objects, String titleOfObjects) {
    TextStringBuilder output = new TextStringBuilder();
    KeyResultBuilder objectKeyResultBuilder = new KeyResultBuilder();
    for (IObject object : objects) {
      KnownObject knownObject = knownObjectProvider.getKnownObject(object);
      Map<String, Set<String>> kOkR = knownObject.getKeyResults();
      objectKeyResultBuilder.addKeyResults(kOkR);
    }
    String keyResults = objectKeyResultBuilder.printKeyResults("Matching info among " + titleOfObjects);
    output.append(keyResults);
    return output.toString();
  }
}
