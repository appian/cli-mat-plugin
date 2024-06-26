package com.appiansupport.mat.console.command.object;

import com.appiansupport.mat.console.CommandExecutor;
import com.appiansupport.mat.console.command.ConsoleCommand;
import com.appiansupport.mat.console.state.ConsoleState;
import org.eclipse.mat.snapshot.model.IObject;

public class GcRootsCommand extends ConsoleCommand {
  final IObject object;

  public GcRootsCommand(CommandExecutor executor, IObject object) {
    super(executor);
    this.object = object;
  }

  @Override public ConsoleState execute() {
    return executor.getGcRoots(object);
  }

  @Override public String toString() {
    return "GC Roots";
  }

  @Override public String getHelpText() {
    return "prints the path of objects keeping this object alive in Heap.";
  }
}
