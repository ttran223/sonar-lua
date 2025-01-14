/*
 * SonarQube Lua Plugin
 * Copyright (C) 2016 SonarSource SA
 * mailto:fati.ahmadi66 AT gmail DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.sonar.lua.checks;

import com.sonar.sslr.api.AstNode;
import org.sonar.check.Priority;
import org.sonar.check.Rule;
import org.sonar.check.RuleProperty;
import org.sonar.lua.grammar.LuaGrammar;
import org.sonar.lua.api.LuaKeyword;
import org.sonar.lua.checks.utils.Tags;
import org.sonar.squidbridge.annotations.ActivatedByDefault;
import org.sonar.squidbridge.annotations.SqaleConstantRemediation;
import org.sonar.squidbridge.checks.SquidCheck;
import org.sonar.sslr.parser.LexerlessGrammar;

import javax.annotation.Nullable;

@Rule(
  key = "S134",
  name = "Control flow statements \"if\", \"for\", \"while\" should not be nested too deeply",
  priority = Priority.MAJOR,
  tags = Tags.BRAIN_OVERLOAD)
@ActivatedByDefault
@SqaleConstantRemediation("10min")
public class NestedControlFlowDepthCheck extends SquidCheck<LexerlessGrammar> {

  private int nestingLevel;

  private static final int DEFAULT_MAX = 10;

  @RuleProperty(
    key = "max",
    description = "Maximum allowed control flow statement nesting depth.",
    defaultValue = "" + DEFAULT_MAX)
  public int max = DEFAULT_MAX;

  public int getMax() {
    return max;
  }

  @Override
  public void init() {
    subscribeTo(
      LuaGrammar.IF_STATEMENT,
      LuaGrammar.DO_STATEMENT,
      LuaGrammar.WHILE_STATEMENT,
      LuaGrammar.FOR_STATEMENT
      );
    
  }

  @Override
  public void visitFile(@Nullable AstNode astNode) {
    nestingLevel = 0;
  }

  @Override
  public void visitNode(AstNode astNode) {
    if (!isElseIf(astNode)) {
      nestingLevel++;
      if (nestingLevel == getMax() + 1) {
        getContext().createLineViolation(this, "Refactor this code to not nest more than {0} if/for/while/ statements.", astNode, getMax());
      }
    }
  }

  @Override
  public void leaveNode(AstNode astNode) {
    if (!isElseIf(astNode)) {
      nestingLevel--;
    }
  }

  private static boolean isElseIf(AstNode astNode) {
    return astNode.getParent().getParent().getPreviousSibling() != null
      && astNode.getParent().getParent().getPreviousSibling().is(LuaKeyword.ELSE);
  }

}
