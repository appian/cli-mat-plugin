package com.appiansupport.mat.console.command.search;

import com.appiansupport.mat.console.CommandExecutor;
import com.appiansupport.mat.console.command.ConsoleCommand;
import com.appiansupport.mat.console.state.ConsoleState;

public class OqlCommand extends ConsoleCommand {

  public OqlCommand(CommandExecutor executor){
    super(executor);
  }

  @Override public ConsoleState execute() {
    return executor.oqlPrompt();
  }

  @Override public String toString() {
    return "Query with OQL [BETA]";
  }
}
