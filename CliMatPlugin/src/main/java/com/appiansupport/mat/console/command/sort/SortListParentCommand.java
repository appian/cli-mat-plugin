package com.appiansupport.mat.console.command.sort;

import com.appiansupport.mat.console.CommandExecutor;
import com.appiansupport.mat.console.command.ConsoleCommand;
import com.appiansupport.mat.console.listmanager.ListManager;
import com.appiansupport.mat.console.state.ConsoleState;
import com.appiansupport.mat.console.state.SortListState;

public class SortListParentCommand<T> extends ConsoleCommand {
  final ListManager<T> listManager;

  public SortListParentCommand(CommandExecutor executor, ListManager<T> listManager) {
    super(executor);
    this.listManager = listManager;
  }

  @Override public ConsoleState execute() {
    return new SortListState<>(executor, listManager);
  }

  @Override public String toString() {
    return "Sort";
  }
}
