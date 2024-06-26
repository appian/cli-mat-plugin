package com.appiansupport.mat.console.command.overview;

import com.appiansupport.mat.console.CommandExecutor;
import com.appiansupport.mat.console.command.ConsoleCommand;
import com.appiansupport.mat.console.state.ConsoleState;
import com.appiansupport.mat.console.state.InitialState;

public class HomeCommand extends ConsoleCommand {

  public HomeCommand(CommandExecutor executor) {
    super(executor);
  }

  @Override public ConsoleState execute() {
    return new InitialState(executor);
  }

  @Override public String toString() {
    return "Home";
  }
}
