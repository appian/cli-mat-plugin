package com.appiansupport.mat.console.listmanager;

import com.appiansupport.mat.console.CommandExecutor;
import com.appiansupport.mat.console.command.ConsoleCommand;
import com.appiansupport.mat.console.command.object.ListObjectCommand;
import com.appiansupport.mat.console.command.print.PrintBatchListCommand;
import com.appiansupport.mat.console.command.print.PrintFullListCommand;
import com.appiansupport.mat.console.command.search.SearchListParentCommand;
import com.appiansupport.mat.console.command.sort.SortListParentCommand;
import com.appiansupport.mat.console.command.sort.SortOption;
import com.appiansupport.mat.console.state.ConsoleState;
import com.appiansupport.mat.constants.CliConstants;
import com.appiansupport.mat.constants.Messages;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class ListManager<T> {
  final List<T> objects;
  final CommandExecutor executor;

  ListManager(CommandExecutor executor, List<T> objects) {
    this.executor = executor;
    this.objects = Objects.requireNonNull(objects, "ListManager cannot manage a null list");
  }

  /**
   * @param startIndex the first index last printed to the console.
   * @param batchSize the batchSize last printed to the console.
   * @return A list of applicable commands given the data.
   */
  public List<ConsoleCommand> getAllOptions(int startIndex, int batchSize) {
    List<ConsoleCommand> options = new ArrayList<>();
    int nextIndex = startIndex + batchSize;
    if (nextIndex < objects.size()) {
      options.add(new PrintBatchListCommand<>(executor, this, nextIndex));
    }
    if (objects.size() <= CliConstants.MAX_OBJECTS_TO_OFFER_DIRECT_CHOICE) {
      options.addAll(chooseItemFromList(0, objects.size()));
    }
    options.add(new PrintFullListCommand<>(executor, this));
    getSortOptions().ifPresent(o -> options.add(new SortListParentCommand<>(executor, this)));
    options.add(new SearchListParentCommand<>(executor, this, startIndex));
    Optional<List<ConsoleCommand>> customOptions = getCustomOptions();
    customOptions.ifPresent(options::addAll);
    return options;
  }

  protected abstract Optional<List<ConsoleCommand>> getCustomOptions();

  public ConsoleState printAll() {
    return printBatch(0, getSize());
  }

  public ConsoleState printBatch(int startIndex) {
    return printBatch(startIndex, CliConstants.DEFAULT_RESULTS_BATCH_SIZE);
  }

  public abstract ConsoleState printBatch(int startIndex, int batchSize);

  /**
   * @param startIndex The first index last printed to the console.
   * @param batchSize The batch size last printed to the console.
   * @return A List of ListObjectCommands, which provide the necessary information do display and select the objects.
   */
  public List<ListObjectCommand> chooseItemFromList(int startIndex, int batchSize) {
    List<ListObjectCommand> choices = new ArrayList<>();
    int numChoices = Math.min(objects.size() - startIndex, batchSize);
    for (int i = 0; i < numChoices; i++) {
      ListObjectCommand choice = chooseItem(startIndex + i);
      if (choice != null) {
        choices.add(choice);
      }
    }
    return choices;
  }

  /**
   * @param index index of the object chosen.
   * @return A ListObjectCommand, which handles how the object is displayed and what happens when it is selected.
   */
  public abstract ListObjectCommand chooseItem(int index);

  public abstract ConsoleState search(String searchString);

  Optional<List<T>> searchByName(String searchString) {
    if (searchString == null || searchString.isEmpty()) {
      return Optional.empty();
    }
    final List<T> matchingObjects = new ArrayList<>();
    Pattern pattern = Pattern.compile(searchString);
    for (T object : objects) {
      String searchTarget = getSearchTarget(object);
      Matcher matcher = pattern.matcher(searchTarget);
      if (matcher.find()) {
        matchingObjects.add(object);
      }
    }
    if (matchingObjects.isEmpty()) {
      System.out.println(Messages.EMPTY_SEARCH_RESULTS_CASE_SENSITIVE);
      return Optional.empty();
    } else {
      return Optional.of(matchingObjects);
    }
  }

  /**
   * @param row an instance of T.
   * @return the value to match in searching this list.
   */
  public abstract String getSearchTarget(T row);

  public abstract Optional<List<SortOption<T>>> getSortOptions();

  public ConsoleState sortByComparator(Comparator<? super T> comp) {
    objects.sort(comp);
    return printBatch(0);
  }

  public int getSize() {
    return objects.size();
  }
}
