package com.appiansupport.mat.console.command.references;

import com.appiansupport.mat.console.CommandExecutor;
import com.appiansupport.mat.console.command.ConsoleCommand;
import com.appiansupport.mat.console.state.ConsoleState;
import org.eclipse.mat.snapshot.ClassHistogramRecord;

public class GetObjectsOfClassCommand extends ConsoleCommand {
  private final ClassHistogramRecord record;

  public GetObjectsOfClassCommand(CommandExecutor executor, ClassHistogramRecord record) {
    super(executor);
    this.record = record;
  }

  @Override public ConsoleState execute() {
    return executor.getObjectsFromClass(record);
  }

  @Override public String toString() {
    return "Get objects of Class";
  }
}
