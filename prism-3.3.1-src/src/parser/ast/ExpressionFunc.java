//==============================================================================
//	
//	Copyright (c) 2002-
//	Authors:
//	* Dave Parker <david.parker@comlab.ox.ac.uk> (University of Oxford, formerly University of Birmingham)
//	
//------------------------------------------------------------------------------
//	
//	This file is part of PRISM.
//	
//	PRISM is free software; you can redistribute it and/or modify
//	it under the terms of the GNU General Public License as published by
//	the Free Software Foundation; either version 2 of the License, or
//	(at your option) any later version.
//	
//	PRISM is distributed in the hope that it will be useful,
//	but WITHOUT ANY WARRANTY; without even the implied warranty of
//	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//	GNU General Public License for more details.
//	
//	You should have received a copy of the GNU General Public License
//	along with PRISM; if not, write to the Free Software Foundation,
//	Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
//	
//==============================================================================

package parser.ast;

import java.util.ArrayList;

import parser.*;
import parser.visitor.*;
import prism.PrismLangException;
import prism.PrismUtils;

public class ExpressionFunc extends Expression
{
	// Built-in function name constants
	public static final int MIN = 0;
	public static final int MAX = 1;
	public static final int FLOOR = 2;
	public static final int CEIL = 3;
	public static final int POW = 4;
	public static final int MOD = 5;
	public static final int LOG = 6;
	// Built-in function names
	public static final String names[] = { "min", "max", "floor", "ceil", "pow", "mod", "log" };
	// Min/max function arities
	public static final int minArities[] = { 2, 2, 1, 1, 2, 2, 2 };
	public static final int maxArities[] = { -1, -1, 1, 1, 2, 2, 2 };

	// Function name
	private String name = "";
	private int code = -1;
	// Operands
	private ArrayList<Expression> operands;
	// Was function written in old style notation (using "func" keyword)?
	private boolean oldStyle = false;

	// Constructors

	public ExpressionFunc()
	{
		operands = new ArrayList<Expression>();
	}

	public ExpressionFunc(String name)
	{
		setName(name);
		operands = new ArrayList<Expression>();
	}

	// Set methods

	public void setName(String s)
	{
		int i, n;
		// Set string
		name = s;
		// Determine and set code
		n = names.length;
		code = -1;
		for (i = 0; i < n; i++) {
			if (s.equals(names[i])) {
				code = i;
				break;
			}
		}
	}

	public void addOperand(Expression e)
	{
		operands.add(e);
	}

	public void setOperand(int i, Expression e)
	{
		operands.set(i, e);
	}

	public void setOldStyle(boolean b)
	{
		oldStyle = b;
	}

	// Get methods

	public String getName()
	{
		return name;
	}

	public int getNameCode()
	{
		return code;
	}

	public int getNumOperands()
	{
		return operands.size();
	}

	public Expression getOperand(int i)
	{
		return operands.get(i);
	}

	public boolean getOldStyle()
	{
		return oldStyle;
	}

	public int getMinArity()
	{
		return code == -1 ? Integer.MAX_VALUE : minArities[code];
	}

	public int getMaxArity()
	{
		return code == -1 ? -1 : maxArities[code];
	}

	// Methods required for Expression:

	/**
	 * Is this expression constant?
	 */
	public boolean isConstant()
	{
		int i, n;
		n = getNumOperands();
		for (i = 0; i < n; i++) {
			if (!getOperand(i).isConstant())
				return false;
		}
		return true;
	}

	/**
	 * Evaluate this expression, return result. Note: assumes that type checking
	 * has been done already.
	 */
	public Object evaluate(Values constantValues, Values varValues) throws PrismLangException
	{
		switch (code) {
		case MIN:
			return evaluateMin(constantValues, varValues);
		case MAX:
			return evaluateMax(constantValues, varValues);
		case FLOOR:
			return evaluateFloor(constantValues, varValues);
		case CEIL:
			return evaluateCeil(constantValues, varValues);
		case POW:
			return evaluatePow(constantValues, varValues);
		case MOD:
			return evaluateMod(constantValues, varValues);
		case LOG:
			return evaluateLog(constantValues, varValues);
		}
		throw new PrismLangException("Unknown function \"" + name + "\"", this);
	}

	private Object evaluateMin(Values constantValues, Values varValues) throws PrismLangException
	{
		int i, j, n, iMin;
		double d, dMin;

		if (type == Expression.INT) {
			iMin = getOperand(0).evaluateInt(constantValues, varValues);
			n = getNumOperands();
			for (i = 1; i < n; i++) {
				j = getOperand(i).evaluateInt(constantValues, varValues);
				iMin = (j < iMin) ? j : iMin;
			}
			return new Integer(iMin);
		} else {
			dMin = getOperand(0).evaluateDouble(constantValues, varValues);
			n = getNumOperands();
			for (i = 1; i < n; i++) {
				d = getOperand(i).evaluateDouble(constantValues, varValues);
				dMin = (d < dMin) ? d : dMin;
			}
			return new Double(dMin);
		}
	}

	private Object evaluateMax(Values constantValues, Values varValues) throws PrismLangException
	{
		int i, j, n, iMax;
		double d, dMax;

		if (type == Expression.INT) {
			iMax = getOperand(0).evaluateInt(constantValues, varValues);
			n = getNumOperands();
			for (i = 1; i < n; i++) {
				j = getOperand(i).evaluateInt(constantValues, varValues);
				iMax = (j > iMax) ? j : iMax;
			}
			return new Integer(iMax);
		} else {
			dMax = getOperand(0).evaluateDouble(constantValues, varValues);
			n = getNumOperands();
			for (i = 1; i < n; i++) {
				d = getOperand(i).evaluateDouble(constantValues, varValues);
				dMax = (d > dMax) ? d : dMax;
			}
			return new Double(dMax);
		}
	}

	public Object evaluateFloor(Values constantValues, Values varValues) throws PrismLangException
	{
		return new Integer((int) Math.floor(getOperand(0).evaluateDouble(constantValues, varValues)));
	}

	public Object evaluateCeil(Values constantValues, Values varValues) throws PrismLangException
	{
		return new Integer((int) Math.ceil(getOperand(0).evaluateDouble(constantValues, varValues)));
	}

	public Object evaluatePow(Values constantValues, Values varValues) throws PrismLangException
	{
		return new Double(Math.pow(getOperand(0).evaluateDouble(constantValues, varValues), getOperand(1)
				.evaluateDouble(constantValues, varValues)));
	}

	public Object evaluateMod(Values constantValues, Values varValues) throws PrismLangException
	{
		int i = getOperand(0).evaluateInt(constantValues, varValues);
		int j = getOperand(1).evaluateInt(constantValues, varValues);
		if (j == 0)
			throw new PrismLangException("Attempt to compute modulo zero", this);
		return new Integer(i % j);
	}

	public Object evaluateLog(Values constantValues, Values varValues) throws PrismLangException
	{
		double x, b;
		x = getOperand(0).evaluateDouble(constantValues, varValues);
		b = getOperand(1).evaluateDouble(constantValues, varValues);
		return new Double(PrismUtils.log(x, b));
	}

	// Methods required for ASTElement:

	/**
	 * Visitor method.
	 */
	public Object accept(ASTVisitor v) throws PrismLangException
	{
		return v.visit(this);
	}

	/**
	 * Convert to string.
	 */
	public String toString()
	{
		int i, n;
		String s = "";
		boolean first = true;

		if (!oldStyle)
			s += name + "(";
		else
			s += "func(" + name + ",";
		n = operands.size();
		for (i = 0; i < n; i++) {
			if (!first)
				s += ",";
			else
				first = false;
			s = s + getOperand(i);
		}
		s += ")";

		return s;
	}

	/**
	 * Perform a deep copy.
	 */
	public Expression deepCopy()
	{
		int i, n;
		ExpressionFunc e;

		e = new ExpressionFunc(name);
		e.setOldStyle(oldStyle);
		n = getNumOperands();
		for (i = 0; i < n; i++) {
			e.addOperand((Expression) getOperand(i).deepCopy());
		}
		e.setType(type);
		e.setPosition(this);

		return e;
	}
}

// ------------------------------------------------------------------------------
