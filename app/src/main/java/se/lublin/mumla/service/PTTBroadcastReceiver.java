/*
 * Copyright (C) 2014 Andrew Comminos
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package se.lublin.mumla.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Receives device-firmware PTT broadcasts sent by TID walkie-talkie devices (and compatible
 * Zello-protocol devices).  On these devices the firmware intercepts the hardware PTT key at
 * {@code PhoneWindowManager.interceptKeyBeforeQueueing()} and broadcasts the events before normal
 * input dispatch, so {@link PTTAccessibilityService} never sees the key press.
 *
 * Supported broadcast actions:
 * <ul>
 *   <li>{@code unipro.hotkey.ptt.down} / {@code unipro.hotkey.ptt.up} — TID/Unipro firmware</li>
 *   <li>{@code com.zello.ptt.down} / {@code com.zello.ptt.up} — Zello-compatible firmware</li>
 * </ul>
 *
 * This receiver is declared in the manifest so it is invoked even when the app is in the
 * background.  It simply forwards the event to {@link MumlaService} via a start-service intent.
 */
public class PTTBroadcastReceiver extends BroadcastReceiver {

    public static final String ACTION_UNIPRO_PTT_DOWN = "unipro.hotkey.ptt.down";
    public static final String ACTION_UNIPRO_PTT_UP   = "unipro.hotkey.ptt.up";
    public static final String ACTION_ZELLO_PTT_DOWN  = "com.zello.ptt.down";
    public static final String ACTION_ZELLO_PTT_UP    = "com.zello.ptt.up";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null) return;

        String serviceAction;
        switch (intent.getAction()) {
            case ACTION_UNIPRO_PTT_DOWN:
            case ACTION_ZELLO_PTT_DOWN:
                serviceAction = MumlaService.ACTION_PTT_DOWN;
                break;
            case ACTION_UNIPRO_PTT_UP:
            case ACTION_ZELLO_PTT_UP:
                serviceAction = MumlaService.ACTION_PTT_UP;
                break;
            default:
                return;
        }

        Intent serviceIntent = new Intent(context, MumlaService.class);
        serviceIntent.setAction(serviceAction);
        context.startService(serviceIntent);
    }
}
