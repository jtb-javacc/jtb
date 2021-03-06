/**
 * Copyright (c) 2004,2005 UCLA Compilers Group. All rights reserved. Redistribution and use in
 * source and binary forms, with or without modification, are permitted provided that the following
 * conditions are met: Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer. Redistributions in binary form must reproduce
 * the above copyright notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution. Neither UCLA nor the names
 * of its contributors may be used to endorse or promote products derived from this software without
 * specific prior written permission. THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND
 * CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO
 * EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF
 * THE POSSIBILITY OF SUCH DAMAGE.
 **/
/*
 * All files in the distribution of JTB, The Java Tree Builder are Copyright 1997, 1998, 1999 by the
 * Purdue Research Foundation of Purdue University. All rights reserved. Redistribution and use in
 * source and binary forms are permitted provided that this entire copyright notice is duplicated in
 * all such copies, and that any documentation, announcements, and other materials related to such
 * distribution and use acknowledge that the software was developed at Purdue University, West
 * Lafayette, Indiana by Kevin Tao, Wanjun Wang and Jens Palsberg. No charge may be made for copies,
 * derivations, or distributions of this material without the express written consent of the
 * copyright holder. Neither the name of the University nor the name of the author may be used to
 * endorse or promote products derived from this material without specific prior written permission.
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND WITHOUT ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING,
 * WITHOUT LIMITATION, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR ANY PARTICULAR
 * PURPOSE.
 */
package EDU.purdue.jtb.misc;

import static EDU.purdue.jtb.misc.Globals.*;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import EDU.purdue.jtb.syntaxtree.ExpansionChoices;
import EDU.purdue.jtb.syntaxtree.INode;
import EDU.purdue.jtb.syntaxtree.NodeToken;
import EDU.purdue.jtb.visitor.CommentsPrinter;
import EDU.purdue.jtb.visitor.GlobalDataBuilder;

/**
 * Class {@link ClassInfo} is used by the visitors to store and ask for information about a class
 * including its name, the list of field types, names and initializers.
 * <p>
 * Uses class {@link CommentsPrinter} to find field javadoc comments and format them.<br>
 * 
 * @author Marc Mazas
 * @version 1.4.0 : 05-08/2009 : MMa : adapted to JavaCC v4.2 grammar and JDK 1.5
 * @version 1.4.6 : 01/2011 : FA/MMa : added -va and -npfx and -nsfx options
 * @version 1.4.7 : 09/2012 : MMa : refactored comment handling to add sub comments and optimization
 *          ; added the reference to the {@link GlobalDataBuilder}
 * @version 1.4.8 : 10/2012 : MMa : added JavaCodeProduction class generation ;<br>
 *          optimized common code 1.4.8 : 12/2014 : MMa : improved some debug printing
 */
public class ClassInfo {

  /** The {@link GlobalDataBuilder} visitor */
  final GlobalDataBuilder   gdbv;
  /** The corresponding ExpansionChoices node */
  public final INode        astEcNode;
  /** The class name (including optional prefix and suffix) */
  public final String       className;
  /** The list of the types of the class fields representing the node's children */
  public final List<String> fieldTypes;
  /** The list of the names of the class fields representing the node's children */
  public final List<String> fieldNames;
  /** The list of the initializers of the class fields representing the node's children */
  final List<String>        fieldInitializers;
  /**
   * True if the class allows specific initializing constructor(s) (without {@link NodeToken} nodes,
   * false otherwise
   */
  boolean                   needInitializingConstructor = false;
  /** The list of the java code elements in an EUTCF */
  final List<String>        fieldEUTCFCodes;
  /** The list of the field comments data */
  public List<CommentData>  fieldCmts                   = null;
  /**
   * The list of the sub comments data (without field comments data).<br>
   * Built and used only when the "inline accept methods" option is on.
   */
  public List<CommentData>  subCmts                     = null;
  /**
   * The javadoc formatted field comments used by the visit methods (more than once, so that's why
   * they are stored as an optimization)
   */
  public StringBuilder      visitFieldCmts              = null;
  /** The common code (that does not depend of the class) */
  String                    commonCode                  = null;

  /**
   * Constructs an instance giving an ExpansionChoices node and a name.
   * 
   * @param aEC - the ExpansionChoices node
   * @param aCN - the class name
   * @param aGdbv - the global data builder visitor
   */
  public ClassInfo(final ExpansionChoices aEC, final String aCN, final GlobalDataBuilder aGdbv) {
    astEcNode = aEC;
    className = getFixedName(aCN);
    if (aEC != null) {
      final int nb = (aEC.f1.present() ? aEC.f1.size() + 1 : 1);
      fieldTypes = new ArrayList<String>(nb);
      fieldNames = new ArrayList<String>(nb);
      fieldInitializers = new ArrayList<String>(nb);
      fieldEUTCFCodes = new ArrayList<String>(nb);
    } else {
      fieldTypes = null;
      fieldNames = null;
      fieldInitializers = null;
      fieldEUTCFCodes = null;
    }
    gdbv = aGdbv;
  }

  /**
   * Adds a field type, name (with no initializer) to the internal lists.
   * 
   * @param aFT - the field type
   * @param aFN - the field name
   */
  public void addField(final String aFT, final String aFN) {
    addField(aFT, aFN, null, null);
  }

  /**
   * Adds a field type, name, initializer, code and node to the internal lists.
   * 
   * @param aFT - the field type
   * @param aFN - the field name
   * @param aFI - the field initializer
   * @param aFEC - the field EUTCF code
   */
  public void addField(final String aFT, final String aFN, final String aFI, final String aFEC) {
    fieldTypes.add(aFT);
    fieldNames.add(aFN);

    if (aFI == null || aFI.equals(""))
      fieldInitializers.add(null);
    else {
      fieldInitializers.add(aFI);
      needInitializingConstructor = true;
    }

    if (aFEC == null || aFEC.equals(""))
      fieldEUTCFCodes.add(null);
    else
      fieldEUTCFCodes.add(aFEC);
  }

  /**
   * Append to a given buffer a set of javadoc comments showing a BNF description of the current
   * class. They include de debug comments (after the break) if they have been produced.
   * 
   * @param aSb - the buffer to append the BNF description to
   * @param aSpc - the indentation
   */
  public void fmtFieldsJavadocCmts(final StringBuilder aSb, final Spacing aSpc) {
    genCommentsData();
    if (fieldCmts == null)
      return;
    if (aSpc.indentLevel == 1) {
      // for visit methods that have an indentation of 1, store the result
      if (visitFieldCmts == null) {
        int len = 0;
        for (final CommentData fieldCmt : fieldCmts) {
          for (final CommentLineData line : fieldCmt.lines) {
            // 3 is length of " * "
            len += aSpc.spc.length() + 3 + line.bare.length() + BRLEN;
            if (line.debug != null)
              len += line.debug.length();
            len += BRLSLEN;
          }
        }
        visitFieldCmts = new StringBuilder(len);
        for (final CommentData fieldCmt : fieldCmts) {
          for (final CommentLineData line : fieldCmt.lines) {
            visitFieldCmts.append(aSpc.spc).append(" * ").append(line.bare);
            if (line.debug != null)
              visitFieldCmts.append(line.debug);
            visitFieldCmts.append(BRLS);
          }
        }
      }
      aSb.append(visitFieldCmts);
    } else
      // other cases
      for (final CommentData fieldCmt : fieldCmts) {
        for (final CommentLineData line : fieldCmt.lines) {
          aSb.append(aSpc.spc).append(" * ").append(line.bare);
          if (line.debug != null)
            aSb.append(line.debug);
          aSb.append(BRLS);
        }
      }
    return;
  }

  /**
   * Append to a given buffer a java code comment showing a BNF description of the current field.
   * They do not include the debug comments even if they have been produced.
   * 
   * @param aSb - the buffer to append the BNF description to
   * @param aSpc - the indentation
   * @param i - the field index
   * @param aStr - an additional comment
   */
  @SuppressWarnings("unused")
  public void fmt1JavacodeFieldCmt(final StringBuilder aSb, final Spacing aSpc, final int i,
                                   final String aStr) {
    genCommentsData();
    if (fieldCmts == null)
      return;
    final CommentData fieldCmt = fieldCmts.get(i);
    for (final CommentLineData line : fieldCmt.lines) {
      aSb.append(aSpc.spc).append("// ").append(line.bare);
      if (DEBUG_FIELD_AND_SUB_COMMENTS && aStr != null)
        aSb.append(" ; ").append(aStr);
      aSb.append(LS);
    }
    return;
  }

  /**
   * Append to a given buffer a java code comment showing a BNF description of the current part.
   * They do not include the debug comments even if they have been produced.
   * 
   * @param aSb - the buffer to append the BNF description to
   * @param aSpc - the indentation
   * @param i - the sub comment index
   * @param aStr - an additional comment
   */
  @SuppressWarnings("unused")
  public void fmt1JavacodeSubCmt(final StringBuilder aSb, final Spacing aSpc, final int i,
                                 final String aStr) {
    //    genCommentsData();
    if (subCmts == null)
      return;
    if (i >= subCmts.size()) {
      aSb.append(aSpc.spc).append("// invalid sub comment index (").append(i).append("), size = ")
         .append(subCmts.size());
      if (aStr != null)
        aSb.append(" ; ").append(aStr);
      aSb.append(LS);
    } else {
      final CommentData subCmt = subCmts.get(i);
      for (final CommentLineData line : subCmt.lines) {
        aSb.append(aSpc.spc).append("// ").append(line.bare);
        if (DEBUG_FIELD_AND_SUB_COMMENTS && aStr != null)
          aSb.append(" ; ").append(aStr);
        aSb.append(LS);
      }
    }
    return;
  }

  /**
   * Generates if not already done the comments trees by calling the {@link CommentsPrinter} visitor
   * on itself.
   */
  void genCommentsData() {
    if (fieldCmts == null) {
      gdbv.getCpv().genCommentsData(this);
    }
  }

  /**
   * Generates the node class code into a newly allocated buffer.
   * 
   * @param aSpc - the current indentation
   * @return the buffer with the node class code
   */
  public StringBuilder genClassString(final Spacing aSpc) {
    if (fieldTypes != null) {
      // normal case, for BNFProductions
      return genBNFProductionClassString(aSpc);
    } else {
      // specific case for JavaCodeProductions
      return genJavaCodeProductionClassString(aSpc);
    }
  }

  /**
   * Generates the node class code for a BNFProduction into a newly allocated buffer.
   * 
   * @param aSpc - the current indentation
   * @return the buffer with the node class code
   */
  StringBuilder genBNFProductionClassString(final Spacing aSpc) {

    Iterator<String> types = fieldTypes.iterator();
    Iterator<String> names = fieldNames.iterator();
    Iterator<String> inits;
    final StringBuilder sb = new StringBuilder(2048);

    /*
     * class declaration
     */

    sb.append(aSpc.spc).append("public class " + className);

    if (nodesSuperclass != null)
      sb.append(" extends ").append(nodesSuperclass);
    sb.append(" implements ").append(iNode).append(" {").append(LS).append(LS);
    aSpc.updateSpc(+1);

    /*
     * data fields declarations
     */

    for (int i = 1; types.hasNext(); i++) {
      if (javaDocComments)
        sb.append(aSpc.spc).append("/** Child node " + i + " */").append(LS);
      sb.append(aSpc.spc).append("public ").append(types.next()).append(" ").append(names.next())
        .append(";").append(LS).append(LS);
    }

    if (parentPointer) {
      if (javaDocComments)
        sb.append(aSpc.spc).append("/** The parent pointer */").append(LS);
      sb.append(aSpc.spc).append("private ").append(iNode).append(" parent;").append(LS).append(LS);
    }

    if (javaDocComments)
      sb.append(aSpc.spc).append("/** The serial version UID */").append(LS);
    sb.append(aSpc.spc).append("private static final long serialVersionUID = ")
      .append(SERIAL_UID + "L;").append(LS).append(LS);

    /*
     * standard constructor header
     */

    if (javaDocComments) {
      sb.append(aSpc.spc).append("/**").append(LS);
      sb.append(aSpc.spc).append(" * Constructs the node with ");
      sb.append(fieldTypes.size() > 1 ? "all its children nodes." : "its child node.").append(LS);
      sb.append(aSpc.spc).append(" *").append(LS);
      types = fieldTypes.iterator();
      sb.append(aSpc.spc).append(" * @param n0 - ").append(fieldTypes.size() > 1 ? "first" : "the")
        .append(" child node").append(LS);
      for (int i = 1; i < fieldTypes.size(); i++)
        sb.append(aSpc.spc).append(" * @param n").append(i).append(" - next child node").append(LS);
      sb.append(aSpc.spc).append(" */").append(LS);
    }
    sb.append(aSpc.spc).append("public ").append(className).append("(");
    types = fieldTypes.iterator();
    if (types.hasNext())
      sb.append("final ").append(types.next()).append(" n0");
    for (int i = 1; types.hasNext(); i++)
      sb.append(", final ").append(types.next()).append(" n").append(i);
    sb.append(") {").append(LS);

    /*
     * standard constructor body
     */

    names = fieldNames.iterator();
    aSpc.updateSpc(+1);
    for (int count = 0; names.hasNext(); ++count) {
      final String nm = names.next();
      sb.append(aSpc.spc).append(nm).append(" = n").append(count).append(";").append(LS);
      if (parentPointer) {
        sb.append(aSpc.spc).append("if (").append(nm).append(" != null)").append(LS);
        aSpc.updateSpc(+1);
        sb.append(aSpc.spc).append(nm).append(".setParent(this);").append(LS);
        aSpc.updateSpc(-1);
      }
    }

    aSpc.updateSpc(-1);
    sb.append(aSpc.spc).append("}").append(LS);

    /*
     * specific initializing constructor header if necessary
     */

    if (needInitializingConstructor) {
      int count = 0;
      boolean firstTime = true;
      sb.append(LS);
      if (javaDocComments) {
        sb.append(aSpc.spc).append("/**").append(LS);
        sb.append(aSpc.spc).append(" * Constructs the node with only its non ").append(nodeToken);
        sb.append(" child node").append(fieldTypes.size() > 1 ? "(s)." : ".").append(LS);
        sb.append(aSpc.spc).append(" *").append(LS);
        types = fieldTypes.iterator();
        inits = fieldInitializers.iterator();
        while (types.hasNext()) {
          types.next();
          if (inits.next() == null) {
            if (!firstTime)
              sb.append(aSpc.spc).append(" * @param n").append(count).append(" - next child node")
                .append(LS);
            else
              sb.append(aSpc.spc).append(" * @param n").append(count).append(" - first child node")
                .append(LS);
            ++count;
            firstTime = false;
          }
        }
        sb.append(aSpc.spc).append(" */").append(LS);
      }
      sb.append(aSpc.spc).append("public ").append(className).append("(");
      count = 0;
      firstTime = true;
      types = fieldTypes.iterator();
      inits = fieldInitializers.iterator();
      while (types.hasNext()) {
        final String type = types.next();
        if (inits.next() == null) {
          if (!firstTime)
            sb.append(", final ");
          else
            sb.append("final ");
          sb.append(type).append(" n").append(count);
          ++count;
          firstTime = false;
        }
      }

      sb.append(") {").append(LS);
    }

    /*
     * specific initializing constructor body if necessary
     */

    if (needInitializingConstructor) {
      int count = 0;
      names = fieldNames.iterator();
      inits = fieldInitializers.iterator();
      aSpc.updateSpc(+1);
      while (names.hasNext()) {
        final String nm = names.next();
        final String init = inits.next();
        if (init != null)
          sb.append(aSpc.spc).append(nm).append(" = ").append(init).append(";").append(LS);
        else {
          sb.append(aSpc.spc).append(nm).append(" = n").append(count).append(";").append(LS);
          ++count;
        }
        if (parentPointer) {
          sb.append(aSpc.spc).append("if (").append(nm).append(" != null)").append(LS);
          aSpc.updateSpc(+1);
          sb.append(aSpc.spc).append("  ").append(nm).append(".setParent(this);").append(LS);
          aSpc.updateSpc(-1);
        }
      }
      aSpc.updateSpc(-1);
      sb.append(aSpc.spc).append("}").append(LS);
    }

    /*
     * Visit methods, parent methods, end class
     */
    sb.append(getCommonCode(aSpc));

    return sb;

  }

  /**
   * Generates the node class code for a JavaCodeProduction into a newly allocated buffer.
   * 
   * @param aSpc - the current indentation
   * @return the buffer with the node class code
   */
  StringBuilder genJavaCodeProductionClassString(final Spacing aSpc) {

    final StringBuilder sb = new StringBuilder(1024);

    /*
     * class declaration
     */

    sb.append(aSpc.spc).append("public class " + className);

    if (nodesSuperclass != null)
      sb.append(" extends ").append(nodesSuperclass);
    sb.append(" implements ").append(iNode).append(" {").append(LS).append(LS);
    aSpc.updateSpc(+1);

    /*
     * data fields declarations
     */

    if (parentPointer) {
      if (javaDocComments)
        sb.append(aSpc.spc).append("/** The parent pointer */").append(LS);
      sb.append(aSpc.spc).append("private ").append(iNode).append(" parent;").append(LS).append(LS);
    }

    if (javaDocComments)
      sb.append(aSpc.spc).append("/** The serial version UID */").append(LS);
    sb.append(aSpc.spc).append("private static final long serialVersionUID = ")
      .append(SERIAL_UID + "L;").append(LS).append(LS);

    /*
     * standard constructor
     */

    if (javaDocComments) {
      sb.append(aSpc.spc).append("/**").append(LS);
      sb.append(aSpc.spc).append(" * Constructs the node (which has no child).").append(LS);
      sb.append(aSpc.spc).append(" */").append(LS);
    }
    sb.append(aSpc.spc).append("public ").append(className).append("() {").append(LS);
    sb.append(aSpc.spc).append("}").append(LS);

    /*
     * Visit methods, parent methods, end class
     */
    sb.append(getCommonCode(aSpc));

    return sb;
  }

  /**
   * Gets the common code, generating it the first time (visit methods, parent methods, end class).
   * 
   * @param aSpc - the current indentation
   * @return the common code
   */
  String getCommonCode(final Spacing aSpc) {
    if (commonCode == null)
      commonCode = genCommonCode(aSpc);
    return commonCode;
  }

  /**
   * Generates common code (visit methods, parent methods, end class).
   * 
   * @param aSpc - the current indentation
   * @return the generated common code
   */
  static String genCommonCode(final Spacing aSpc) {

    final StringBuilder sb = new StringBuilder(1024);

    /*
     * visit methods
     */

    sb.append(LS);
    if (javaDocComments) {
      sb.append(aSpc.spc).append("/**").append(LS);
      sb.append(aSpc.spc).append(" * Accepts the ").append(iRetArguVisitor).append(" visitor.")
        .append(LS);
      sb.append(aSpc.spc).append(" *").append(LS);
      sb.append(aSpc.spc).append(" * @param <").append(genRetType).append("> the user return type")
        .append(LS);
      sb.append(aSpc.spc).append(" * @param <").append(genArguType)
        .append("> the user argument type").append(LS);
      sb.append(aSpc.spc).append(" * @param vis - the visitor").append(LS);
      sb.append(aSpc.spc).append(" * @param argu - a user chosen argument").append(LS);
      sb.append(aSpc.spc).append(" * @return a user chosen return information").append(LS);
      sb.append(aSpc.spc).append(" */").append(LS);
    }
    sb.append(aSpc.spc).append("@Override").append(LS);
    sb.append(aSpc.spc).append("public <").append(genRetType).append(", ").append(genArguType)
      .append("> " + genRetType).append(" accept(final ").append(iRetArguVisitor)
      .append("<" + genRetType).append(", ").append(genArguType)
      .append("> vis, final " + (varargs ? genArgusType : genArguType)).append(" argu) {")
      .append(LS);
    aSpc.updateSpc(+1);
    sb.append(aSpc.spc).append("return vis.visit(this, argu);").append(LS);
    aSpc.updateSpc(-1);
    sb.append(aSpc.spc).append("}").append(LS);

    sb.append(LS);
    if (javaDocComments) {
      sb.append(aSpc.spc).append("/**").append(LS);
      sb.append(aSpc.spc).append(" * Accepts the ").append(iRetVisitor).append(" visitor.")
        .append(LS);
      sb.append(aSpc.spc).append(" *").append(LS);
      sb.append(aSpc.spc).append(" * @param <").append(genRetType).append("> the user return type")
        .append(LS);
      sb.append(aSpc.spc).append(" * @param vis - the visitor").append(LS);
      sb.append(aSpc.spc).append(" * @return a user chosen return information").append(LS);
      sb.append(aSpc.spc).append(" */").append(LS);
    }
    sb.append(aSpc.spc).append("@Override").append(LS);
    sb.append(aSpc.spc).append("public <").append(genRetType).append("> ")
      .append(genRetType + " accept(final ").append(iRetVisitor).append("<").append(genRetType)
      .append("> vis) {").append(LS);
    aSpc.updateSpc(+1);
    sb.append(aSpc.spc).append("return vis.visit(this);").append(LS);
    aSpc.updateSpc(-1);
    sb.append(aSpc.spc).append("}").append(LS);

    sb.append(LS);
    if (javaDocComments) {
      sb.append(aSpc.spc).append("/**").append(LS);
      sb.append(aSpc.spc).append(" * Accepts the ").append(iVoidArguVisitor).append(" visitor.")
        .append(LS);
      sb.append(aSpc.spc).append(" *").append(LS);
      sb.append(aSpc.spc).append(" * @param <").append(genArguType)
        .append("> the user argument type").append(LS);
      sb.append(aSpc.spc).append(" * @param vis - the visitor").append(LS);
      sb.append(aSpc.spc).append(" * @param argu - a user chosen argument").append(LS);
      sb.append(aSpc.spc).append(" */").append(LS);
    }
    sb.append(aSpc.spc).append("@Override").append(LS);
    sb.append(aSpc.spc).append("public <").append(genArguType)
      .append("> void accept(final " + iVoidArguVisitor).append("<").append(genArguType)
      .append("> vis, final " + (varargs ? genArgusType : genArguType)).append(" argu) {")
      .append(LS);
    aSpc.updateSpc(+1);
    sb.append(aSpc.spc).append("vis.visit(this, argu);").append(LS);
    aSpc.updateSpc(-1);
    sb.append(aSpc.spc).append("}").append(LS);

    sb.append(LS);
    if (javaDocComments) {
      sb.append(aSpc.spc).append("/**").append(LS);
      sb.append(aSpc.spc).append(" * Accepts the ").append(iVoidVisitor).append(" visitor.")
        .append(LS);
      sb.append(aSpc.spc).append(" *").append(LS);
      sb.append(aSpc.spc).append(" * @param vis - the visitor").append(LS);
      sb.append(aSpc.spc).append(" */").append(LS);
    }
    sb.append(aSpc.spc).append("@Override").append(LS);
    sb.append(aSpc.spc).append("public void accept(final ").append(iVoidVisitor).append(" vis) {")
      .append(LS);
    aSpc.updateSpc(+1);
    sb.append(aSpc.spc).append("vis.visit(this);").append(LS);
    aSpc.updateSpc(-1);
    sb.append(aSpc.spc).append("}").append(LS);

    /*
     * parent getter & setter methods
     */

    if (parentPointer) {
      sb.append(LS);
      if (javaDocComments) {
        sb.append(aSpc.spc).append("/**").append(LS);
        sb.append(aSpc.spc).append(" * Setter for the parent node.").append(LS);
        sb.append(aSpc.spc).append(" *").append(LS);
        sb.append(aSpc.spc).append(" * @param n - the parent node").append(LS);
        sb.append(aSpc.spc).append(" */").append(LS);
      }
      sb.append(aSpc.spc).append("public void setParent(final ").append(iNode).append(" n) {")
        .append(LS);
      aSpc.updateSpc(+1);
      sb.append(aSpc.spc).append("parent = n;").append(LS);
      aSpc.updateSpc(-1);
      sb.append(aSpc.spc).append("}").append(LS);
      sb.append(LS);
      if (javaDocComments) {
        sb.append(aSpc.spc).append("/**").append(LS);
        sb.append(aSpc.spc).append(" * Getter for the parent node.").append(LS);
        sb.append(aSpc.spc).append(" *").append(LS);
        sb.append(aSpc.spc).append(" * @return the parent node").append(LS);
        sb.append(aSpc.spc).append(" */").append(LS);
      }
      sb.append(aSpc.spc).append("public ").append(iNode).append(" getParent() {").append(LS);
      aSpc.updateSpc(+1);
      sb.append(aSpc.spc).append("return parent;").append(LS);
      aSpc.updateSpc(-1);
      sb.append(aSpc.spc).append("}").append(LS);
    }

    /*
     * end
     */

    aSpc.updateSpc(-1);
    sb.append(aSpc.spc).append(LS);
    sb.append(aSpc.spc).append("}").append(LS);

    return sb.toString();
  }

  /**
   * Holds the data of the lines of a comment or sub comment.
   */
  public class CommentData {

    /** The list of the lines */
    public List<CommentLineData> lines = null;

  }

  /**
   * Holds the data of a line of a comment or sub comment.
   */
  public class CommentLineData {

    /** The node's bare comment (should be never null after processing) */
    public String bare  = null;
    /** The node's debug comment (null if none, or starts with " //") */
    public String debug = null;
  }

}
