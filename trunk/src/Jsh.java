/***********************************************************************\
*
* $Revision$
* $Date$
* $Author$
* Contents: jsh
* Systems: all
*
\***********************************************************************/

/****************************** Imports ********************************/
// base
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

import java.security.NoSuchAlgorithmException;
import java.security.InvalidKeyException;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.PrintWriter;
import java.io.Serializable;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Iterator;
import java.util.zip.Adler32;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.activation.MimetypesFileTypeMap;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

// graphics
import org.eclipse.swt.custom.CaretEvent;
import org.eclipse.swt.custom.CaretListener;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.LineStyleEvent;
import org.eclipse.swt.custom.LineStyleListener;
import org.eclipse.swt.custom.VerifyKeyListener;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.MouseTrackListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Widget;

// antlr lexer/parser
import org.antlr.runtime.ANTLRStringStream;
import org.antlr.runtime.ANTLRReaderStream;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.tree.CommonTree;
import org.antlr.runtime.tree.CommonTreeNodeStream;

import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.CommonToken;

import antlr.ASTFactory;

/****************************** Classes ********************************/

/** jsh
 */
public class Jsh
{
  /** status text
   */
  class StatusText
  {
    final Thread thread;
    final String text;

    /** create status text
     * @param format format
     * @param arguments optional arguments
     */
    StatusText(String format, Object... arguments)
    {
      this.thread = Thread.currentThread();
      this.text   = String.format(format,arguments);
    }

    /** convert to string
     * @return string
     */
    public String toString()
    {
      return text;
    }
  };

  // --------------------------- constants --------------------------------

  // exit codes
  public static int                  EXITCODE_OK             =   0;
  public static int                  EXITCODE_INTERNAL_ERROR = 127;

  // colors
  public static Color                COLOR_BLACK;
  public static Color                COLOR_WHITE;
  public static Color                COLOR_GREEN;
  public static Color                COLOR_DARK_RED;
  public static Color                COLOR_RED;
  public static Color                COLOR_DARK_BLUE;
  public static Color                COLOR_BLUE;
  public static Color                COLOR_DARK_YELLOW;
  public static Color                COLOR_YELLOW;
  public static Color                COLOR_DARK_GRAY;
  public static Color                COLOR_GRAY;
  public static Color                COLOR_MAGENTA;
  public static Color                COLOR_BACKGROUND;

  // images
  public static Image                IMAGE_DIRECTORY;
  public static Image                IMAGE_FILE;
  public static Image                IMAGE_LINK;
  public static Image                IMAGE_ARROW_UP;
  public static Image                IMAGE_ARROW_DOWN;
  public static Image                IMAGE_ARROW_LEFT;
  public static Image                IMAGE_ARROW_RIGHT;

  // fonts
  public static Font                 FONT_TEXT;

  // cursors
  public static Cursor               CURSOR_WAIT;

  // date/time format
  public static SimpleDateFormat     DATE_FORMAT     = new SimpleDateFormat(Settings.dateFormat);
  public static SimpleDateFormat     TIME_FORMAT     = new SimpleDateFormat(Settings.timeFormat);
  public static SimpleDateFormat     DATETIME_FORMAT = new SimpleDateFormat(Settings.dateTimeFormat);

  public static MimetypesFileTypeMap MIMETYPES_FILE_TYPE_MAP = new MimetypesFileTypeMap();

  // command line options
  private static final Option[] options =
  {
    new Option("--help",                       "-h",Options.Types.BOOLEAN, "helpFlag"),

    new Option("--debug",                      null,Options.Types.BOOLEAN, "debugFlag"),
    new Option("--verbose",                    "-v",Options.Types.INCREMENT,"verboseLevel"),

    new Option("--cvs-prune-empty-directories",null,Options.Types.BOOLEAN, "cvsPruneEmtpyDirectories"),

    // ignored
    new Option("--swing",                      null, Options.Types.BOOLEAN,null),
  };

  private final StyleRange STYLE_BEGIN_OF_LINE = new StyleRange();

  // --------------------------- variables --------------------------------
  private Display                           display;
  private Shell                             shell;

  private LinkedList<String>                textLines = new LinkedList<String>();
  private int                               visibleTextLine = 0;

  private LinkedList<String>                commandHistoryList = new LinkedList<String>();
  private int                               commandHistoryIndex = -1;

  private String                            commandLine = "";
  private int                               commandLineCaretIndex = 0;

  private StyledText                        widgetOutput;
  private Text                              widgetInput;
//  private Terminal                          widgetTerminal;
  private Button                            widgetButtonQuit;
  private Label                             widgetStatus;

  private PipedOutputStream                 pipedOutputStream;
  private PrintWriter                       outputStream;
  //private BufferedReader                    inputStream;
  private Thread                            outputThread;

  public static VariableMap variableMap = new VariableMap();
  public static TargetMap   targetMap   = new TargetMap();

  // ------------------------ native functions ----------------------------

  // ---------------------------- methods ---------------------------------

  /** print error to stderr
   * @param format format string
   * @param args optional arguments
   */
  public static void printError(String format, Object... args)
  {
    System.err.println("ERROR: "+String.format(format,args));
  }

  /** print internal error to stderr
   * @param format format string
   * @param args optional arguments
   */
  public static void printInternalError(String format, Object... args)
  {
    System.err.println("INTERNAL ERROR: "+String.format(format,args));
  }

  /** print internal error to stderr
   * @param throwable throwable
   * @param args optional arguments
   */
  public static void printInternalError(Throwable throwable, Object... args)
  {
    printInternalError(throwable.toString());
    if (Settings.debugFlag)
    {
      for (StackTraceElement stackTraceElement : throwable.getStackTrace())
      {
        System.err.println("  "+stackTraceElement);
      }
    }
  }

  /** print warning to stderr
   * @param format format string
   * @param args optional arguments
   */
  public static void printWarning(String format, Object... args)
  {
    System.err.print("Warning: "+String.format(format,args));
    if (Settings.debugFlag)
    {
      StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
      System.err.print(" at "+stackTrace[2].getFileName()+", "+stackTrace[2].getLineNumber());
    }
    System.err.println();
  }

  /** print verbose message
   * @param level verbose level
   * @param format format string
   * @param args optional arguments
   */
  public static void printVerbose(int level, int indent, String format, Object... args)
  {
    if (Settings.verboseLevel >= level)
    {
      System.out.print(StringUtils.repeat("  ",indent));
      System.out.println(String.format(format,args));
    }
  }

  /** print verbose message
   * @param level verbose level
   * @param format format string
   * @param args optional arguments
   */
  public static void printVerbose(int level, String format, Object... args)
  {
    printVerbose(level,0,format,args);
  }

  /** check if system is Windows system
   * @return TRUE iff Windows, FALSE otherwise
   */
  public static boolean isWindowsSystem()
  {
    String osName = System.getProperty("os.name").toLowerCase();

    return (osName.indexOf("win") >= 0);
  }

  public PipedOutputStream getOutput()
  {
    return pipedOutputStream;
  }

  /** print output
   * @param format format string
   */
  public void output(String format, Object... args)
  {
    outputStream.println(String.format(format,args)); outputStream.flush();
  }

  /** print error
   * @param format format string
   * @param args optional arguments
   */
  public void outputError(String format, Object... args)
  {
    outputStream.println("ERROR: "+String.format(format,args)); outputStream.flush();
  }

  /** parse .sh file
   * @param fileName .sh file name
   * @return evaluation tree
   */
  public ShellEval parse(String string)
  {
    ShellEval shellEval = null;

    // execute shell file
    try
    {
      // parse shell string
      ANTLRStringStream input = new ANTLRStringStream(string);
      ShellLexer shellLexer = new ShellLexer(input);

//      LinkedListTokenSource tokenSource = new LinkedListTokenSource(shellLexer);
//      LinkedListTokenStream tokenStream = new LinkedListTokenStream(tokenSource);
//      ShellParser shellParser = new ShellParser(tokenStream);
      CommonTokenStream tokenStream = new CommonTokenStream();
      tokenStream.setTokenSource(shellLexer);
//Dprintf.dprintf("------------------------");
//for (int i = 0; i < tokenStream.size(); i++) Dprintf.dprintf("token %s",tokenStream.get(i));
//for (CommonToken token : (java.util.List<CommonToken>)tokenStream.getTokens()) Dprintf.dprintf("  %d: %s",token.getType(),token.getText());

// wie tokens ausgeben
//for (Token token : tokenStream) Dprintf.dprintf("token=%s",token);
//???      tokenStream.reset();

      ShellParser shellParser = new ShellParser(tokenStream);

      shellParser.setInput(shellLexer,input);

//ASTFactory factory = new ASTFactory();
//factory.setASTNodeClass(CommonASTWithLines.class);

      ShellParser.start_return start = shellParser.start();
      CommonTree commonTree = (CommonTree)start.getTree();
//Dprintf.dprintf("commonTree: "+commonTree.toStringTree());
//Dprintf.dprintf("------------------------");

      // eval shell tree
      CommonTreeNodeStream nodes = new CommonTreeNodeStream(commonTree);
      shellEval = new ShellEval(nodes);
    }
    catch (RecognitionException exception)
    {
Dprintf.dprintf("exc "+exception);
    }

    return shellEval;
  }

  /** parse .sh file
   * @param fileName .sh file name
   * @return evaluation tree
   */
  public ShellEval parse(File file)
  {
    InputStreamReader shellInputStream;
    ShellEval         shellEval = null;

    // open shell file
    try
    {
      shellInputStream = new InputStreamReader(new FileInputStream(file));
    }
    catch (FileNotFoundException exception)
    {
      throw new Error(String.format("File '%s' not found",file.getName()));
    }
    catch (IOException exception)
    {
      throw new Error(String.format("i/o error reading file '%s'",file.getName()));
    }

    // execute shell file
    try
    {
      // parse shell file
      ANTLRReaderStream input = new ANTLRReaderStream(shellInputStream);
      ShellLexer shellLexer = new ShellLexer(input);
      LinkedListTokenSource tokenSource = new LinkedListTokenSource(shellLexer);
      LinkedListTokenStream tokenStream = new LinkedListTokenStream(tokenSource);
      ShellParser shellParser = new ShellParser(tokenStream);
      shellParser.setInput(shellLexer,input);

//ASTFactory factory = new ASTFactory();
//factory.setASTNodeClass(CommonASTWithLines.class);

      CommonTree commonTree = (CommonTree)shellParser.start().getTree();
  //Dprintf.dprintf("commonTree: "+commonTree.toStringTree());
  //Dprintf.dprintf("------------------------");

      // eval shell file tree
      CommonTreeNodeStream nodes = new CommonTreeNodeStream(commonTree);
      shellEval = new ShellEval(nodes);
    }
    catch (IOException exception)
    {
Dprintf.dprintf("exc "+exception);
    }
    catch (RecognitionException exception)
    {
Dprintf.dprintf("exc "+exception);
    }

    return shellEval;
  }

  /** main
   * @param args command line arguments
   */
  public static void main(String[] args)
  {
    new Jsh(args);
  }

  /** jsh main
   * @param args command line arguments
   */
  Jsh(String[] args)
  {
    int exitcode = 255;
    try
    {
      // load settings
      Settings.load();

      // parse arguments
      parseArguments(args);

      // init
      initAll();
commandHistoryList.add("date");
commandHistoryList.add("ls -la");
commandHistoryIndex = commandHistoryList.size();

      // run
      exitcode = run();

      // done
      doneAll();
    }
    catch (org.eclipse.swt.SWTException exception)
    {
Dprintf.dprintf("exception=%s",exception);
exception.printStackTrace();
      System.err.println("ERROR graphics: "+exception.getCause());
      if (Settings.debugFlag)
      {
        for (StackTraceElement stackTraceElement : exception.getStackTrace())
        {
          System.err.println("  "+stackTraceElement);
        }
      }
    }
    catch (AssertionError assertionError)
    {
      printInternalError(assertionError);
      System.err.println("Please report this assertion error to torsten.rupp@gmx.net.");
      if (Settings.debugFlag)
      {
        for (StackTraceElement stackTraceElement : assertionError.getStackTrace())
        {
          System.err.println("  "+stackTraceElement);
        }
      }
    }
    catch (InternalError error)
    {
      printInternalError(error);
      System.err.println("Please report this internal error to torsten.rupp@gmx.net.");
    }
    catch (Error error)
    {
      printInternalError(error);
      System.err.println("Please report this error to torsten.rupp@gmx.net.");
    }

    System.exit(exitcode);
  }

  /** set status text
   * @param format format string
   * @param arguments optional arguments
   */
  public void setStatusText(final String format, final Object... arguments)
  {
    // create status text
    final StatusText statusText = new StatusText(format,arguments);

    // show
    display.syncExec(new Runnable()
    {
      public void run()
      {
        if (!widgetStatus.isDisposed()) widgetStatus.setText(statusText.text);
        display.update();
      }
    });
  }

  /** clear status text
   */
  public void clearStatusText()
  {
    display.syncExec(new Runnable()
    {
      public void run()
      {
        if (!widgetStatus.isDisposed()) widgetStatus.setText("");
        display.update();
      }
    });
  }

  /** show error message in dialog
   */
  public void showError(final String message)
  {
    display.syncExec(new Runnable()
    {
      public void run()
      {
        Dialogs.error(shell,message);
      }
    });
  }

  /** renice i/o exception (removed java.io.IOExcpetion text)
   * @param exception i/o exception to renice
   * @return reniced exception
   */
  public static IOException reniceIOException(IOException exception)
  {
    final Pattern PATTERN = Pattern.compile("^(.*?)\\s*java.io.IOException: error=\\d+,\\s*(.*)$",Pattern.CASE_INSENSITIVE);

    Matcher matcher;
    if ((matcher = PATTERN.matcher(exception.getMessage())).matches())
    {
      exception = new IOException(matcher.group(1)+" "+matcher.group(2));
    }

    return exception;
  }

  //-----------------------------------------------------------------------

  /** static initializer
   */
  {
    // add known additional mime types
    MIMETYPES_FILE_TYPE_MAP.addMimeTypes("text/x-c c cpp c++");
    MIMETYPES_FILE_TYPE_MAP.addMimeTypes("text/x-java java");
  }

  /** print program usage
   */
  private void printUsage()
  {
    System.out.println("jsh usage: <options> [--] [<repository list name>]");
    System.out.println("");
    System.out.println("Options: ");
    System.out.println("");
    System.out.println("         -h|--help                      - print this help");
    System.out.println("         --debug                        - enable debug mode");
  }

  /** parse arguments
   * @param args arguments
   */
  private void parseArguments(String[] args)
  {
    // parse arguments
    int z = 0;
    boolean endOfOptions = false;
    while (z < args.length)
    {
      if      (!endOfOptions && args[z].equals("--"))
      {
        endOfOptions = true;
        z++;
      }
      else if (!endOfOptions && (args[z].startsWith("--") || args[z].startsWith("-")))
      {
        int i = Options.parse(options,args,z,Settings.class);
        if (i < 0)
        {
          throw new Error("Unknown option '"+args[z]+"'!");
        }
        z = i;
      }
      else
      {
//???
        z++;
      }
    }

    // help
    if (Settings.helpFlag)
    {
      printUsage();
      System.exit(EXITCODE_OK);
    }

    // check arguments
  }

  /** init display variables
   */
  private void initDisplay()
  {
    display = new Display();

    // get colors
    COLOR_BLACK       = display.getSystemColor(SWT.COLOR_BLACK);
    COLOR_WHITE       = display.getSystemColor(SWT.COLOR_WHITE);
    COLOR_GREEN       = display.getSystemColor(SWT.COLOR_GREEN);
    COLOR_DARK_RED    = display.getSystemColor(SWT.COLOR_DARK_RED);
    COLOR_RED         = display.getSystemColor(SWT.COLOR_RED);
    COLOR_DARK_BLUE   = display.getSystemColor(SWT.COLOR_DARK_BLUE);
    COLOR_BLUE        = display.getSystemColor(SWT.COLOR_BLUE);
    COLOR_DARK_YELLOW = display.getSystemColor(SWT.COLOR_DARK_YELLOW);
    COLOR_YELLOW      = display.getSystemColor(SWT.COLOR_YELLOW);
    COLOR_DARK_GRAY   = display.getSystemColor(SWT.COLOR_DARK_GRAY);
    COLOR_GRAY        = display.getSystemColor(SWT.COLOR_GRAY);
    COLOR_MAGENTA     = new Color(null,0xFF,0xA0,0xA0);
    COLOR_BACKGROUND  = display.getSystemColor(SWT.COLOR_WIDGET_BACKGROUND);

    // get images
    IMAGE_DIRECTORY   = Widgets.loadImage(display,"directory.png");
    IMAGE_FILE        = Widgets.loadImage(display,"file.png");
    IMAGE_LINK        = Widgets.loadImage(display,"link.png");
    IMAGE_ARROW_UP    = Widgets.loadImage(display,"arrow-up.png");
    IMAGE_ARROW_DOWN  = Widgets.loadImage(display,"arrow-down.png");
    IMAGE_ARROW_LEFT  = Widgets.loadImage(display,"arrow-left.png");
    IMAGE_ARROW_RIGHT = Widgets.loadImage(display,"arrow-right.png");

    // fonts
    FONT_TEXT         = Widgets.newFont(display,Settings.fontText);

    // get cursors
    CURSOR_WAIT       = new Cursor(display,SWT.CURSOR_WAIT);
  }

  /** create main window
   */
  private void createWindow()
  {
    Pane      pane;
    Composite composite;
    Button    button;
    Label     label;

    // create window
    shell = new Shell(display,SWT.SHELL_TRIM);
    shell.setText("jsh");
    shell.setLayout(new TableLayout(new double[]{0.8,0.2,0.0,0.0},1.0));

    // create output widget
    pane = Widgets.newPane(shell,SWT.HORIZONTAL);
    pane.setLayout(new TableLayout(1.0,1.0,4,0));
    Widgets.layout(pane,0,0,TableLayoutData.NSWE);
    {
      widgetOutput = Widgets.newStyledText(pane,SWT.BORDER|SWT.FULL_SELECTION|SWT.MULTI|SWT.WRAP|SWT.V_SCROLL|SWT.READ_ONLY);
      widgetOutput.setFont(Jsh.FONT_TEXT);
      widgetOutput.setBackground(COLOR_GRAY);
      Widgets.layout(widgetOutput,0,0,TableLayoutData.NSWE);
    }

    // create input widget
    pane = Widgets.newPane(shell,SWT.HORIZONTAL,pane);
    pane.setLayout(new TableLayout(1.0,1.0,4,0));
    Widgets.layout(pane,1,0,TableLayoutData.NSWE);
    {
      widgetInput = Widgets.newText(pane,SWT.BORDER|SWT.FULL_SELECTION|SWT.MULTI|SWT.WRAP|SWT.V_SCROLL);
      widgetInput.setFont(Jsh.FONT_TEXT);
      Widgets.layout(widgetInput,0,0,TableLayoutData.NSWE);
    }

    // create buttons
    composite = Widgets.newComposite(shell);
    composite.setLayout(new TableLayout(1.0,1.0,2));
    Widgets.layout(composite,2,0,TableLayoutData.WE);
    {
    }

    // create status line
    composite = Widgets.newComposite(shell);
    composite.setLayout(new TableLayout(1.0,new double[]{0.0,1.0},2));
    Widgets.layout(composite,3,0,TableLayoutData.WE);
    {
      label = Widgets.newLabel(composite,"Status:");
      Widgets.layout(label,0,0,TableLayoutData.W);

      widgetStatus = Widgets.newView(composite,"",SWT.NONE);
      Widgets.layout(widgetStatus,0,1,TableLayoutData.WE);
    }
  }

  /** create menu
   */
  private void createMenu()
  {
    Menu     menuBar;
    Menu     menu,subMenu;
    MenuItem menuItem;

    // create menu
    menuBar = Widgets.newMenuBar(shell);

    menu = Widgets.addMenu(menuBar,"Program");
    {
      menuItem = Widgets.addMenuItem(menu,"New...");
menuItem.setEnabled(false);
      menuItem.addSelectionListener(new SelectionListener()
      {
        public void widgetDefaultSelected(SelectionEvent selectionEvent)
        {
        }
        public void widgetSelected(SelectionEvent selectionEvent)
        {
Dprintf.dprintf("");
//          newRepository(rootPath);
        }
      });

      Widgets.addMenuSeparator(menu);

      menuItem = Widgets.addMenuItem(menu,"Quit...",SWT.CTRL+'Q');
      menuItem.addSelectionListener(new SelectionListener()
      {
        public void widgetDefaultSelected(SelectionEvent selectionEvent)
        {
        }
        public void widgetSelected(SelectionEvent selectionEvent)
        {
          // send close-event to shell
          Widgets.notify(shell,SWT.Close,0);
        }
      });
    }

//    menu = Widgets.addMenu(menuBar,"Options");
//    {
//    }

    menu = Widgets.addMenu(menuBar,"Help");
    {
      menuItem = Widgets.addMenuItem(menu,"About");
      menuItem.addSelectionListener(new SelectionListener()
      {
        public void widgetDefaultSelected(SelectionEvent selectionEvent)
        {
        }
        public void widgetSelected(SelectionEvent selectionEvent)
        {
          Dialogs.info(shell,"About","jsh "+Config.VERSION_MAJOR+"."+Config.VERSION_MINOR+" (revision "+Config.VERSION_REVISION+").\n\nWritten by Torsten Rupp.");
        }
      });
    }

    if (Settings.debugFlag)
    {
      menu = Widgets.addMenu(menuBar,"Debug");
      {
/*
menuItem = Widgets.addMenuItem(menu,"XXXXX");
menuItem.addSelectionListener(new SelectionListener()
{
  public void widgetDefaultSelected(SelectionEvent selectionEvent)
  {
  }
  public void widgetSelected(SelectionEvent selectionEvent)
  {
  }
});
*/
      }
    }
  }

  /** create event handlers
   */
  private void createEventHandlers()
  {
    widgetOutput.addMouseListener(new MouseListener()
    {
      public void mouseDoubleClick(MouseEvent mouseEvent)
      {
//        mouseEvent.doit = false;
Dprintf.dprintf("");

//        commandLineCaretIndex = widgetTerminal.getCaretOffset() - commandLineStartOffset;
      }

      public void mouseDown(final MouseEvent mouseEvent)
      {
Dprintf.dprintf("mouseEvent=%s",mouseEvent);
/*
        int offset = widgetTerminal.getCaretOffset();

        widgetTerminal.setSelection(offset,offset);

        if (offset < commandLineStartOffset)
        {
          widgetTerminal.setCaretOffset(commandLineStartOffset + commandLineCaretIndex);
        }

        commandLineCaretIndex = widgetTerminal.getCaretOffset() - commandLineStartOffset;
*/
        String selectedText = widgetOutput.getSelectionText();
Dprintf.dprintf("selectedText=%s",selectedText);
for (int i : widgetOutput.getSelectionRanges()) Dprintf.dprintf("i=%d",i);
      }

      public void mouseUp(final MouseEvent mouseEvent)
      {

//        commandLineCaretIndex = widgetTerminal.getCaretOffset() - commandLineStartOffset;
      }
    });
/*
    widgetTerminal.addSelectionListener(new SelectionListener()
    {
      public void widgetDefaultSelected(SelectionEvent selectionEvent)
      {
      }
      public void widgetSelected(SelectionEvent selectionEvent)
      {
      }
    });
*/
/*
    widgetTerminal.addCaretListener(new CaretListener()
    {
      public void caretMoved(CaretEvent caretEvent)
      {
//Dprintf.dprintf("caretEvent=%s %d",caretEvent,caretEvent.caretOffset);
//Dprintf.dprintf("caret=%d",widgetTerminal.getCaretOffset());
//        if (caretEvent.caretOffset < commandLineStartOffset) widgetTerminal.setCaretOffset(commandLineStartOffset);
      }
    });
*/
    widgetInput.addKeyListener(new KeyListener()
    {
      public void keyPressed(KeyEvent keyEvent)
      {
/*
int offset = widgetTerminal.getCaretOffset();
if (offset < widgetTerminal.getCharCount())
{
Dprintf.dprintf("offset=%d",offset);
StyleRange style = widgetTerminal.getStyleRangeAtOffset(offset);
Dprintf.dprintf("style=%s",style);
}
*/
        if      (   (keyEvent.keyCode == SWT.CR)
                 || (keyEvent.keyCode == SWT.KEYPAD_CR)
                )
        {
          String commandLine = fetchCommandLine();
Dprintf.dprintf("");

          addCommandLine(commandLine);
          execute(commandLine);
          addHistory(commandLine);

          keyEvent.doit = false;
        }
        else if (keyEvent.keyCode == SWT.HOME)
        {
Dprintf.dprintf("");
//          widgetTerminal.setCaretOffset(commandLineStartOffset);
          keyEvent.doit = false;
        }
        else if (keyEvent.keyCode == SWT.END)
        {
Dprintf.dprintf("");
//          int length = widgetTerminal.getCharCount();

//          widgetTerminal.setCaretOffset(length);
          keyEvent.doit = false;
        }
        else if (keyEvent.keyCode == SWT.ARROW_UP)
        {
Dprintf.dprintf("commandHistoryIndex=%d",commandHistoryIndex);
          //commandHistortList
          if (commandHistoryIndex > 0)
          {
            commandHistoryIndex--;
            setCommandLine(commandHistoryList.get(commandHistoryIndex));
          }

          keyEvent.doit = false;
        }
        else if (keyEvent.keyCode == SWT.ARROW_DOWN)
        {
Dprintf.dprintf("");
          //commandHistortList
          keyEvent.doit = false;
        }
        else if (keyEvent.keyCode == SWT.PAGE_UP)
        {
Dprintf.dprintf("");
          //commandHistortList
          keyEvent.doit = false;
        }
        else if (keyEvent.keyCode == SWT.PAGE_DOWN)
        {
Dprintf.dprintf("");
          //commandHistortList
          keyEvent.doit = false;
        }
//        commandLineCaretIndex = widgetTerminal.getCaretOffset() - commandLineStartOffset;
      }

      public void keyReleased(KeyEvent keyEvent)
      {
//Dprintf.dprintf("");
      }
    });
  }

  private void createConsoleOutput()
  {
    try
    {
      pipedOutputStream = new PipedOutputStream();
      outputStream      = new PrintWriter(pipedOutputStream);

//outputStream.println("XXXXXXXXXXXXXXXXXXXXXX\n");
//for (char c : "XXXXXXXXXXXXXXXXXXXXXX\n\n".toCharArray()) pipedOutputStream.write(c);

      PipedInputStream     pipedInputStream  = new PipedInputStream(pipedOutputStream);
      final BufferedReader inputStream       = new BufferedReader(new InputStreamReader(pipedInputStream));
      outputThread = new Thread()
      {
        public void run()
        {
          try
          {
            String line;
            while ((line = inputStream.readLine()) != null)
            {
//Dprintf.dprintf("console %s",line);
              final String s = line;
              display.asyncExec(new Runnable()
              {
                public void run()
                {
                  addTextLine(s);
                }
              });
/**/
            }
          }
          catch (IOException exception)
          {
            Dprintf.dprintf("exception=%s",exception);
          }
        }
      };
      outputThread.start();
    }
    catch (IOException exception)
    {
Dprintf.dprintf("exception=%s",exception);
    }
  }

  /** init all
   */
  private void initAll()
  {
    // init display
    initDisplay();

    // open main window
    createWindow();
    createMenu();
    createEventHandlers();
    createConsoleOutput();

    clearText();
    clearCommandLine();
  }

  /** done all
   */
  private void doneAll()
  {
    // shutdown running background tasks
//    Background.executorService.shutdownNow();
  }

  /** run application
   * @return exit code
   */
  private int run()
  {
    final int[] result = new int[1];

    // set window size, manage window
    shell.setSize(Settings.geometryMain.x,Settings.geometryMain.y);
    shell.open();

    // listener
    shell.addListener(SWT.Resize,new Listener()
    {
      public void handleEvent(Event event)
      {
        Settings.geometryMain = shell.getSize();
      }
    });
    shell.addListener(SWT.Close,new Listener()
    {
      public void handleEvent(Event event)
      {
/*
        // save settings
        boolean saveSettings = true;
        if (Settings.isFileModified())
        {
          switch (Dialogs.select(shell,"Confirmation","Settings were modified externally.",new String[]{"Overwrite","Just quit","Cancel"},0))
          {
            case 0:
              break;
            case 1:
              saveSettings = false;
              break;
            case 2:
              event.doit = false;
              return;
          }
        }
        if (saveSettings)
        {
          Settings.save();
        }
*/

        // store exitcode
        result[0] = event.index;

        // close
        shell.dispose();
      }
    });


//execute("");
////execute("\n");
////execute("\n\n");
////execute("ls\n");

//execute("ls /tmp");
//execute("ls -la /tmp");
//execute("ls -la /tmp > /dev/null");
//execute("cat abc");
//execute("cat -");
//execute("dd -of xxx");
//execute("date | cat -");
//Dprintf.dprintf("");
//System.exit(1);

    // SWT event loop
    while (!shell.isDisposed())
    {
//System.err.print(".");
      if (!display.readAndDispatch())
      {
        display.sleep();
      }
    }

    return result[0];
  }

  private void refreshText()
  {
    StringBuilder buffer = new StringBuilder();
    final String EOL = widgetOutput.getLineDelimiter();
    for (String line : textLines)
    {
      buffer.append(line); buffer.append(EOL);
    }
    widgetOutput.setText(buffer.toString());

int o = widgetOutput.getOffsetAtLine(visibleTextLine);
widgetOutput.setTopIndex(o);
  }

  private void showLine(int lineNb)
  {
int o = widgetOutput.getOffsetAtLine(lineNb);
widgetOutput.setTopIndex(o);
  }

  private void clearText()
  {
    textLines.clear();
    visibleTextLine = 0;
    refreshText();
  }

  private void addTextLine(final String line)
  {
    textLines.add(line);
    while (textLines.size() > Settings.historyLength)
    {
      textLines.remove(0);
      String s = widgetOutput.getLine(0);
      widgetOutput.replaceTextRange(0,s.length()+1,"");
    }
    visibleTextLine = textLines.size()-1;

    widgetOutput.append(line+widgetOutput.getLineDelimiter());
    showLine(visibleTextLine);
  }

  private void addCommandLine(String line)
  {
    textLines.add(Settings.prompt+line);
    while (textLines.size() > Settings.historyLength)
    {
      textLines.remove(0);
      line = widgetOutput.getLine(0);
      widgetOutput.replaceTextRange(0,line.length()+1,"");
    }
    visibleTextLine = textLines.size()-1;

    widgetOutput.append(Settings.prompt+line+widgetOutput.getLineDelimiter());
    showLine(visibleTextLine);
  }

  private String getCommandLine()
  {
    return widgetInput.getText();
  }

  private void setCommandLine(String commandLine)
  {
    widgetInput.setText(commandLine);
  }

  private void clearCommandLine()
  {
    widgetInput.setText("");
    Widgets.setFocus(widgetInput);
  }

  private String fetchCommandLine()
  {
    String commandLine = getCommandLine();
    clearCommandLine();

    return commandLine;
  }

  private void execute(String commandLine)
  {
    if (!commandLine.trim().isEmpty())
    {
Dprintf.dprintf("ececute : %s",commandLine);
      try
      {
        ShellEval shellEval = parse(commandLine);
        shellEval.start(this);
      }
      catch (RecognitionException exception)
      {
Dprintf.dprintf("");
exception.printStackTrace();
      }
    }
/*
    if (!commandLine.trim().isEmpty())
    {
      Command command = new Command(commandLine);

      Exec exec = null;
      try
      {
        exec = new Exec(command);
        String line;
        while (   !exec.eofStdout()
               || !exec.eofStderr()
              )
        {
          line = exec.getStdout();
          if (line != null)
          {
            addTextLine(line);
          }

          line = exec.getStderr();
          if (line != null)
          {
            addTextLine(line);
          }
        }
      }
      catch (IOException exception)
      {
        addTextLine("ERROR: "+reniceIOException(exception).getMessage());
      }
      catch (Exception exception)
      {
        addTextLine("ERROR: "+exception.getMessage());
      }
      finally
      {
        if (exec != null) exec.done();
      }
    }
*/
  }

  private void addHistory(String commandLine)
  {
    commandHistoryList.add(commandLine);
    commandHistoryIndex = commandHistoryList.size();
  }
}

/* end of file */
