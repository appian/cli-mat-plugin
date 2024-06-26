package com.appiansupport.mat.utils.internal;

import com.appiansupport.mat.constants.Messages;
import com.appiansupport.mat.utils.PrintUtils;
import com.appiansupport.mat.utils.ThreadFinder;
import org.apache.commons.text.TextStringBuilder;
import org.eclipse.mat.snapshot.model.IObject;

import java.util.List;
import java.util.Optional;

public class ThreadPrinter {
  private final ThreadFinder threadFinder;
  private final HeapSizer heapSizer;
  private static final int TOP_THREADS_THRESHOLD_MB = 1;
  private static final int NUM_TOP_THREADS_TO_REPORT = 10;

  public ThreadPrinter(ThreadFinder threadFinder, HeapSizer heapSizer) {
    this.threadFinder = threadFinder;
    this.heapSizer = heapSizer;
  }

  /**
   * @param tablePrinter A HeapTablePrinter to control the table display
   * @param numThreads How many top threads to print
   * @param topThreadsThresholdMb The minimum Retained Heap Mb for a thread to be displayed
   * @return String table of top threads
   */
  //todo: make numThreads configurable at CLI or property
  public String printTopThreadsTable(HeapTablePrinter tablePrinter, int numThreads, int topThreadsThresholdMb) {
    final int bToMb = 1024 * 1024;
    final int topThreadsThresholdB = topThreadsThresholdMb * bToMb;

    TextStringBuilder output = new TextStringBuilder();
    List<IObject> allThreadsSorted = threadFinder.sortThreadsByHeapUsage();
    int numThreadsToDisplay = (int) allThreadsSorted.stream().limit(numThreads).filter(t -> t.getRetainedHeapSize() >= topThreadsThresholdB).count();

    if (numThreadsToDisplay < 1) {
      output.appendln(PrintUtils.printNote(String.format("No Threads >%dMB", topThreadsThresholdMb)));
    } else {
      String tableHeader = String.format("Top Threads >%dMB", topThreadsThresholdMb);
      output.append(tablePrinter.printObjectsTableWithHeapAndPercent(allThreadsSorted.subList(0, numThreadsToDisplay), tableHeader));
    }
    return output.toString();
  }

  /**
   * @return A String report of the thread which threw OutOfMemoryError, empty if there isn't one.
   */
  public String printOomThreadInfo() {
    TextStringBuilder output = new TextStringBuilder();
    Optional<IObject> oomThread = threadFinder.getOomThread();
    if (oomThread.isPresent()) {
      output.appendln(PrintUtils.printHeader("OutOfMemoryError found"));
      output.appendln("%s - %s", oomThread.get().getDisplayName(), heapSizer.printObjectHeapUsage(oomThread.get()));
      output.appendln(Messages.OutOfMemory_ThreadTip);
      if (shouldPrintOomExplosionTip()) {
        output.appendln(Messages.OutOfMemoryThread_LowHeapTip);
      }
      output.appendNewLine();
    }
    return output.toString();
  }

  boolean shouldPrintOomExplosionTip() {
    final float OOM_EXPLOSION_THRESHOLD = .5f;
    Optional<Double> heapUsedDecimal = heapSizer.getUsedHeapDecimal();
    return heapUsedDecimal.filter(heap -> heap < OOM_EXPLOSION_THRESHOLD).isPresent();
  }

  /**
   * @return A String report on the threads present in the Heap snapshot
   */
  public String printThreadStatistics() {
    TextStringBuilder output = new TextStringBuilder();
    HeapTablePrinter tablePrinter = new HeapTablePrinter(heapSizer);
    output.appendln(printTopThreadsTable(tablePrinter, NUM_TOP_THREADS_TO_REPORT, TOP_THREADS_THRESHOLD_MB));
    output.append(printOomThreadInfo());
    return output.toString();
  }
}
