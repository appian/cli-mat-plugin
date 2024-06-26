package com.appiansupport.mat.console.command.references;

import com.appiansupport.mat.console.CommandExecutor;
import com.appiansupport.mat.console.command.ConsoleCommand;
import com.appiansupport.mat.console.state.ConsoleState;
import com.appiansupport.mat.constants.Messages;
import org.eclipse.mat.snapshot.model.IObject;

public class ReferencesOfObjectCommand extends ConsoleCommand {
  final IObject object;
  final boolean outgoing;

  public ReferencesOfObjectCommand(CommandExecutor executor, IObject object, boolean outgoing) {
    super(executor);
    this.object = object;
    this.outgoing = outgoing;
  }

  @Override public String toString() {
    return String.format("%s references", outgoing ? "Outgoing" : "Incoming");
  }

  public ConsoleState execute() {
    return executor.getObjectReferences(object, outgoing);
  }

  @Override public String getHelpText() {
    return outgoing ? Messages.CLI_OutgoingReferences_HelpText : Messages.CLI_IncomingReferences_HelpText;
  }
}
