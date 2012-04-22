/***********************************************************************\
*
* $Source: /home/torsten/cvs/jmake/src/Action.java,v $
* $Revision$
* $Author$
* Contents: jsh command
* Systems: all
*
\***********************************************************************/

/****************************** Imports ********************************/
import java.lang.reflect.InvocationTargetException;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.PrintWriter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import groovy.lang.GroovyRuntimeException;
import groovy.lang.MissingMethodException;
import org.codehaus.groovy.control.CompilerConfiguration;

import antlr.ANTLRException;
import antlr.RecognitionException;

/****************************** Classes ********************************/

class CommandException extends RecognitionException//ANTLRException
{
  CommandException(String message, Object... arguments)
  {
    super(String.format(message,arguments));
  }

  CommandException(Exception cause)
  {
    super(cause.toString());
  }
}

enum CommandOptionTypes
{
  STRING,
  INTEGER,
  FLOAT,
  BOOLEAN,
  ENUMERATION,
  INCREMENT,
  SPECIAL
};

class CommandOption
{
  CommandOptionTypes type;
  String             name;
  String             shortName;

  String             s;
  long               l;
  double             d;
  Enum               e;
  int                i;
}

abstract class JavaCommand extends Command
{
  // --------------------------- constants --------------------------------

  // --------------------------- variables --------------------------------

  // ------------------------ native functions ----------------------------

  // ---------------------------- methods ---------------------------------

  protected String[] arguments;

//  public JavaCommand()
//  {
//}

  public void setArguments(String[] arguments)
  {
    this.arguments = arguments;
  }

  public void setArguments(List<String> argumentList)
  {
    this.arguments = argumentList.toArray(new String[argumentList.size()]);
  }

  public int getExitCode()
  {
    return -1;
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

class ExternalCommand extends Command
{
  // --------------------------- constants --------------------------------

  // --------------------------- variables --------------------------------

  // ------------------------ native functions ----------------------------

  // ---------------------------- methods ---------------------------------

  private Process           process;
  private int               exitCode;
  private OutputStream      stdin  = null;
  private InputStream       stdout = null;
  private InputStream       stderr = null;

  public int run(PipedInputStream pipedInputStream, PipedOutputStream pipedOutputStream)
  {
    int exitCode = -1;

    try
    {
      stdin  = process.getOutputStream();
      stdout = process.getInputStream();
      stderr = process.getErrorStream();

Dprintf.dprintf("stdin, stderr?");

      byte[] buffer = new byte[4096];
      int    n;
      while ((n = stdout.read(buffer)) != -1)
      {
        if (pipedOutputStream != null)
        {
//Dprintf.dprintf("n=%d",n);
          pipedOutputStream.write(buffer,0,n); pipedOutputStream.flush();
        }
      }

      stderr.close();
      stdout.close();
      stdin.close();

      exitCode = process.waitFor();
    }
    catch (IOException exception)
    {
      throw new Error("Execute command "+exception);
    }
    catch (InterruptedException exception)
    {
      throw new Error("Execute command "+exception);
    }

    return exitCode;
  }
}

public class Command extends Thread
{
  enum OptionTypes
  {
    STRING,
    INTEGER,
    FLOAT,
    BOOLEAN,
    ENUMERATION,
    INCREMENT,
    SPECIAL
  };

  class Option
  {
    CommandOptionTypes type;
    String             name;
    String             shortName;

    String             s;
    long               l;
    double             d;
    Enum               e;
    int                i;
  }

  // --------------------------- constants --------------------------------
  enum Types
  {
    NONE,

    JAVA,
    EXTERNAL,

    UNKNOWN
  };

  // --------------------------- variables --------------------------------

  private Types             type;

  private PipedInputStream  pipedInputStream  = null;
  private PipedOutputStream pipedOutputStream = null;

  private int               exitCode = -1;

  // Java command
  private JavaCommand       javaCommand;

  // external program
  private Process           process;
  private OutputStream      stdin  = null;
  private InputStream       stdout = null;
  private InputStream       stderr = null;

  // ------------------------ native functions ----------------------------

  // ---------------------------- methods ---------------------------------

  Command()
  {
    this.type = Types.UNKNOWN;
  }

  Command(Types type)
  {
    this.type = type;
  }

  /** create new Java command
   * @param javaCommandClass Java command class
   * @param argumentList argument list
   */
  Command(Class javaCommandClass, List<String> argumentList)
    throws CommandException
  {
    this.type = Types.JAVA;

    // instanciate Java command
    try
    {
      this.javaCommand = (JavaCommand)javaCommandClass.getDeclaredConstructor().newInstance();
    }
    catch (InstantiationException exception)
    {
Dprintf.dprintf("exception=%s",exception);
      throw new CommandException(exception);
    }
    catch (IllegalAccessException exception)
    {
Dprintf.dprintf("exception=%s",exception);
      throw new CommandException(exception);
    }
    catch (InvocationTargetException exception)
    {
Dprintf.dprintf("exception=%s",exception);
      throw new CommandException(exception);
    }
    catch (NoSuchMethodException exception)
    {
Dprintf.dprintf("exception=%s",exception);
//exception.printStackTrace();
      throw new CommandException(exception);
    }

    // set values
    this.javaCommand.setArguments(argumentList);
  }

  /** create new external command
   * @param name command name
   * @param argumentList argument list
   */
  Command(String name, List<String> argumentList)
    throws CommandException
  {
    this.type = Types.EXTERNAL;

    try
    {
      // find binary in PATH
      File binaryFile = null;
      File file = new File(name);
      if (file.exists() && file.canExecute())
      {
        binaryFile = file;
      }
      else
      {
        for (String path : System.getenv("PATH").split(File.pathSeparator))
        {
          file = new File(path,name);
          if (file.exists() && file.canExecute())
          {
            binaryFile = file;
            break;
          }
        }
      }
      if (binaryFile == null)
      {
        throw new CommandException("Command '"+name+"' not found");
      }
Dprintf.dprintf("run=%s",binaryFile.getAbsolutePath());

      // get command line
      ArrayList<String> commandLineList = new ArrayList<String>();
      if (Jsh.isWindowsSystem())
      {
        commandLineList.add("cmd.exe");
        commandLineList.add("/C");
      }
      else
      {
      }
      commandLineList.add(binaryFile.getAbsolutePath());
      for (String argument : argumentList)
      {
        commandLineList.add(argument);
      }
      String[] commandLine = commandLineList.toArray(new String[commandLineList.size()]);
//for (String s : commandLine) Dprintf.dprintf("commandLine=\%s",s);

      // start external program
      this.process = Runtime.getRuntime().exec(commandLine);
    }
    catch (IOException exception)
    {
Dprintf.dprintf("exception=%s",exception);
      throw new CommandException(exception);
    }
  }

  public void run()
  {
    switch (type)
    {
      case JAVA:
        exitCode = javaCommand.run(pipedInputStream,pipedOutputStream);
        break;
      case EXTERNAL:
        try
        {
          stdin  = process.getOutputStream();
          stdout = process.getInputStream();
          stderr = process.getErrorStream();

    Dprintf.dprintf("stdin, stderr?");

          byte[] buffer = new byte[4096];
          int    n;
          while ((n = stdout.read(buffer)) != -1)
          {
            if (pipedOutputStream != null)
            {
    //Dprintf.dprintf("n=%d",n);
              pipedOutputStream.write(buffer,0,n); pipedOutputStream.flush();
            }
          }

          stderr.close();
          stdout.close();
          stdin.close();

          exitCode = process.waitFor();
        }
        catch (IOException exception)
        {
          throw new Error("Execute command "+exception);
        }
        catch (InterruptedException exception)
        {
          throw new Error("Execute command "+exception);
        }
        break;
    }
  }

  public PipedInputStream getOutput()
  {
    return pipedInputStream;
  }

  public void setInput(PipedInputStream pipedInputStream)
  {
    this.pipedInputStream = pipedInputStream;
  }

  public void setOutput(PipedOutputStream pipedOutputStream)
  {
    this.pipedOutputStream = pipedOutputStream;
  }

  public void waitTerminated()
  {
    switch (type)
    {
      case JAVA:
        break;
      case EXTERNAL:
        try
        {
          process.waitFor();
        }
        catch (InterruptedException exception)
        {
exception.printStackTrace();
System.exit(1);
        }
        break;
    }
  }

  public int getExitCode()
  {
    switch (type)
    {
      case JAVA:
        break;
      case EXTERNAL:
        try
        {
          exitCode = process.waitFor();
        }
        catch (InterruptedException exception)
        {
exception.printStackTrace();
System.exit(1);
        }
        break;
    }

    return exitCode;
  }

  /** convert to string
   * @return string
   */
  public String toString()
  {
    String string;

    switch (type)
    {
      case NONE:
        string = "";
        break;
      case JAVA:
        string = "{ type=JAVA"+", class="+javaCommand+" }";
        break;
      case EXTERNAL:
        string = "{ type=EXTERNAL"+", process="+process+" }";
        break;
      default:
        string = "unknown";
        break;
    }

    return string;
  }
}

/* end of file */
