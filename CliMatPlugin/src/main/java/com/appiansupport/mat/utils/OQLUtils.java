package com.appiansupport.mat.utils;

import com.appiansupport.mat.constants.OqlConstants;
import org.eclipse.mat.SnapshotException;
import org.eclipse.mat.query.IResultTable;
import org.eclipse.mat.snapshot.IOQLQuery;
import org.eclipse.mat.snapshot.ISnapshot;
import org.eclipse.mat.snapshot.OQLParseException;
import org.eclipse.mat.snapshot.SnapshotFactory;
import org.eclipse.mat.snapshot.model.IObject;
import org.eclipse.mat.util.IProgressListener;
import org.eclipse.mat.util.VoidProgressListener;

public class OQLUtils {

  private static String getInformationFromArrayWithOql(ISnapshot snapshot, String oqlQuery) throws SnapshotException {
    StringBuilder results = new StringBuilder();
    Object oqlResult = executeOql(oqlQuery, snapshot, new VoidProgressListener());
    IResultTable table = (IResultTable) oqlResult;
    if (table == null) {
      return "";
    }
    for (int rowNumber = 0; rowNumber < table.getRowCount(); ++rowNumber) {
      Object currentRow = table.getRow(rowNumber);
      int columnCount = table.getColumns().length;
      for (int colNumber = 0; colNumber < columnCount; ++colNumber) {
        results.append((String) table.getColumnValue(currentRow, colNumber));
        if (colNumber < columnCount - 1) {
          results.append(", ");
        }
      }
      results.append("\n");
    }
    return results.toString();
  }

  /**
   * Helper for receiving String results from querying an array via Eclipse MAT SnapshotFactory.createQuery().
   * @param snapshot The ISnapshot of this HPROF
   * @param arrayObject the array object to query
   * @param baseOqlQuery The String query
   * @return A String representation of the query results, or an empty String if no results were found.
   * @throws SnapshotException
   */
  public static String getInformationFromArrayWithOql(ISnapshot snapshot, IObject arrayObject, String baseOqlQuery) throws SnapshotException {
    String oqlQuery = baseOqlQuery.replace(OqlConstants.OBJECT_ID_REPLACEMENT_STRING, String.valueOf(arrayObject.getObjectId()));
    return getInformationFromArrayWithOql(snapshot, oqlQuery);
  }

  /**
   * Execute OQL via SnapshotFactory.createQuery()
   * @param oql The String query
   * @param snapshot The ISnapshot of this HPROF
   * @param listener The IProgressListener which the MAT API will write progress to
   * @return An Object representing the query results. See the MAT API for more details.
   * @throws SnapshotException
   */
  public static Object executeOql(String oql, ISnapshot snapshot, IProgressListener listener) throws SnapshotException {
    try {
      IOQLQuery query = SnapshotFactory.createQuery(oql);
      return query.execute(snapshot, listener);
    } catch (OQLParseException e) {
      throw new SnapshotException("Error occurred parsing: " + oql, e);
    }
  }
}
