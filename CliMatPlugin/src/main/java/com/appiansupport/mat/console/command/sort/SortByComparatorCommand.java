package com.appiansupport.mat.console.command.sort;

import com.appiansupport.mat.console.CommandExecutor;
import com.appiansupport.mat.console.command.ConsoleCommand;
import com.appiansupport.mat.console.listmanager.ListManager;
import com.appiansupport.mat.console.state.ConsoleState;

public class SortByComparatorCommand<T> extends ConsoleCommand {
  final ListManager<? extends T> listManager;
  final SortOption<? super T> sortOption;

  public SortByComparatorCommand(CommandExecutor executor, ListManager<? extends T> listManager, SortOption<? super T> sortOption) {
    super(executor);
    this.listManager = listManager;
    this.sortOption = sortOption;
  }

  @Override public ConsoleState execute() {
    return listManager.sortByComparator(sortOption.getComp());
  }

  @Override public String toString() {
    return sortOption.getOptionText();
  }
}