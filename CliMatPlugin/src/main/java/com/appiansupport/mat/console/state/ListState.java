package com.appiansupport.mat.console.state;

import com.appiansupport.mat.console.CommandExecutor;
import com.appiansupport.mat.console.command.ConsoleCommand;
import com.appiansupport.mat.console.listmanager.ListManager;
import java.util.List;

public class ListState<T> extends NoHistoryState {

  public ListState(CommandExecutor commandExecutor, ListManager<T> listManager, int startIndex, int batchSize) {
    super(commandExecutor);
    List<ConsoleCommand> listOptions = listManager.getAllOptions(startIndex, batchSize);
    options.addAll(ConsoleState.endOptionsWithHomeOption(commandExecutor, listOptions));

  }
}
