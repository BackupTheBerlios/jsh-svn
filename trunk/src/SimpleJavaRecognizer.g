grammar SimpleJavaRecognizer;

@parser::header
{
}

@parser::members
{

  private StringBuffer result;
  private int          braceCounter;

  /** create simple Java recognition parser
   * @param tokenStream token stream
   */
  SimpleJavaRecognizerParser(LinkedListTokenStream tokenStream)
  {
    super(tokenStream);

    result       = new StringBuffer();
    braceCounter = 1;
  }

  /** get Java code result
   * @return recognized Java code
   */
  public String getResult()
  {
    return result.toString();
  }

  /** print error message
   * @param message error message text
   */
  public void emitErrorMessage(String message)
  {
    System.err.println("ERROR: "+message);
  }

  public void xxxdisplayRecognitionError(String[]             tokenNames,
                                      RecognitionException exception
                                     )
  {
    String header  = getErrorHeader(exception);
    String message = getErrorMessage(exception,tokenNames);
    System.err.println("ERROR: "+header+" "+message);
  }
}

@lexer::header
{
}

@lexer::members
{
}

// --- parser section --------------------------------------------------

start
  : (statement
      {
//Dprintf.dprintf("statement "+braceCounter);
        if (braceCounter <= 0)
        {
          return;
        }
      }
    )*
  ;

statement
  : CHARLITERAL
    {
      result.append($CHARLITERAL.text);
    }
  | STRINGLITERAL
    {
      result.append($STRINGLITERAL.text);
    }
  | LBRACE
    {
//Dprintf.dprintf("LBRACE "+braceCounter);
      result.append($LBRACE.text);
      braceCounter++;
    }
  | RBRACE
    {
//Dprintf.dprintf("RBRACE "+braceCounter);
      braceCounter--;
      if (braceCounter > 0)
      {
        result.append($RBRACE.text);
      }
    }
  | CHAR
    {
      result.append($CHAR.text);
    }
  | comment
    {
      result.append($comment.value);
    }
  ;

comment returns [String value]
  : COMMENT
    {
      $value = $COMMENT.text;
//Dprintf.dprintf("comment1 "+$value);
    }
  | LINE_COMMENT
    {
      $value = $LINE_COMMENT.text;
//Dprintf.dprintf("comment2 "+$value);
    }
  ;

// --- lexer section ---------------------------------------------------

//NEWLINE
//  : '\r'? '\n'
//  ;

COMMENT
  @init
  {
    boolean isComment = false;
  }
  : '/*'
    {
      if ((char)input.LA(1) == '*')
      {
        isComment = true;
      }
    }
    (options {greedy = false;} : . )*
    '*/'
  ;

LINE_COMMENT
  : ('//'|'#') ~('\n'|'\r')* ('\r\n'|'\r'|'\n')
  | ('//'|'#') ~('\n'|'\r')* // comment at the end of the file without CR/LF
  ;

CHARLITERAL
  : '\''
    (  EscapeSequence
     | ~('\''|'\\'|'\r'|'\n')
    )
    '\''
//    { Dprintf.dprintf("CHARLITERAL "+getText()+"#"); }
  ;

STRINGLITERAL
  : '"'
    (  EscapeSequence
     | ~('\\'|'"'|'\r'|'\n')
    )*
    '"'
//    { Dprintf.dprintf("STRINGLITERAL "+getText()+"#"); }
  ;

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

LBRACE
  : '{'
//    { Dprintf.dprintf("LBRACE "+getText()+"#"); }
  ;

RBRACE
  : '}'
//    { Dprintf.dprintf("RBRACE "+getText()+"#"); }
  ;

CHAR
  : ~('{'|'}')
//    { Dprintf.dprintf("CHAR "+getText()+"#"); }
  ;

// end of file

