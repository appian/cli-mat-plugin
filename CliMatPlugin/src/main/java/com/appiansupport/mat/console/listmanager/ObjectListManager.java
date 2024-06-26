package com.appiansupport.mat.console.listmanager;

import com.appiansupport.mat.console.CommandExecutor;
import com.appiansupport.mat.console.command.ConsoleCommand;
import com.appiansupport.mat.console.command.object.ListObjectCommand;
import com.appiansupport.mat.console.command.sort.SortOption;
import com.appiansupport.mat.console.state.ConsoleState;
import com.appiansupport.mat.console.state.ListState;
import com.appiansupport.mat.console.state.SingleObjectState;
import com.appiansupport.mat.utils.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import com.appiansupport.mat.utils.internal.HeapTablePrinter;
import org.eclipse.mat.snapshot.ISnapshot;
import org.eclipse.mat.snapshot.model.IObject;

public class ObjectListManager extends ListManager<IObject> {
  static final List<SortOption<IObject>> sortOptions = new ArrayList<>(Arrays.asList(
      new SortOption<>(SortOption.SORT_BY_RETAINED_HEAP, ObjectUtils.compareIObjectByRetainedHeapDescending),
      new SortOption<>(SortOption.SORT_ALPHABETICALLY_ASCENDING, ObjectUtils.compareIObjectByDisplayName),
      new SortOption<>(SortOption.SORT_ALPHABETICALLY_DESCENDING, ObjectUtils.compareIObjectByDisplayName.reversed())));
  private final ISnapshot snapshot;
  private final HeapTablePrinter tablePrinter;

  public ObjectListManager(ISnapshot snapshot, CommandExecutor executor, HeapTablePrinter tablePrinter, List<IObject> objects) {
    super(executor, objects);
    this.snapshot = snapshot;
    this.tablePrinter = tablePrinter;
  }

  @Override protected Optional<List<ConsoleCommand>> getCustomOptions() {
    return Optional.empty();
  }

  public ConsoleState printBatch(int startIndex, int batchSize) {
    int finalIndex = Math.min(objects.size(), startIndex + batchSize);
    System.out.println(tablePrinter.printObjectsTableWithHeapAndPercent(objects.subList(startIndex, finalIndex),null));
    return new ListState<>(executor, this, startIndex, batchSize);
  }

  public ListObjectCommand chooseItem(int index) {
    IObject targetObject = objects.get(index);
    SingleObjectState targetState = new SingleObjectState(executor, targetObject);
    return new ListObjectCommand(executor, targetState, targetObject.getDisplayName());
  }

  @Override
  public ConsoleState search(String searchString) {
    Optional<List<IObject>> matchingObjects = searchByName(searchString);
    if (matchingObjects.isPresent()) {
      List<IObject> result = matchingObjects.get();
      result.sort(ObjectUtils.compareIObjectByRetainedHeapDescending);
      ObjectListManager newListManager = new ObjectListManager(snapshot, executor, tablePrinter, result);
      return newListManager.printBatch(0);
    } else {
      return null;
    }
  }

  @Override public String getSearchTarget(IObject row) {
    return row.getDisplayName();
  }

  @Override public Optional<List<SortOption<IObject>>> getSortOptions() {
    return Optional.of(sortOptions);
  }
}
