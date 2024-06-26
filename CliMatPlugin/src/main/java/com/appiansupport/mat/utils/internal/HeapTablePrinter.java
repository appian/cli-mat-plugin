package com.appiansupport.mat.utils.internal;

import com.appiansupport.mat.console.listmanager.TableColumnPrinter;
import com.appiansupport.mat.utils.PrintUtils;
import com.appiansupport.mat.utils.internal.HeapSizer;
import org.eclipse.mat.snapshot.ClassHistogramRecord;
import org.eclipse.mat.snapshot.HistogramRecord;
import org.eclipse.mat.snapshot.model.IObject;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class HeapTablePrinter extends PrintUtils {
  private final HeapSizer heapSizer;

  public HeapTablePrinter(HeapSizer heapSizer) {
    this.heapSizer = heapSizer;
  }

  public String printOverallHeapTable() {
    final long GB_BYTES = 1024 * 1024 * 1024;
    final long totalHeapUsage = heapSizer.getUsedHeapBytes();
    String usedHeap = String.format("%.2fGB", ((double) totalHeapUsage / (double) GB_BYTES));
    Optional<Long> heapAllocationB = heapSizer.getMaxHeapBytes();
    if (heapAllocationB.isEmpty()) {
      return PrintUtils.printNote("Used Heap: " + usedHeap);
    } else {
      usedHeap += String.format(" (%.1f%%)", ( (double) totalHeapUsage / (double) heapAllocationB.get() * 100));
      String heapAllocation = String.format("%.2fGB", ( (double) heapAllocationB.get() / (double) GB_BYTES));
      List<String> usedHeapRow = Collections.singletonList(usedHeap);
      List<String> totalHeapRow = Collections.singletonList(heapAllocation);
      List<List<String>> heapUsageData = Arrays.asList(usedHeapRow, totalHeapRow);
      return printTableFromLists(heapUsageData, new String[]{"Used Heap", "Max Heap (-Xmx)"}, null);
    }
  }

  public String printObjectsTableWithHeapAndPercent(List<? extends IObject> records, String borderHeader) {
    final TableColumnPrinter<IObject> objectDisplayNamePrinter = IObject::getDisplayName;
    final TableColumnPrinter<IObject> objectRetainedHeapPrinter = t -> String.format("%,d", t.getRetainedHeapSize());
    final TableColumnPrinter<IObject> objectHeapPercentPrinter = t -> heapSizer.printHeapUsedPercent(t.getRetainedHeapSize());
    final TableColumnPrinter<IObject>[] printers =  new TableColumnPrinter[] { objectDisplayNamePrinter, objectRetainedHeapPrinter, objectHeapPercentPrinter };
    final String[] columnHeaders = new String[] { "Name", "Retained Heap", "% of total" };
    return printTableFromList(records, columnHeaders, printers, borderHeader);
  }

  public String printClassTableWithHeapAndPercent(List<? extends ClassHistogramRecord> records,String borderHeader) {
    final TableColumnPrinter<ClassHistogramRecord> labelPrinter = HistogramRecord::getLabel;
    final TableColumnPrinter<ClassHistogramRecord> numObjectsPrinter = r -> String.format("%,d", r.getNumberOfObjects());
    final TableColumnPrinter<ClassHistogramRecord> retainedHeapPrinter = r -> String.format("%,d", r.getRetainedHeapSize());
    final TableColumnPrinter<ClassHistogramRecord> heapPercentPrinter = r -> heapSizer.printHeapUsedPercent(r.getRetainedHeapSize());
    final TableColumnPrinter<ClassHistogramRecord>[] printers =  new TableColumnPrinter[] { labelPrinter, numObjectsPrinter, retainedHeapPrinter, heapPercentPrinter };
    final String[] columnHeaders = new String[] { "Class Name", "# Objects", "Retained Heap", "% of total" };
    return printTableFromList(records, columnHeaders, printers, borderHeader);
  }
}
