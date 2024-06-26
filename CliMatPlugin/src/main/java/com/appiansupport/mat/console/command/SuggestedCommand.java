package com.appiansupport.mat.console.command;

import com.appiansupport.mat.console.CommandExecutor;
import com.appiansupport.mat.console.state.ConsoleState;
import java.util.Comparator;

/**
 * SuggestedCommands represent ConsoleCommands added dynamically by code other than the ConsoleState itself.
 */
public class SuggestedCommand extends ConsoleCommand {
  public static final Comparator<SuggestedCommand> compareByDescription = Comparator.nullsLast(Comparator.comparing(SuggestedCommand::getRelevanceDescription));
  private final ConsoleState targetState;
  private final String relevanceDescription;

  public SuggestedCommand(CommandExecutor executor, ConsoleState targetState, String relevanceDescription) {
    super(executor);
    this.targetState = targetState;
    this.relevanceDescription = relevanceDescription;
  }

  @Override public ConsoleState execute() {
    return targetState;
  }

  @Override public String toString() {
    return "(Suggested) Investigate " + relevanceDescription;
  }

  public ConsoleState getTargetState() {
    return targetState;
  }

  public String getRelevanceDescription() {
    return relevanceDescription;
  }

}
