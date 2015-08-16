package android.hardware.input;

import android.view.InputEvent;

public class InputManager {
    public static final int INJECT_INPUT_EVENT_MODE_ASYNC = 0;
    public static final int INJECT_INPUT_EVENT_MODE_WAIT_FOR_RESULT = 1; 
    public static final int INJECT_INPUT_EVENT_MODE_WAIT_FOR_FINISH = 2;

    public static InputManager getInstance() {
        return null;
    }
    public boolean injectInputEvent(InputEvent event, int mode) {
        return true;
    }
}
