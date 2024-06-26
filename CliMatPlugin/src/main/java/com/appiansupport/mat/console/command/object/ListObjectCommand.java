package com.appiansupport.mat.console.command.object;

import com.appiansupport.mat.console.CommandExecutor;
import com.appiansupport.mat.console.command.ConsoleCommand;
import com.appiansupport.mat.console.state.ConsoleState;

public class ListObjectCommand extends ConsoleCommand {
  final ConsoleState targetState;
  final String rowText;

  public ListObjectCommand(CommandExecutor executor, ConsoleState targetState, String rowText) {
    super(executor);
    this.targetState = targetState;
    this.rowText = rowText;
  }

  @Override public ConsoleState execute() {
    return targetState;
  }

  @Override public String toString() {
    return rowText;
  }
}