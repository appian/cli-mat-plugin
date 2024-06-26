package com.appiansupport.mat.console.listmanager;

import org.eclipse.mat.snapshot.model.IObject;

public interface TableColumnPrinter<T> {
  String getData(T t);

  public static final TableColumnPrinter<IObject> objectDisplayNamePrinter = IObject::getDisplayName;
  public final TableColumnPrinter<IObject> objectRetainedHeapPrinter = t -> Long.toString(t.getRetainedHeapSize());
}

