package com.appiansupport.mat.console.command.references;

import com.appiansupport.mat.console.CommandExecutor;
import com.appiansupport.mat.console.command.ConsoleCommand;
import com.appiansupport.mat.console.state.ConsoleState;
import com.appiansupport.mat.constants.Messages;
import java.util.Objects;
import org.eclipse.mat.snapshot.ClassHistogramRecord;
import org.eclipse.mat.snapshot.model.IObject;

public class ClassReferencesCommand extends ConsoleCommand {
  private final ClassHistogramRecord sourceClass;
  private final IObject object;
  private final boolean outgoing;
  private final boolean isClass;

  public ClassReferencesCommand(CommandExecutor executor, IObject object, boolean outgoing) {
    super(executor);
    this.object = Objects.requireNonNull(object, "Cannot generate references from a null input");
    this.outgoing = outgoing;
    this.sourceClass = null;
    this.isClass = false;
  }

  public ClassReferencesCommand(CommandExecutor executor, ClassHistogramRecord sourceClass, boolean outgoing) {
    super(executor);
    this.sourceClass = Objects.requireNonNull(sourceClass, "Cannot generate references from a null input");
    this.outgoing = outgoing;
    this.object = null;
    this.isClass = true;
  }

  @Override public ConsoleState execute() {
    return executor.getClassReferences(getObjectIds(), outgoing);
  }

  @Override public String toString() {
    return String.format("%s references by Class", outgoing ? "Outgoing" : "Incoming");
  }

  private int[] getObjectIds() {
    if (isClass) {
      return sourceClass.getObjectIds();
    } else {
      if (object != null) {
        return new int[] { object.getObjectId() };
      } else {
        //Should be impossible
        return null;
      }
    }
  }

  @Override public String getHelpText() {
    return outgoing ? (isClass ? Messages.CLI_OutgoingClassReferencesOfClass_HelpText : Messages.CLI_OutgoingClassReferences_HelpText) : (isClass ? Messages.CLI_IncomingClassReferencesOfClass_HelpText : Messages.CLI_IncomingClassReferences_HelpText);

  }
}