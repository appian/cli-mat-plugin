package com.appiansupport.mat.console.command.search;

import com.appiansupport.mat.console.CommandExecutor;
import com.appiansupport.mat.console.command.ConsoleCommand;
import com.appiansupport.mat.console.listmanager.ListManager;
import com.appiansupport.mat.console.state.ConsoleState;

public class SearchListByNameCommand<T> extends ConsoleCommand {
  private final ListManager<T> listManager;

  public SearchListByNameCommand(CommandExecutor executor, ListManager<T> listManager) {
    super(executor);
    this.listManager = listManager;
  }

  @Override public ConsoleState execute() {
    return executor.matchNamePrompt(listManager);
  }

  @Override public String toString() {
    return "Search by name within this list";
  }
}
