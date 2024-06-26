package com.appiansupport.mat.console.command.references;

import com.appiansupport.mat.console.CommandExecutor;
import com.appiansupport.mat.console.command.ConsoleCommand;
import com.appiansupport.mat.console.state.ConsoleState;
import org.eclipse.mat.snapshot.model.IObject;

public class OutgoingStringsCommand extends ConsoleCommand {
  final IObject object;

  public OutgoingStringsCommand(CommandExecutor executor, IObject object) {
    super(executor);
    this.object = object;
  }

  public ConsoleState execute() {
    return executor.printStrings(object);
  }

  @Override public String toString() {
    return "Referenced Strings";
  }
}
