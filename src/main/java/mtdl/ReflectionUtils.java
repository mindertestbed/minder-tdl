package mtdl;

import java.util.HashMap;

/**
 * This class provides helper methods for reflection
 *
 * @author yerlibilgin
 */
public class ReflectionUtils {
  public static final String keywords = "(abstract|continue|for|new|switch" + //
      "assert|default|if|package|synchronized" + //
      "boolean|do|goto|private|this" + //
      "break|double|implements|protected|throw" + //
      "byte|else|import|public|throws" + //
      "case|enum|instanceof|return|transient" + //
      "catch|extends|int|short|try" + //
      "char|final|interface|static|void" + //
      "class|finally|long|strictfp|volatile" + //
      "const|float|native|super|while)";

  public static final String JAVA_TYPE_EXP = "([a-zA-Z_]\\w*)(\\.([a-zA-Z_]\\w*))*(\\s*(\\[\\s*\\]))*";
  public static final String STARTS_WITH_KEYWORD = keywords + "\\..*";
  public static final String ENDS_WITH_KEYWORD = ".*\\." + keywords;

  public static final String INCLUDES_KEYWORD = ".*\\." + keywords + "\\..*";
  public static final String PRIMARY = keywords;

  /**
   * A validator that assumes that an array identifier would be like xyz.Abc[] or int[] <br>
   * int is a valid identifier but int.int is not
   *
   * @param str
   * @return
   */
  public static synchronized boolean isValidJavaType(String str) {
    if (str.matches(PRIMARY))
      return true;

    if (str.matches(JAVA_TYPE_EXP) && !str.matches(STARTS_WITH_KEYWORD)
        && !str.matches(ENDS_WITH_KEYWORD)
        && !str.matches(INCLUDES_KEYWORD)) {
      return true;
    }

    return false;
  }

  private static HashMap<String, Class<?>> primitiveMap = new HashMap<String, Class<?>>();
  private static HashMap<String, String> primitiveArrayMap = new HashMap<String, String>();

  static {
    primitiveMap.put("boolean", boolean.class);
    primitiveMap.put("byte", byte.class);
    primitiveMap.put("char", char.class);
    primitiveMap.put("short", short.class);
    primitiveMap.put("int", int.class);
    primitiveMap.put("float", float.class);
    primitiveMap.put("long", long.class);
    primitiveMap.put("double", double.class);
    primitiveArrayMap.put("boolean", "Z");
    primitiveArrayMap.put("byte", "B");
    primitiveArrayMap.put("char", "C");
    primitiveArrayMap.put("short", "S");
    primitiveArrayMap.put("int", "I");
    primitiveArrayMap.put("float", "F");
    primitiveArrayMap.put("long", "J");
    primitiveArrayMap.put("double", "D");
  }

  /**
   * converts the canonical name to a Java class
   *
   * @param cannonical The class descriptor (might be a primitive, an array and a
   *                   class)
   * @return the class that is resolved
   */
  public static Class<?> cannonical2Class(String cannonical, ClassLoader loader) {/*
    if (!isValidJavaType(cannonical))
      throw new IllegalArgumentException(cannonical + " is not a valid Java type");

    if (primitiveMap.containsKey(cannonical))
      return primitiveMap.get(cannonical);

    // if is array
    if (cannonical.contains("[")) {
      // get the part before [
      String firstPart = cannonical.substring(0, cannonical.indexOf('['))
          .trim();

      StringBuilder outer = new StringBuilder();
      char[] canArray = cannonical.toCharArray();
      int firstBracketIndex = -1;

      for (int i = canArray.length - 1; i >= 0; --i) {
        if (canArray[i] == '[') {
          outer.append('[');
          firstBracketIndex = i;
        }
      }

      if (primitiveArrayMap.containsKey(firstPart)) {
        try {
          String className = outer.append(
              primitiveArrayMap.get(firstPart)).toString();
          return TDLClassLoaderProvider.loadClass(className);
        } catch (Exception e) {
          throw new IllegalArgumentException(cannonical
              + " is not valid in this context", e);
        }
      } else {
        // not primitive
        try {
          String className = outer.append('L')
              .append(new String(canArray, 0, firstBracketIndex))
              .append(';').toString();
          return TDLClassLoaderProvider.loadClass(className);
        } catch (Exception e) {
          throw new IllegalArgumentException(cannonical
              + " is not valid in this context", e);
        }
      }
    } else {
      // not array, not primitive
      try {
        return TDLClassLoaderProvider.loadClass(cannonical);
      } catch (Exception e) {
        throw new IllegalArgumentException(cannonical
            + " is not valid in this context", e);
      }
    }*/

    return null;
  }
}
