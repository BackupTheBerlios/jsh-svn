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

import java.util.LinkedList;
import java.util.Arrays;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import groovy.lang.GroovyRuntimeException;
import groovy.lang.MissingMethodException;
import org.codehaus.groovy.control.CompilerConfiguration;

/****************************** Classes ********************************/

/** action error
 */
class ActionError extends Error
{
  ActionError(String message)
  {
    super(message);
  }

  ActionError(String format, Object... args)
  {
    super(String.format(format,args));
  }
}


public class Action
{
  // --------------------------- constants --------------------------------
  enum Types
  {
    COMMAND,
    JAVACODE,
  };

  // --------------------------- variables --------------------------------

  private Types    type;
  private String   fileName;
  private int      line,column;

  private String[] command;

  private Binding  binding;
  private String   javaCode;

  // ------------------------ native functions ----------------------------

  // ---------------------------- methods ---------------------------------

  /** create new command action
   * @param command command string list
   * @param fileName file name
   * @param line line number
   * @param column column numbner
   */
  Action(LinkedList<String> command, String fileName, int line, int column)
  {
    this.type     = Types.COMMAND;
    this.fileName = fileName;
    this.line     = line;
    this.column   = column;
    this.command  = command.toArray(new String[0]);
  }

  /** create new Java code action
   * @param javaCode Java code
   * @param fileName file name
   * @param line line number
   * @param column column numbner
   */
  Action(Binding binding, String javaCode, String fileName, int line, int column)
  {
    this.type     = Types.JAVACODE;
    this.fileName = fileName;
    this.line     = line;
    this.column   = column;
    this.binding  = binding;
    this.javaCode = javaCode;
  }

  /** execute action
   * @return exit code
   */
  public int execute()
  {
//Dprintf.dprintf("execute "+toString());

    switch (type)
    {
      case COMMAND:
        // execute command
        try
        {
          Process process = Runtime.getRuntime().exec(command);

          InputStream stdout = process.getInputStream();
          InputStream stderr = process.getErrorStream();
          int ch;
          while ((ch = stdout.read()) != -1)
          {
            System.out.print((char)ch);
          }
          StringBuffer errorMessage = new StringBuffer();
          while ((ch = stderr.read()) != -1)
          {
            errorMessage.append((char)ch);
          }          

          int exitCode = process.waitFor();
          if (exitCode != 0)
          {
            System.err.println("ERROR: execute command '"+getCommandString()+"' fail at "+fileName+", line "+line+":"+column+" (exit code "+exitCode+")");
            System.err.println(errorMessage.toString());
          }
        }
        catch (IOException exception)
        {
          throw new ActionError("Execute command '%s' fail at %s, line %d:%d: %s",getCommandString(),fileName,line,column,exception.getMessage());
        }
        catch (Exception exception)
        {
Dprintf.dprintf("exception="+exception);
          return 128;
        }
        break;
      case JAVACODE:
        // execute Java code with Groovy
        try
        {
          CompilerConfiguration compilerConfiguration = new CompilerConfiguration(CompilerConfiguration.DEFAULT);
//          compilerConfiguration.setDebug(true);
//compilerConfiguration.setScriptBaseClass("MyScript");
//Dprintf.dprintf("base===========%s",compilerConfiguration.getScriptBaseClass());
    //      binding.setVariable("foo", new Integer(2));
          GroovyShell shell = new GroovyShell(binding,compilerConfiguration);

          Object value = shell.evaluate(javaCode,"jmake");
        }
        catch (MissingMethodException exception)
        {        
//exception.printStackTrace();

          // get Java line number
          int javaLine=0;
          for (StackTraceElement stackTraceElement : exception.getStackTrace())
          {
//Dprintf.dprintf("e=%s %s",stackTraceElement.getClassName(),stackTraceElement.getClassName());
            if (stackTraceElement.getClassName().equals("jmake"))
            {
              javaLine = stackTraceElement.getLineNumber();
            }
          }

          throw new ActionError("Cannot find Java method '%s' at %s, line %d",exception.getMethod(),fileName,line+javaLine-1);
        }
        catch (GroovyRuntimeException exception)
        {
//Dprintf.dprintf("exception="+exception);
          // get Java line number
          int javaLine=0;
          for (StackTraceElement stackTraceElement : exception.getStackTrace())
          {
//Dprintf.dprintf("e=%s %s",stackTraceElement.getClassName(),stackTraceElement.getClassName());
            if (stackTraceElement.getClassName().equals("jmake"))
            {
              javaLine = stackTraceElement.getLineNumber();
            }
          }

          throw new ActionError("Runtime exection '%s' at %s, line %d",exception.getMessage(),fileName,line+javaLine-1);
        }
        catch (Exception exception)
        {
Dprintf.dprintf("exception="+exception);
          return 128;
        }
        break;
    }

    return 0;
  }

  /** convert to string
   * @return string
   */
  public String toString()
  {
    switch (type)
    {
      case COMMAND:  return "{COMMAND: "+command+"}";
      case JAVACODE: return "{JAVACODE: "+javaCode+"}";
    }
    return "{unknown}";
  }

  /** get command as string
   * @return string
   */
  private String getCommandString()
  {
    StringBuffer buffer = new StringBuffer();

    for (String string : command)
    {
      if (buffer.length() > 0) buffer.append(' ');
      buffer.append(string);
    }

    return buffer.toString();
  }
}

/* end of file */
