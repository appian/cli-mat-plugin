package com.appiansupport.mat.console.state;

import com.appiansupport.mat.console.CommandExecutor;
import com.appiansupport.mat.console.command.overview.HomeCommand;
import com.appiansupport.mat.console.listmanager.ListManager;
import com.appiansupport.mat.constants.CliConstants;

public class ChooseObjectFromListState<T> extends NoHistoryState {

  public ChooseObjectFromListState(CommandExecutor executor, ListManager<T> listManager, int startIndex) {
    super(executor);
    options.addAll(listManager.chooseItemFromList(startIndex, CliConstants.MAX_OBJECTS_TO_CHOOSE_FROM_LIST));
    options.add(new HomeCommand(executor));
  }
}
