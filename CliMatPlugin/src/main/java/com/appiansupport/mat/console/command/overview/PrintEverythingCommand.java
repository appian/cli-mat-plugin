package com.appiansupport.mat.console.command.overview;

import com.appiansupport.mat.console.CommandExecutor;
import com.appiansupport.mat.console.command.ConsoleCommand;
import com.appiansupport.mat.console.state.ConsoleState;

public class PrintEverythingCommand extends ConsoleCommand {

  public PrintEverythingCommand(CommandExecutor executor) {
    super(executor);
  }

  @Override public ConsoleState execute() {
    return executor.printEverything();
  }

  @Override public String toString() {
    return "Full report";
  }

  @Override public String getHelpText() {
    return "Prints the entire report: Thread Statistics, Leak Suspects," + " and all AJP & Work Item Thread information.";
  }
}
