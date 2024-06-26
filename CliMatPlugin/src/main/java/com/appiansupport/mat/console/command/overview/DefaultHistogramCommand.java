package com.appiansupport.mat.console.command.overview;

import com.appiansupport.mat.console.CommandExecutor;
import com.appiansupport.mat.console.command.ConsoleCommand;
import com.appiansupport.mat.console.state.ConsoleState;

public class DefaultHistogramCommand extends ConsoleCommand {

  public DefaultHistogramCommand(CommandExecutor executor) {
    super(executor);
  }

  @Override public ConsoleState execute() {
    return executor.printDefaultHistogram();
  }

  @Override public String toString() {
    return "Class Histogram";
  }

  @Override public String getHelpText() {
    return "prints Classes sorted by retained (total) Heap. Choose a Class to see its largest instances.";
  }
}