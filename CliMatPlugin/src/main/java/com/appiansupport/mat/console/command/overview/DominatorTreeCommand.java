package com.appiansupport.mat.console.command.overview;

import com.appiansupport.mat.console.CommandExecutor;
import com.appiansupport.mat.console.command.ConsoleCommand;
import com.appiansupport.mat.console.state.ConsoleState;

public class DominatorTreeCommand extends ConsoleCommand {

  public DominatorTreeCommand(CommandExecutor executor) {
    super(executor);
  }

  @Override public ConsoleState execute() {
    return executor.dominatorTreeCommand();
  }

  @Override public String toString() {
    return "Dominator Tree";
  }

  @Override public String getHelpText() {
    return "prints the largest objects ordered by retained Heap usage." + " Retained Heap represents the total Heap an object is responsible for.";
  }
}
