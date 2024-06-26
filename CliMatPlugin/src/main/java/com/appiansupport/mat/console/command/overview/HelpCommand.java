package com.appiansupport.mat.console.command.overview;

import com.appiansupport.mat.console.command.ConsoleCommand;
import com.appiansupport.mat.console.state.ConsoleState;

public class HelpCommand extends ConsoleCommand {
  private final String helpText;

  public HelpCommand(String helpText) {
    this.helpText = helpText;
  }

  @Override public ConsoleState execute() {
    System.out.println(helpText);
    return null;
  }

  @Override public String toString() {
    return "Help";
  }
}
