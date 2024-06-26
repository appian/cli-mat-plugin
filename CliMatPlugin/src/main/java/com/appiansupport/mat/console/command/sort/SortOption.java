package com.appiansupport.mat.console.command.sort;

import java.util.Comparator;

public class SortOption<T> {
  public static final String SORT_ALPHABETICALLY_ASCENDING = "Sort A -> Z";
  public static final String SORT_ALPHABETICALLY_DESCENDING = "Sort Z -> A";
  public static final String SORT_BY_NUM_OBJECTS = "Sort by # objects";
  public static final String SORT_BY_SHALLOW_HEAP = "Sort by shallow Heap";
  public static final String SORT_BY_RETAINED_HEAP = "Sort by retained Heap";
  private final String optionText;
  private final Comparator<T> comp;

  public SortOption(String optionText, Comparator<T> comp) {
    this.optionText = optionText;
    this.comp = comp;
  }

  public String getOptionText() {
    return optionText;
  }

  public Comparator<T> getComp() {
    return comp;
  }

}
