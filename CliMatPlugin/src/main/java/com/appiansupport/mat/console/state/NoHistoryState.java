package com.appiansupport.mat.console.state;

import com.appiansupport.mat.console.CommandExecutor;
import com.appiansupport.mat.console.command.ConsoleCommand;
import java.util.List;

public class NoHistoryState extends ConsoleState {
  public NoHistoryState(CommandExecutor executor) {
    super(executor);
  }

  public NoHistoryState(CommandExecutor executor, List<? extends ConsoleCommand> customOptions) {
    super(executor, customOptions, null);
  }

  public NoHistoryState(CommandExecutor executor, List<? extends ConsoleCommand> customOptions, String stateDescription) {
    super(executor, customOptions, stateDescription);
  }
}
