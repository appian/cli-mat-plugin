package com.appiansupport.mat.console.state;

import com.appiansupport.mat.console.CommandExecutor;
import com.appiansupport.mat.console.command.overview.DefaultHistogramCommand;
import com.appiansupport.mat.console.command.overview.DominatorTreeCommand;
import com.appiansupport.mat.console.command.overview.CustomObjectInfoCommand;
import com.appiansupport.mat.console.command.overview.LeakSuspectsCommand;
import com.appiansupport.mat.console.command.overview.PrintEverythingCommand;
import com.appiansupport.mat.console.command.search.OqlCommand;
import com.appiansupport.mat.console.command.search.SearchListParentCommand;
import com.appiansupport.mat.console.command.thread.ThreadStatisticsCommand;

public class InitialState extends ConsoleState {

  public InitialState(CommandExecutor executor) {
    super(executor);
    options.add(new PrintEverythingCommand(executor));
    options.add(new LeakSuspectsCommand(executor));
    options.add(new ThreadStatisticsCommand(executor));
    options.add(new CustomObjectInfoCommand(executor));
    options.add(new DominatorTreeCommand(executor));
    options.add(new DefaultHistogramCommand(executor));
    options.add(new SearchListParentCommand(executor, null));
    options.add(new OqlCommand(executor));
  }

  @Override public String toString() {
    return "Choose an option:";
  }

}