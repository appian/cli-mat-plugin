package com.appiansupport.mat.console.state;

import com.appiansupport.mat.console.CommandExecutor;
import com.appiansupport.mat.console.command.object.ChooseObjectByIdCommand;
import com.appiansupport.mat.console.command.object.ChooseObjectFromListCommand;
import com.appiansupport.mat.console.command.overview.HomeCommand;
import com.appiansupport.mat.console.command.search.GlobalSearchByClassCommand;
import com.appiansupport.mat.console.command.search.SearchListByNameCommand;
import com.appiansupport.mat.console.listmanager.ListManager;

public class SearchForObjectState<T> extends NoHistoryState {

  public SearchForObjectState(CommandExecutor executor, ListManager<T> listManager) {
    this(executor, listManager, 0);
  }

  public SearchForObjectState(CommandExecutor executor, ListManager<T> listManager, int startIndex) {
    super(executor);
    if (listManager != null) {
      options.add(new ChooseObjectFromListCommand<>(executor, listManager, startIndex));
      options.add(new SearchListByNameCommand<>(executor, listManager));
    } else {
      options.add(new GlobalSearchByClassCommand(executor));
    }
    options.add(new ChooseObjectByIdCommand(executor));
    options.add(new HomeCommand(executor));
  }
}