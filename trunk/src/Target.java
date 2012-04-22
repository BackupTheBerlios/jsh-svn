/***********************************************************************\
*
* $Source: /home/torsten/cvs/jmake/src/Target.java,v $
* $Revision$
* $Author$
* Contents: shell target
* Systems: all
*
\***********************************************************************/

/****************************** Imports ********************************/
import java.io.File;

import java.util.ArrayList;
import java.util.LinkedList;

/****************************** Classes ********************************/

/** target error
 */
class TargetError extends Error
{
  TargetError(String message)
  {
    super(message);
  }

  TargetError(String format, Object... args)
  {
    super(String.format(format,args));
  }
}

/** target
 */
public class Target
{
  // --------------------------- constants --------------------------------

  // --------------------------- variables --------------------------------

//  private 
public  String              name;
  public  LinkedList<Target> dependencyList;
  public  LinkedList<Action> actionList;
public long               makeTimestamp;

  private static int recursionLevel = 0;

  // ------------------------ native functions ----------------------------

  // ---------------------------- methods ---------------------------------

  /** create target
   * @param name target name
   * @param dependencyList target dependencies
   * @param actionList target actions
   */
  Target(String name, LinkedList<Target> dependencyList, LinkedList<Action> actionList)
  {
    this.name           = name;
    this.dependencyList = dependencyList;
    this.actionList     = actionList;
    File file = new File(name);
    if (file.exists())
    {
      this.makeTimestamp = file.lastModified();
    }
    else
    {
      this.makeTimestamp = 0;
    }
  }

  /** create target
   * @param name target name
   * @param actionList target actions
   */
  Target(String name, LinkedList<Action> actionList)
  {
    this(name,null,actionList);
  }

  /** create target
   * @param name target name
   */
  Target(String name)
  {
    this(name,null);
  }

  /** remake target if required
   * @return TRUE iff target was rebuilt
   */
  public boolean remake()
    throws TargetError
  {
    boolean remakeFlag    = false;
    boolean notExistsFlag = false;

    recursionLevel++;

    // check dependencies
    ArrayList<Target> remakeTargets = new ArrayList<Target>();
    if (dependencyList != null)
    {
      for (Target target : dependencyList)
      {
        if (   target.remake()
            || (target.makeTimestamp > makeTimestamp)
           )
        {
          remakeTargets.add(target);
          remakeFlag = true;
        }
      }
    }

    // check if exists
    File file = new File(name);
    if (!file.exists())
    {
      notExistsFlag = true;
    }

    // remake if required
    if (remakeFlag || notExistsFlag)
    {
      if (Settings.verboseLevel >= 2)
      {
        if (notExistsFlag)
        {
          Jsh.printVerbose(2,recursionLevel,"INFO: remake target '%s', because it does not exists.",name);
        }
        else
        {
          StringBuffer buffer = new StringBuffer();
          for (Target remakeTarget : remakeTargets)
          {
            if (buffer.length() > 0) buffer.append(", ");
            buffer.append(remakeTarget.name);
          }

          Jsh.printVerbose(2,recursionLevel,"INFO: remake target '%s', because of remake of '%s'.",name,buffer.toString());
        }
      }
      make();
    }

    recursionLevel--;

    return remakeFlag;
  }

  /** make target
   */
  public void make()
    throws TargetError
  {
    // execute action block to make target
    execute();

    // store make timestamp
    File file = new File(name);
    if (file.exists())
    {
      this.makeTimestamp = file.lastModified();
    }
  }
  
  /** execute target
   */
  public void execute()
    throws TargetError
  {
    if (actionList != null)
    {
      for (Action action : actionList)
      {
//Dprintf.dprintf("action=%s",action);
        int exitcode = action.execute();
        if (exitcode != 0) throw new TargetError(String.format("Cannot execute '%s' (exitcode %d)",action,exitcode));
      }
    }
    else
    {
//      throw new TargetError("No actions to make target '%s'",name);
    }
  }

  public String toString()
  {
    return "{TARGET "+name+"}";
  }
}

/* end of file */
