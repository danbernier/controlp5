package controlP5;

/**
 * controlP5 is a processing gui library.
 *
 *  2006-2011 by Andreas Schlegel
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2.1
 * of the License, or (at your option) any later version.
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General
 * Public License along with this library; if not, write to the
 * Free Software Foundation, Inc., 59 Temple Place, Suite 330,
 * Boston, MA 02111-1307 USA
 *
 * @author 		Andreas Schlegel (http://www.sojamo.de)
 * @modified	##date##
 * @version		##version##
 *
 */

import java.util.ArrayList;

import processing.core.PApplet;

/**
 * A slider is either used horizontally or vertically. when adding a slider to controlP5, the width
 * is compared against the height. if the width is bigger, you get a horizontal slider, is the
 * height bigger, you get a vertical slider. a slider can have a fixed slider handle (one end of the
 * slider is fixed to the left or bottom side of the controller), or a flexible slider handle (a
 * handle you can drag).
 * 
 * 
 * @example ControlP5slider
 */
public class Slider extends Controller {

	private int _myDirection;

	public final static int FIX = 1;

	public final static int FLEXIBLE = 0;

	protected int _mySliderMode = FIX;

	protected float _myValuePosition;

	protected int _myHandleSize = 0;

	protected int _myDefaultHandleSize = 10;

	protected int triggerId = PRESSED;

	protected ArrayList<TickMark> _myTickMarks;

	protected boolean isShowTickMarks;

	protected boolean isSnapToTickMarks;

	protected static int autoWidth = 100;

	protected static int autoHeight = 10;

	protected int alignValueLabel = CENTER;

	public int valueLabelPositioning = FIX;

	private float scrollSensitivity = 0.1f;

	private int _myColorTickMark = 0xffffffff;

	/*
	 * TODO currently the slider value goes up and down linear, provide an option to make it
	 * logarithmic, potential, curved.
	 */
	/**
	 * 
	 * @example ControlP5slider
	 * 
	 * @param theControlP5 ControlP5
	 * @param theParent ControllerGroup
	 * @param theName String
	 * @param theMin float
	 * @param theMax float
	 * @param theDefaultValue float
	 * @param theX int
	 * @param theY int
	 * @param theWidth int
	 * @param theHeight int
	 */
	public Slider(ControlP5 theControlP5, ControllerGroup theParent, String theName, float theMin, float theMax, float theDefaultValue, int theX, int theY, int theWidth, int theHeight) {
		super(theControlP5, theParent, theName, theX, theY, theWidth, theHeight);
		_myCaptionLabel = new Label(cp5, theName);
		_myCaptionLabel.setColor(color.getCaptionLabel());
		_myMin = theMin;
		_myMax = theMax;

		// initialize the valueLabel with the longest string available, which is
		// either theMax or theMin.
		_myValueLabel = new Label(cp5, "" + (((adjustValue(_myMax)).length() > (adjustValue(_myMin)).length()) ? adjustValue(_myMax) : adjustValue(_myMin)));
		_myCaptionLabel.setColor(color.getValueLabel());

		// after initializing valueLabel, set the value to the default value.
		_myValueLabel.set("" + adjustValue(_myValue));
		_myValue = theDefaultValue;

		_myTickMarks = new ArrayList<TickMark>();
		setSliderMode(FIX);
		_myDirection = (width > height) ? HORIZONTAL : VERTICAL;
		if (_myDirection == HORIZONTAL) {
			alignValueLabel = CENTER;
			valueLabelPositioning = FIX;
		} else {
			valueLabelPositioning = FLEXIBLE;
		}
	}

	/**
	 * use the slider mode to set the mode of the slider bar, which can be Slider.FLEXIBLE or
	 * Slider.FIX
	 * 
	 * @param theMode int
	 */
	public void setSliderMode(int theMode) {
		_mySliderMode = theMode;
		if (_mySliderMode == FLEXIBLE) {
			_myHandleSize = (_myDefaultHandleSize >= getHeight() / 2) ? _myDefaultHandleSize / 2 : _myDefaultHandleSize;
		} else {
			_myHandleSize = 0;
		}
		_myUnit = (_myMax - _myMin) / ((width > height) ? width - _myHandleSize : height - _myHandleSize);
		setValue(_myValue);
	}

	/**
	 * sets the size of the Slider handle, by default it is set to either the width or height of the
	 * slider.
	 * 
	 * @param theSize
	 */
	public void setHandleSize(int theSize) {
		_myDefaultHandleSize = theSize;
		setSliderMode(_mySliderMode);
	}

	/**
	 * @see ControllerInterface.updateInternalEvents
	 * 
	 */
	@ControlP5.Invisible
	public Slider updateInternalEvents(PApplet theApplet) {
		if (isVisible) {
			if (isMousePressed && !cp5.keyHandler.isAltDown) {
				if (_myDirection == HORIZONTAL) {
					setValue(_myMin + (_myControlWindow.mouseX - (_myParent.getAbsolutePosition().x + position.x)) * _myUnit);
				} else {
					setValue(_myMin + (-(_myControlWindow.mouseY - (_myParent.getAbsolutePosition().y + position.y) - height)) * _myUnit);
				}
			}
		}
		return this;
	}

	/**
	 * the trigger event is set to Slider.PRESSED by default but can be changed to Slider.RELEASE so
	 * that events are triggered when the slider is released.
	 * 
	 * @param theEventID
	 */
	public void setTriggerEvent(int theEventID) {
		triggerId = theEventID;
	}

	/**
	 * returns the current trigger event which is either Slider.PRESSED or Slider.RELEASE
	 * 
	 * @return int
	 */
	public int getTriggerEvent() {
		return triggerId;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see controlP5.Controller#mouseReleased()
	 */
	@Override
	protected void mouseReleased() {
		if (triggerId == RELEASE) {

			if (_myDirection == HORIZONTAL) {
				setValue(_myMin + (_myControlWindow.mouseX - (_myParent.getAbsolutePosition().x + position.x)) * _myUnit);
			} else {
				setValue(_myMin + (-(_myControlWindow.mouseY - (_myParent.getAbsolutePosition().y + position.y) - height)) * _myUnit);
			}
			broadcast(FLOAT);
		}
	}

	protected void snapValue(float theValue) {
		if (isSnapToTickMarks) {
			_myValuePosition = ((_myValue - _myMin) / _myUnit);
			float n = PApplet.round(PApplet.map(_myValuePosition, 0, (_myDirection == HORIZONTAL) ? getWidth() : getHeight(), 0, _myTickMarks.size() - 1));
			_myValue = PApplet.map(n, 0, _myTickMarks.size() - 1, _myMin, _myMax);
		}
	}

	/**
	 * set the value of the slider.
	 * 
	 * @param theValue float
	 */
	@Override
	public Slider setValue(float theValue) {
		_myValue = theValue;
		snapValue(_myValue);
		_myValue = (_myValue <= _myMin) ? _myMin : _myValue;
		_myValue = (_myValue >= _myMax) ? _myMax : _myValue;
		_myValuePosition = ((_myValue - _myMin) / _myUnit);
		_myValueLabel.set(adjustValue(_myValue));
		if (triggerId == PRESSED) {
			broadcast(FLOAT);
		}
		return this;
	}

	/**
	 * assigns a random value to the slider.
	 */
	public Slider shuffle() {
		float r = (float) Math.random();
		setValue(PApplet.map(r, 0, 1, getMin(), getMax()));
		return this;
	}

	/**
	 * sets the sensitivity for the scroll behavior when using the mouse wheel or the scroll
	 * function of a multi-touch track pad. The smaller the value (closer to 0) the higher the
	 * sensitivity. by default this value is set to 0.1
	 * 
	 * @param theValue
	 * @return Slider
	 */
	public Slider setScrollSensitivity(float theValue) {
		scrollSensitivity = theValue;
		return this;
	}

	/**
	 * changes the value of the slider when hovering and using the mouse wheel or the scroll
	 * function of a multi-touch track pad.
	 * 
	 * @param theRotationValue
	 * @return Slider
	 */
	@ControlP5.Invisible
	public Slider scrolled(int theRotationValue) {
		float f = getValue();
		float steps = isSnapToTickMarks ? (1.0f / getNumberOfTickMarks()) : scrollSensitivity * 0.1f;
		f += (getMax() - getMin()) * (-theRotationValue * steps);
		setValue(f);
		return this;
	}

	@Override
	public Slider update() {
		return setValue(_myValue);
	}

	/**
	 * sets the minimum value of the slider.
	 * 
	 * @param theValue float
	 */
	@Override
	public Slider setMin(float theValue) {
		_myMin = theValue;
		setSliderMode(_mySliderMode);
		return this;
	}

	/**
	 * set the maximum value of the slider.
	 * 
	 * @param theValue float
	 */
	@Override
	public Slider setMax(float theValue) {
		_myMax = theValue;
		setSliderMode(_mySliderMode);
		return this;
	}

	/**
	 * set the width of the slider.
	 * 
	 * @param theValue int
	 */
	@Override
	public Slider setWidth(int theValue) {
		width = theValue;
		setSliderMode(_mySliderMode);
		return this;
	}

	/**
	 * set the height of the slider.
	 * 
	 * @param theValue int
	 */
	@Override
	public Slider setHeight(int theValue) {
		height = theValue;
		setSliderMode(_mySliderMode);
		return this;
	}

	/*
	 * TODO new implementations follow: http://www.ibm.com/developerworks/java/library/j-dynui/ take
	 * interface builder as reference
	 */
	protected void setTickMarks() {

	}

	/**
	 * sets the number of tickmarks for a slider, by default tick marks are turned off.
	 * 
	 * @param theNumber
	 */
	public Slider setNumberOfTickMarks(int theNumber) {
		_myTickMarks.clear();
		if (theNumber > 0) {
			for (int i = 0; i < theNumber; i++) {
				_myTickMarks.add(new TickMark(this));
			}
			showTickMarks(true);
			snapToTickMarks(true);
			setHandleSize(20);
		} else {
			showTickMarks(false);
			snapToTickMarks(false);
			setHandleSize(_myDefaultHandleSize);
		}
		setValue(_myValue);
		return this;
	}

	/**
	 * returns the amount of tickmarks available for a slider
	 * 
	 * @return int
	 */
	public int getNumberOfTickMarks() {
		return _myTickMarks.size();
	}

	/**
	 * shows or hides tickmarks for a slider
	 * 
	 * @param theFlag
	 * @return Slider
	 */
	public Slider showTickMarks(boolean theFlag) {
		isShowTickMarks = theFlag;
		return this;
	}

	/**
	 * enables or disables snap to tick marks.
	 * 
	 * @param theFlag
	 * @return Slider
	 */
	public Slider snapToTickMarks(boolean theFlag) {
		isSnapToTickMarks = theFlag;
		return this;
	}

	/**
	 * returns an instance of a tickmark by index.
	 * 
	 * @see TickMark
	 * @param theIndex
	 * @return
	 */
	public TickMark getTickMark(int theIndex) {
		if (theIndex >= 0 && theIndex < _myTickMarks.size()) {
			return _myTickMarks.get(theIndex);
		} else {
			return null;
		}
	}

	/**
	 * returns an ArrayList of available tick marks for a slider.
	 * 
	 * @return ArrayList<TickMark>
	 */
	public ArrayList<TickMark> getTickMarks() {
		return _myTickMarks;
	}

	/**
	 * use static variables ControlP5.TOP, ControlP5.CENTER, ControlP5.BOTTOM to align the
	 * ValueLabel of a slider.
	 * 
	 * @param theValue
	 */
	public Slider alignValueLabel(int theValue) {
		alignValueLabel = theValue;
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@ControlP5.Invisible
	public Slider linebreak() {
		cp5.linebreak(this, true, autoWidth, autoHeight, autoSpacing);
		return this;
	}

	/**
	 * sets the color of tick marks if enabled. by default the color is set to white.
	 * 
	 * @param theColor
	 * @return Slider
	 */
	public Slider setColorTickMark(int theColor) {
		_myColorTickMark = theColor;
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@ControlP5.Invisible
	public Slider updateDisplayMode(int theMode) {
		_myDisplayMode = theMode;
		switch (theMode) {
		case (DEFAULT):
			_myDisplay = new SliderDisplay();
			break;
		case (IMAGE):
			// TODO
			// _myDisplay = new ButtonImageDisplay();
			break;
		case (SPRITE):
			// TODO
			// _myDisplay = new ButtonSpriteDisplay();
			break;
		case (CUSTOM):
		default:
			break;
		}
		return this;
	}

	class SliderDisplay implements ControllerDisplay {

		public void display(PApplet theApplet, Controller theController) {
			theApplet.fill(color.getBackground());
			theApplet.noStroke();
			if ((color.getBackground() >> 24 & 0xff) > 0) {
				theApplet.rect(0, 0, width, height);
			}
			theApplet.fill(getIsInside() ? color.getActive() : color.getForeground());
			if (_myDirection == HORIZONTAL) {
				if (_mySliderMode == FIX) {
					theApplet.rect(0, 0, _myValuePosition, height);

				} else {
					if (isShowTickMarks) {
						theApplet.triangle(_myValuePosition, 0, _myValuePosition + _myHandleSize, 0, _myValuePosition + _myHandleSize / 2, getHeight());
					} else {

						theApplet.rect(_myValuePosition, 0, _myHandleSize, height);
					}

				}
				theApplet.fill(255);
			} else {
				if (_mySliderMode == FIX) {
					theApplet.rect(0, height, width, -_myValuePosition);
				} else {
					if (isShowTickMarks) {
						theApplet.triangle(width, height - _myValuePosition, width, height - _myValuePosition - _myHandleSize, 0, height - _myValuePosition - _myHandleSize / 2);
					} else {
						theApplet.rect(0, height - _myValuePosition - _myHandleSize, width, _myHandleSize);
					}
				}
			}

			if (isLabelVisible) {
				int py = 0;
				int px = 0;
				if (_myDirection == HORIZONTAL) {
					_myCaptionLabel.draw(theApplet, width + 3, height / 2 - 3);
					switch (alignValueLabel) {
					case (TOP):
						py = -10;
						break;
					case (CENTER):
					default:
						py = height / 2 - 3;
						px = 3;
						break;
					case (BOTTOM):
						py = height + 3;
						break;
					}
					_myValueLabel.draw(theApplet, (valueLabelPositioning == FIX) ? px : (int) (_myValuePosition), py);

				} else {
					_myCaptionLabel.draw(theApplet, 0, height + 3);
					switch (alignValueLabel) {
					case (TOP):
					default:
						py = -10;
						break;
					case (CENTER):
						py = height / 2 - 3;
						px = 3;
						break;
					case (BOTTOM):
						py = height + 3;
						break;
					}
					_myValueLabel.draw(theApplet, (valueLabelPositioning == FIX) ? 0 : width + 4, (valueLabelPositioning == FIX) ? py : -(int) _myValuePosition + height - 8);
				}
			}

			if (isShowTickMarks) {
				theApplet.pushMatrix();
				float n = (_myDirection == HORIZONTAL) ? getWidth() : getHeight();

				if (_myDirection == HORIZONTAL) {
					theApplet.translate((_mySliderMode == FIX) ? 0 : _myHandleSize / 2, getHeight());
				} else {
					theApplet.translate(-4, (_mySliderMode == FIX) ? 0 : _myHandleSize / 2);
				}
				theApplet.stroke(_myColorTickMark);
				float x = (n - ((_mySliderMode == FIX) ? 0 : _myHandleSize)) / (_myTickMarks.size() - 1);
				for (TickMark tm : _myTickMarks) {
					tm.draw(theApplet, _myDirection);
					if (_myDirection == HORIZONTAL) {
						theApplet.translate(x, 0);
					} else {
						theApplet.translate(0, x);
					}
				}
				theApplet.noStroke();
				theApplet.popMatrix();
			}
		}
	}

	@Deprecated
	public void setSliderBarSize(int theSize) {
		_myDefaultHandleSize = theSize;
		setSliderMode(_mySliderMode);
	}

	/**
	 * @see controlP5.Slider#setScrollSensitivity(float)
	 * 
	 * @param theValue
	 * @return Slider
	 */
	@Deprecated
	public Slider setSensitivity(float theValue) {
		return setScrollSensitivity(theValue);
	}

}