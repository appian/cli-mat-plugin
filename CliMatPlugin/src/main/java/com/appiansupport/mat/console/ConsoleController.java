package com.appiansupport.mat.console;

import com.appiansupport.mat.console.command.ConsoleCommand;
import com.appiansupport.mat.console.state.ConsoleState;
import com.appiansupport.mat.console.state.InitialState;
import com.appiansupport.mat.console.state.NoHistoryState;
import com.appiansupport.mat.console.state.NullState;
import java.util.List;
import java.util.Scanner;
import java.util.Stack;
import org.apache.commons.text.TextStringBuilder;

public class ConsoleController {
  private final CommandExecutor executor;
  private final Scanner scanner;
  private Stack<ConsoleState> stateHistory;
  private List<ConsoleCommand> currentOptions;
  private boolean canBack = false;

  public ConsoleController(CommandExecutor executor, Scanner scanner) {
    this.executor = executor;
    this.scanner = scanner;
  }

  public void execute() {
    System.out.println("\nWelcome to CLIMAT CLI");
    stateHistory = new Stack<>();
    stateHistory.push(new InitialState(executor));
    boolean exited = false;
    while (!exited) {
      ConsoleState currentState = stateHistory.peek();
      if (currentState instanceof NoHistoryState) {
        stateHistory.pop();
      }
      currentOptions = currentState.getOptions();
      String currentStateDescription = currentState.toString();
      if (currentStateDescription != null) {
        System.out.println(currentStateDescription);
      }
      System.out.println(listOptions());
      try {
        String userInput = scanner.nextLine();
        int userChoice = Integer.parseInt(userInput.trim());
        //if 'back' was chosen
        if ((userChoice == (currentOptions.size() + 1)) && canBack) {
          //If in a NoHistoryState, the top of stateHistory already contains the desired next state, so keep the stack as-is.
          if (!(currentState instanceof NoHistoryState)) {
            stateHistory.pop();
          }
          continue;
        //if 'exit' was chosen
        } else if (((userChoice == (currentOptions.size() + 2)) && canBack) || ((userChoice == (currentOptions.size() + 1)) & !canBack)) {
          exited = true;
          continue;
        }
        ConsoleCommand choice = currentOptions.get(userChoice - 1);
        System.out.println();
        ConsoleState commandResult = currentState.execute(choice);
        if (commandResult instanceof NullState) {
          //Don't change the state, but update relevant objects
          currentState.addSuggestedCommands(commandResult.getSuggestedCommands());
        } else if (commandResult != null) {
          stateHistory.push(commandResult);
        }
      } catch (NumberFormatException | IndexOutOfBoundsException numberException) {
        System.out.println("Could not parse input to a numbered option. Please try again!");
      }
    }
  }

  /**
   * Converts the options list into a String for Console display by prefixing each option with a number.
   * This method also appends the 'back' (if applicable) and 'exit' commands,
   * which are supported logically in execute() and never exist as ConsoleCommand objects in the list.
   * @return the String representation of currently available options.
   */
  private String listOptions() {
    TextStringBuilder output = new TextStringBuilder();
    int numOptions = currentOptions.size();
    for (int i = 1; i < numOptions + 1; i++) {
      output.appendln("%d: %s", i, currentOptions.get(i - 1).toString());
    }
    if (stateHistory.size() == 1) {
      canBack = false;
    } else {
      canBack = true;
      output.appendln(++numOptions + ": Back");
    }
    output.append(String.valueOf(++numOptions)).append(": Exit");
    return output.toString();
  }
}