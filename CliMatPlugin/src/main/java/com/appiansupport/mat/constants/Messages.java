/*******************************************************************************
 * These Strings were copied from org.eclipse.mat.internal.Messages,
 *   which is not API.
 * Original copyright notice:
 *
 * Copyright (c) 2008, 2021 SAP AG and IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     SAP AG - initial API and implementation
 *     IBM Corporation - updates including multiple snapshots
 *******************************************************************************/

package com.appiansupport.mat.constants;

public class Messages {
  public static final String NESTED_PATH_INDENTATION = "    ";
  public static final String EMPTY_SEARCH_RESULTS_CASE_SENSITIVE = "No matches found (search is case-sensitive)";

  public static final String FindLeaksQuery_PathNotFound = "Couldn't find paths for {0} of the {1} objects";
  public static final String FindLeaksQuery_SearchingGroupsOfObjects = "Searching suspicious groups of objects";
  public static final String FindLeaksQuery_SearchingSingleObjects = "Searching suspicious single objects";
  public static final String FindLeaksQuery_TooManySuspects = "Too many suspect instances ({0}). " + "Will use top {1} to search for common path.";

  public static final String LeakHunterQuery_Msg_AccumulatedBy = "classloader/component %s, which occupies %s bytes.";
  public static final String LeakHunterQuery_Msg_AccumulatedByInstance = "one instance of %s, which occupies %s bytes.";
  public static final String LeakHunterQuery_Msg_AccumulatedByLoadedBy = "class %s, which occupies %s bytes.";

  public static final String OutOfMemory_ThreadTip = "Investigate this Thread's details & stack trace " + "if suspects are inconclusive.";
  public static final String OutOfMemoryThread_LowHeapTip = "When Heap used is much lower than max, it is most likely that this Thread "
      + "is the root cause of the issue due to a rapid allocation spike (e.g. Arrays.copyOf()).";

  public static final String CLI_OutgoingReferences_HelpText = "objects which X holds a reference to." + " Use outgoing references to learn more about what an object was doing.";
  public static final String CLI_IncomingReferences_HelpText = "objects which hold a reference to X." + " Use incoming references to learn more about where an object came from.";

  public static final String CLI_OutgoingClassReferences_HelpText = "all Classes which X holds a reference to at least one instance of.";
  public static final String CLI_IncomingClassReferences_HelpText = "all Classes which have at least one instance holding a reference to X.";

  public static final String CLI_OutgoingClassReferencesOfClass_HelpText = "all Classes which at least one instance of X holds a reference to.";
  public static final String CLI_IncomingClassReferencesOfClass_HelpText = "all Classes which hold a reference to at least one instance of X.";
}
