package com.appiansupport.mat.console.state;

import com.appiansupport.mat.console.CommandExecutor;
import com.appiansupport.mat.console.command.object.ChooseObjectByIdCommand;
import com.appiansupport.mat.console.command.overview.HomeCommand;
import com.appiansupport.mat.console.command.print.PrintBatchPrimitiveArrayCommand;
import org.eclipse.mat.snapshot.model.IPrimitiveArray;

public class PrimitiveArrayState extends NoHistoryState {

  public PrimitiveArrayState(CommandExecutor executor, IPrimitiveArray arr, int nextIndex) {
    super(executor);
    if (nextIndex < arr.getLength()) {
      options.add(new PrintBatchPrimitiveArrayCommand(executor, arr, nextIndex));
    }
    options.add(new ChooseObjectByIdCommand(executor));
    options.add(new HomeCommand(executor));
  }
}
