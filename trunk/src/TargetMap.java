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

/****************************** Classes ********************************/

public class TargetMap extends HashMap<String,Target>
{
  // --------------------------- constants --------------------------------

  // --------------------------- variables --------------------------------

  // ------------------------ native functions ----------------------------

  // ---------------------------- methods ---------------------------------

  /** create variable map
   */
  TargetMap()
  {
  }

  /** add target
   * @param name variable name
   * @param value variable value
   */
  public void add(String name, Target target)
  {
    super.put(name,target);
  }

  /** get variable value
   * @param name variable name
   * @return variable value
   */
  public Target get(String name)
  {
    return super.get(name);
  }
}

/* end of file */
