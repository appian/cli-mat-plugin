package com.appiansupport.mat.console.command.references;

import com.appiansupport.mat.console.CommandExecutor;
import com.appiansupport.mat.console.command.ConsoleCommand;
import com.appiansupport.mat.console.state.ChooseObjectReferencesState;
import com.appiansupport.mat.console.state.ConsoleState;
import org.eclipse.mat.snapshot.model.IObject;

public class ReferencesOfObjectParentCommand extends ConsoleCommand {
  private final IObject object;

  public ReferencesOfObjectParentCommand(CommandExecutor executor, IObject object) {
    super(executor);
    this.object = object;
  }

  @Override public ConsoleState execute() {
    return new ChooseObjectReferencesState(executor, object);
  }

  @Override public String toString() {
    return "Object references";
  }
}
