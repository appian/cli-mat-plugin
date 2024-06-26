package com.appiansupport.mat.console.command.object;

import com.appiansupport.mat.console.CommandExecutor;
import com.appiansupport.mat.console.command.ConsoleCommand;
import com.appiansupport.mat.console.state.ConsoleState;
import org.eclipse.mat.snapshot.model.IObject;

public class SuspectObjectCommand extends ConsoleCommand {
  private final IObject object;

  public SuspectObjectCommand(CommandExecutor executor, IObject object) {
    super(executor);
    this.object = object;
  }

  @Override public ConsoleState execute() {
    return executor.getSuspectInformation(object);
  }

  @Override public String toString() {
    return "Suspect information";
  }

  @Override public String getHelpText() {
    return "looks for relevant information about the object.";
  }
}
