/*
 * Copyright (c) Eric D. Friedman 1998. All Rights Reserved.
 * Copyright (c) Paul Kinnucan 1998. All Rights Reserved.
 *
 * $Revision: 1.26 $ 
 * $Date: 2002/02/27 10:32:22 $ 
 *
 * InterfaceFactory is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation; either version 2, or (at
 * your option) any later version.
 *
 * InterfaceFactory is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * To obtain a copy of the GNU General Public License write to the
 * Free Software Foundation, Inc.,  59 Temple Place - Suite 330,
 * Boston, MA 02111-1307, USA.  
 */

package jde.wizards;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.io.PrintWriter;
import java.util.Hashtable;
import java.util.Vector;
import java.util.Enumeration;

/**
 * Defines a factory for creating  skeleton implementations of 
 * Java interfaces. The factory can be invoked from the command line
 * or from another program. The factory can generate implementations for
 * multiple interfaces when invoked from the command line.
 *
 * @author Eric D. Friedman and Paul Kinnucan
 * @version $Revision: 1.26 $
 */

public class MethodFactory
  implements ClassRegistry
{

  /** Unique storage of the classes to import */
  protected Hashtable imports = new Hashtable();

  /** A factory for generating parameter names */
  protected NameFactory namefactory = null;
  
  /**
   * Constructs a default method factory.
   */
  public MethodFactory()
  {
    this(new DefaultNameFactory());
  }

  /** 
   * Creates a method factory that uses the specified NameFactory
   * for generating parameter names 
   *
   * @param factory Factory for generating parameter names
   */
  public MethodFactory(NameFactory factory)
  {
    namefactory = factory;
  }

  /** 
   * Sets the factory for generating parameter names.
   *
   * @param factory Factory for generating parameter names.
   */
  public void setNameFactory(NameFactory factory)
  {
    namefactory = factory;
  }

  /** 
   * Gets the factory used to generating parameter names for 
   * methods generated by this interface factory.
   *
   * @return Name factory
   */
  public NameFactory getNameFactory()
  {
    return namefactory;
  }

  /** 
   * Gets a table containing the classes that must be imported to
   * implement an interface generated by this factory.
   *
   * @return Classes required to implement the current interface.
   */
  public Hashtable getImports()
  {
    return imports;
  }

  /** 
   * Registers a class that needs to be imported by the interface 
   * implementation generated by this factory. Store the class in the 
   * import hashtable if it passes the shouldImport test.  
   * Arrays have to be handled differently here. 
   *
   * @param register Imported class candidate
   */
  public void registerImport(Class register)
  {
    if (register.isArray())
    {
      try
      {
        Class cl = register;
        while (cl.isArray())
          cl = cl.getComponentType();
        register = cl;
      }
      catch (Throwable t)
      {
        throw new RuntimeException("Caught error walking up an Array object: " + t);
      }
    }
    if (shouldImport( register ))
      imports.put( register, "");
  }

  /**
   * Tests whether a specified class needs to be imported by the interface
   * implementation generated by this factory.
   * We don't import primitives or classes from java.lang.*  Since we do
   * have to import classes from java.lang.reflect (and whatever else
   * Javasoft may throw under java.lang.*, we have to study the package name
   * in more detail if it starts with java.lang
   *
   * @return <code>true</code> if the class should be imported
   */
  private final boolean shouldImport(Class c)
  {
    if (c.isPrimitive())        // do not import primitives
      return false;
    
    String name = c.getName();
    if ( (name.startsWith("java.lang")) )
    {
      char[] n = name.toCharArray();

      // Start at position ten, or after the final '.' in java.lang.
      for (int i = 10; i < n.length; i++)
        if (n[i] == '.')
          return true;          // must import packages below java.lang 
      return false;             // but nothing in java.lang itself
    }
    return true;                // import everything else
  }


  public String getMethodSkeleton(Signature sig,
				  boolean javadoc,
				  boolean newline,
				  String todo)
  {
    String res = "";
    res += "\n" ;
    if (javadoc)
      res += sig.toJavaDoc() + "\n";
    if (newline)
      {
	res += sig  + "\n";
	res += "{\n";
      }
    else
      res += sig + " {\n";
          
    res += todo;

    Method m = sig.getMethod();
    Class cl = m.getReturnType();

    if (! cl.getName().equals("void"))
      res += "  return null;\n";

    res += "}\n";
    return res;
  }

  /** 
   * Clears the import hashtables for this factory so it
   * can be re-used to process a new set of methods.
   */
  public void flush()
  {
    imports.clear();
  }

  public static void println(String s) {
    System.out.print(s + "\n");
    System.out.flush();
  }


} // MethodFactory

