package com.appiansupport.mat.console.command.search;

import com.appiansupport.mat.console.CommandExecutor;
import com.appiansupport.mat.console.command.ConsoleCommand;
import com.appiansupport.mat.console.listmanager.ListManager;
import com.appiansupport.mat.console.state.ConsoleState;
import com.appiansupport.mat.console.state.SearchForObjectState;

public class SearchListParentCommand<T> extends ConsoleCommand {
  final ListManager<T> listManager;
  final int firstIndex;

  public SearchListParentCommand(CommandExecutor executor, ListManager<T> listManager) {
    this(executor, listManager, 0);
  }

  public SearchListParentCommand(CommandExecutor executor, ListManager<T> listManager, int firstIndex) {
    this.executor = executor;
    this.listManager = listManager;
    this.firstIndex = firstIndex;
  }

  public ConsoleState execute() {
    return new SearchForObjectState<>(executor, listManager, firstIndex);
  }

  @Override public String toString() {
    return "Choose or search";
  }
}
