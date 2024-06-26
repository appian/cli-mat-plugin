package com.appiansupport.mat.console.command;

import com.appiansupport.mat.console.CommandExecutor;
import com.appiansupport.mat.console.state.ConsoleState;

public abstract class ConsoleCommand {
  protected CommandExecutor executor;

  public ConsoleCommand() {
  }

  public ConsoleCommand(CommandExecutor executor) {
    this.executor = executor;
  }

  public abstract ConsoleState execute();

  public abstract String toString();

  public String getHelpText() {
    return null;
  }
}
