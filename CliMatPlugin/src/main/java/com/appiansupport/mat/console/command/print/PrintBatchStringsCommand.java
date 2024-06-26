package com.appiansupport.mat.console.command.print;

import com.appiansupport.mat.console.CommandExecutor;
import com.appiansupport.mat.console.command.ConsoleCommand;
import com.appiansupport.mat.console.state.ConsoleState;
import com.appiansupport.mat.constants.CliConstants;

public class PrintBatchStringsCommand extends ConsoleCommand {

  final boolean isAllresults;
  private final int startIndex;
  private final int batchSize;
  private final String[] data;

  public PrintBatchStringsCommand(CommandExecutor executor, String[] data) {
    super(executor);
    this.data = data;
    this.startIndex = 0;
    this.batchSize = data.length;
    this.isAllresults = true;
  }

  public PrintBatchStringsCommand(CommandExecutor executor, String[] data, int startIndex) {
    super(executor);
    this.data = data;
    this.startIndex = startIndex;
    this.batchSize = Math.min((data.length - startIndex), CliConstants.DEFAULT_RESULTS_BATCH_SIZE);
    this.isAllresults = false;
  }

  @Override public ConsoleState execute() {
    return executor.printBatchStrings(data, startIndex, batchSize);
  }

  @Override public String toString() {
    return String.format("Show %s lines (%d)", isAllresults ? "all" : "more", batchSize);
  }
}


