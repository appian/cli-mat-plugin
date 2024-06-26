package com.appiansupport.mat.console.command.object;

import com.appiansupport.mat.console.CommandExecutor;
import com.appiansupport.mat.console.command.ConsoleCommand;
import com.appiansupport.mat.console.listmanager.ListManager;
import com.appiansupport.mat.console.state.ChooseObjectFromListState;
import com.appiansupport.mat.console.state.ConsoleState;

public class ChooseObjectFromListCommand<T> extends ConsoleCommand {
  final ListManager<T> listManager;
  final int startIndex;

  public ChooseObjectFromListCommand(CommandExecutor executor, ListManager<T> listManager, int startIndex) {
    super(executor);
    this.listManager = listManager;
    this.startIndex = startIndex;
  }

  @Override public ConsoleState execute() {
    return new ChooseObjectFromListState<>(executor, listManager, startIndex);
  }

  @Override public String toString() {
    return "Choose from list";
  }
}