package com.appiansupport.mat.console.command.print;

import com.appiansupport.mat.console.CommandExecutor;
import com.appiansupport.mat.console.command.ConsoleCommand;
import com.appiansupport.mat.console.listmanager.ListManager;
import com.appiansupport.mat.console.state.ConsoleState;

public class PrintBatchListCommand<T> extends ConsoleCommand {
  private final ListManager<T> listManager;
  private final int startIndex;

  public PrintBatchListCommand(CommandExecutor executor, ListManager<T> listManager, int startIndex) {
    super(executor);
    this.listManager = listManager;
    this.startIndex = startIndex;
  }

  @Override public ConsoleState execute() {
    return listManager.printBatch(startIndex);
  }

  @Override public String toString() {
    return "Show more";
  }
}
