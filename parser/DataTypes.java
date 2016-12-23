package edu.emu;

public class DataTypes {
	public static enum ActionType {
		Load, LoadCap, UnLoad, Click, Change, MouseDown, Scroll, MouseMove, MouseOver, MouseOut, MouseUp, LocationChange0, LocationChange1, KeyPress, PageShow, PageHide, Focus, Blur, Resize, TabOpen, TabSelect, TabClose, Fixation;
		public static boolean contains(String actionType) {
			try {
				valueOf(actionType);
				return true;
			} catch (IllegalArgumentException e) {
				return false;
			}
		}
	}

	public static enum Version {
		v_xx, v_04, v_05, v_06, v_07, v_07c
	};
}
