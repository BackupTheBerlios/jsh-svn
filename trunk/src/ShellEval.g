/***********************************************************************\
*
* $Source: /home/torsten/cvs/jmake/src/JMakeEval.g,v $
* $Revision: 1.1 $
* $Author: torsten $
* Contents: shell command evaluator grammar
* Systems: all
*
\***********************************************************************/

tree grammar ShellEval;

/* options */
options
{
  tokenVocab   = Shell;
  ASTLabelType = CommonTree;
}

@header
{
  import java.lang.reflect.InvocationTargetException;

  //import java.io.BufferedReader;
  import java.io.PipedInputStream;
  import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

  import java.lang.Long;
  import java.math.BigDecimal;

  import java.util.HashMap;
  import java.util.LinkedList;
  import java.util.List;

  import groovy.lang.Binding;
}

@members
{
  /** calculated value
   */
  class Value
  {
// NYI: how to get file name information here?
    String fileName;
    int    line,column;

    Value(int line, int column)
    {
      this.fileName = null;
      this.line     = line;
      this.column   = column;
    }
  }

  /** calculated string value
   */
  class StringValue extends Value
  {
    String string;

    StringValue(String string, int line, int column)
    {
      super(line,column);
      this.string = string;
    }

    StringValue(String string, Value fromValue)
    {
      this(string,fromValue.line,fromValue.column);
    }

    StringValue(BigDecimal number, int line, int column)
    {
      this(number.toString(),line,column);
    }

    StringValue(NumberValue value)
    {
      this(value.number.toString(),value);
    }
  }

  /** calculated string list value
   */
  class StringListValue extends Value
  {
    LinkedList<String> stringList;

    StringListValue()
    {
      super(0,0);
      stringList = new LinkedList<String>();
    }

    void append(StringValue value)
    {
      if (stringList.size() <= 0)
      {
        this.line   = value.line;
        this.column = value.column;
      }
      stringList.add(value.string);
    }
  }

  /** calculated number value (big decimal)
   */
  class NumberValue extends Value
  {
    BigDecimal number;

    NumberValue(String string, int line, int column)
    {
      super(line,column);
      this.number = new BigDecimal(string);
    }

    NumberValue(long n, int line, int column)
    {
      super(line,column);
      this.number = new BigDecimal(n);
    }
  }

  //-----------------------------------------------------------------------

  /** print error message
   * @param message error message
   */
  public void emitErrorMessage(String message)
  {
    Jsh.printError(message);
  }

/*
  public void displayRecognitionError(String[]             tokenNames,
                                      RecognitionException exception
                                     )
  {
    String header  = getErrorHeader(exception);
    String message = getErrorMessage(exception,tokenNames);
    System.err.println("ERROR: h="+header+" m="+message);
  }
*/

  /** print fatal error and exit
   * @param format format string
   * @param args optional arguments
   */
  public void fatalError(String format, Object... args)
  {
    emitErrorMessage(String.format(format,args));
    System.exit(1);
  }

  //-----------------------------------------------------------------------

  /** get string value: remove " and convert escapes
   * @param stringLiteral string literal
   * @return string
   */
  private String stringLiteralToString(String stringLiteral)
  {
    StringBuffer buffer = new StringBuffer();

    int i = 1;
    while (i < stringLiteral.length()-1)
    {
      char ch = stringLiteral.charAt(i);
      if (ch == '\\')
      {
        ch = stringLiteral.charAt(i+1);
        switch (ch)
        {
          case 'b' : buffer.append('\b'); break;
          case 't' : buffer.append('\t'); break;
          case 'n' : buffer.append('\n'); break;
          case 'f' : buffer.append('\f'); break;
          case 'r' : buffer.append('\r'); break;
          case '\"': buffer.append('"'); break;
          case '\'': buffer.append('\''); break;
          case '\\': buffer.append('\\'); break;
          case '\r': break;
          case '\n': break;
          default:
// NYI: TODO: convert octal representation
//          | ('0'..'3') ('0'..'7') ('0'..'7')
//          | ('0'..'7') ('0'..'7') 
//          | ('0'..'7')          }
fatalError("convert octal representation not implemented: "+stringLiteral.substring(i));
            break;
        }
        i+=2;
      }
      else
      {
        buffer.append(ch);
        i+=1;
      }
    }

    return buffer.toString();
  }

  HashMap<String,Class> javaCommandMap = new HashMap<String,Class>();

  {
    javaCommandMap.put("ls",ls.class);
  }  
}

// --- tree walker section ---------------------------------------------

// statement block
start [Jsh jsh]
  : (commandList=statement1)+
    {
Dprintf.dprintf("(statement)*=\%s",commandList);
      PipedInputStream inputStream = null;
      for (Command command : commandList)
      {
Dprintf.dprintf("inputStream=%s",inputStream);
        command.setInput(inputStream);
        inputStream = command.getOutput();
      }
      Command lastCommand = commandList.getLast();
Dprintf.dprintf("jsh.getOutput()=%s",jsh.getOutput());
      lastCommand.setOutput(jsh.getOutput());
Dprintf.dprintf("lastCommand=%s",lastCommand);

      for (Command command : commandList)
      {
        command.run();
      }
    }
  | (NEWLINE)*
  ;

statement1 returns [LinkedList<Command> commandList]
  : a=commandRedirected '|' b=statement1
    {
Dprintf.dprintf("a=\%s",a);
Dprintf.dprintf("b=\%s",b);
      b.addFirst(a);
      $commandList = b;
    }
  | a=commandRedirected ';'
    {
Dprintf.dprintf("command1");
      $commandList = new LinkedList<Command>();
      $commandList.add(a);
    }
  | a=commandRedirected NEWLINE
    {
Dprintf.dprintf("command2");
      $commandList = new LinkedList<Command>();
      $commandList.add(a);
    }
  | a=commandRedirected EOF
    {
Dprintf.dprintf("command3");
      $commandList = new LinkedList<Command>();
      $commandList.add(a);
    }
  ;

/*
statement2
  : pipedCommand ';'
    {
Dprintf.dprintf("command1");
    }
  | pipedCommand NEWLINE
    {
Dprintf.dprintf("command2");
    }
  | pipedCommand EOF
    {
Dprintf.dprintf("command3");
    }
  ;
*/

// command
/*
pipedCommand returns [int exitcode]
  : a=commandRedirected (('|'^) b=command)*
    {
      $exitcode=22;
    }
  ;
*/

commandRedirected returns [Command command]
  : a=command redirect
    {
      $command = a;
    }
  ;

// command
command returns [Command command]
  : name=WORD argumentList=arguments
    {
Dprintf.dprintf("command=\%s",$name.text);
//for (String string : argumentList) Dprintf.dprintf("arg=\%s",string);
//for (String s : javaCommandMap.keySet()) Dprintf.dprintf("java commands=\%s",s);
      Class javaCommandClass = javaCommandMap.get($name.text);
      if (javaCommandClass != null)
      {
        try
        {
          JavaCommand javaCommand = (JavaCommand)javaCommandClass.getDeclaredConstructor().newInstance();
          javaCommand.setArguments(argumentList);
Dprintf.dprintf("xxxxxxxxxxxxxxxxx");

          $command = new Command(javaCommand);
        }
        catch (InstantiationException exception)
        {
Dprintf.dprintf("exception=%s",exception);
          $command = null;
System.exit(1);
          }
        catch (IllegalAccessException exception)
        {
Dprintf.dprintf("exception=%s",exception);
          $command = null;
System.exit(1);
        }
        catch (InvocationTargetException exception)
        {
Dprintf.dprintf("exception=%s",exception);
          $command = null;
System.exit(1);
        }
        catch (NoSuchMethodException exception)
        {
Dprintf.dprintf("exception=%s",exception);
          $command = null;
System.exit(1);
        }
      }
      else
      {
        try
        {
          // get command line
          ArrayList<String> commandLineList = new ArrayList<String>();
          commandLineList.add("cmd.exe");
          commandLineList.add("/C");
          commandLineList.add($name.text);
          for (String argument : argumentList)
          {
            commandLineList.add(argument);
          }
          String[] commandLineArray = commandLineList.toArray(new String[commandLineList.size()]);
  //for (String s : commandArray) Dprintf.dprintf("commandLineArray=\%s",s);

          // run external program
          Process process = Runtime.getRuntime().exec(commandLineArray);

          $command = new Command(process);
        }
        catch (IOException exception)
        {
Dprintf.dprintf("exception=%s",exception);
          $command = null;
        }
      }
    }
  ;

arguments returns [ArrayList<String> value = new ArrayList<String>()]
  : (argument
     {
//Dprintf.dprintf("dep="+$argument.value);
       $value.add($argument.value);
//Dprintf.dprintf("\%s: deplist=\%s",$value.hashCode(),$value);
     }
    )*
  ;

argument returns [String value]
  : word=WORD
    {
      $value = $word.text;
    }
  | '-'
    {
Dprintf.dprintf("");
    }
  ;

redirect
  : INPUT fileName=WORD
    {
Dprintf.dprintf("input="+$fileName.text);
    }
  | OUTPUT fileName=WORD
    {
Dprintf.dprintf("output="+$fileName.text);
    }
  | OUTPUT_APPEND fileName=WORD
    {
Dprintf.dprintf("output_append="+$fileName.text);
    }
  | // no redirect
    {
Dprintf.dprintf("");
    }
  ;

// -------------------------------

statementx
  : import_
  | assignment
  | rule
  ;

// import
import_
  : ^(IMPORT expr)
    {
Dprintf.dprintf("filenaem=\%s",$expr.value.string);
      //ShellEval shellEval = Jsh.parse($expr.value.string);
      //shellEval.start();
    }
  ;

// variable assignment
assignment
  : ^('=' identifier=IDENTIFIER expr)
    {
//Dprintf.dprintf("expr="+$expr.value);
      Jsh.variableMap.add($identifier.text,$expr.value.string);
    }
  ;

// rule
rule
  : ^(RULE identifier=IDENTIFIER dependencies=dependencyList actions=actionList
      {
        Target target;

        target = Jsh.targetMap.get($identifier.text);
        if (target != null)
        {
//Dprintf.dprintf("set rule "+$identifier.text);
          target.dependencyList = $dependencies.value;
          target.actionList     = $actions.value;
        }
        else
        {
//Dprintf.dprintf("create rule "+$identifier.text);
          target = new Target($identifier.text,$dependencies.value,$actions.value);
          Jsh.targetMap.put($identifier.text,target);
        }
      }
     )
  ;

// dependency block
dependencyList returns [LinkedList<Target> value = new LinkedList<Target>()]
  : (dependency
     {
//Dprintf.dprintf("dep="+$dependency.value);
       $value.add($dependency.value);
//Dprintf.dprintf("\%s: deplist=\%s",$value.hashCode(),$value);
     }
    )*
  ;

dependency returns [Target value]
  : identifier=IDENTIFIER
    {
      $value = Jsh.targetMap.get($identifier.text);
      if ($value == null) $value = new Target($identifier.text);
      Jsh.targetMap.add($identifier.text,$value);
//Dprintf.dprintf("dep name=\%s: \%s",$identifier.text,$value);
    }
  ;

// action block
actionList returns [LinkedList<Action> value = new LinkedList<Action>()]
  : (action
     {
       $value.add($action.value);
     }
    )*
  ;

action returns [Action value]
  : commandx
    {
      if ($commandx.value != null)
      {
        $value = new Action($commandx.value.stringList,
                            "<NYI>",
                            $commandx.value.line,
                            $commandx.value.column
                           );
      }
    }
  | javaCode
    {
      if ($javaCode.value != null)
      {
        $value = new Action(Jsh.variableMap.getBinding(),
                            $javaCode.value.string,
                            "<NYI>",
                            $javaCode.value.line,
                            $javaCode.value.column
                           );
      }
    }
  ;

// command code
commandx returns [StringListValue value]
  : ^(COMMAND commandParts=commandPartList
      {
        $value = $commandParts.value;
      }
     )
  ;

commandPartList returns [StringListValue value = new StringListValue()]
  : (commandPart
     {
       $value.append($commandPart.value);
     }
    )*
  ;

commandPart returns [StringValue value]
  : IDENTIFIER
    {
      $value = new StringValue($IDENTIFIER.text,
                               $IDENTIFIER.getLine(),
                               $IDENTIFIER.getCharPositionInLine()
                              );
    }
  | number
    {
      $value = new StringValue($number.value);
    }
  | string
    {
      $value = $string.value;
    }
  | '$' '(' expr ')'
    {
      $value = $expr.value;
    }
  ;

// Java code
javaCode returns [StringValue value]
  : JAVACODE
    {
//Dprintf.dprintf("JAVACODE="+$JAVACODE.text);
      $value = new StringValue($JAVACODE.text,
                               $JAVACODE.getLine(),
                               $JAVACODE.getCharPositionInLine()
                              );
    }
  ;

// expression
expr returns [StringValue value]
  : ^('+' v0=expr v1=expr)
    {
      BigDecimal n0 = null;
      BigDecimal n1 = null;
      try
      {
        // number +
        n0 = new BigDecimal($v0.value.string);
        n1 = new BigDecimal($v1.value.string);
        $value = new StringValue(n0.add(n1),$v0.value.line,$v0.value.column);
      }
      catch (NumberFormatException exception)
      {
        // string +
        $value = new StringValue($v0.value.string+$v1.value.string,$v0.value.line,$v0.value.column);
      }
    }
  | ^('-' v0=expr v1=expr)
    {
      BigDecimal n0 = null;
      BigDecimal n1 = null;
      try
      {
        n0 = new BigDecimal($v0.value.string);
        n1 = new BigDecimal($v1.value.string);
        $value = new StringValue(n0.subtract(n1),$v0.value.line,$v0.value.column);
      }
      catch (NumberFormatException exception)
      {
        fatalError("'\%s - \%s' is not a valid operation",$v0.value,$v1.value);
      }
    }
  | ^('*' v0=expr v1=expr)
    {
      BigDecimal n0 = null;
      BigDecimal n1 = null;
      try
      {
        n0 = new BigDecimal($v0.value.string);
        n1 = new BigDecimal($v1.value.string);
        $value = new StringValue(n0.multiply(n1),$v0.value.line,$v0.value.column);
      }
      catch (NumberFormatException exception)
      {
        fatalError("'\%s * \%s' is not a valid operation",$v0.value,$v1.value);
      }
    }
  | ^('/' v0=expr v1=expr)
    {
      BigDecimal n0 = null;
      BigDecimal n1 = null;
      try
      {
        n0 = new BigDecimal($v0.value.string);
        n1 = new BigDecimal($v1.value.string);
        $value = new StringValue(n0.divide(n1),$v0.value.line,$v0.value.column);
      }
      catch (NumberFormatException exception)
      {
        fatalError("'\%s / \%s' is not a valid operation",$v0.value,$v1.value);
      }
      catch (ArithmeticException exception)
      {
        fatalError("'\%s / \%s': devide by 0",$v0.value,$v1.value);
      }
    }
  | ^('%' v0=expr v1=expr)
    {
      BigDecimal n0 = null;
      BigDecimal n1 = null;
      try
      {
        n0 = new BigDecimal($v0.value.string);
        n1 = new BigDecimal($v1.value.string);
        $value = new StringValue(n0.remainder(n1),$v0.value.line,$v0.value.column);
      }
      catch (NumberFormatException exception)
      {
        fatalError("'\%s \%\% \%s' is not a valid operation",$v0.value,$v1.value);
      }
      catch (ArithmeticException exception)
      {
        fatalError("'\%s \%\% \%s': devide by 0",$v0.value,$v1.value);
      }
    }
  | number
    {
      $value = new StringValue($number.value);
    }
  | string
    {
      $value = $string.value;
    }
  | IDENTIFIER
    {
      $value = new StringValue(Jsh.variableMap.get($IDENTIFIER.text),
                               $IDENTIFIER.getLine(),
                               $IDENTIFIER.getCharPositionInLine()
                              );
    }
  ;

// scalars
number returns [NumberValue value]
  : DECIMAL_NUMBER
    {
      $value = new NumberValue($DECIMAL_NUMBER.text,
                               $DECIMAL_NUMBER.getLine(),
                               $DECIMAL_NUMBER.getCharPositionInLine()
                              );
    }
  | HEX_NUMBER
    {
      String s = $HEX_NUMBER.text;
      if (s != null)
      {
        $value = new NumberValue(Long.parseLong(s.substring(2),16),
                                 $HEX_NUMBER.getLine(),
                                 $HEX_NUMBER.getCharPositionInLine()
                                 );
      }
      else
      {
        $value = new NumberValue(0,
                                 $HEX_NUMBER.getLine(),
                                 $HEX_NUMBER.getCharPositionInLine()
                                );
      }
    }
  | OCTAL_NUMBER
    {
      $value = new NumberValue($OCTAL_NUMBER.text,
                               $OCTAL_NUMBER.getLine(),
                               $OCTAL_NUMBER.getCharPositionInLine()
                              );
    }
  | BINARY_NUMBER
    {
      $value = new NumberValue($BINARY_NUMBER.text,
                               $BINARY_NUMBER.getLine(),
                               $BINARY_NUMBER.getCharPositionInLine()
                              );
    }
  ;

string returns [StringValue value]
  : CHARLITERAL
    {
      char ch='\0';

      // get char value: remove ' and convert escapes
      String s = $CHARLITERAL.text;
      if (s.charAt(1) == '\\')
      {
        switch (s.charAt(2))
        {
          case 'b' : ch = '\b'; break;
          case 't' : ch = '\t'; break;
          case 'n' : ch = '\n'; break;
          case 'f' : ch = '\f'; break;
          case 'r' : ch = '\r'; break;
          case '\"': ch = '"'; break;
          case '\'': ch = '\''; break;
          case '\\': ch = '\\'; break;
          default:
// NYI: TODO: convert octal representation
//          | ('0'..'3') ('0'..'7') ('0'..'7')
//          | ('0'..'7') ('0'..'7') 
//          | ('0'..'7')          }
fatalError("CHARLITERAL convert octal representation not implemented");
            break;
        }
      }
      else
      {
        ch = s.charAt(1);
      }

      $value = new StringValue(""+ch,
                               $CHARLITERAL.getLine(),
                               $CHARLITERAL.getCharPositionInLine()
                              );
    }
  | STRINGLITERAL
    {
      $value = new StringValue(stringLiteralToString($STRINGLITERAL.text),
                               $STRINGLITERAL.getLine(),
                               $STRINGLITERAL.getCharPositionInLine()
                              );
    }
  ;

// END:rules
