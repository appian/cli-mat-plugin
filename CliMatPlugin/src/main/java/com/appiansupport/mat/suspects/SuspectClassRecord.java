package com.appiansupport.mat.suspects;

import java.util.List;
import org.eclipse.mat.snapshot.ClassHistogramRecord;
import org.eclipse.mat.snapshot.model.IObject;

public record SuspectClassRecord(
    ClassHistogramRecord histogramRecord,
    List<IObject> classObjects,
    List<SuspectParent> commonParents)
{}
