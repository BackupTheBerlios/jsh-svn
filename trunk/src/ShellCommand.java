/***********************************************************************\
*
* $Source: /home/torsten/cvs/jmake/src/Action.java,v $
* $Revision$
* $Author$
* Contents: jmake action
* Systems: all
*
\***********************************************************************/

/****************************** Imports ********************************/
import java.io.InputStream;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.BufferedWriter;
import java.io.PrintWriter;

import java.util.LinkedList;
import java.util.Arrays;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import groovy.lang.GroovyRuntimeException;
import groovy.lang.MissingMethodException;
import org.codehaus.groovy.control.CompilerConfiguration;

/****************************** Classes ********************************/

public class ShellCommand extends Thread
{
  // --------------------------- constants --------------------------------
  enum Types
  {
    COMMAND,
    JAVACODE,
  };

  // --------------------------- variables --------------------------------

  private Types    type;
  private Process process;
  private int     exitCode;

  // ------------------------ native functions ----------------------------

  // ---------------------------- methods ---------------------------------

  /** create new command action
   * @param command command string list
   */
  ShellCommand(Process process)
  {
    this.type    = Types.COMMAND;
    this.process = process;
  }

  public void run()
  {
    try
    {
      PrintWriter stdin     = new PrintWriter(process.getOutputStream());
      BufferedReader stdout = new BufferedReader(new InputStreamReader(process.getInputStream()));
      BufferedReader stderr = new BufferedReader(new InputStreamReader(process.getErrorStream()));

      String line;
      while ((line = stdout.readLine()) != null)
      {
      }

      stderr.close();
      stdout.close();
      stdin.close();
    }
    catch (IOException exception)
    {
      throw new Error("Execute command");
    }
  }
  
  public void waitTerminated()
  {
    try
    {
      exitCode = process.waitFor();
    }
    catch (InterruptedException exception)
    {
      exception.printStackTrace();
      System.exit(1);
    }
  }
  
  public int getExitCode()
  {
    return exitCode;
  }

  /** convert to string
   * @return string
   */
  public String toString()
  {
    return "{unknown}";
  }
}

/* end of file */
