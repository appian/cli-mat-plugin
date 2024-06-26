package com.appiansupport.mat.console.state;

import com.appiansupport.mat.console.CommandExecutor;
import com.appiansupport.mat.console.command.references.ClassReferencesCommand;
import org.eclipse.mat.snapshot.ClassHistogramRecord;

public class ChooseClassReferencesState extends NoHistoryState {

  public ChooseClassReferencesState(CommandExecutor executor, ClassHistogramRecord record) {
    super(executor);
    options.add(new ClassReferencesCommand(executor, record, true));
    options.add(new ClassReferencesCommand(executor, record, false));
  }

}