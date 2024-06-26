package com.appiansupport.mat.console.command.object;

import com.appiansupport.mat.console.CommandExecutor;
import com.appiansupport.mat.console.command.ConsoleCommand;
import com.appiansupport.mat.console.state.ConsoleState;

public class ChooseObjectByIdCommand extends ConsoleCommand {

  public ChooseObjectByIdCommand(CommandExecutor executor) {
    super(executor);
  }

  public String toString() {
    return "Choose object by hex address";
  }

  @Override public ConsoleState execute() {
    return executor.chooseObjectPrompt();
  }
}
