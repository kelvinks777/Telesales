package com.gin.ngeretail.telesales.Component.UI;

import java.util.ArrayList;
import java.util.List;

public class ScrollBroadcaster {
    private static List<ScrollListener> listenerList = new ArrayList<>();

    public static void registerReceiver(ScrollListener receiver) {
        boolean isNeedToAdd = true;
        for (ScrollListener rec : listenerList) {
            if (rec.equals(receiver)) {
                isNeedToAdd = false;
                break;
            }
        }
        if (isNeedToAdd) {
            listenerList.add(receiver);
        }
    }

    public static Boolean isListenerExist(ScrollListener receiver) {
        boolean isExist = false;
        for (ScrollListener rec : listenerList) {
            if (rec.equals(receiver)) {
                isExist = true;
                break;
            }
        }

        return isExist;

    }

    public static void unregisterAll() {
        listenerList.clear();
    }

    static void NotifyAllReceiver() {
        for (ScrollListener receiver : listenerList) {
            if (receiver != null) {
                receiver.onScrollIdle();
            }
        }
    }

    public interface ScrollListener {
        void onScrollIdle();
    }
}
