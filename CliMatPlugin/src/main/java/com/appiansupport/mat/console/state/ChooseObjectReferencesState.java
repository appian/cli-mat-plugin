package com.appiansupport.mat.console.state;

import com.appiansupport.mat.console.CommandExecutor;
import com.appiansupport.mat.console.command.references.ClassReferencesCommand;
import com.appiansupport.mat.console.command.references.ReferencesOfObjectCommand;
import org.eclipse.mat.snapshot.model.IObject;

public class ChooseObjectReferencesState extends NoHistoryState {

  public ChooseObjectReferencesState(CommandExecutor executor, IObject object) {
    super(executor);
    options.add(new ReferencesOfObjectCommand(executor, object, true));
    options.add(new ReferencesOfObjectCommand(executor, object, false));
    options.add(new ClassReferencesCommand(executor, object, true));
    options.add(new ClassReferencesCommand(executor, object, false));
  }
}
