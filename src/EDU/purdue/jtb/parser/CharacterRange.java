/* Copyright (c) 2006, Sun Microsystems, Inc.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright notice,
 *       this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the Sun Microsystems, Inc. nor the names of its
 *       contributors may be used to endorse or promote products derived from
 *       this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF
 * THE POSSIBILITY OF SUCH DAMAGE.
 */
package EDU.purdue.jtb.parser;

/**
 * Describes character range descriptors in a character list.
 * 
 * @author Marc Mazas
 * @version 1.4.0 : 05/2009 : MMa : adapted to JavaCC v4.2 grammar
 * @version 1.4.8 : 12/2014 : MMa : improved javadoc
 */
public class CharacterRange {

  /** The column number of the construct that corresponds most closely to this node */
  private int  column;
  /** The line number of the construct that corresponds most closely to this node */
  private int  line;
  /** The leftmost character in this character range */
  private char right;
  /** The rightmost character in this character range */
  private char left;

  /** Standard constructor */
  CharacterRange() {
  }

  /**
   * Constructor with parameters
   * 
   * @param l - the leftmost character in this character range
   * @param r - the rightmost character in this character range
   */
  CharacterRange(final char l, final char r) {
    if (l > r)
      JavaCCErrors.semantic_error(this, "Invalid range : \"" + (int) l + "\" - \"" + (int) r +
                                        "\". First character shoud be less than or equal to the second one in a range.");

    setLeft(l);
    setRight(r);
  }

  /**
   * @param ln - the line to set
   */
  final void setLine(final int ln) {
    line = ln;
  }

  /**
   * @return the line
   */
  final int getLine() {
    return line;
  }

  /**
   * @param cl - the column to set
   */
  final void setColumn(final int cl) {
    column = cl;
  }

  /**
   * @return the column
   */
  final int getColumn() {
    return column;
  }

  /**
   * @param lf - the left to set
   */
  public final void setLeft(final char lf) {
    left = lf;
  }

  /**
   * @return the left
   */
  public final char getLeft() {
    return left;
  }

  /**
   * @param rg - the right to set
   */
  public final void setRight(final char rg) {
    right = rg;
  }

  /**
   * @return the right
   */
  public final char getRight() {
    return right;
  }
}
