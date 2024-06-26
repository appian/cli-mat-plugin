package com.appiansupport.mat.console.command.overview;

import com.appiansupport.mat.console.CommandExecutor;
import com.appiansupport.mat.console.command.ConsoleCommand;
import com.appiansupport.mat.console.state.ConsoleState;

public class LeakSuspectsCommand extends ConsoleCommand {
  public LeakSuspectsCommand(CommandExecutor executor) {
    super(executor);
  }

  @Override public ConsoleState execute() {
    return executor.leakSuspectsCommand();
  }

  @Override public String toString() {
    return "Leak Suspects Report";
  }

  @Override public String getHelpText() {
    return "attempts to logically identify problematic objects and classes.";
  }
}
