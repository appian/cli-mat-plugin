package com.appiansupport.mat.utils.internal;

import com.appiansupport.mat.constants.OqlConstants;
import com.appiansupport.mat.utils.OQLUtils;
import com.appiansupport.mat.utils.PrintUtils;
import org.eclipse.mat.SnapshotException;
import org.eclipse.mat.snapshot.ClassHistogramRecord;
import org.eclipse.mat.snapshot.ISnapshot;
import org.eclipse.mat.snapshot.model.IObject;
import org.eclipse.mat.util.ConsoleProgressListener;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HeapSizer {
    private final ISnapshot snapshot;
    private final ObjectFetcher objectFetcher;
    private Long usedHeapBytes;

    public HeapSizer(ISnapshot snapshot, ObjectFetcher objectFetcher) {
        this.snapshot = snapshot;
        this.objectFetcher = objectFetcher;
    }

    public long getUsedHeapBytes() {
        if(usedHeapBytes == null) {
            usedHeapBytes = snapshot.getSnapshotInfo().getUsedHeapSize();
        }
        return usedHeapBytes;
    }

    public Optional<Double> getUsedHeapDecimal(){
        final long heapUsedBytes = getUsedHeapBytes();
        final Optional<Long> maxHeapBytes = getMaxHeapBytes();
        return maxHeapBytes.map(maxBytes -> (double) heapUsedBytes / maxBytes);
    }

    public Optional<Long> getMaxHeapBytes() {
        final Pattern JVM_PROPS_CLASS = Pattern.compile("sun.management.RuntimeImpl",Pattern.LITERAL);
        final String PATH_FROM_JVM_PROPS_CLASS_TO_PROPS_ARRAY = "jvm.vmArgs.list.a";
        try {
            int[] ids = objectFetcher.getObjectIdsByClass(JVM_PROPS_CLASS);
            if (ids.length < 1) {
                System.out.printf("WARNING: found no %s objects; cannot report -Xmx value.\n", JVM_PROPS_CLASS);
                return Optional.empty();
            }
            if (ids.length > 1) {
                System.out.println(PrintUtils.printUnexpected(String.format("Found multiple %s objects", JVM_PROPS_CLASS)));
            }
            IObject jvmPropsObject = snapshot.getObject(ids[0]);
            IObject jvmPropsArray = (IObject) jvmPropsObject.resolveValue(PATH_FROM_JVM_PROPS_CLASS_TO_PROPS_ARRAY);
            Long xmx = getXmxFromJvmProps(OQLUtils.getInformationFromArrayWithOql(snapshot, jvmPropsArray, OqlConstants.VALUES_OF_ARRAY_OQL));
            if (xmx == null) {
                System.out.println(PrintUtils.printUnexpected(String.format("Unable to find -Xmx value in %s object", JVM_PROPS_CLASS)));
            }
            return Optional.ofNullable(xmx);
        } catch (SnapshotException snapshotException) {
            System.err.println("Error resolving -Xmx value");
            snapshotException.printStackTrace();
            return Optional.empty();
        }
    }

    Long getXmxFromJvmProps(String jvmProps) {
        final int B_TO_KB = 1024;
        final Pattern matchHeapValueAndUnit = Pattern.compile("-Xmx(?<value>[0-9]+)(?<unit>[mMgGkK]?)");
        Matcher maxHeapMatcher = matchHeapValueAndUnit.matcher(jvmProps);
        if (maxHeapMatcher.find()) {
            Long heapValue = Long.valueOf(maxHeapMatcher.group("value"));
            String heapUnit = maxHeapMatcher.group("unit");
            if (heapUnit.isEmpty()) {
                //default unit is Bytes.
                return heapValue;
            } else if (heapUnit.equalsIgnoreCase("k")) {
                return heapValue * B_TO_KB;
            } else if (heapUnit.equalsIgnoreCase("m")) {
                return heapValue * B_TO_KB * B_TO_KB;
            } else if (heapUnit.equalsIgnoreCase("g")) {
                return heapValue * B_TO_KB * B_TO_KB * B_TO_KB;
            }
        }
        return null;
    }

    public String printHeapUsedPercent(long retainedHeap) {
        return String.format("%.1f%%", ((double) retainedHeap / (double) getUsedHeapBytes()) * 100);
    }

    public String printObjectHeapUsageNoWords(IObject object) {
        long retainedHeap = object.getRetainedHeapSize();
        return String.format("%,d (%s)", retainedHeap, printHeapUsedPercent(retainedHeap));
    }

    public String printObjectHeapUsage(IObject object) {
        long retainedHeap = object.getRetainedHeapSize();
        return String.format("Heap used: %,d bytes (%s)", retainedHeap, printHeapUsedPercent(retainedHeap));
    }

    public String printHeapUsage(long retainedHeap) {
        return String.format("Heap used: %,d bytes (%s)", retainedHeap, printHeapUsedPercent(retainedHeap));
    }

    public void calculateClassRetained(ClassHistogramRecord rec) {
        try {
            rec.calculateRetainedSize(snapshot, true, true, new ConsoleProgressListener(System.out));
            //The fact that this is an approximation is not relevant to the end user, so let's scrap the negative value.
            rec.setRetainedHeapSize(Math.abs(rec.getRetainedHeapSize()));
        } catch (SnapshotException snapshotException) {
            System.err.printf("Unable to calculate retained Heap for %s; attempting manual calculation%n", rec.getLabel());
            snapshotException.printStackTrace();
            rec.setRetainedHeapSize(sumRetainedSize(rec.getObjectIds()));
        }
    }

    private long sumRetainedSize(int[] objectIds) {
        long sum = 0;
        for (int id : objectIds) {
            try {
                sum += snapshot.getRetainedHeapSize(id);
            } catch (SnapshotException snapshotException) {
                System.err.printf("Error resolving Heap usage of Object ID %d; resulting retained Heap usage will be inaccurate", id);
                snapshotException.printStackTrace();
            }
        }
        return sum;
    }
}
