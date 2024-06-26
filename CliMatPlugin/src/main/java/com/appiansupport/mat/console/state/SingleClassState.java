package com.appiansupport.mat.console.state;

import com.appiansupport.mat.console.CommandExecutor;
import com.appiansupport.mat.console.command.overview.HomeCommand;
import com.appiansupport.mat.console.command.references.GetObjectsOfClassCommand;
import com.appiansupport.mat.console.command.references.ReferencesOfClassParentCommand;
import org.eclipse.mat.snapshot.ClassHistogramRecord;

public class SingleClassState extends ConsoleState {
  private final ClassHistogramRecord record;

  public SingleClassState(CommandExecutor executor, ClassHistogramRecord record) {
    super(executor);
    this.record = record;
    options.add(new GetObjectsOfClassCommand(executor, record));
    options.add(new ReferencesOfClassParentCommand(executor, record));
    options.add(new HomeCommand(executor));
  }

  public String toString() {
    return "Class " + record.getLabel();
  }
}
