package com.appiansupport.mat.constants;

public class OqlConstants {

  public static final String VALUES_OF_ARRAY_OQL = "SELECT arrayElement.toString() FROM OBJECTS ( SELECT OBJECTS o"
      + ".@referenceArray FROM OBJECTS OBJECT_ID o  ) arrayElement";
  public static final String ACP_NAME_AND_VALUES_OQL = "SELECT arrayElement.name.toString() AS \"ACP name\", arrayElement"
      + ".value.toString() AS \"ACP value\" FROM OBJECTS ( SELECT OBJECTS o.@referenceArray FROM OBJECTS OBJECT_ID o  )"
      + " arrayElement ";
  public static final String URI_IN_TASK_THREAD_OQL = "SELECT s.string.toString() AS URI FROM OBJECTS ( SELECT OBJECTS "
      + "outbounds(t) FROM org.apache.tomcat.util.threads.TaskThread t WHERE (t.@objectId = OBJECT_ID) ) s WHERE s"
      + ".@displayName.contains(\"java.net.URI\")";
  public static final String ALL_EXPRESSIONS_AND_CONSTANTS_OQL = "SELECT c.name.toString() AS \"Expression or Constant\", c"
      + ".expression.value.toString() AS \"Value (If expression)\", c.contentId.toString() AS \"Content ID\", c.uuid"
      + ".toString() FROM com.appiancorp.core.expr.rule.Rule c WHERE ((c.parentUuid.value = null) and toString(c.uuid"
      + ".value).startsWith(\"_a\"))";
  public static final String OBJECT_ID_REPLACEMENT_STRING = "OBJECT_ID";


}
