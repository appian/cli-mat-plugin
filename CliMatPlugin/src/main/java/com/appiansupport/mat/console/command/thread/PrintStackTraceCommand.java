package com.appiansupport.mat.console.command.thread;

import com.appiansupport.mat.console.CommandExecutor;
import com.appiansupport.mat.console.command.ConsoleCommand;
import com.appiansupport.mat.console.state.ConsoleState;
import org.eclipse.mat.snapshot.model.IObject;

public class PrintStackTraceCommand extends ConsoleCommand {
  final IObject object;

  public PrintStackTraceCommand(CommandExecutor executor, IObject object) {
    super(executor);
    this.object = object;
  }

  @Override public ConsoleState execute() {
    return executor.printStackTrace(object.getObjectId());
  }

  @Override public String toString() {
    return "Stack trace";
  }

  @Override public String getHelpText() {
    return "Prints the stack trace of this Thread from the .threads file.";
  }
}
