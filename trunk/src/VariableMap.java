/***********************************************************************\
*
* $Source$
* $Revision$
* $Author$
* Contents:
* Systems:
*
\***********************************************************************/

/****************************** Imports ********************************/
import java.util.HashMap;

import groovy.lang.Binding;

/****************************** Classes ********************************/

public class VariableMap extends HashMap<String,String>
{
  // --------------------------- constants --------------------------------

  // --------------------------- variables --------------------------------
  private Binding binding;

  // ------------------------ native functions ----------------------------

  // ---------------------------- methods ---------------------------------

  /** create variable map
   */
  VariableMap()
  {
    this.binding = new Binding();
  }

  /** get Groovy binding for variable map
   * @return Groovy binding
   */
  public Binding getBinding()
  {
    return binding;
  }

  /** add variable
   * @param name variable name
   * @param value variable value
   */
  public void add(String name, String value)
  {
    super.put(name,value);
    binding.setVariable(name,value);
  }

  /** remove variable
   * @param name variable name
   */
  public void remove(String name)
  {
    super.remove(name);
//    binding.removeVariable(name);
  }

  /** get variable value
   * @param name variable name
   * @return variable value
   */
  public String get(String name)
  {
    return super.get(name);
  }

  /** set variable value
   * @param name variable name
   * @param value variable value
   */
  public void set(String name, String value)
  {
    put(name,value);
    binding.setVariable(name,value);
  }
}

/* end of file */
