package com.appiansupport.mat.utils.internal;

import org.eclipse.mat.SnapshotException;
import org.eclipse.mat.snapshot.ClassHistogramRecord;
import org.eclipse.mat.snapshot.ISnapshot;
import org.eclipse.mat.snapshot.model.IClass;
import org.eclipse.mat.snapshot.model.IObject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;

public class ObjectFetcher {
    private final ISnapshot snapshot;

    public ObjectFetcher(ISnapshot snapshot) {
        this.snapshot = snapshot;
    }

    /**
     * @param hexAddress The hexadecimal address to resolve.
     * @return the matching IObject
     * @throws SnapshotException if the address is not found
     */
    public IObject getObjectFromHexAddress(String hexAddress) throws SnapshotException {
        return snapshot.getObject(getIdFromHexAddress(hexAddress));
    }

    /**
     * @param hexAddress The hexadecimal address to resolve.
     * @return the matching ID
     * @throws SnapshotException if the address is not found
     */
    public int getIdFromHexAddress(String hexAddress) throws SnapshotException {
        if (!hexAddress.startsWith("0x")) {
            hexAddress = "0x" + hexAddress;
        }
        long addressLong = Long.decode(hexAddress);
        return snapshot.mapAddressToId(addressLong);
    }

    /**
     * @param classPattern Class to fetch objects IDs of
     * @return an int[] of all object IDs found of class classPattern
     * @throws SnapshotException Should not normally occur
     */
    public int[] getObjectIdsByClass(Pattern classPattern) throws SnapshotException {
        ArrayList<Integer> objectIds = new ArrayList<>();
        Collection<IClass> classes = snapshot.getClassesByName(classPattern, false);
        if (classes != null) {
            for (IClass clazz : classes) {
                Arrays.stream(clazz.getObjectIds()).forEach(objectIds::add);
            }
        }
        return objectIds.stream().mapToInt(Integer::valueOf).toArray();
    }

    /**
     * @param classPattern Class to fetch objects of
     * @return A List of all objects found of class classPattern
     * @throws SnapshotException Should not normally occur
     */
    public List<IObject> getObjectsByClass(Pattern classPattern) throws SnapshotException {
        List<IObject> objects = new ArrayList<>();
        int[] ids = getObjectIdsByClass(classPattern);
        for (int id : ids) {
            try {
                objects.add(snapshot.getObject(id));
            } catch (SnapshotException snapshotException) {
                System.err.printf("Unable to resolve %s ID %s%n", classPattern, id);
                snapshotException.printStackTrace();
            }
        }
        return objects;
    }

    /**
     * @param record The ClassHistogramRecord to fetch all objects from
     * @return A List of all objects from record.
     */
    public List<IObject> getObjectsFromClassRecord(ClassHistogramRecord record) {
        int[] objectIds = record.getObjectIds();
        ArrayList<IObject> objects = new ArrayList<>();
        for (int i : objectIds) {
            try {
                objects.add(snapshot.getObject(i));
            } catch (SnapshotException snapshotException) {
                System.err.println("Unable to resolve object " + i);
                snapshotException.printStackTrace();
            }
        }
        return objects;
    }
}
