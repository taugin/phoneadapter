package com.android.phoneadapter.event;

import java.lang.reflect.Field;
import java.lang.reflect.Method;


import android.hardware.input.InputManager;
import android.view.InputEvent;
import android.view.MotionEvent;

public abstract class Injection {
	public static final int INJECT_SUCCESS = 1;
	public static final int INJECT_FAIL = 0;

	public static int injectEvent(InputEvent me) {
		try {
			Class c = InputManager.class;

			// 获取InputManager实例
			Method method1;

			method1 = c.getMethod("getInstance", null);

			method1.setAccessible(true);
			Object obj1 = method1.invoke(null, null);
			InputManager inputmanager = (InputManager) obj1;

			// 获取变量
			Field field = c
					.getDeclaredField("INJECT_INPUT_EVENT_MODE_WAIT_FOR_RESULT");
			int value = field.getInt(c);

			// 获取成员方法
			Method method2 = c.getMethod("injectInputEvent", InputEvent.class,
					int.class);
			method2.setAccessible(true);
			Object obj2 = method2.invoke(inputmanager, me, value);
			boolean bool = (Boolean) obj2;

			// if (!InputManager.getInstance().injectInputEvent(me,
			// InputManager.INJECT_INPUT_EVENT_MODE_WAIT_FOR_RESULT)) {
			// return RTSEvent.INJECT_FAIL;
			// }
			if (!bool) {
				return INJECT_FAIL;
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		finally {
//			me.recycle();
		}
		return INJECT_SUCCESS;
	}
	// protected abstract String getTypeLabel();
}
