package com.appiansupport.mat.console.command.overview;

import com.appiansupport.mat.console.CommandExecutor;
import com.appiansupport.mat.console.command.ConsoleCommand;
import com.appiansupport.mat.console.state.ConsoleState;

public class CustomObjectInfoCommand extends ConsoleCommand {

  public CustomObjectInfoCommand(CommandExecutor executor) {
    super(executor);
  }

  @Override public ConsoleState execute() {
    return executor.printCustomObjectInfo();
  }

  @Override public String toString() {
    return "Custom object info";
  }
}
