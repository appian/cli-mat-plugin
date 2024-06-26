package com.appiansupport.mat.console.command.references;

import com.appiansupport.mat.console.CommandExecutor;
import com.appiansupport.mat.console.command.ConsoleCommand;
import com.appiansupport.mat.console.state.ChooseClassReferencesState;
import com.appiansupport.mat.console.state.ConsoleState;
import org.eclipse.mat.snapshot.ClassHistogramRecord;

public class ReferencesOfClassParentCommand extends ConsoleCommand {
  private final ClassHistogramRecord record;

  public ReferencesOfClassParentCommand(CommandExecutor executor, ClassHistogramRecord record) {
    super(executor);
    this.record = record;
  }

  @Override public ConsoleState execute() {
    return new ChooseClassReferencesState(executor, record);
  }

  @Override public String toString() {
    return "References";
  }
}


