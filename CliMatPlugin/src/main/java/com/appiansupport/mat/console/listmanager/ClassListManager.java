package com.appiansupport.mat.console.listmanager;

import com.appiansupport.mat.console.CommandExecutor;
import com.appiansupport.mat.console.command.ConsoleCommand;
import com.appiansupport.mat.console.command.object.ListObjectCommand;
import com.appiansupport.mat.console.command.sort.SortOption;
import com.appiansupport.mat.console.state.ConsoleState;
import com.appiansupport.mat.console.state.ListState;
import com.appiansupport.mat.console.state.SingleClassState;
import com.appiansupport.mat.utils.PrintUtils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import org.eclipse.mat.snapshot.ClassHistogramRecord;
import org.eclipse.mat.snapshot.Histogram;
import org.eclipse.mat.snapshot.HistogramRecord;

public class ClassListManager extends ListManager<ClassHistogramRecord> {
  static final TableColumnPrinter<ClassHistogramRecord> labelPrinter = HistogramRecord::getLabel;
  static final TableColumnPrinter<ClassHistogramRecord> numObjectsPrinter = r -> String.format("%,d", r.getNumberOfObjects());
  static final TableColumnPrinter<ClassHistogramRecord> retainedHeapPrinter = r -> String.format("%,d", r.getRetainedHeapSize());
  static final TableColumnPrinter<ClassHistogramRecord> usedHeapPrinter = r -> String.format("%,d", r.getUsedHeapSize());
  static final Comparator<ClassHistogramRecord> COMPARE_LABEL_ASCENDING = Comparator.comparing(ClassHistogramRecord::getLabel);
  static final Comparator<ClassHistogramRecord> COMPARE_LABEL_DESCENDING = Comparator.comparing(ClassHistogramRecord::getLabel).reversed();
  static final Comparator<ClassHistogramRecord> COMPARE_NUMBER_OF_OBJECTS = Comparator.comparing(ClassHistogramRecord::getNumberOfObjects).reversed();
  static final Comparator<ClassHistogramRecord> COMPARE_USED_HEAP = Comparator.comparing(ClassHistogramRecord::getUsedHeapSize).reversed();
  static final Comparator<ClassHistogramRecord> COMPARE_RETAINED_HEAP = Comparator.comparing(ClassHistogramRecord::getRetainedHeapSize).reversed();
  static final List<SortOption<ClassHistogramRecord>> sortOptions = new ArrayList<>(Arrays.asList(
      new SortOption<>(SortOption.SORT_ALPHABETICALLY_ASCENDING, COMPARE_LABEL_ASCENDING),
      new SortOption<>(SortOption.SORT_ALPHABETICALLY_DESCENDING, COMPARE_LABEL_DESCENDING),
      new SortOption<>(SortOption.SORT_BY_NUM_OBJECTS, COMPARE_NUMBER_OF_OBJECTS),
      new SortOption<>(SortOption.SORT_BY_SHALLOW_HEAP, COMPARE_USED_HEAP),
      new SortOption<>(SortOption.SORT_BY_RETAINED_HEAP, COMPARE_RETAINED_HEAP)
  ));

  public ClassListManager(CommandExecutor executor, List<ClassHistogramRecord> records) {
    super(executor, records);
    //Calculate retained early to ensure we never print un-calculated values
    records.forEach(executor::calculateClassRecordRetained);
  }

  @Override protected Optional<List<ConsoleCommand>> getCustomOptions() {
    return Optional.empty();
  }

  public ConsoleState printBatch(int startIndex, int batchSize) {
    int finalIndex = Math.min(objects.size(), startIndex + batchSize);
    System.out.println();
    System.out.println(printTable(objects.subList(startIndex, finalIndex)));
    return new ListState<>(executor, this, startIndex, batchSize);
  }

  @Override public ListObjectCommand chooseItem(int index) {
    ClassHistogramRecord record = objects.get(index);
    SingleClassState targetState = new SingleClassState(executor, record);
    return new ListObjectCommand(executor, targetState, record.getLabel());
  }

  @Override public ConsoleState search(String searchString) {
    Optional<List<ClassHistogramRecord>> matchingObjects = searchByName(searchString);
    if (matchingObjects.isPresent()) {
      List<ClassHistogramRecord> results = matchingObjects.get();
      results.sort(Histogram.COMPARATOR_FOR_RETAINEDHEAPSIZE.reversed());
      ClassListManager newListManager = new ClassListManager(executor, results);
      return newListManager.printBatch(0);
    } else {
      return null;
    }
  }

  @Override public String getSearchTarget(ClassHistogramRecord row) {
    return row.getLabel();
  }

  @Override public Optional<List<SortOption<ClassHistogramRecord>>> getSortOptions() {
    return Optional.of(sortOptions);
  }

  public String printTable(List<? extends ClassHistogramRecord> recordList) {
    //Safe unchecked assignment, as values are all internal & final. Unavoidable as generic Arrays are prohibited.
    final TableColumnPrinter<ClassHistogramRecord>[] printers = new TableColumnPrinter[]{ labelPrinter, numObjectsPrinter, usedHeapPrinter, retainedHeapPrinter };
    String[] headers = new String[] { "Class Name", "# Objects", "Shallow Heap", "Retained Heap" };
    return PrintUtils.printTableFromList(recordList, headers, printers);
  }
}
