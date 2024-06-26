package com.appiansupport.mat.console.state;

import com.appiansupport.mat.console.CommandExecutor;
import com.appiansupport.mat.console.command.SuggestedCommand;
import java.util.ArrayList;

//Null States rerun the current state, but add relevant objects passed in
public class NullState extends ConsoleState {

  public NullState(CommandExecutor executor) {
    super(executor);
  }

  public NullState(CommandExecutor executor, SuggestedCommand suggestedCommand) {
    super(executor);
    addSuggestedCommand(suggestedCommand);
  }

  public NullState(CommandExecutor executor, ArrayList<? extends SuggestedCommand> suggestedCommands) {
    super(executor);
    addSuggestedCommands(suggestedCommands);
  }
}
