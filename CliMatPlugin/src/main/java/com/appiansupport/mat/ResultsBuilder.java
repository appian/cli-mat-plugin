package com.appiansupport.mat;

import com.appiansupport.mat.suspects.KeyResultBuilder;
import com.appiansupport.mat.knownobjects.KnownObject;
import com.appiansupport.mat.knownobjects.KnownObjectProvider;
import com.appiansupport.mat.utils.ThreadFinder;
import com.appiansupport.mat.utils.internal.ThreadPrinter;
import com.appiansupport.mat.resolvers.KnownObjectResolver;
import com.appiansupport.mat.suspects.LeakSuspectPrinter;
import com.appiansupport.mat.suspects.LeakSuspectsFinder;
import com.appiansupport.mat.suspects.SuspectClassRecord;
import com.appiansupport.mat.utils.internal.HeapSizer;
import com.appiansupport.mat.utils.internal.HeapTablePrinter;
import com.appiansupport.mat.utils.PrintUtils;
import org.apache.commons.text.TextStringBuilder;
import org.eclipse.mat.snapshot.model.IObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ResultsBuilder {
  private final HeapSizer heapSizer;
  private final KnownObjectProvider knownObjectProvider;
  private final LeakSuspectsFinder leakSuspectsFinder;
  private final LeakSuspectPrinter leakSuspectPrinter;
  private final HeapTablePrinter tablePrinter;
  private final com.appiansupport.mat.utils.ThreadFinder threadFinder;
  private final ThreadPrinter threadPrinter;
  private final List<KnownObjectResolver> topObjectResolvers;
  final String THREAD_COUNTS_HEADER = "All Threads";


  ResultsBuilder(HeapSizer heapSizer, HeapTablePrinter tablePrinter, KnownObjectProvider knownObjectProvider,
                 LeakSuspectsFinder suspectFinder, ThreadFinder threadFinder, ThreadPrinter threadPrinter,
                 LeakSuspectPrinter leakSuspectPrinter, List<KnownObjectResolver> topObjectResolvers) {
    this.heapSizer = heapSizer;
    this.knownObjectProvider = knownObjectProvider;
    this.leakSuspectsFinder = suspectFinder;
    this.leakSuspectPrinter = leakSuspectPrinter;
    this.tablePrinter = tablePrinter;
    this.threadFinder = threadFinder;
    this.threadPrinter = threadPrinter;
    this.topObjectResolvers = topObjectResolvers;
  }

  /**
   * @param doPrintSuspectStrings Whether to print all Outgoing Referenced Strings of all suspects (provides the most detail but may be verbose and/or excessive)
   * @return A String report including overall Heap usage, Leak Suspects, counts and details of the knownObjectResolver extensions with doBreakdownInDefaultReport=true
   */
  public String printFullReport(boolean doPrintSuspectStrings) {
    TextStringBuilder output = new TextStringBuilder();
    output.appendNewLine();
    output.appendln(tablePrinter.printOverallHeapTable());
    output.appendln(printTopKnownObjectsCount());
    output.append(threadPrinter.printThreadStatistics());
    output.append(printLeakSuspectsReport(doPrintSuspectStrings));
    output.append(printTopKnownObjects());
    return output.toString();
  }

  /**
   * @param printSuspectStrings Whether to print all Outgoing Referenced Strings of all suspects
   * @return A String Leak Suspects report akin to the Eclipse MAT report
   */
  public String printLeakSuspectsReport(boolean printSuspectStrings){
    return leakSuspectPrinter.printLeakSuspectsReport(printSuspectStrings);
  }


  /**
   * @param object The object to report details on
   * @param doPrintStrings Whether to print all Outgoing Referenced Strings
   * @return A String report of all details of the inputted object
   */
  public String printSingleObjectInfo(IObject object,boolean doPrintStrings){
    return leakSuspectPrinter.printSingleSuspectObjectInfo(object, doPrintStrings);
  }

  /**
   * @param objects A list of all objects to report details on
   * @return A String report of all details of all inputted objects, including their matching Key Results
   */
  public String printManyObjectsInfo(List<IObject> objects) {
    TextStringBuilder output = new TextStringBuilder();
    output.appendln(tablePrinter.printObjectsTableWithHeapAndPercent(objects, null));
    output.appendln(leakSuspectPrinter.printKeyResultsOfObjects(objects, "Objects"));
    objects.forEach(o -> output.appendln(printSingleObjectInfo(o, true)));
    return output.toString();
  }

  /**
   * @return A List of all objects designated os suspects
   */
  public List<IObject> getSuspectObjects() {
    return leakSuspectsFinder.getSuspectObjects();
  }

  /**
   * @return An array af all classes designated as a suspect
   */
  public SuspectClassRecord[] getSuspectClasses() {
    return leakSuspectsFinder.getSuspectClasses();
  }

  /**
   * @return A String report of details of every resolved KnownObjectResolver instance from topObjectResolvers
   */
  public String printTopKnownObjects() {
    TextStringBuilder topKnownObjectsReport = new TextStringBuilder();
    for (KnownObjectResolver resolver : topObjectResolvers) {
      topKnownObjectsReport.append(printAllDetailsOfTopKnownObject(resolver));
    }
    return topKnownObjectsReport.toString();
  }

  /**
   * @param resolver The KnownObjectResolver to print all details of
   * @return A String report of details of every instance of the resolved KnownObject
   */
  public String printAllDetailsOfTopKnownObject(KnownObjectResolver resolver) {
    TextStringBuilder output = new TextStringBuilder();
    TextStringBuilder individualObjectOutput = new TextStringBuilder();
    List<KnownObject> knownObjects = knownObjectProvider.getKnownObjectsFromResolver(resolver);
    if (!knownObjects.isEmpty()) {
      String sectionName = resolver.getDisplayName().trim();
      KeyResultBuilder keyResultBuilder = new KeyResultBuilder();
      for (KnownObject kO : knownObjects) {
        individualObjectOutput.append(printKnownObjectInfoWithHeaders(kO));
        keyResultBuilder.addKeyResults(kO.getKeyResults());
      }
      String keyResultOutput = keyResultBuilder.printKeyResults(String.format("Matching info among %ss", sectionName));
      if (!(keyResultOutput.isEmpty() && individualObjectOutput.isEmpty())) {
        output.appendln(PrintUtils.printHeader(String.format("%s Info (%d found)", sectionName, knownObjects.size())));
      }
      output.appendln(keyResultOutput);
      output.append(individualObjectOutput.toString());
    }
    return output.toString();
  }

  String printKnownObjectInfoWithHeaders(KnownObject knownObject) {
    String data = knownObject.printInfo();
    if (data != null && !data.isEmpty()) {
      TextStringBuilder output = new TextStringBuilder();
      output.appendln(PrintUtils.printSubHeader(knownObject.getObject().getDisplayName()));
      output.appendln(heapSizer.printObjectHeapUsage(knownObject.getObject()));
      output.appendln(data);
      return output.toString();
    } else {
      return null;
    }
  }

  /**
   * @return A String report of the count of top KnownObjects with doBreakdownInDefaultReport=true
   */
  public String printTopKnownObjectsCount(){
    TextStringBuilder output = new TextStringBuilder();
    List<String> rowNames = new ArrayList<>(topObjectResolvers.size() + 2);
    List<String> rowCounts = new ArrayList<>(topObjectResolvers.size() + 2);
    Map<String,Integer> objectNameToCount = new HashMap<>(topObjectResolvers.size() + 2, 1.0f);
    objectNameToCount.put(THREAD_COUNTS_HEADER,threadFinder.getAllThreads().size());
    for (KnownObjectResolver resolver : topObjectResolvers) {
      objectNameToCount.put(pluralize(resolver.getDisplayName()),knownObjectProvider.getKnownObjectsFromResolver(resolver).size());
    }
    //Order the table descending by count
    objectNameToCount.entrySet().stream().sorted(Map.Entry.comparingByValue(Comparator.reverseOrder())).forEachOrdered(e -> {
      rowNames.add(e.getKey());
      rowCounts.add(String.format("%,d", e.getValue()));
    });
    List<List<String>> threadTableColumns = Arrays.asList(rowNames, rowCounts);
    output.append(PrintUtils.printTableFromLists(threadTableColumns, new String[]{"Name", "Count"}, "Top Object Counts"));
    return output.toString();
  }

  private static String pluralize(String text) {
    return text.endsWith("s") ? text : text + 's';
  }
}