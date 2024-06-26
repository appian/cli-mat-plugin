package com.appiansupport.mat.console.state;

import com.appiansupport.mat.console.CommandExecutor;
import com.appiansupport.mat.console.command.overview.HomeCommand;
import com.appiansupport.mat.console.command.sort.SortByComparatorCommand;
import com.appiansupport.mat.console.command.sort.SortOption;
import com.appiansupport.mat.console.listmanager.ListManager;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class SortListState<T> extends NoHistoryState {

  public SortListState(CommandExecutor executor, ListManager<T> listManager) {
    super(executor);
    Optional<List<SortOption<T>>> sortOptions = listManager.getSortOptions();
    sortOptions.orElse(Collections.emptyList()).forEach(o -> options.add(new SortByComparatorCommand<T>(executor, listManager, o)));
    options.add(new HomeCommand(executor));
  }
}