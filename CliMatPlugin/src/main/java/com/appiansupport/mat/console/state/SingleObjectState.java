package com.appiansupport.mat.console.state;

import com.appiansupport.mat.console.CommandExecutor;
import com.appiansupport.mat.console.command.object.ChooseObjectByIdCommand;
import com.appiansupport.mat.console.command.object.GcRootsCommand;
import com.appiansupport.mat.console.command.object.SuspectObjectCommand;
import com.appiansupport.mat.console.command.overview.HomeCommand;
import com.appiansupport.mat.console.command.print.PrintBatchPrimitiveArrayCommand;
import com.appiansupport.mat.console.command.references.OutgoingStringsCommand;
import com.appiansupport.mat.console.command.references.ReferencesOfObjectParentCommand;
import com.appiansupport.mat.console.command.thread.PrintStackTraceCommand;
import org.eclipse.mat.snapshot.model.IObject;
import org.eclipse.mat.snapshot.model.IPrimitiveArray;

public class
SingleObjectState extends ConsoleState {
  private final IObject object;

  public SingleObjectState(CommandExecutor executor, IObject object) {
    super(executor);
    this.object = object;
    options.add(new SuspectObjectCommand(executor, object));
    addObjectOptions();
  }

  private void addObjectOptions() {
    options.add(new ReferencesOfObjectParentCommand(executor, object));
    if (object instanceof IPrimitiveArray) {
      options.add(new PrintBatchPrimitiveArrayCommand(executor, (IPrimitiveArray) object,0));
    } else {
      options.add(new OutgoingStringsCommand(executor, object));
    }

    if (executor.isThread(object.getObjectId())) {
      options.add(new PrintStackTraceCommand(executor, object));
    }
    options.add(new GcRootsCommand(executor, object));
    options.add(new ChooseObjectByIdCommand(executor));
    options.add(new HomeCommand(executor));
  }

  @Override public String toString() {
    return object.getDisplayName();
  }
}