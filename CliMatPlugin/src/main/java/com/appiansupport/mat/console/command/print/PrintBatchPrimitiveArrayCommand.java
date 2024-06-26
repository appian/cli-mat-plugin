package com.appiansupport.mat.console.command.print;

import com.appiansupport.mat.console.CommandExecutor;
import com.appiansupport.mat.console.command.ConsoleCommand;
import com.appiansupport.mat.console.state.ConsoleState;
import org.eclipse.mat.snapshot.model.IPrimitiveArray;

public class PrintBatchPrimitiveArrayCommand extends ConsoleCommand {
  private static final int PRIMITIVE_ARRAY_BATCH_SIZE=5000;
  private IPrimitiveArray array;
  private int startIndex;

  public PrintBatchPrimitiveArrayCommand(CommandExecutor executor, IPrimitiveArray array,int startIndex){
    this.executor = executor;
    this.array = array;
    this.startIndex = startIndex;
  }

  @Override public ConsoleState execute() {
    return executor.printPrimitiveArrayBatch(array, startIndex, PRIMITIVE_ARRAY_BATCH_SIZE);
  }

  @Override public String toString() {
    int arrLen = array.getLength();
    int lastInd = Math.min(arrLen,startIndex+PRIMITIVE_ARRAY_BATCH_SIZE);
    return startIndex == 0 ? "Traverse Array contents" : String.format("Show more (%d-%d of %d)",startIndex,lastInd,arrLen);
  }
}
