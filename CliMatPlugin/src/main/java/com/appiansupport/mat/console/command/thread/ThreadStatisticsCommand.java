package com.appiansupport.mat.console.command.thread;

import com.appiansupport.mat.console.CommandExecutor;
import com.appiansupport.mat.console.command.ConsoleCommand;
import com.appiansupport.mat.console.state.ConsoleState;

public class ThreadStatisticsCommand extends ConsoleCommand {

  public ThreadStatisticsCommand(CommandExecutor executor) {
    super(executor);
  }

  @Override public ConsoleState execute() {
    return executor.printThreadStatistics();
  }

  @Override public String toString() {
    return "Thread Statistics";
  }

  @Override public String getHelpText() {
    return "Prints Thread counts, top Threads, and the OutOfMemoryEror Thread if applicable.";
  }
}
