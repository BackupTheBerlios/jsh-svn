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
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.BufferedWriter;
import java.io.PrintWriter;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import groovy.lang.GroovyRuntimeException;
import groovy.lang.MissingMethodException;
import org.codehaus.groovy.control.CompilerConfiguration;

/****************************** Classes ********************************/

abstract class JavaCommand// extends Command
{
  protected String[] arguments;

  public JavaCommand()
  {
  }
  
  public void setArguments(String[] arguments)
  {
    this.arguments = arguments;
  }

  public void setArguments(List<String> argumentList)
  {
    this.arguments = argumentList.toArray(new String[argumentList.size()]);
  }

  public int run(PipedInputStream pipedInputStream, PipedOutputStream pipedOutputStream)
  {
    BufferedReader inputStream  = (pipedInputStream != null) ? new BufferedReader(new InputStreamReader(pipedInputStream)) : null;
    PrintWriter    outputStream = new PrintWriter(pipedOutputStream);

    return run(inputStream,outputStream);
  }
  
  public int run(BufferedReader inputStream, PrintWriter outputStream)
  {
Dprintf.dprintf("no run implemented!");
    System.exit(1);

    return -1;
  }
}

public class Command extends Thread
{
  // --------------------------- constants --------------------------------
  enum Types
  {
    JAVA,
    EXTERNAL,
  };

  // --------------------------- variables --------------------------------

  private Types             type;

  private PipedInputStream  inputStream  = null;
  private PipedOutputStream outputStream = null;

  private JavaCommand       javaCommand;
  
  private Process           process;
  private int               exitCode;
  private OutputStream      stdin  = null;
  private InputStream       stdout = null;
  private InputStream       stderr = null;

  // ------------------------ native functions ----------------------------

  // ---------------------------- methods ---------------------------------

  /** create new external command
   * @param command command string list
   */
  Command(JavaCommand javaCommand)
  {
    this.type        = Types.JAVA;
    this.javaCommand = javaCommand;
  }

  /** create new external command
   * @param command command string list
   */
  Command(Process process)
  {
    this.type    = Types.EXTERNAL;
    this.process = process;
  }

  public void run()
  {
    switch (type)
    {
      case JAVA:
        javaCommand.run(inputStream,outputStream);
        break;
      case EXTERNAL:
        try
        {
          stdin  = process.getOutputStream();
          stdout = process.getInputStream();
          stderr = process.getErrorStream();

          byte[] buffer = new byte[4096];
          int    n;
          while ((n = stdout.read(buffer)) != -1)
          {
            if (outputStream != null)
            {
    //Dprintf.dprintf("n=%d",n);
              outputStream.write(buffer,0,n); outputStream.flush();
            }
          }

          stderr.close();
          stdout.close();
          stdin.close();
        }
        catch (IOException exception)
        {
          throw new Error("Execute command");
        }
        break;
    }
  }

  public PipedInputStream getOutput()
  {
    return inputStream;
  }

  public void setInput(PipedInputStream inputStream)
  {
    this.inputStream = inputStream;
  }

  public void setOutput(PipedOutputStream outputStream)
  {
    this.outputStream = outputStream;
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
    return "{ type="+type+", process="+process+" }";
  }
}

/* end of file */
