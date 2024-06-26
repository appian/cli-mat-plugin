package com.appiansupport.mat.console.state;

import com.appiansupport.mat.console.CommandExecutor;
import com.appiansupport.mat.console.command.ConsoleCommand;
import com.appiansupport.mat.console.command.SuggestedCommand;
import com.appiansupport.mat.console.command.overview.HelpCommand;
import com.appiansupport.mat.console.command.overview.HomeCommand;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;
import org.apache.commons.text.TextStringBuilder;

public class ConsoleState {
  final CommandExecutor executor;
  final List<ConsoleCommand> options;
  private TreeSet<SuggestedCommand> suggestedCommands;
  private String stateDescription;

  ConsoleState(CommandExecutor executor) {
    this.executor = executor;
    options = new ArrayList<>();
    suggestedCommands = new TreeSet<>(SuggestedCommand.compareByDescription);
  }

  public ConsoleState(CommandExecutor executor, List<? extends ConsoleCommand> customOptions) {
    this(executor, customOptions, null);
  }

  public ConsoleState(CommandExecutor executor, List<? extends ConsoleCommand> customOptions, String stateDescription) {
    this(executor);
    this.stateDescription = stateDescription;
    List<ConsoleCommand> optionsWithHomeAtEnd = endOptionsWithHomeOption(executor, customOptions);
    options.addAll(optionsWithHomeAtEnd);
  }

  static List<ConsoleCommand> endOptionsWithHomeOption(CommandExecutor executor, List<? extends ConsoleCommand> options) {
    List<ConsoleCommand> optionsWithHomeAtEnd = new ArrayList<>();
    options.forEach(o -> {
      if (!(o instanceof HomeCommand)) {
        optionsWithHomeAtEnd.add(o);
      }
    });
    optionsWithHomeAtEnd.add(new HomeCommand(executor));
    return optionsWithHomeAtEnd;
  }

  public ConsoleState execute(ConsoleCommand choice) {
    return choice.execute();
  }

  public String toString() {
    return stateDescription;
  }

  public String getStateDescription() {
    return stateDescription;
  }

  public List<ConsoleCommand> getOptions() {
    generateHelpOption();
    if (suggestedCommands == null) {
      return new ArrayList<>(options);
    } else {
      List<ConsoleCommand> allOptions = new ArrayList<>();
      allOptions.addAll(suggestedCommands);
      allOptions.addAll(options);
      return allOptions;
    }
  }

  private HelpCommand generateHelpOption() {
    TextStringBuilder helpText = new TextStringBuilder();
    for (ConsoleCommand option : options) {
      if (option instanceof HelpCommand) {
        //Abort, as we already have a HelpCommand
        return null;
      }
      String optionHelp = option.getHelpText();
      if (optionHelp != null) {
        helpText.appendln(option.toString() + ": " + optionHelp);
      }
    }
    if (!helpText.isEmpty()) {
      HelpCommand helpCommand = new HelpCommand(helpText.toString());
      options.add(helpCommand);
      return helpCommand;
    } else {
      return null;
    }
  }

  public TreeSet<SuggestedCommand> getSuggestedCommands() {
    return new TreeSet<>(suggestedCommands);
  }

  public TreeSet<SuggestedCommand> addSuggestedCommand(SuggestedCommand relevantObject) {
    if (relevantObject == null) {
      return new TreeSet<>(suggestedCommands);
    }
    suggestedCommands.add(relevantObject);
    return new TreeSet<>(suggestedCommands);
  }

  public TreeSet<SuggestedCommand> addSuggestedCommands(List<? extends SuggestedCommand> newSuggestedCommands) {
    if (newSuggestedCommands != null) {
      newSuggestedCommands.forEach(this::addSuggestedCommand);
    }
    return new TreeSet<>(suggestedCommands);
  }

  public TreeSet<SuggestedCommand> addSuggestedCommands(TreeSet<? extends SuggestedCommand> newSuggestedCommands) {
    if (newSuggestedCommands == null) {
      return new TreeSet<>(suggestedCommands);
    }
    suggestedCommands.addAll(newSuggestedCommands);
    return new TreeSet<>(suggestedCommands);
  }
}
