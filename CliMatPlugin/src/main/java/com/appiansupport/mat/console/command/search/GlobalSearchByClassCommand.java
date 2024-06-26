package com.appiansupport.mat.console.command.search;

import com.appiansupport.mat.console.CommandExecutor;
import com.appiansupport.mat.console.command.ConsoleCommand;
import com.appiansupport.mat.console.state.ConsoleState;

public class GlobalSearchByClassCommand extends ConsoleCommand {

  public GlobalSearchByClassCommand(CommandExecutor executor) {
    super(executor);
  }

  @Override public ConsoleState execute() {
    return executor.globalSearchByClass();
  }

  @Override public String toString() {
    return "Search by Class";
  }
}
