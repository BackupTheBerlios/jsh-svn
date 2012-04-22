/***********************************************************************\
*
* $Source: /home/torsten/cvs/jmake/src/JMake.g,v $
* $Revision: 1.1 $
* $Author: torsten $
* Contents: shell commands grammar and lexer definitions
* Systems: all
*
\***********************************************************************/

grammar Shell;

/* options */
options
{
  output       = AST;
  ASTLabelType = CommonTree;
}

/* special tokens */
tokens
{
  RULE;
  COMMAND;
  JAVACODE;
}

/* parser */
@parser::header
{
  import java.lang.Long;
  import java.io.IOException;
  import java.io.StringReader;
  import java.util.HashMap;

  import java.io.InputStreamReader;
  import java.io.FileInputStream;
  import java.io.IOException;
  import java.io.FileNotFoundException;

  import org.antlr.runtime.ANTLRStringStream;
}

@parser::members
{
  HashMap<String,String> variables = new HashMap<String,String>();

  private String StringLiteralToString(String stringLiteral)
  {
    StringBuffer buffer = new StringBuffer();

    // get string value: remove " and convert escapes
    assert stringLiteral.charAt(0) == '"';
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
          default:
// NYI: TODO: convert octal representation
//          | ('0'..'3') ('0'..'7') ('0'..'7')
//          | ('0'..'7') ('0'..'7') 
//          | ('0'..'7')          }
//fatalError("convert octal representation not implemented");
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
    assert stringLiteral.charAt(i) == '"';

    return buffer.toString();
  }

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

  private ShellLexer shellLexer;               // current used lexer
  private CharStream charStream;               // current input stream

  /** set input
   * @param shellLexer lexer
   * @param charStream input stream
   */
  public void setInput(ShellLexer shellLexer, CharStream charStream)
  {
    this.shellLexer = shellLexer;
    this.charStream = charStream;
//Dprintf.dprintf("call setInput "+charStream);
  }

  /** get Java code with an 'island parser'
   */
  private String getJavaCode()
    throws RecognitionException
  {
//Dprintf.dprintf("cs="+charStream+" "+charStream.index());
    // get rest of input as string
    String tail1 = charStream.substring(charStream.index(),charStream.size()-1);
//Dprintf.dprintf("tail1=#"+tail1+"#");

    // create simple Java recognizer parser
    ANTLRStringStream input = new ANTLRStringStream(tail1);
    SimpleJavaRecognizerLexer simpleJavaRecognizerLexer = new SimpleJavaRecognizerLexer(input);
    LinkedListTokenSource tokenSource = new LinkedListTokenSource(simpleJavaRecognizerLexer);
    LinkedListTokenStream tokenStream = new LinkedListTokenStream(tokenSource);
    SimpleJavaRecognizerParser simpleJavaRecognizerParser = new SimpleJavaRecognizerParser(tokenStream);

    // run simple Java recognizer parser
    simpleJavaRecognizerParser.start();
//Dprintf.dprintf("input="+input+" "+input.index());
//Dprintf.dprintf("javaCode result="+simpleJavaRecognizerParser.getResult());

    // get rest of input after Java code, get new input position
    String tail2 = input.substring(input.index(),input.size()-1);
    int line = charStream.getLine()+input.getLine()-1;
    int charPositionInLine = charStream.getCharPositionInLine()+input.getCharPositionInLine()-1;
//Dprintf.dprintf("tail2=#"+tail2+"#");

    // reset input for shell parser
    try
    {
      charStream = new ANTLRReaderStream(new StringReader(tail2));
      charStream.setLine(line);
      charStream.setCharPositionInLine(charPositionInLine);
    }
    catch (IOException execption)
    {
      throw new RuntimeException(execption);
    }
    shellLexer.setCharStream(charStream);
    LinkedListTokenSource tokenSource2 = (LinkedListTokenSource)tokenStream.getTokenSource();
    tokenStream.setTokenSource(tokenSource2);
    tokenSource2.setDelegate(shellLexer);

    return simpleJavaRecognizerParser.getResult();
  }
}

/* lexer */
@lexer::header
{
  import java.lang.Long;
  import java.io.IOException;
  import java.io.StringReader;
  import java.util.HashMap;

  import java.io.InputStreamReader;
  import java.io.FileInputStream;
  import java.io.IOException;
  import java.io.FileNotFoundException;
}

@lexer::members
{
/* obsolete
  class ShellFile
  {
    public CharStream input;
    public int        mark;

    ShellFile(CharStream input)
    {
      this.input = input;
      this.mark  = input.mark();
    }
  }

  Stack<ShellFile> fileStack = new Stack<ShellFile>();

  public Token nextToken()
  {
    Token token = super.nextToken();

    if (token.getType() == Token.EOF && !fileStack.empty())
    {
      // EOF -> pop include stack
      popFile();

      // get token
      token = this.nextToken();
    }

    // Skip first token after switching on another input.
    // You need to use this rather than super as there may be nested include files
    if (((CommonToken)token).getStartIndex() < 0)
    {
      token = this.nextToken();
    }

    return token;
  }

  public void pushFile(String fileName)
  {
    // open include file
    InputStreamReader shellInputStream;
    try
    {
      shellInputStream = new InputStreamReader(new FileInputStream(fileName));
    }
    catch (FileNotFoundException exception)
    {
// TODO: antlr-bug "File 'backslash percentage s' not found" is not possible here
      throw new Error(String.format("File '"+fileName+"' not found"));
    }
    catch (IOException exception)
    {
// TODO: antlr-bug "File 'backslash percentage s' not found" is not possible here
      throw new Error(String.format("i/o error reading file '"+fileName+"'"));
    }

    // read include file
    try
    {
      // parse Shell file
      ANTLRReaderStream input = new ANTLRReaderStream(shellInputStream);
      ShellLexer shellLexer = new ShellLexer(input);
      LinkedListTokenSource tokenSource = new LinkedListTokenSource(shellLexer);
      LinkedListTokenStream tokenStream = new LinkedListTokenStream(tokenSource);

//      shellLexer.pushFile(new ShellFile(tokenStream));
    }
    catch (IOException exception)
    {
Dprintf.dprintf("exc "+exception);
    }
  }

  private void popFile()
  {
    ShellFile shellFile = fileStack.pop();
    setCharStream(shellFile.input);
    input.rewind(shellFile.mark);
  }
*/
}

// --- parser section --------------------------------------------------

// statement block
start
  : (statement1)+
  | (NEWLINE)*
  ;

statement1
  : command '|' statement1
  | command ';'
  | command NEWLINE
  | command EOF
  ;

/*
statement2
//  : pipedCommand '|' statement
  : pipedCommand ';'
  | pipedCommand NEWLINE
  | pipedCommand EOF
  ;
*/

// command
/*
pipedCommand
  : command (('|'^) command)*
  ;
*/

command
  : program redirect
  ;

// program
program
  : name=WORD argumentList=arguments
  ;

arguments
  : (argument)*
    -> (argument)*
  ;

argument
  : WORD
  | '-'
  ;

// redirect
redirect
  : INPUT WORD
  | OUTPUT WORD
  | OUTPUT_APPEND WORD
  | // no redirect
  ;

// -----------------------------

statementx
//  : import_ 
  : assignment
  | rule
  | NEWLINE
    -> // ignored
//  | ANY
//    {
//      Jsh.printError("xxxxx ratat");
//    }
  ;

// import
/*
import_
  : IMPORT expr ';'
    -> ^(IMPORT expr)
  ;
*/

// variable assignment
assignment
  : name=WORD '=' value=expr ';'
    {
      Jsh.printVerbose(2,"DEBUG: assign '\%s' -> \%s",((CommonTree)value.getTree()).toStringTree(),name.getText());
    }
    -> ^('=' WORD expr)
  ;

// rule
rule
  : name=WORD ':' dependencies=dependencyList '{' actions=actionList '}'
    {
      CommonTree commonTree;

      commonTree = (CommonTree)dependencies.getTree();
      if (commonTree != null)
      {
        Jsh.printVerbose(2,"DEBUG: rule '\%s' <- '\%s', line \%d:\%d",name.getText(),(commonTree != null)?commonTree.toStringTree():"(none)",name.getLine(),name.getCharPositionInLine());
      }
      else
      {
        Jsh.printVerbose(2,"DEBUG: rule '\%s', line \%d:\%d",name.getText(),name.getLine(),name.getCharPositionInLine());
      }
//      if (actions != null)
//      {
        commonTree = (CommonTree)actions.getTree();
        if (commonTree != null)
        {
//Dprintf.dprintf("commonTree=\%s \%s",actions,commonTree);
          List children = commonTree.getChildren();
          if (children != null)
          {
            for (Object object : children)
            {
              Token token = ((CommonTree)object).getToken();
              Jsh.printVerbose(3,"DEBUG:   \%s, line \%d:\%d",tokenNames[token.getType()],token.getLine(),token.getCharPositionInLine());
            }
          }
          else
          {
            Token token = commonTree.getToken();
            Jsh.printVerbose(3,"DEBUG:   \%s, line \%d:\%d",tokenNames[token.getType()],token.getLine(),token.getCharPositionInLine());
          }
        }
//      }
    }
    -> ^(RULE WORD dependencyList* actionList*)
  ;

// dependency block
dependencyList
  : (dependency)*
    -> (dependency)*
  ;

dependency
  : WORD
  ;

// action block
actionList
  : (action)*
    -> (action)*
  ;

action
  : command
  | javaCode
  ;

// command with arguments
commandx returns [Action action]
  : x=commandPartList ';'
    -> ^(COMMAND commandPartList*)
//-> ^({ new Action(Action.Types.COMMAND,null,null,0,"xxx") })
  ;

commandPartList
  : (commandPart)*
  ;

commandPart
  : WORD
  | number
  | string
  | '$' '(' expr ')'
/*
  | x='-' y=commandPart
    {
Dprintf.dprintf("x=\%s",x);
Dprintf.dprintf("y=\%s",y.getTree().toString());
String z = "-"+y.getTree().toString();
    }
    -> ^(STRINGLITERAL[z])
*/
  ;

// Java code
javaCode
  @init
  {
    CommonToken token;
  }
  : lbrace=LBRACE
    {
      String javaCode = getJavaCode();
      token = new CommonToken(JAVACODE,javaCode);
      token.setLine(lbrace.getLine());
      token.setCharPositionInLine(lbrace.getCharPositionInLine()+1);
    }
    -> { new CommonTree(token) }
  ;

// expression
expr
  : multExpr (('+'^|'-'^) multExpr)*
  ;

multExpr
  : atom (('*'|'/'|'%')^ atom)*
  ;

atom
  : '(' expr ')'
    -> expr
  | number
  | string
  | WORD
  ;

// scalars
number
  : DECIMAL_NUMBER
  | HEX_NUMBER
  | OCTAL_NUMBER
  | BINARY_NUMBER
  ;

string
  : CHARLITERAL
  | STRINGLITERAL
  ;

// --- lexer section ---------------------------------------------------

NEWLINE
  : '\r'? '\n'
//    {
//      skip();
//Dprintf.dprintf("");
//    }
  | '\\' '\r'? '\n'
//    {
//      skip();
//Dprintf.dprintf("");
//    }
  ;

// '/*...*/' comment
COMMENT
  @init
  {
    boolean isComment = false;
  }
  : '/*'
    {
      // look ahead one character
      if ((char)input.LA(1) == '*')
      {
        isComment = true;
      }
    }
    (options {greedy = false;} : . )*
    '*/'
    {
      if (isComment)
      {
        $channel = HIDDEN;
      }
      else
      {
        skip();
      }
    }
  ;

// '//' comment
LINE_COMMENT
  : ('//'|'#') ~('\n'|'\r')* ('\r\n'|'\r'|'\n')
    {
      skip();
    }
  | ('//'|'#') ~('\n'|'\r')* // comment at the end of the file without CR/LF
    {
      skip();
    }
    ;   

// white space
WHITESPACE
  : (' '|'\t')+
    {
      skip();
    }
  ;

// keywords
/*
IMPORT
  : 'import'
  ;
*/

// identifier
/*
IDENTIFIER
  : ('a'..'z'|'A'..'Z'|'_')('a'..'z'|'A'..'Z'|'_'|'0'..'9')*
  ;
*/

// decimal number
/*
DECIMAL_NUMBER
  : (  
       (('1'..'9')('0'..'9')*)
     | (('0'..'9')*'.'('0'..'9')*)
    )
  ;
*/

// hexa-decimal number
/*
HEX_NUMBER
  : '0x'('0'..'9'|'a'..'f'|'A'..'F')*
  ;
*/

// octal number
/*
OCTAL_NUMBER
  : '0'('0'..'7')*
  ;
*/

// binary number
/*
BINARY_NUMBER
  : '0b'('0'|'1')*
  ;
*/

/*
STRING
  : ~('\s'|'('|')'|'{'|'}'|'['|']')*
  ;
*/

// '...'
/*
CHARLITERAL
  : '\'' 
    (  EscapeSequence 
     | ~('\''|'\\')
    )
    '\''
  ;
*/

// string "..."
/*
STRINGLITERAL
  : '"'
    (  '\\' '\r'
     | '\\' '\n'
     | EscapeSequence
     | ~('\\'|'"')
    )* 
    '"'
  ;
*/

fragment
EscapeSequence 
  : '\\' (  'b' 
          | 't' 
          | 'n' 
          | 'f' 
          | 'r' 
          | '\"' 
          | '\'' 
          | '\\' 
          | ('0'..'3') ('0'..'7') ('0'..'7')
          | ('0'..'7') ('0'..'7') 
          | ('0'..'7')
         )         
  ;

// single characters
LPAREN
  : '('
  ;

RPAREN
  : ')'
  ;

LBRACE
  : '{'
  ;

RBRACE
  : '}'
  ;

LBRACKET
  : '['
  ;

RBRACKET
  : ']'
  ;

INPUT
  : '<'
  ;

OUTPUT
  : '>'
  ;

OUTPUT_APPEND
  : '>>'
  ;

WORD
  : ~(' '|'\t')+
  {
//Dprintf.dprintf("");
}
  ;

/*
ANY
  : .
  ;
*/

// end of file
