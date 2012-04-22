/***********************************************************************\
*
* $Revision$
* $Date$
* $Author$
* Contents: terminal widget
* Systems: all
*
\***********************************************************************/

/****************************** Imports ********************************/
import java.util.LinkedList;

import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Caret;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Composite;

/****************************** Classes ********************************/

/** terminal widget
 */
class Terminal extends Canvas
{
  // --------------------------- constants --------------------------------
  private final Font DEFAULT_FONT;

  private final Color COLOR_BACKGROUND;
  private final Color COLOR_TEXT;
  private final Color COLOR_CARET;

  private final int MARGIN_LEFT = 4;
  private final int MARGIN_TOP  = 4;

  // --------------------------- variables --------------------------------
  private final Display      display;
  private GC                 gc;

  private Image              caretImage;
  private Caret              caret;

  private Point              fontSize;
  private Point              textSize;
  private String             text;

  private String             prompt             = "> ";
  private LinkedList<String> historyList        = new LinkedList<String>();
  private LinkedList<String> commandHistoryList = new LinkedList<String>();
  private String             commandLine        = "";
  private int                caretIndex         = 0;

  // ------------------------ native functions ----------------------------

  // ---------------------------- methods ---------------------------------

  /** create progress bar
   * @param composite parent composite widget
   * @param style style flags
   */
  Terminal(Composite composite, int style)
  {
    super(composite,style);

    display = composite.getDisplay();

    this.DEFAULT_FONT     = new Font(display, "Monospace", 12, SWT.NONE);

    this.COLOR_BACKGROUND = getDisplay().getSystemColor(SWT.COLOR_WHITE);
    this.COLOR_TEXT       = getDisplay().getSystemColor(SWT.COLOR_BLACK);
    this.COLOR_CARET      = getDisplay().getSystemColor(SWT.COLOR_BLACK);

    gc = new GC(this);
    setFont(DEFAULT_FONT);

    caretImage = getCaretImage();
    caret = new Caret(this,SWT.NONE);
    caret.setImage(caretImage);

    addDisposeListener(new DisposeListener()
    {
      public void widgetDisposed(DisposeEvent disposeEvent)
      {
        Terminal.this.dispose(disposeEvent);
      }
    });
    addPaintListener(new PaintListener()
    {
      public void paintControl(PaintEvent paintEvent)
      {
        Terminal.this.paint(paintEvent);
      }
    });
    addKeyListener(new KeyListener()
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
Dprintf.dprintf("");
        }
        else if (keyEvent.keyCode == SWT.HOME)
        {
Dprintf.dprintf("");
        }
        else if (keyEvent.keyCode == SWT.END)
        {
Dprintf.dprintf("");
        }
        else if (keyEvent.keyCode == SWT.ARROW_LEFT)
        {
Dprintf.dprintf("");
          if (caretIndex > 0)
          {
            caretIndex--;
            Rectangle caretBounds = indexToBounds(caretIndex);
            caret.setLocation(caretBounds.x, caretBounds.y);
          }
        }
        else if (keyEvent.keyCode == SWT.ARROW_RIGHT)
        {
Dprintf.dprintf("");
          if (caretIndex <= commandLine.length())
          {
            caretIndex++;
            Rectangle caretBounds = indexToBounds(caretIndex);
            caret.setLocation(caretBounds.x, caretBounds.y);
          }
        }
        else if (keyEvent.keyCode == SWT.ARROW_UP)
        {
Dprintf.dprintf("");
        }
        else if (keyEvent.keyCode == SWT.ARROW_DOWN)
        {
Dprintf.dprintf("");
        }
        else if (keyEvent.keyCode == SWT.PAGE_UP)
        {
Dprintf.dprintf("");
        }
        else if (keyEvent.keyCode == SWT.PAGE_DOWN)
        {
Dprintf.dprintf("");
        }
        else
        {
Dprintf.dprintf("");
          commandLine = commandLine+keyEvent.character;
          redrawCommandLine();
        }
      }

      public void keyReleased(KeyEvent keyEvent)
      {
Dprintf.dprintf("");
      }
    });

addHistory("test1");
addHistory("test2");
addHistory("test3");
  }

  /** create progress bar
   * @param composite parent composite widget
   */
  Terminal(Composite composite)
  {
    this(composite,SWT.NONE);
  }

  /** compute size of pane
   * @param wHint,hHint width/height hint
   * @param changed TRUE iff no cache values should be used
   * @return size
   */
  public Point xcomputeSize(int wHint, int hHint, boolean changed)
  {
    GC gc;
    int width,height;

    width  = 0;
    height = 0;

//    width  = 2+textSize.x+2;
//    height = 2+textSize.y+2;
    if (wHint != SWT.DEFAULT) width  = wHint;
    if (hHint != SWT.DEFAULT) height = hHint;

    return new Point(width,height);
  }

  @Override
  public void setFont(Font font)
  {
    super.setFont(font);

    gc.setFont(font);
//Dprintf.dprintf("font=%s",font);
//for (FontData f : font.getFontData()) Dprintf.dprintf("f=%s",f);
//Dprintf.dprintf("gc.textExtent(w)=%s",gc.textExtent("w"));

    assert gc.getCharWidth('w') == gc.getCharWidth('.') : "Font is not monospace";
    fontSize = gc.textExtent("w");
  }

  public String getPrompt()
  {
    return prompt;
  }

  public void setPrompt(String prompt)
  {
    this.prompt = prompt;
//    repaint();
  }

  public String[] getHistory()
  {
    return historyList.toArray(new String[historyList.size()]);
  }

  public void setHistory(String[] history)
  {
    historyList.clear();
    for (String line : history)
    {
      historyList.add(line);
    }
  }

  public void addHistory(String line)
  {
    historyList.add(line);
  }

  public String[] getCommandHistory()
  {
    return commandHistoryList.toArray(new String[commandHistoryList.size()]);
  }

  public void setCommandHistory(String[] commandHistory)
  {
    commandHistoryList.clear();
    for (String command : commandHistory)
    {
      commandHistoryList.add(command);
    }
  }

  /** set progress value
   * @param n value
   */
  public void setSelection(double n)
  {
  }

  //-----------------------------------------------------------------------

  private Image getCaretImage()
  {
    Image image = new Image (display, fontSize.x, fontSize.y);

    GC gc = new GC(image);
    {
      gc.setBackground(COLOR_CARET);
      gc.fillRectangle(0, 0, fontSize.x, fontSize.y);
      gc.setForeground(COLOR_BACKGROUND);
      gc.drawRectangle(0, 0, fontSize.x-1, fontSize.y-1);
    }
    gc.dispose();

    return image;
  }

  /** free allocated resources
   * @param disposeEvent dispose event
   */
  private void dispose(DisposeEvent disposeEvent)
  {
    COLOR_BACKGROUND.dispose();
    COLOR_TEXT.dispose();
  }

  private Rectangle indexToBounds(int index)
  {
    Rectangle rectangle = new Rectangle(index*fontSize.x,0,fontSize.x,fontSize.y);

    return rectangle;
  }

  private void redrawText()
  {
    Rectangle bounds;
    int       x,y,w,h;
    int       xt,yt;

    caret.setVisible(false);

    bounds = getBounds();
    x = 0;
    y = 0;
    w = bounds.width;
    h = bounds.height;

    xt = x + MARGIN_LEFT;
    yt = y + MARGIN_TOP;

/*
    gc.setForeground(COLOR_TEXT);
    gc.drawLine(x+1  ,y+1  ,x+w-3,y+1  );
    gc.drawLine(x+1  ,y+2  ,x+1  ,y+h-3);
    gc.drawLine(x+0  ,y+h-1,x+w-1,y+h-1);
    gc.drawLine(x+w-1,y+0  ,x+w-1,y+h-2);
*/

    // draw background
    gc.setBackground(COLOR_BACKGROUND);
    gc.fillRectangle(0+2,0+2,w-4,h-4);

    // draw history
    gc.setForeground(COLOR_TEXT);
    for (String line : historyList)
    {
      gc.drawString(line,xt,yt,true);
      yt += fontSize.y;
    }

    // draw prompt
    gc.setForeground(COLOR_TEXT);
    gc.drawString(prompt+commandLine,xt,yt,true);
    xt += prompt.length()*fontSize.x;

    // draw current command line
    gc.setForeground(COLOR_TEXT);
    gc.drawString(commandLine,xt,yt,true);
    xt += commandLine.length()*fontSize.x;

    caret.setLocation(xt,yt);

    caret.setVisible(true);
  }

  private void redrawCommandLine()
  {
redrawText();
  }

  private void redrawAll()
  {
    redrawText();
    redrawCommandLine();
  }

  /** paint progress bar
   * @param paintEvent paint event
   */
  private void paint(PaintEvent paintEvent)
  {
    redrawAll();
  }
}

/* end of file */
