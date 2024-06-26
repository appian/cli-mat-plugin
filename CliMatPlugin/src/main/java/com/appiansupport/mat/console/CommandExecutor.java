package com.appiansupport.mat.console;

import com.appiansupport.mat.ResolvedReference;
import com.appiansupport.mat.ResultsBuilder;
import com.appiansupport.mat.utils.ThreadFinder;
import com.appiansupport.mat.console.command.ConsoleCommand;
import com.appiansupport.mat.console.command.SuggestedCommand;
import com.appiansupport.mat.console.command.search.SearchListParentCommand;
import com.appiansupport.mat.console.listmanager.ClassListManager;
import com.appiansupport.mat.console.listmanager.ListManager;
import com.appiansupport.mat.console.listmanager.ObjectListManager;
import com.appiansupport.mat.console.listmanager.ResolvedReferenceListManager;
import com.appiansupport.mat.console.state.BatchStringsState;
import com.appiansupport.mat.console.state.ConsoleState;
import com.appiansupport.mat.console.state.NoHistoryState;
import com.appiansupport.mat.console.state.NullState;
import com.appiansupport.mat.console.state.PrimitiveArrayState;
import com.appiansupport.mat.console.state.SingleClassState;
import com.appiansupport.mat.console.state.SingleObjectState;
import com.appiansupport.mat.constants.CliConstants;
import com.appiansupport.mat.knownobjects.KnownObjectProvider;
import com.appiansupport.mat.utils.internal.ThreadPrinter;
import com.appiansupport.mat.suspects.SuspectClassRecord;
import com.appiansupport.mat.utils.*;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import com.appiansupport.mat.utils.internal.GcRootsHandler;
import com.appiansupport.mat.utils.internal.HeapSizer;
import com.appiansupport.mat.utils.internal.HeapTablePrinter;
import com.appiansupport.mat.utils.internal.ObjectFetcher;
import org.eclipse.mat.SnapshotException;
import org.eclipse.mat.snapshot.ClassHistogramRecord;
import org.eclipse.mat.snapshot.Histogram;
import org.eclipse.mat.snapshot.IOQLQuery;
import org.eclipse.mat.snapshot.ISnapshot;
import org.eclipse.mat.snapshot.model.IObject;
import org.eclipse.mat.snapshot.model.IPrimitiveArray;
import org.eclipse.mat.snapshot.model.NamedReference;
import org.eclipse.mat.snapshot.model.ObjectReference;
import org.eclipse.mat.util.IProgressListener;

public class CommandExecutor {
  final Scanner scanner;
  private final ISnapshot snapshot;
  private final IProgressListener listener;
  private final HeapSizer heapSizer;
  private final HeapTablePrinter heapTablePrinter;
  private final KnownObjectProvider knownObjectProvider;
  private final ThreadPrinter threadPrinter;
  private final ObjectFetcher objectFetcher;
  private final ResultsBuilder resultsBuilder;
  private final ThreadFinder threadFinder;

  private String leakSuspectsReport;
  private String threadStatistics;

  public CommandExecutor(ISnapshot snapshot, IProgressListener listener, Scanner scanner, KnownObjectProvider knownObjectProvider,
    ObjectFetcher objectFetcher, ResultsBuilder resultsBuilder, ThreadFinder threadFinder, ThreadPrinter threadPrinter, HeapSizer heapSizer) {
    this.snapshot = snapshot;
    this.listener = listener;
    this.scanner = scanner;
    this.knownObjectProvider = knownObjectProvider;
    this.objectFetcher = objectFetcher;
    this.resultsBuilder = resultsBuilder;
    this.threadFinder = threadFinder;
    this.threadPrinter = threadPrinter;
    this.heapSizer = heapSizer;
    heapTablePrinter = new HeapTablePrinter(heapSizer);
  }

  public ConsoleState chooseObjectPrompt() {
    int promptAttempt = 1;
    while (promptAttempt <= CliConstants.MAX_PROMPT_ATTEMPTS_BEFORE_REVERTING_STATE) {
      System.out.println("Enter hexadecimal object address:");
      String objectIdInput = scanner.nextLine().trim();
      try {
        IObject chosenObject = objectFetcher.getObjectFromHexAddress(objectIdInput);
        return new SingleObjectState(this, chosenObject);
      } catch (SnapshotException snapshotException) {
        System.err.println("Unable to identify object specified; please try again.");
        promptAttempt++;
      } catch (NumberFormatException numberFormatException) {
        System.err.println("Inputted value is not an address; Please try again.");
        promptAttempt++;
      }
    }
    return null;
  }

  public <T> ConsoleState matchNamePrompt(ListManager<T> listManager) {
    System.out.println("Enter name to match:");
    String matchInput = scanner.nextLine().trim();
    return listManager.search(matchInput);
  }

  public ConsoleState oqlPrompt(){
    System.out.println("Enter OQL Query:");
    String providedQuery = scanner.nextLine().trim();
    try {
      // Need to remake this logic https://git.eclipse.org/c/mat/org.eclipse.mat.git/tree/plugins/org.eclipse.mat.api/src/org/eclipse/mat/inspections/OQLQuery.java
      Object queryResult = OQLUtils.executeOql(providedQuery, snapshot, listener);
      if (queryResult == null) {
        System.out.println("No results");
      } else if (queryResult instanceof int[]){
        int[] queryResultAsObjectIds = (int[]) queryResult;
        System.out.println("(OQL beta: processed as int[])");
        List<IObject> resultObjects = new ArrayList<>(queryResultAsObjectIds.length);
        for (int id : queryResultAsObjectIds) {
          try {
            resultObjects.add(snapshot.getObject(id));
          } catch (SnapshotException snapshotException) {
              System.out.println("Unable to resolve object ID "+id);
          }
        }
        if(resultObjects.isEmpty()){
          System.out.println("Unable to resolve any objects returned");
          return null;
        } else {
          ObjectListManager objectListManager = new ObjectListManager(snapshot,this, heapTablePrinter, resultObjects);
          return objectListManager.printBatch(0,CliConstants.DEFAULT_RESULTS_BATCH_SIZE);
        }
      } else if (queryResult instanceof List<?>){
        List<?> result = (List<?>) queryResult;
        if (result.isEmpty()) {
          System.out.println("Query returned an empty list.");
          return null;
        }
        if (result.get(0) instanceof ObjectReference) {
          List<IObject> resultObjects = new ArrayList<>(result.size());
          for(Object o : result){
            try{
              resultObjects.add(((ObjectReference) o).getObject());
            } catch (ClassCastException c){
              listener.sendUserMessage(IProgressListener.Severity.ERROR,String.format("Could not cast result row %s to IObject",o.toString()),c);
            }
          }
          ObjectListManager objectListManager = new ObjectListManager(snapshot,this, heapTablePrinter,resultObjects);
          return objectListManager.printBatch(0,CliConstants.DEFAULT_RESULTS_BATCH_SIZE);
        } else if (result.get(0) instanceof IObject) {
          List<IObject> resultObjects = new ArrayList<>(result.size());
          for(Object o : result){
            try{
              resultObjects.add((IObject) o);
            } catch (ClassCastException c){
              listener.sendUserMessage(IProgressListener.Severity.ERROR,String.format("Could not cast result row %s to IObject",o.toString()),c);
            }
          }
          ObjectListManager objectListManager = new ObjectListManager(snapshot,this, heapTablePrinter, resultObjects);
          return objectListManager.printBatch(0,CliConstants.DEFAULT_RESULTS_BATCH_SIZE);
        } else {
          System.out.println("(OQL beta: processed as unknown List)");
          List<String> resultStrings = new ArrayList<>(result.size());
          result.forEach(o -> resultStrings.add(o.toString()));
          String[] resultArr = resultStrings.toArray(new String[0]);
          return printBatchStrings(resultArr,0);
        }
      } else if (queryResult instanceof IOQLQuery.Result){
        // What to do with these?
        System.out.println("(OQL beta: processed as IOQLQuery.Result)" + queryResult.toString());
      } else {
        System.out.println(queryResult);
      }
    } catch (SnapshotException snapshotException) {
      System.err.println("Unable to parse input into Query");
    }
    return null;
  }

  public ConsoleState printDefaultHistogram() {
    return getClassesFromObjectIds(null);
  }

  public ConsoleState printHistogram(List<ClassHistogramRecord> records, int startIndex) {
    return printHistogram(records, startIndex, CliConstants.DEFAULT_RESULTS_BATCH_SIZE);
  }

  public ConsoleState printHistogram(List<ClassHistogramRecord> records, int startIndex, int batchSize) {
    if (records == null) {
      return null;
    }
    ClassListManager classListManager = new ClassListManager(this, records);
    return classListManager.printBatch(startIndex, batchSize);
  }

  private ConsoleState getClassesFromObjectIds(int[] ids) {
    try {
      listener.beginTask("Fetching Histogram",3);
      listener.subTask("Querying records");
      Histogram classHistogram = ids == null ? snapshot.getHistogram(listener) : snapshot.getHistogram(ids, listener);
      List<ClassHistogramRecord> records = new ArrayList<>(classHistogram.getClassHistogramRecords());
      listener.worked(1);
      listener.subTask("Calculating retained Heap sizes");
      records.forEach(this::calculateClassRecordRetained);
      listener.worked(1);
      listener.subTask("Sorting");
      records.sort(ClassHistogramRecord.COMPARATOR_FOR_RETAINEDHEAPSIZE.reversed());
      listener.worked(1);
      return printHistogram(records, 0);
    } catch (SnapshotException snapshotException) {
      listener.sendUserMessage(IProgressListener.Severity.ERROR, "Unable to acquire Class Histogram", snapshotException);
      return null;
    }
  }

  public void calculateClassRecordRetained(ClassHistogramRecord rec) {
    heapSizer.calculateClassRetained(rec);
  }

  public ConsoleState getClassReferences(int objectId, boolean outgoing) {
    return getClassReferences(new int[] { objectId }, outgoing);
  }

  public ConsoleState getClassReferences(int[] objectIds, boolean outgoing) {
    try {
      int[] targetIds;
      if (outgoing) {
        targetIds = snapshot.getOutboundReferentIds(objectIds, listener);
      } else {
        targetIds = snapshot.getInboundRefererIds(objectIds, listener);
      }
      if (targetIds.length == 0) {
        System.out.println("No references found");
        return null;
      }
      return getClassesFromObjectIds(targetIds);
    } catch (SnapshotException snapshotException) {
      listener.sendUserMessage(IProgressListener.Severity.ERROR, String.format("Unable to acquire %s references", outgoing ? "outgoing" : "incoming"), snapshotException);
      return null;
    }
  }

  public ConsoleState getObjectReferences(IObject object, boolean outgoing) {
    return outgoing ? getOutgoingReferences(object) : getIncomingReferences(object);
  }

  /**
   * @param object The object to fetch references of.
   * @return a ConsoleState after printing a batch of results.
   * MAT API does not offer an easy way to maintain incoming NamedRefereces.
   * Therefore, unlike getOutgoingReferences(), this method uses an ObjectListManager.
   */
  public ConsoleState getIncomingReferences(IObject object) {
    ReferenceFinder referenceFinder = new ReferenceFinder(snapshot,listener);
    Set<IObject> references = referenceFinder.findIncomingReferences(object);
    ArrayList<IObject> referencesList = new ArrayList<>(references);
    referencesList.sort(ObjectUtils.compareIObjectByRetainedHeapDescending);
    ObjectListManager listManager = new ObjectListManager(snapshot, this, heapTablePrinter, referencesList);
    return listManager.printBatch(0);
  }

  /**
   * @param object The object to fetch references of.
   * @return a ConsoleState after printing a batch of results.
   *  This method uses ResolvedReferenceListManager to preserve the referenced field names, which are only held by NamedReferences.
   */
  public ConsoleState getOutgoingReferences(IObject object) {
    List<NamedReference> namedReferences = object.getOutboundReferences();
    List<ResolvedReference> resolvedReferences = new ArrayList<>();
    for (NamedReference ref : namedReferences) {
      try {
        IObject o = ref.getObject();
        resolvedReferences.add(new ResolvedReference(snapshot, o, ref));
      } catch (SnapshotException snapshotException) {
        listener.sendUserMessage(
            IProgressListener.Severity.ERROR,
            "Unable to acquire object from reference " + ref.toString(),
            snapshotException
        );
      }
    }
    ResolvedReferenceListManager refManager = new ResolvedReferenceListManager(snapshot, this, heapSizer, resolvedReferences);
    return refManager.printBatch(0);
  }

  /**
   * @param record the source ClassHistogramRecord
   * @return A ConsoleState determine by the ListManager implementation, to provide access to the objects after batch-displaying them.
   */
  public ConsoleState getObjectsFromClass(ClassHistogramRecord record) {
    List<IObject> objects = objectFetcher.getObjectsFromClassRecord(record);
    if (objects.isEmpty()) {
      System.out.println("Failed to find any objects of Class");
      return null;
    }
    objects.sort(ObjectUtils.compareIObjectByRetainedHeapDescending);
    ObjectListManager listManager = new ObjectListManager(snapshot, this, heapTablePrinter, objects);
    return listManager.printBatch(0);
  }

  /**
   * globalSearchByClass prompts the user for a case-sensitive search term
   * @return A ConsoleState determine by the ListManager implementation, to provide access to the objects after batch-displaying them.
   */
  public ConsoleState globalSearchByClass() {
    final ArrayList<IObject> matchingObjects = new ArrayList<>();
    System.out.println("Enter name to match (Regex):");
    String matchInput = scanner.nextLine().trim();
    try {
      final String wildCardRegex = ".*";
      final Pattern matchWithWildCard = Pattern.compile(wildCardRegex + matchInput + wildCardRegex);
      int[] matchingObjectIds = objectFetcher.getObjectIdsByClass(matchWithWildCard);
      Arrays.stream(matchingObjectIds).forEach(id -> {
        try {
          matchingObjects.add(snapshot.getObject(id));
        } catch (SnapshotException snapshotExceptionInner) {
          System.err.println("Unable to resolve object ID " + id);
        }
      });
    } catch (SnapshotException snapshotExceptionOuter) {
      System.err.println("An error occurred while searching objects: " + snapshotExceptionOuter.toString());
      return null;
    } catch (PatternSyntaxException patternSyntaxException){
      System.err.printf("Unable to convert search input to Regex:%n%s%n",patternSyntaxException.toString());
      return null;
    }
    if (matchingObjects.isEmpty()) {
      System.out.println("No matches found (search is case-sensitive)");
      return null;
    } else {
      ObjectListManager listManager = new ObjectListManager(snapshot, this, heapTablePrinter, matchingObjects);
      return listManager.printBatch(0);
    }
  }

  public ConsoleState getGcRoots(IObject object) {
    GcRootsHandler gcRootsHandler = new GcRootsHandler(snapshot, object);
    if (gcRootsHandler.getGcRoots().isPresent()) {
      System.out.println(gcRootsHandler.printGcRoots());
      System.out.println();
      List<IObject> gcRoots = gcRootsHandler.getGcRoots().get();
      if (gcRoots.size() == 1) {
        //should always be true, but just in case
        if (gcRoots.get(0).getObjectId() == object.getObjectId()) {
          return null;
        }
      }
      List<ConsoleCommand> customOptionsForGcRoots = new ArrayList<>();
      ObjectListManager listManager = new ObjectListManager(snapshot, this, heapTablePrinter, gcRoots);
      customOptionsForGcRoots.add(new SearchListParentCommand<>(this, listManager, 0));
      return new NoHistoryState(this, customOptionsForGcRoots);
    } else {
      System.out.println("No paths found");
      return null;
    }
  }

  public ConsoleState getSuspectInformation(IObject object) {
    System.out.println(resultsBuilder.printSingleObjectInfo(object, false));
    NullState returnState = new NullState(this);
    Set<IObject> suggestedObjects = knownObjectProvider.getKnownObject(object).getSuggestedObjects();
    if (suggestedObjects != null) {
      suggestedObjects.remove(object);
      suggestedObjects.forEach(o -> returnState.addSuggestedCommand(new SuggestedCommand(this, new SingleObjectState(this, o), o.getDisplayName())));
    }
    return returnState;
  }

  public ConsoleState printCustomObjectInfo() {
    System.out.println(resultsBuilder.printTopKnownObjects());
    return null;
  }

  public ConsoleState searchStringsPrompt(String[] strings) {
    System.out.println("Enter String to search");
    String searchString = scanner.nextLine().trim();
    ArrayList<String> matches = new ArrayList<>();
    for (String string : strings) {
      if (string.contains(searchString)) {
        matches.add(string);
      }
    }
    if (matches.isEmpty()) {
      System.out.println("No matches :(");
      return null;
    }
    return printBatchStrings(matches.toArray(new String[0]), 0);
  }

  public ConsoleState printStrings(IObject object) {
    System.out.println(ObjectUtils.printObjectStrings(object));
    return null;
  }

  public ConsoleState printBatchStrings(String[] strings, int startIndex) {
    return printBatchStrings(strings, startIndex, CliConstants.DEFAULT_RESULTS_BATCH_SIZE);
  }

  public ConsoleState printBatchStrings(String[] strings, int startIndex, int batchSize) {
    int finalIndex = Math.min(strings.length, startIndex + batchSize);
    for (int i = startIndex; i < finalIndex; i++) {
      if (i < strings.length) {
        System.out.println(strings[i]);
      }
    }
    System.out.println();
    return new BatchStringsState(this, strings, finalIndex);
  }

  public ConsoleState printPrimitiveArrayBatch(IPrimitiveArray arr,int startIndex, int batchSize){
    final Class<?> arrType = arr.getComponentType();
    final int arrLen = arr.getLength();
    final int finalIndex = Math.min(arrLen, startIndex + batchSize);
    final int printLen = finalIndex - startIndex;
    if(finalIndex>startIndex) {
      if (arrType == boolean.class) {
        boolean[] subArr = (boolean[]) arr.getValueArray(startIndex,printLen);
        System.out.println(Arrays.toString(subArr));
      } else if (arrType == byte.class) {
        byte[] subArr = (byte[]) arr.getValueArray(startIndex,printLen);
        String charSet = PrintUtils.DEFAULT_CHARSET;
        try {
          System.out.println(new String(subArr, charSet));
        } catch (UnsupportedEncodingException e) {
          System.err.printf("Unable to print byte[] in %s encoding due to %s%n",charSet,e.toString());
        }
      } else if (arrType == char.class) {
        char[] subArr = (char[]) arr.getValueArray(startIndex,printLen);
        System.out.println(new String(subArr));
      } else if (arrType == double.class) {
        double[] subArr = (double[]) arr.getValueArray(startIndex,printLen);
        System.out.println(Arrays.toString(subArr));
      } else if (arrType == float.class) {
        float[] subArr = (float[]) arr.getValueArray(startIndex,printLen);
        System.out.println(Arrays.toString(subArr));
      } else if (arrType == int.class) {
        int[] subArr = (int[]) arr.getValueArray(startIndex,printLen);
        System.out.println(Arrays.toString(subArr));
      } else if (arrType == long.class) {
        long[] subArr = (long[]) arr.getValueArray(startIndex,printLen);
        System.out.println(Arrays.toString(subArr));
      } else {
        System.err.println("Could not resolve primitive type of "+arr.getDisplayName());
        return null;
      }
    }
    System.out.println();
    return finalIndex == arrLen ? null :  new PrimitiveArrayState(this,arr,finalIndex);
  }

  public ConsoleState dominatorTreeCommand() {
    try {
      int[] domIds = snapshot.getImmediateDominatedIds(-1);
      ArrayList<IObject> topDominators = new ArrayList<>(CliConstants.DOMINATOR_TREE_MAX_SIZE);
      for (int i = 0; i < CliConstants.DOMINATOR_TREE_MAX_SIZE; i++) {
        try {
          IObject currObject = snapshot.getObject(domIds[i]);
          topDominators.add(currObject);
        } catch (SnapshotException snapshotExceptionInner) {
          System.err.printf("Error resolving object %d: %s", domIds[i], snapshotExceptionInner.toString());
        }
      }
      ObjectListManager listManager = new ObjectListManager(snapshot, this, heapTablePrinter, topDominators);
      return listManager.printBatch(0);
    } catch (SnapshotException snapshotExceptionOuter) {
      System.err.println("Unable to open Dominator tree: " + snapshotExceptionOuter.toString());
      return null;
    }
  }

  public ConsoleState printThreadStatistics() {
    if (threadStatistics == null) {
      threadStatistics = threadPrinter.printThreadStatistics();
    }
    System.out.println(threadStatistics);
    NullState returnState = new NullState(this);
    ArrayList<SuggestedCommand> dynamicThreadOptions = getSuggestedOptionsFromThreadStatistics();
    if (dynamicThreadOptions != null) {
      returnState.addSuggestedCommands(dynamicThreadOptions);
    }
    return returnState;
  }

  public ConsoleState leakSuspectsCommand() {
    if (leakSuspectsReport == null) {
      leakSuspectsReport = resultsBuilder.printLeakSuspectsReport(false);
    }
    System.out.println(leakSuspectsReport);
    NullState returnState = new NullState(this);
    ArrayList<SuggestedCommand> dynamicSuspectOptions = getSuggestedOptionsFromLeakSuspects();
    if (dynamicSuspectOptions != null) {
      returnState.addSuggestedCommands(dynamicSuspectOptions);
    }
    return returnState;
  }

  public ConsoleState printEverything() {
    System.out.println(resultsBuilder.printFullReport(true));
    NullState returnState = new NullState(this);
    ArrayList<SuggestedCommand> dynamicSuspectOptions = getSuggestedOptionsFromLeakSuspects();
    ArrayList<SuggestedCommand> dynamicThreadOptions = getSuggestedOptionsFromThreadStatistics();
    if (dynamicSuspectOptions != null) {
      returnState.addSuggestedCommands(dynamicSuspectOptions);
    }
    if (dynamicThreadOptions != null) {
      returnState.addSuggestedCommands(dynamicThreadOptions);
    }
    return returnState;
  }

  private ArrayList<SuggestedCommand> getSuggestedOptionsFromThreadStatistics() {
    Optional<IObject> oomThread = threadFinder.getOomThread();
    if (oomThread.isPresent()) {
        ArrayList<SuggestedCommand> dynamicThreadOptions = new ArrayList<>();
        ConsoleState oomThreadState = new SingleObjectState(this, oomThread.get());
        SuggestedCommand oomSuggestedCommand = new SuggestedCommand(this, oomThreadState, "OutOfMemoryError Thread: " + oomThread.get().getDisplayName());
        dynamicThreadOptions.add(oomSuggestedCommand);
        return dynamicThreadOptions;
    } else {
      return null;
    }
  }

  private ArrayList<SuggestedCommand> getSuggestedOptionsFromLeakSuspects() {
    ArrayList<IObject> suspectObjects = (ArrayList<IObject>) resultsBuilder.getSuspectObjects();
    SuspectClassRecord[] suspectClasses = resultsBuilder.getSuspectClasses();
    if (suspectObjects == null && suspectClasses == null) {
      return null;
    }
    ArrayList<SuggestedCommand> dynamicSuspectOptions = new ArrayList<>();
    if (suspectObjects != null) {
      for (int i = 0; i < suspectObjects.size(); i++) {
        IObject suspectObject = suspectObjects.get(i);
        dynamicSuspectOptions.add(new SuggestedCommand(this, new SingleObjectState(this, suspectObject), String.format("Suspect object %s: %s", i + 1, suspectObject.getDisplayName())));
      }
    }
    if (suspectClasses != null) {
      for (int i = 0; i < suspectClasses.length; i++) {
        ClassHistogramRecord classHistogram = suspectClasses[i].histogramRecord();
        dynamicSuspectOptions.add(new SuggestedCommand(this, new SingleClassState(this, classHistogram), String.format("Suspect class %s: %s", i + 1, classHistogram.getLabel())));
      }
    }
    return dynamicSuspectOptions;
  }

  public ConsoleState printStackTrace(Integer threadId) {
    String stackTrace = threadFinder.getStackTrace(threadId);
    if (stackTrace != null) {
      String[] stackTraceByLine = stackTrace.split("\n");
      return printBatchStrings(stackTraceByLine, 0);
    } else {
      System.out.println("Either this isn't a Thread, or I couldn't find the stack trace :(");
      return null;
    }
  }

  public boolean isThread(IObject object) {
    return isThread(object.getObjectId());
  }

  public boolean isThread(int objectId) {
    return threadFinder.isThread(objectId);
  }
}
