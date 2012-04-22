/***********************************************************************\
*
* $Revision$
* $Date$
* $Author$
* Contents: load/save program settings
* Systems: all
*
\***********************************************************************/

/****************************** Imports ********************************/
import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.lang.reflect.Constructor;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.TYPE;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import java.util.EnumSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.StringTokenizer;
import java.util.LinkedHashSet;
import java.util.regex.Pattern;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;

/****************************** Classes ********************************/

/** setting comment annotation
 */
@Target({TYPE,FIELD})
@Retention(RetentionPolicy.RUNTIME)
@interface SettingComment
{
  String[] text() default {""};                  // comment before value
}

/** setting value annotation
 */
@Target({TYPE,FIELD})
@Retention(RetentionPolicy.RUNTIME)
@interface SettingValue
{
  String name()         default "";              // name of value
  String defaultValue() default "";              // default value
  Class  type()         default DEFAULT.class;   // adapter class

  static final class DEFAULT
  {
  }
}

/** setting value adapter
 */
abstract class SettingValueAdapter<String,Value>
{
  /** convert to value
   * @param string string
   * @return value
   */
  abstract public Value toValue(String string) throws Exception;

  /** convert to string
   * @param value value
   * @return string
   */
  abstract public String toString(Value value) throws Exception;

  /** check if equals
   * @param value0,value1 values to compare
   * @return true if value0==value1
   */
  public boolean equals(Value value0, Value value1)
  {
    return false;
  }
}

/** settings
 */
public class Settings
{
  /** file pattern
   */
  static class FilePattern implements Cloneable
  {
    public final String  string;
    public final Pattern pattern;

    /** create file pattern
     * @param string glob pattern string
     */
    FilePattern(String string)
    {
      this.string  = string;
      this.pattern = Pattern.compile(StringUtils.globToRegex(string));
    }

    /** clone object
     * @return cloned object
     */
    public FilePattern clone()
    {
      return new FilePattern(string);
    }

    /** convert data to string
     * @return string
     */
    public String toString()
    {
      return "FilePattern {"+string+"}";
    }
  }

  /** config value adapter String <-> file pattern
   */
  class SettingValueAdapterFilePattern extends SettingValueAdapter<String,FilePattern>
  {
    /** convert to value
     * @param string string
     * @return value
     */
    public FilePattern toValue(String string) throws Exception
    {
      return new FilePattern(StringUtils.unescape(string));
    }

    /** convert to string
     * @param value value
     * @return string
     */
    public String toString(FilePattern filePattern) throws Exception
    {
      return StringUtils.escape(filePattern.string);
    }

    public boolean equals(FilePattern filePattern0, FilePattern filePattern1)
    {
      return filePattern0.string.equals(filePattern1.string);
    }
  }

  /** config value adapter String <-> size
   */
  class SettingValueAdapterSize extends SettingValueAdapter<String,Point>
  {
    /** convert to value
     * @param string string
     * @return value
     */
    public Point toValue(String string) throws Exception
    {
      Point point = null;

      StringTokenizer tokenizer = new StringTokenizer(string,"x");
      point = new Point(Integer.parseInt(tokenizer.nextToken()),
                        Integer.parseInt(tokenizer.nextToken())
                       );

      return point;
    }

    /** convert to string
     * @param value value
     * @return string
     */
    public String toString(Point p) throws Exception
    {
      return String.format("%dx%d",p.x,p.y);
    }
  }

  /** column sizes
   */
  static class ColumnSizes
  {
    public final int[] width;

    /** create column sizes
     * @param width width array
     */
    ColumnSizes(int[] width)
    {
      this.width = width;
    }

    /** create column sizes
     * @param width width (int list)
     */
    ColumnSizes(Object... width)
    {
      this.width = new int[width.length];
      for (int z = 0; z < width.length; z++)
      {
        this.width[z] = (Integer)width[z];
      }
    }

    /** create column sizes
     * @param widthList with list
     */
    ColumnSizes(ArrayList<Integer> widthList)
    {
      this.width = new int[widthList.size()];
      for (int z = 0; z < widthList.size(); z++)
      {
        this.width[z] = widthList.get(z);
      }
    }

    /** get width
     * @param columNb column index (0..n-1)
     * @return width or 0
     */
    public int get(int columNb)
    {
      return (columNb < width.length) ? width[columNb] : 0;
    }

    /** convert data to string
     * @return string
     */
    public String toString()
    {
      return "ColumnSizes {"+width+"}";
    }
  }

  /** config value adapter String <-> column width array
   */
  class SettingValueAdapterWidthArray extends SettingValueAdapter<String,ColumnSizes>
  {
    /** convert to value
     * @param string string
     * @return value
     */
    public ColumnSizes toValue(String string) throws Exception
    {
      StringTokenizer tokenizer = new StringTokenizer(string,",");
      ArrayList<Integer> widthList = new ArrayList<Integer>();
      while (tokenizer.hasMoreTokens())
      {
        widthList.add(Integer.parseInt(tokenizer.nextToken()));
      }
      return new ColumnSizes(widthList);
    }

    /** convert to string
     * @param value value
     * @return string
     */
    public String toString(ColumnSizes columnSizes) throws Exception
    {
      StringBuilder buffer = new StringBuilder();
      for (int width : columnSizes.width)
      {
        if (buffer.length() > 0) buffer.append(',');
        buffer.append(Integer.toString(width));
      }
      return buffer.toString();
    }

    public boolean equals(FilePattern filePattern0, FilePattern filePattern1)
    {
      return filePattern0.string.equals(filePattern1.string);
    }
  }

  /** color
   */
  static class Color implements Cloneable
  {
    public RGB foreground;
    public RGB background;

    /** create color
     * @param foreground,background foreground/background RGB values
     */
    Color(RGB foreground, RGB background)
    {
      this.foreground = foreground;
      this.background = background;
    }

    /** create color
     * @param foreground foreground/background RGB values
     */
    Color(RGB foreground)
    {
      this(foreground,foreground);
    }

    /** clone object
     * @return cloned object
     */
    public Color clone()
    {
      return new Color(foreground,background);
    }

    /** convert data to string
     * @return string
     */
    public String toString()
    {
      return "Color {"+foreground+", "+background+"}";
    }
  }

  /** config value adapter String <-> Color
   */
  class SettingValueAdapterColor extends SettingValueAdapter<String,Color>
  {
    /** convert to value
     * @param string string
     * @return value
     */
    public Color toValue(String string) throws Exception
    {
      Color color = null;

      Object[] data = new Object[6];
      if      (StringParser.parse(string,"%d,%d,%d:%d,%d,%d",data))
      {
        color = new Color(new RGB((Integer)data[0],(Integer)data[1],(Integer)data[2]),
                          new RGB((Integer)data[3],(Integer)data[4],(Integer)data[5])
                         );
      }
      else if (StringParser.parse(string,"%d,%d,%d",data))
      {
        color = new Color(new RGB((Integer)data[0],(Integer)data[1],(Integer)data[2]));
      }
      else
      {
        throw new Exception(String.format("Cannot parse color definition '%s'",string));
      }

      return color;
    }

    /** convert to string
     * @param value value
     * @return string
     */
    public String toString(Color color) throws Exception
    {
      if      (color.foreground != null)
      {
        if      ((color.background != null) && (color.foreground != color.background))
        {
          return  ((color.foreground != null) ? color.foreground.red+","+color.foreground.green+","+color.foreground.blue : "")
                 +":"
                 +((color.background != null) ? color.background.red+","+color.background.green+","+color.background.blue : "");
        }
        else
        {
          return color.foreground.red+","+color.foreground.green+","+color.foreground.blue;
        }
      }
      else if (color.background != null)
      {
        return color.background.red+","+color.background.green+","+color.background.blue;
      }
      else
      {
        return "0,0,0";
      }
    }
  }

  /** config value adapter String <-> Key
   */
  class SettingValueAdapterKey extends SettingValueAdapter<String,Integer>
  {
    /** convert to value
     * @param string string
     * @return value
     */
    public Integer toValue(String string) throws Exception
    {
      int accelerator = 0;
      if (!string.isEmpty())
      {
        accelerator = Widgets.textToAccelerator(string);
        if (accelerator == 0)
        {
          throw new Exception(String.format("Cannot parse key definition '%s'",string));
        }
      }

      return accelerator;
    }

    /** convert to string
     * @param value value
     * @return string
     */
    public String toString(Integer accelerator) throws Exception
    {
      return Widgets.menuAcceleratorToText(accelerator);
    }
  }

  /** editor
   */
  static class Editor implements Cloneable
  {
    public String  mimeTypePattern;
    public String  command;
    public Pattern pattern;

    /** create editor
     * @param mimeTypePattern glob pattern string
     * @param command command
     */
    Editor(String mimeTypePattern, String command)
    {
      this.mimeTypePattern = mimeTypePattern;
      this.command         = command;
      this.pattern         = Pattern.compile(StringUtils.globToRegex(mimeTypePattern));
    }

    /** create editor
     */
    Editor()
    {
      this("","");
    }

    /** clone object
     * @return cloned object
     */
    public Editor clone()
    {
      return new Editor(mimeTypePattern,command);
    }

    /** convert data to string
     * @return string
     */
    public String toString()
    {
      return "Editor {"+mimeTypePattern+", command: "+command+"}";
    }
  }

  /** config value adapter String <-> editor
   */
  class SettingValueAdapterEditor extends SettingValueAdapter<String,Editor>
  {
    /** convert to value
     * @param string string
     * @return value
     */
    public Editor toValue(String string) throws Exception
    {
      Editor editor = null;

      Object[] data = new Object[2];
      if (StringParser.parse(string,"%s:% s",data))
      {
        editor = new Editor(((String)data[0]).trim(),((String)data[1]).trim());
      }
      else
      {
        throw new Exception(String.format("Cannot parse editor definition '%s'",string));
      }

      return editor;
    }

    /** convert to string
     * @param value value
     * @return string
     */
    public String toString(Editor editor) throws Exception
    {
      return editor.mimeTypePattern+":"+editor.command;
    }

    public boolean equals(Editor editor0, Editor editor1)
    {
      return    editor0.mimeTypePattern.equals(editor1.mimeTypePattern)
             && editor0.command.equals(editor1.command);
    }
  }

  /** shell command
   */
  static class ShellCommand implements Cloneable
  {
    public String name;
    public String command;

    /** create shell command
     * @param name name
     * @param command command
     */
    ShellCommand(String name, String command)
    {
      this.name    = name;
      this.command = command;
    }

    /** create shell command
     */
    ShellCommand()
    {
      this("","");
    }

    /** clone object
     * @return cloned object
     */
    public ShellCommand clone()
    {
      return new ShellCommand(name,command);
    }

    /** convert data to string
     * @return string
     */
    public String toString()
    {
      return "ShellCommand {"+name+", command: "+command+"}";
    }
  }

  /** config value adapter String <-> shell command
   */
  class SettingValueAdapterShellCommand extends SettingValueAdapter<String,ShellCommand>
  {
    /** convert to value
     * @param string string
     * @return value
     */
    public ShellCommand toValue(String string) throws Exception
    {
      ShellCommand shellCommand = null;

      Object[] data = new Object[2];
      if (StringParser.parse(string,"%S% s",data,StringParser.QUOTE_CHARS))
      {
        shellCommand = new ShellCommand(((String)data[0]).trim(),((String)data[1]).trim());
      }
      else
      {
        throw new Exception(String.format("Cannot parse shell command definition '%s'",string));
      }

      return shellCommand;
    }

    /** convert to string
     * @param value value
     * @return string
     */
    public String toString(ShellCommand shellCommand) throws Exception
    {
      return StringUtils.escape(shellCommand.name)+" "+shellCommand.command;
    }

    public boolean equals(ShellCommand shellCommand0, ShellCommand shellCommand1)
    {
      return    shellCommand0.name.equals(shellCommand1.name)
             && shellCommand0.command.equals(shellCommand1.command);
    }
  }

  /** config value adapter String <-> font data
   */
  class SettingValueAdapterFontData extends SettingValueAdapter<String,FontData>
  {
    /** convert to value
     * @param string string
     * @return value
     */
    public FontData toValue(String string) throws Exception
    {
      return Widgets.textToFontData(string);
    }

    /** convert to string
     * @param value value
     * @return string
     */
    public String toString(FontData fontData) throws Exception
    {
      String string;

      if (fontData != null)
      {
        string = Widgets.fontDataToText(fontData);
      }
      else
      {
        string = "";
      }

      return string;
    }
  }

  // --------------------------- constants --------------------------------
  public static final String JSH_DIRECTORY = System.getProperty("user.home")+File.separator+".jsh";

  /** host system
   */
  public enum HostSystems
  {
    UNKNOWN,
    LINUX,
    SOLARIS,
    WINDOWS,
    MACOS,
    QNX
  };

  private static final String JSH_CONFIG_FILE_NAME = JSH_DIRECTORY+File.separator+"jsh.cfg";

  // --------------------------- variables --------------------------------

  private static long lastModified = 0L;

  @SettingComment(text={"Jsh configuration",""})

  // program settings
  public static HostSystems              hostSystem                     = HostSystems.LINUX;

  @SettingComment(text={"","Geometry: <width>x<height>"})
  @SettingValue(type=SettingValueAdapterSize.class)
  public static Point                    geometryMain                   = new Point(900,600);

  @SettingComment(text={""})
  @SettingValue(type=SettingValueAdapterColor.class)
  public static Color                    colorStatusText                 = new Color(null,new RGB(255,255,255));

  @SettingComment(text={"","Fonts: <name>,<height>,normal|bold|italic|bold italic"})
  @SettingValue(type=SettingValueAdapterFontData.class)
  public static FontData                 fontText                        = new FontData("Courier",10,SWT.NORMAL);;

  @SettingComment(text={"","Accelerator keys"})
  @SettingValue(type=SettingValueAdapterKey.class)
  public static int                      keyCheckoutRepository          = SWT.NONE;

  // miscelanous
  @SettingValue
  public static String                   prompt                         = "jsh> ";
  @SettingValue
  public static int                      historyLength                  = 1000;

  @SettingComment(text={"","temporary directory"})
  @SettingValue
  public static String                   tmpDirectory                   = "/tmp";

  @SettingComment(text={"","date/time formats"})
  @SettingValue
  public static String                   dateFormat                     = "yyyy-MM-dd";
  @SettingValue
  public static String                   timeFormat                     = "HH:mm:ss";
  @SettingValue
  public static String                   dateTimeFormat                 = "yyyy-MM-dd HH:mm:ss";

  // debug
  public static boolean                  debugFlag                      = false;
  public static int                      verboseLevel                   = 0;

  // help
  public static boolean                  helpFlag                       = false;


  // ------------------------ native functions ----------------------------

  // ---------------------------- methods ---------------------------------

  /** load program settings
   * @param file settings file to load
   */
  public static void load(File file)
  {
    if (file.exists())
    {
      BufferedReader input = null;
      try
      {
        // get setting classes
        Class[] settingClasses = getSettingClasses();

        // open file
        input = new BufferedReader(new FileReader(file));

        // read file
        int      lineNb = 0;
        String   line;
        Object[] data = new Object[2];
        while ((line = input.readLine()) != null)
        {
          line = line.trim();
          lineNb++;

          // check comment
          if (line.isEmpty() || line.startsWith("#"))
          {
            continue;
          }

          // parse
          if (StringParser.parse(line,"%s = % s",data))
          {
            String name   = (String)data[0];
            String string = (String)data[1];

            for (Class clazz : settingClasses)
            {
              for (Field field : clazz.getDeclaredFields())
              {
                for (Annotation annotation : field.getDeclaredAnnotations())
                {
                  if (annotation instanceof SettingValue)
                  {
                    SettingValue settingValue = (SettingValue)annotation;

                    if (((!settingValue.name().isEmpty()) ? settingValue.name() : field.getName()).equals(name))
                    {
                      try
                      {
                        Class type = field.getType();
                        if (type.isArray())
                        {
                          type = type.getComponentType();
                          if      (SettingValueAdapter.class.isAssignableFrom(settingValue.type()))
                          {
                            // instantiate config adapter class
                            SettingValueAdapter settingValueAdapter;
                            Class enclosingClass = settingValue.type().getEnclosingClass();
                            if (enclosingClass == Settings.class)
                            {
                              Constructor constructor = settingValue.type().getDeclaredConstructor(Settings.class);
                              settingValueAdapter = (SettingValueAdapter)constructor.newInstance(new Settings());
                            }
                            else
                            {
                              settingValueAdapter = (SettingValueAdapter)settingValue.type().newInstance();
                            }

                            // convert to value
                            Object value = settingValueAdapter.toValue(string);
                            field.set(null,addArrayUniq((Object[])field.get(null),value,settingValueAdapter));
                          }
                          else if (type == int.class)
                          {
                            int value = Integer.parseInt(string);
                            field.set(null,addArrayUniq((int[])field.get(null),value));
                          }
                          else if (type == long.class)
                          {
                            long value = Long.parseLong(string);
                            field.set(null,addArrayUniq((long[])field.get(null),value));
                          }
                          else if (type == boolean.class)
                          {
                            boolean value = StringUtils.parseBoolean(string);
                            field.set(null,addArrayUniq((boolean[])field.get(null),value));
                          }
                          else if (type == String.class)
                          {
                            field.set(null,addArrayUniq((String[])field.get(null),StringUtils.unescape(string)));
                          }
                          else if (type.isEnum())
                          {
                            field.set(null,addArrayUniq((Enum[])field.get(null),StringUtils.parseEnum(type,string)));
                          }
                          else if (type == EnumSet.class)
                          {
                            field.set(null,addArrayUniq((EnumSet[])field.get(null),StringUtils.parseEnumSet(type,string)));
                          }
                          else
                          {
Dprintf.dprintf("field.getType()=%s",type);
                          }
                        }
                        else
                        {
                          if      (SettingValueAdapter.class.isAssignableFrom(settingValue.type()))
                          {
                            // instantiate config adapter class
                            SettingValueAdapter settingValueAdapter;
                            Class enclosingClass = settingValue.type().getEnclosingClass();
                            if (enclosingClass == Settings.class)
                            {
                              Constructor constructor = settingValue.type().getDeclaredConstructor(Settings.class);
                              settingValueAdapter = (SettingValueAdapter)constructor.newInstance(new Settings());
                            }
                            else
                            {
                              settingValueAdapter = (SettingValueAdapter)settingValue.type().newInstance();
                            }

                            // convert to value
                            Object value = settingValueAdapter.toValue(string);
                            field.set(null,value);
                          }
                          else if (type == int.class)
                          {
                            int value = Integer.parseInt(string);
                            field.setInt(null,value);
                          }
                          else if (type == long.class)
                          {
                            long value = Long.parseLong(string);
                            field.setLong(null,value);
                          }
                          else if (type == boolean.class)
                          {
                            boolean value = StringUtils.parseBoolean(string);
                            field.setBoolean(null,value);
                          }
                          else if (type == String.class)
                          {
                            field.set(null,StringUtils.unescape(string));
                          }
                          else if (type.isEnum())
                          {
                            field.set(null,StringUtils.parseEnum(type,string));
                          }
                          else if (type == EnumSet.class)
                          {
                            Class enumClass = settingValue.type();
                            if (!enumClass.isEnum())
                            {
                              throw new Error(enumClass+" is not an enum class!");
                            }
                            field.set(null,StringUtils.parseEnumSet(enumClass,string));
                          }
                          else
                          {
Dprintf.dprintf("field.getType()=%s",type);
                          }
                        }
                      }
                      catch (NumberFormatException exception)
                      {
                        Jsh.printWarning("Cannot parse number '%s' for configuration value '%s' in line %d",string,name,lineNb);
                      }
                      catch (Exception exception)
                      {
Dprintf.dprintf("exception=%s",exception);
exception.printStackTrace();
                      }
                    }
                  }
                  else
                  {
                  }
                }
              }
            }
          }
          else
          {
            Jsh.printWarning("Unknown configuration value '%s' in line %d",line,lineNb);
          }
        }

        // close file
        input.close();
      }
      catch (IOException exception)
      {
        // ignored
      }
      finally
      {
        try
        {
          if (input != null) input.close();
        }
        catch (IOException exception)
        {
          // ignored
        }
      }
    }
  }

  /** load program settings
   * @param fileName settings file name
   */
  public static void load(String fileName)
  {
    load(new File(fileName));
  }

  /** load default program settings
   */
  public static void load()
  {
    File file = new File(JSH_CONFIG_FILE_NAME);

    // load file
    load(file);

    // save last modified time
    lastModified = file.lastModified();
  }

  /** save program settings
   * @param fileName file nam
   */
  public static void save(File file)
  {
    // create directory
    File directory = file.getParentFile();
    if ((directory != null) && !directory.exists()) directory.mkdirs();

    PrintWriter output = null;
    try
    {
      // get setting classes
      Class[] settingClasses = getSettingClasses();

      // open file
      output = new PrintWriter(new FileWriter(file));

      // write settings
      for (Class clazz : settingClasses)
      {
        for (Field field : clazz.getDeclaredFields())
        {
//Dprintf.dprintf("field=%s",field);
          for (Annotation annotation : field.getDeclaredAnnotations())
          {
            if      (annotation instanceof SettingValue)
            {
              SettingValue settingValue = (SettingValue)annotation;

              // get value and write to file
              String name = (!settingValue.name().isEmpty()) ? settingValue.name() : field.getName();
              try
              {
                Class type = field.getType();
                if (type.isArray())
                {
                  type = type.getComponentType();
                  if      (SettingValueAdapter.class.isAssignableFrom(settingValue.type()))
                  {
                    // instantiate config adapter class
                    SettingValueAdapter settingValueAdapter;
                    Class enclosingClass = settingValue.type().getEnclosingClass();
                    if (enclosingClass == Settings.class)
                    {
                      Constructor constructor = settingValue.type().getDeclaredConstructor(Settings.class);
                      settingValueAdapter = (SettingValueAdapter)constructor.newInstance(new Settings());
                    }
                    else
                    {
                      settingValueAdapter = (SettingValueAdapter)settingValue.type().newInstance();
                    }

                    // convert to string
                    for (Object object : (Object[])field.get(null))
                    {
                      String value = (String)settingValueAdapter.toString(object);
                      output.printf("%s = %s\n",name,value);
                    }
                  }
                  else if (type == int.class)
                  {
                    for (int value : (int[])field.get(null))
                    {
                      output.printf("%s = %d\n",name,value);
                    }
                  }
                  else if (type == long.class)
                  {
                    for (long value : (long[])field.get(null))
                    {
                      output.printf("%s = %ld\n",name,value);
                    }
                  }
                  else if (type == boolean.class)
                  {
                    for (boolean value : (boolean[])field.get(null))
                    {
                      output.printf("%s = %s\n",name,value ? "yes" : "no");
                    }
                  }
                  else if (type == String.class)
                  {
                    for (String value : (String[])field.get(null))
                    {
                      output.printf("%s = %s\n",name,StringUtils.escape(value));
                    }
                  }
                  else if (type.isEnum())
                  {
                    for (Enum value : (Enum[])field.get(null))
                    {
                      output.printf("%s = %s\n",name,value.toString());
                    }
                  }
                  else if (type == EnumSet.class)
                  {
                    for (EnumSet enumSet : (EnumSet[])field.get(null))
                    {
                      output.printf("%s = %s\n",name,StringUtils.join(enumSet,","));
                    }
                  }
                  else
                  {
Dprintf.dprintf("field.getType()=%s",type);
                  }
                }
                else
                {
                  if      (SettingValueAdapter.class.isAssignableFrom(settingValue.type()))
                  {
                    // instantiate config adapter class
                    SettingValueAdapter settingValueAdapter;
                    Class enclosingClass = settingValue.type().getEnclosingClass();
                    if (enclosingClass == Settings.class)
                    {
                      Constructor constructor = settingValue.type().getDeclaredConstructor(Settings.class);
                      settingValueAdapter = (SettingValueAdapter)constructor.newInstance(new Settings());
                    }
                    else
                    {
                      settingValueAdapter = (SettingValueAdapter)settingValue.type().newInstance();
                    }

                    // convert to string
                    String value = (String)settingValueAdapter.toString(field.get(null));
                    output.printf("%s = %s\n",name,value);
                  }
                  else if (type == int.class)
                  {
                    int value = field.getInt(null);
                    output.printf("%s = %d\n",name,value);
                  }
                  else if (type == long.class)
                  {
                    long value = field.getLong(null);
                    output.printf("%s = %ld\n",name,value);
                  }
                  else if (type == boolean.class)
                  {
                    boolean value = field.getBoolean(null);
                    output.printf("%s = %s\n",name,value ? "yes" : "no");
                  }
                  else if (type == String.class)
                  {
                    String value = (type != null) ? (String)field.get(null) : settingValue.defaultValue();
                    output.printf("%s = %s\n",name,StringUtils.escape(value));
                  }
                  else if (type.isEnum())
                  {
                    Enum value = (Enum)field.get(null);
                    output.printf("%s = %s\n",name,value.toString());
                  }
                  else if (type == EnumSet.class)
                  {
                    EnumSet enumSet = (EnumSet)field.get(null);
                    output.printf("%s = %s\n",name,StringUtils.join(enumSet,","));
                  }
                  else
                  {
Dprintf.dprintf("field.getType()=%s",type);
                  }
                }
              }
              catch (Exception exception)
              {
Dprintf.dprintf("exception=%s",exception);
exception.printStackTrace();
              }
            }
            else if (annotation instanceof SettingComment)
            {
              SettingComment settingComment = (SettingComment)annotation;

              for (String line : settingComment.text())
              {
                if (!line.isEmpty())
                {
                  output.printf("# %s\n",line);
                }
                else
                {
                  output.printf("\n");
                }
              }
            }
          }
        }
      }

      // close file
      output.close();

      // save last modified time
      lastModified = file.lastModified();
    }
    catch (IOException exception)
    {
      // ignored
    }
    finally
    {
      if (output != null) output.close();
    }
  }

  /** save program settings
   * @param fileName settings file name
   */
  public static void save(String fileName)
  {
    save(new File(fileName));
  }

  /** save default program settings (only if not external modified)
   */
  public static void save()
  {
    File file = new File(JSH_CONFIG_FILE_NAME);

    if ((lastModified == 0L) || (file.lastModified() <= lastModified))
    {
      save(file);
    }
  }

  /** check if program settings file is modified
   * @return true iff modified
   */
  public static boolean isFileModified()
  {
    return (lastModified != 0L) && (new File(JSH_CONFIG_FILE_NAME).lastModified() > lastModified);
  }

  //-----------------------------------------------------------------------

  /** get all setting classes
   * @return classes array
   */
  protected static Class[] getSettingClasses()
  {
    // get all setting classes
    ArrayList<Class> classList = new ArrayList<Class>();

    classList.add(Settings.class);
    for (Class clazz : Settings.class.getDeclaredClasses())
    {
//Dprintf.dprintf("c=%s",clazz);
      classList.add(clazz);
    }

    return classList.toArray(new Class[classList.size()]);
  }

  /** unique add element to int array
   * @param array array
   * @param n element
   * @return extended array or array
   */
  private static int[] addArrayUniq(int[] array, int n)
  {
    int z = 0;
    while ((z < array.length) && (array[z] != n))
    {
      z++;
    }
    if (z >= array.length)
    {
      array = Arrays.copyOf(array,array.length+1);
      array[array.length-1] = n;
    }

    return array;
  }

  /** unique add element to long array
   * @param array array
   * @param n element
   * @return extended array or array
   */
  private static long[] addArrayUniq(long[] array, long n)
  {
    int z = 0;
    while ((z < array.length) && (array[z] != n))
    {
      z++;
    }
    if (z >= array.length)
    {
      array = Arrays.copyOf(array,array.length+1);
      array[array.length-1] = n;
    }

    return array;
  }

  /** unique add element to long array
   * @param array array
   * @param n element
   * @return extended array or array
   */
  private static boolean[] addArrayUniq(boolean[] array, boolean n)
  {
    int z = 0;
    while ((z < array.length) && (array[z] != n))
    {
      z++;
    }
    if (z >= array.length)
    {
      array = Arrays.copyOf(array,array.length+1);
      array[array.length-1] = n;
    }

    return array;
  }

  /** unique add element to string array
   * @param array array
   * @param string element
   * @return extended array or array
   */
  private static String[] addArrayUniq(String[] array, String string)
  {
    int z = 0;
    while ((z < array.length) && !array[z].equals(string))
    {
      z++;
    }
    if (z >= array.length)
    {
      array = Arrays.copyOf(array,array.length+1);
      array[array.length-1] = string;
    }

    return array;
  }

  /** unique add element to enum array
   * @param array array
   * @param string element
   * @return extended array or array
   */
  private static Enum[] addArrayUniq(Enum[] array, Enum n)
  {
    int z = 0;
    while ((z < array.length) && (array[z] != n))
    {
      z++;
    }
    if (z >= array.length)
    {
      array = Arrays.copyOf(array,array.length+1);
      array[array.length-1] = n;
    }

    return array;
  }

  /** unique add element to enum set array
   * @param array array
   * @param string element
   * @return extended array or array
   */
  private static EnumSet[] addArrayUniq(EnumSet[] array, EnumSet n)
  {
    int z = 0;
    while ((z < array.length) && (array[z].equals(n)))
    {
      z++;
    }
    if (z >= array.length)
    {
      array = Arrays.copyOf(array,array.length+1);
      array[array.length-1] = n;
    }

    return array;
  }

  /** unique add element to object array
   * @param array array
   * @param object element
   * @param settingAdapter setting adapter (use equals() function)
   * @return extended array or array
   */
  private static Object[] addArrayUniq(Object[] array, Object object, SettingValueAdapter settingValueAdapter)
  {
    int z = 0;
    while ((z < array.length) && !settingValueAdapter.equals(array[z],object))
    {
      z++;
    }
    if (z >= array.length)
    {
      array = Arrays.copyOf(array,array.length+1);
      array[array.length-1] = object;
    }

    return array;
  }
}

/* end of file */
