//==============================================================================
//	
//	Copyright (c) 2002-
//	Authors:
//	* Andrew Hinton <ug60axh@cs.bham.ac.uk> (University of Birmingham)
//	* Dave Parker <david.parker@comlab.ox.ac.uk> (University of Oxford, formerly University of Birmingham)
//	* Mark Kattenbelt <mark.kattenbelt@comlab.ox.ac.uk> (University of Oxford, formerly University of Birmingham)
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

package userinterface.properties;
import userinterface.GUIPrism;
import parser.*;
import parser.ast.*;
import prism.*;
import javax.swing.*;

/**
 *
 * @author  ug60axh
 */
public class GUIProperty
{
	//Constants
	
	/** A property state constant image */
	public static final ImageIcon IMAGE_NOT_DONE = GUIPrism.getIconFromImage("smallFilePrism.png");
	/** A property state constant image */
	public static final ImageIcon IMAGE_DOING = GUIPrism.getIconFromImage("smallClockAnim1.png");
	/** A property state constant image */
	public static final ImageIcon IMAGE_TICK = GUIPrism.getIconFromImage("smallTick.png");
	/** A property state constant image */
	public static final ImageIcon IMAGE_CROSS = GUIPrism.getIconFromImage("smallCross.png");
	/** A property state constant image */
	public static final ImageIcon IMAGE_ERROR = GUIPrism.getIconFromImage("smallError.png");
	/** A property state constant image */
	public static final ImageIcon IMAGE_INVALID = GUIPrism.getIconFromImage("smallWarning.png");
	/** A property state constant image */
	public static final ImageIcon IMAGE_NUMBER = GUIPrism.getIconFromImage("smallCompute.png");
	
	/** Property status */
	public static final int STATUS_NOT_DONE = 0;
	/** Property status */
	public static final int STATUS_DOING = 1;
	/** Property status */
	public static final int STATUS_PARSE_ERROR = 2;
	/** Property status */
	public static final int STATUS_RESULT_ERROR = 3;
	/** Property status */
	public static final int STATUS_RESULT_TRUE = 4;
	/** Property status */
	public static final int STATUS_RESULT_FALSE = 5;
	/** Property status */
	public static final int STATUS_RESULT_NUMBER = 6;
	
	
	//ATTRIBUTES
	
	private Prism prism;			// Required for parsing
	
	private String id;				// Unique ID
	private int status;				// Status - see constants above
	private ImageIcon doingImage;	// Image when in DOING state - can be modified externally for animations
	private boolean beingEdited;	// Is this property currently being edited?
	
	private String propString;		// String representing the property
	private Expression expr;		// The parsed property
	private String comment;			// The property's comment
	
	private Result result;			// Result of model checking etc. (if done, null if not)
	private String parseError;		// Parse error (if property is invalid)
	
	private String method;			// Method used (verification, simulation)
	private String constantsString;	// Constant values used
	
	/** Creates a new instance of GUIProperty */
	public GUIProperty(Prism prism, String id, String propString, String comment)
	{
		this.prism = prism;
		
		this.id = id;
		status = STATUS_NOT_DONE;
		doingImage = IMAGE_DOING;
		beingEdited = false;
		
		this.propString = propString;
		expr = null;
		this.comment = comment;
		
		result = null;
		parseError = "";
		method = "<none>";
		constantsString = "<none>";
	}
	
	//ACCESS METHODS
	
	public String getID()
	{
		return id;
	}
	
	public int getStatus()
	{
		return status;
	}
	
	public ImageIcon getImage()
	{
		switch (status) {
		case STATUS_NOT_DONE: return IMAGE_NOT_DONE;
		case STATUS_DOING: return doingImage;
		case STATUS_PARSE_ERROR: return IMAGE_INVALID;
		case STATUS_RESULT_ERROR: return IMAGE_ERROR;
		case STATUS_RESULT_TRUE: return IMAGE_TICK;
		case STATUS_RESULT_FALSE: return IMAGE_CROSS;
		case STATUS_RESULT_NUMBER: return IMAGE_NUMBER;
		default: return IMAGE_NOT_DONE;
		}
	}
	
	public boolean isBeingEdited()
	{
		return beingEdited;
	}
	
	public String getPropString()
	{
		return propString;
	}
	
	public Expression getProperty()
	{
		return expr;
	}
	
	public String getComment()
	{
		return comment;
	}
	
	public boolean isValid()
	{
		return expr != null;
	}
	
	// just a basic check - see if it's a P=? or R=? property
	
	public boolean isValidForSimulation()
	{
		boolean b = isValid() && (expr instanceof ExpressionProb || expr instanceof ExpressionReward);
		if(b)
		{
			if(expr instanceof ExpressionProb)
			{
				if ((((ExpressionProb)expr).getProb() != null)) 
				{
					return false;
				}
			}
			else if(expr instanceof ExpressionReward)
			{
				if ((((ExpressionReward)expr).getReward() != null)) 
				{
					return false;
				}
			}
			return true;
		}
		else return false;
	}
	
	public Result getResult()
	{
		return result;
	}
	
	public String getResultString()
	{
		return result == null ? "Unknown" : result.getResultString();
	}
	
	public String getToolTipText()
	{
		switch (status) {
		case STATUS_DOING: return "In progress...";
		case STATUS_PARSE_ERROR: return "Invalid property: " + parseError;
		case STATUS_RESULT_ERROR: return getResultString();
		default: return "Result: " + getResultString();
		}
	}
	
	public String getConstantsString()
	{
		return constantsString;
	}
	
	public String getMethodString()
	{
		return method;
	}
	
	public String toString()
	{
		return propString;
	}
	
	//UPDATE METHODS
	
	public void setStatus(int status)
	{
		this.status = status;
	}
	
	public void setDoingImage(ImageIcon image)
	{
		doingImage = image;
	}
	
	public void setPropString(String propString, ModulesFile m, String constantsString, String labelString)
	{
		this.propString = propString;
		setStatus(STATUS_NOT_DONE);
		parse(m, constantsString, labelString);
	}
	
	public void setComment(String comment)
	{
		this.comment = comment;
	}
	
	public void setBeingEdited(boolean beingEdited)
	{
		this.beingEdited = beingEdited;
	}
	
	public void setResult(Result res)
	{
		result = res;
		if (result.getResult() instanceof Boolean)
		{
			if (((Boolean)result.getResult()).booleanValue()) {
				setStatus(STATUS_RESULT_TRUE);
			} else {
				setStatus(STATUS_RESULT_FALSE);
			}
		}
		else if (result.getResult() instanceof Double)
		{
			setStatus(STATUS_RESULT_NUMBER);
		}
		else if (result.getResult() instanceof Exception)
		{
			setStatus(STATUS_RESULT_ERROR);
		}
		else
		{
			setStatus(STATUS_NOT_DONE);
			result = null;
		}
	}
	
	public void setMethodString(String method)
	{
		this.method = (method == null) ? "<none>" : method;
	}
	
	public void setConstants(Values mfConstants, Values pfConstants)
	{
		if (mfConstants != null && mfConstants.getNumValues() > 0) {
			constantsString = mfConstants.toString();
			if (pfConstants != null && pfConstants.getNumValues() > 0)
				constantsString += ", " + pfConstants.toString();
		}
		else if (pfConstants != null && pfConstants.getNumValues() > 0) {
			constantsString = pfConstants.toString();
		}
		else {
			constantsString = "<none>";
		}
	}
	
	public void parse(ModulesFile m, String constantsString, String labelString)
	{
		if(propString == null || constantsString == null || labelString == null)
		{
			expr = null;
			setStatus(STATUS_PARSE_ERROR);
			parseError = "(Unexpected) Properties, constants or labels are null";
			return;
		}
		try
		{
			//Parse constants and labels
			boolean couldBeNoConstantsOrLabels = false;
			PropertiesFile fConLab = null;
			try
			{
				fConLab = prism.parsePropertiesString(m, constantsString+"\n"+labelString);
			}
			catch(PrismException e)
			{
				couldBeNoConstantsOrLabels = true;
			}
			//Parse all together
			String withConsLabs = constantsString+"\n"+labelString+"\n"+propString;
			PropertiesFile ff = prism.parsePropertiesString(m, withConsLabs);
			
			//Validation of number of properties
			if(ff.getNumProperties() == 0) throw new PrismException("Empty Property");
			else if(ff.getNumProperties() > 1) throw new PrismException("Contains Multiple Properties");
			
			//Validation of constants and labels
			if(!couldBeNoConstantsOrLabels)
			{
				if(ff.getConstantList().size() != fConLab.getConstantList().size()) throw new PrismException("Contains constants");
				if(ff.getLabelList().size() != fConLab.getLabelList().size()) throw new PrismException("Contains labels");
			}
			else
			{
				if(ff.getConstantList().size() != 0) throw new PrismException("Contains constants");
				if(ff.getLabelList().size() != 0) throw new PrismException("Contains labels");
			}
			//Now set the property
			expr = ff.getProperty(0);
			parseError = "(Unexpected) no error!";
			// if status was previously a parse error, reset status.
			// otherwise, don't set status - reparse doesn't mean existing results should be lost
			if (getStatus() == STATUS_PARSE_ERROR) setStatus(STATUS_NOT_DONE);
		}
		catch(PrismException ex)
		{
			expr = null;
			setStatus(STATUS_PARSE_ERROR);
			parseError = ex.getMessage();
		}
	}
}
