package com.appiansupport.mat.console.command.search;

import com.appiansupport.mat.console.CommandExecutor;
import com.appiansupport.mat.console.command.ConsoleCommand;
import com.appiansupport.mat.console.state.ConsoleState;

public class SearchStringsCommand extends ConsoleCommand {
  final String[] strings;

  public SearchStringsCommand(CommandExecutor executor, String[] strings) {
    this.executor = executor;
    this.strings = strings;
  }

  public ConsoleState execute() {
    return executor.searchStringsPrompt(strings);
  }

  @Override public String toString() {
    return "Text search";
  }
}