package com.appiansupport.mat.console.listmanager;

import com.appiansupport.mat.ResolvedReference;
import com.appiansupport.mat.console.CommandExecutor;
import com.appiansupport.mat.console.command.ConsoleCommand;
import com.appiansupport.mat.console.command.object.ListObjectCommand;
import com.appiansupport.mat.console.command.sort.SortOption;
import com.appiansupport.mat.console.state.ConsoleState;
import com.appiansupport.mat.console.state.ListState;
import com.appiansupport.mat.console.state.SingleObjectState;
import com.appiansupport.mat.utils.internal.HeapSizer;
import com.appiansupport.mat.utils.PrintUtils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.eclipse.mat.snapshot.ISnapshot;
import org.eclipse.mat.snapshot.model.IObject;

/** Handles ResolvedReferences, primarily for the purpose of printing field names.
 * ResolvedReference is used to avoid the complications with IObject resolution of NamedReference.
 */
public class ResolvedReferenceListManager extends ListManager<ResolvedReference> {
  private final ISnapshot snapshot;
  private final HeapSizer heapSizer;
  static final TableColumnPrinter<ResolvedReference> namePrinter = ResolvedReference::getFullName;
  static final List<SortOption<ResolvedReference>> sortOptions = new ArrayList<>(Arrays.asList(
      new SortOption<>(SortOption.SORT_BY_RETAINED_HEAP, ResolvedReference.COMPARE_BY_RETAINED_HEAP),
      new SortOption<>(SortOption.SORT_ALPHABETICALLY_ASCENDING, ResolvedReference.COMPARE_BY_NAME),
      new SortOption<>(SortOption.SORT_ALPHABETICALLY_DESCENDING, ResolvedReference.COMPARE_BY_NAME.reversed())
  ));

  public ResolvedReferenceListManager(ISnapshot snapshot, CommandExecutor executor, HeapSizer heapSizer, List<ResolvedReference> objects) {
    super(executor, objects);
    this.snapshot = snapshot;
    this.heapSizer = heapSizer;
  }


  @Override protected Optional<List<ConsoleCommand>> getCustomOptions() {
    return Optional.empty();
  }

  @Override public ConsoleState printBatch(int startIndex, int batchSize) {
    int finalIndex = Math.min(objects.size(), startIndex + batchSize);
    final TableColumnPrinter<ResolvedReference> heapPrinter = nr -> heapSizer.printObjectHeapUsageNoWords(nr.getObject());
    String[] headers = new String[] { "(Field Name) Object", "Retained bytes" };
    //Safe unchecked assignment, as values are all internal & final. Unavoidable as generic Arrays are prohibited.
    final TableColumnPrinter<ResolvedReference>[] printers = new TableColumnPrinter[] { namePrinter, heapPrinter };
    List<ResolvedReference> subList = objects.subList(startIndex, finalIndex);
    System.out.println(PrintUtils.printTableFromList(subList, headers, printers));
    return new ListState<>(executor, this, startIndex, batchSize);
  }

  @Override public ListObjectCommand chooseItem(int index) {
    ResolvedReference chosenRef = objects.get(index);
    IObject targetObject = chosenRef.getObject();
    SingleObjectState targetState = new SingleObjectState(executor, targetObject);
    return new ListObjectCommand(executor, targetState, ResolvedReference.getFullName(chosenRef));
  }

  @Override public ConsoleState search(String searchString) {
    Optional<List<ResolvedReference>> matches = searchByName(searchString);
    if (matches.isPresent()) {
      List<ResolvedReference> result = matches.get();
      result.sort(ResolvedReference.COMPARE_BY_RETAINED_HEAP);
      ResolvedReferenceListManager newListManager = new ResolvedReferenceListManager(snapshot, executor, heapSizer, result);
      return newListManager.printBatch(0);
    } else {
      return null;
    }
  }


  @Override public String getSearchTarget(ResolvedReference row) {
    return ResolvedReference.getFullName(row);
  }

  @Override public Optional<List<SortOption<ResolvedReference>>> getSortOptions() {
    return Optional.of(sortOptions);
  }
}