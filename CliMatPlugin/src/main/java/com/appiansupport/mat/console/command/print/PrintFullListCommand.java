package com.appiansupport.mat.console.command.print;

import com.appiansupport.mat.console.CommandExecutor;
import com.appiansupport.mat.console.command.ConsoleCommand;
import com.appiansupport.mat.console.listmanager.ListManager;
import com.appiansupport.mat.console.state.ConsoleState;

public class PrintFullListCommand<T> extends ConsoleCommand {
  private final ListManager<T> listManager;

  public PrintFullListCommand(CommandExecutor executor, ListManager<T> listManager) {
    super(executor);
    this.listManager = listManager;
  }

  @Override public ConsoleState execute() {
    return listManager.printAll();
  }

  @Override public String toString() {
    return String.format("Show all (%d)", listManager.getSize());
  }
}
