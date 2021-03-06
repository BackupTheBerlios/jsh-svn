/***********************************************************************\
*
* $Revision$
* $Date$
* $Author$
* Contents: execute external command
* Systems: all
*
\***********************************************************************/

/****************************** Imports ********************************/
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Stack;

/****************************** Classes ********************************/

/** execute external command
 */
class Exec
{
  /** external command
   */
  class Command
  {
    /** hidden string class
     */
    class Hidden
    {
      private String string;

      /** create hidden string
       * @param string string
       */
      Hidden(String string)
      {
        this.string = string;
      }

      /** convert to string
       * @return string
       */
      public String toString()
      {
        return string;
      }
    };

    // --------------------------- constants --------------------------------

    // --------------------------- variables --------------------------------
    private ArrayList<String> commandArray;           // command line arguments

    // ------------------------ native functions ----------------------------

    // ---------------------------- methods ---------------------------------

    /** create external command
     */
    Command()
    {
      commandArray = new ArrayList<String>();
    }

    /** create external command
     */
    Command(String commandLine)
    {
      commandArray = new ArrayList<String>();
      for (String string : StringUtils.split(commandLine,StringUtils.WHITE_SPACES,StringUtils.QUOTE_CHARS))
      {
        commandArray.add(string);
      }
    }

    /** clear command array
     */
    public void clear()
    {
      commandArray.clear();
    }

    /** append arguments to command array
     * @param arguments strings to append
     */
    public void append(Object... arguments)
    {
      for (Object object : arguments)
      {
        commandArray.add(object.toString());
      }
    }

    /** append arguments to command array
     * @param strings strings to append
     */
    public void append(String[] strings)
    {
      for (String string : strings)
      {
        commandArray.add(string);
      }
    }

    /** get command array
     * @return command array
     */
    public String[] getCommandArray()
    {
      return commandArray.toArray(new String[commandArray.size()]);
    }

    /** create hidden argument
     * @param string argument
     * @return hidden argument
     */
    public Hidden hidden(String string)
    {
      return new Hidden(string);
    }

    /** convert data to string
     * @return string
     */
    public String toString()
    {
      StringBuilder buffer = new StringBuilder();
      for (String string : commandArray)
      {
        if (buffer.length() > 0) buffer.append(' ');
        if (string.isEmpty() || string.indexOf(' ') >= 0)
        {
          buffer.append(StringUtils.escape(string,'\''));
        }
        else
        {
          buffer.append(string);
        }
      }

      return buffer.toString();
    }
  }

  // --------------------------- constants --------------------------------

  // --------------------------- variables --------------------------------
  private static HashSet<Process> processHash = new HashSet<Process>();

  private final BufferedWriter    stdin;
  private final BufferedReader    stdout;
  private final BufferedReader    stderr;
  private final Stack<String>     stdoutStack = new Stack<String>();
  private final Stack<String>     stderrStack = new Stack<String>();
  private final DataInputStream   stdoutBinary;

  private Process                 process;
  private int                     pid;
  private ArrayList<String>       stdoutList = new ArrayList<String>();
  private ArrayList<String>       stderrList = new ArrayList<String>();
  private int                     exitCode = -1;

  // ------------------------ native functions ----------------------------

  // ---------------------------- methods ---------------------------------

  /** execute external command
   * @param path working directory or null
   * @param subDirectory working subdirectory or null
   * @param commandArray command line array
   * @param binaryFlag true to read stdout in binary mode
   */
  public Exec(String path, String subDirectory, String[] commandArray, boolean binaryFlag)
    throws IOException
  {
    if (Settings.debugFlag)
    {
      System.err.println("DEBUG execute "+path+((subDirectory != null) ? File.separator+subDirectory : "")+": "+StringUtils.join(commandArray));
    }

    // get working directory
    File workingDirectory;
    if      ((path != null) && (subDirectory != null)) workingDirectory = new File(path,subDirectory);
    else if (path != null)                             workingDirectory = new File(path);
    else                                               workingDirectory = null;

    // start process
    process = Runtime.getRuntime().exec(commandArray,null,workingDirectory);
    processHash.add(process);

    // get stdin, stdout, stderr
    stdin = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));
    if (binaryFlag)
    {
      stdout       = null;
      stdoutBinary = new DataInputStream(process.getInputStream());
    }
    else
    {
      stdout       = new BufferedReader(new InputStreamReader(process.getInputStream()));
      stdoutBinary = null;
    }
    stderr = new BufferedReader(new InputStreamReader(process.getErrorStream()));
  }

  /** execute external command
   * @param path working directory or null
   * @param subDirectory working subdirectory or null
   * @param command command to execute
   * @param binaryFlag true to read stdout in binary mode
   */
  public Exec(String path, String subDirectory, Command command, boolean binaryFlag)
    throws IOException
  {
    this(path,subDirectory,command.getCommandArray(),binaryFlag);
  }

  /** execute external command
   * @param path working directory or null
   * @param command command to execute
   * @param binaryFlag true to read stdout in binary mode
   */
  public Exec(String path, Command command, boolean binaryFlag)
    throws IOException
  {
    this(path,null,command,binaryFlag);
  }

  /** execute external command
   * @param path working directory or null
   * @param subDirectory working subdirectory or null
   * @param command command to execute
   */
  public Exec(String path, String subDirectory, Command command)
    throws IOException
  {
    this(path,subDirectory,command,false);
  }

  /** execute external command
   * @param path working directory or null
   * @param command command to execute
   */
  public Exec(String path, Command command)
    throws IOException
  {
    this(path,command,false);
  }

  /** execute external command
   * @param command command to execute
   */
  public Exec(Command command)
    throws IOException
  {
    this(null,command);
  }

  /** done execute command
   * @return exit value or -1
   */
  public int done()
  {
    return waitFor();
  }

  /** get next line from stdout
   * @return line or null
   */
  public String getStdout()
  {
    String line = null;

    if (stdoutStack.isEmpty())
    {
      if (stdout != null)
      {
        try
        {
          line = stdout.readLine();
        }
        catch (IOException exception)
        {
          /* ignored => no input */
        }
      }
    }
    else
    {
      line = stdoutStack.pop();
    }

    return line;
  }

  /** push back line from stdout
   * @param  line
   */
  public void ungetStdout(String line)
  {
    if (line != null) stdoutStack.push(line);
  }

  /** peek  (do not get) next line from stdout
   * @return line or null
   */
  public String peekStdout()
  {
    String line = getStdout();
    ungetStdout(line);
    return line;
  }

  /** check if EOF of stdout
   * @return true iff EOF
   */
  public boolean eofStdout()
  {
    String line = getStdout();
    ungetStdout(line);
    return line == null;
  }

  /** get next line from stderr
   * @return line or null
   */
  public String getStderr()
  {
    String line = null;

    if (stderrStack.isEmpty())
    {
      try
      {
        line = stderr.readLine();
      }
      catch (IOException exception)
      {
        /* ignored => no input */
      }
    }
    else
    {
      line = stderrStack.pop();
    }

    return line;
  }

  /** push back line from stderr
   * @param  line
   */
  public void ungetStderr(String line)
  {
    if (line != null) stderrStack.push(line);
  }

  /** peek (do not get) next line from stderr
   * @return line or null
   */
  public String peekStderr()
  {
    String line = getStderr();
    ungetStderr(line);
    return line;
  }

  /** check if EOF of stdout
   * @return true iff EOF
   */
  public boolean eofStderr()
  {
    String line = getStderr();
    ungetStderr(line);
    return line == null;
  }

  /** read data from stdout
   * @return line or null
   */
  public int readStdout(byte[] buffer)
  {
    int n = 0;

    if (stdoutBinary != null)
    {
      try
      {
        n = stdoutBinary.read(buffer);
      }
      catch (IOException exception)
      {
        /* ignored => no input */
      }
    }

    return n;
  }

  /** wait until exec terminated
   * @return exit value or -1
   */
  public int waitFor()
  {
    // store stdout, stderr
    String line;
    while ((line = getStdout()) != null)
    {
      stdoutList.add(line);
    }
    while ((line = getStderr()) != null)
    {
      stderrList.add(line);
    }

    // wait for process
    try
    {
      exitCode = process.waitFor();
      processHash.remove(process);
    }
    catch (InterruptedException exception)
    {
      exitCode = -1;
    }

    return exitCode;
  }

  /** check if external command terminated
   * @return true iff external command terminated, false otherwise
   */
  public boolean terminated()
  {
    try
    {
      process.exitValue();
      return true;
    }
    catch (IllegalThreadStateException exception)
    {
      return false;
    }
  }

  /** get extended error message lines
   * @return message lines array
   */
  public String[] getExtendedErrorMessage()
  {
    ArrayList<String> extendedErrorMessageList = new ArrayList<String>();

    extendedErrorMessageList.add("Stderr:");
    for (String line : stderrList)
    {
      extendedErrorMessageList.add(line);
    }

    return extendedErrorMessageList.toArray(new String[extendedErrorMessageList.size()]);
  }

  /** print stdout output to stdout
   */
  public void printStdout()
  {
    if (stdoutList.size() > 0)
    {
      System.out.println("Stdout:");
      System.out.println("");
      for (String string : stdoutList)
      {
        System.out.println(string);
      }
    }
  }

  /** print stderr output to stdout
   */
  public void printStderr()
  {
    if (stderrList.size() > 0)
    {
      System.out.println("Stderr:");
      System.out.println("");
      for (String string : stderrList)
      {
        System.out.println(string);
      }
    }
  }

  //-----------------------------------------------------------------------
}

/* end of file */
