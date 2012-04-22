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

  // Java command
  private JavaCommand       javaCommand;

  // external program
  private Process           process;
  private int               exitCode;
  private OutputStream      stdin  = null;
  private InputStream       stdout = null;
  private InputStream       stderr = null;

  // ------------------------ native functions ----------------------------

  // ---------------------------- methods ---------------------------------

  /** create new Java command
   * @param javaCommandClass Java command class
   * @param argumentList argument list
   */
  Command(Class javaCommandClass, List<String> argumentList)
    throws CommandException
  {
    try
    {
      // instanciate Java command
      this.type        = Types.JAVA;
      this.javaCommand = (JavaCommand)javaCommandClass.getDeclaredConstructor().newInstance();
      this.javaCommand.setArguments(argumentList);
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
  }

  /** create new external command
   * @param name command name
   * @param argumentList argument list
   */
  Command(String name, List<String> argumentList)
    throws CommandException
  {
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

      // run external program
      this.type    = Types.EXTERNAL;
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
