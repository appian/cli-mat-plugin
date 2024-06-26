package com.appiansupport.mat.console.state;

import com.appiansupport.mat.console.CommandExecutor;
import com.appiansupport.mat.console.command.overview.HomeCommand;
import com.appiansupport.mat.console.command.print.PrintBatchStringsCommand;
import com.appiansupport.mat.console.command.search.SearchStringsCommand;

public class BatchStringsState extends NoHistoryState {

  public BatchStringsState(CommandExecutor executor, String[] strings) {
    this(executor, strings, 0);
  }

  public BatchStringsState(CommandExecutor executor, String[] strings, int nextIndex) {
    super(executor);
    if (nextIndex < strings.length) {
      options.add(new PrintBatchStringsCommand(executor, strings, nextIndex));
    }
    options.add(new PrintBatchStringsCommand(executor, strings));
    options.add(new SearchStringsCommand(executor, strings));
    options.add(new HomeCommand(executor));
  }
}
