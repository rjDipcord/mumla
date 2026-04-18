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
import android.view.KeyEvent;

import se.lublin.mumla.Settings;

/**
 * Receives device-firmware PTT broadcasts sent by TID walkie-talkie devices (and compatible
 * Zello-protocol devices).  On these devices the firmware intercepts hardware keys at
 * {@code PhoneWindowManager.interceptKeyBeforeQueueing()} and broadcasts the events before normal
 * input dispatch, so {@link PTTAccessibilityService} never sees the key presses.
 *
 * <p>Each broadcast is only forwarded if the user has assigned the matching key code in
 * Settings → Push-to-talk key.  Supported broadcast → keycode mappings:
 * <ul>
 *   <li>{@code unipro.hotkey.ptt.*} / {@code com.zello.ptt.*} → KEYCODE_F5 (135) — large PTT button</li>
 *   <li>{@code unipro.hotkey.help.*} → KEYCODE_F3 (133) — upper side key</li>
 *   <li>{@code unipro.hotkey.p2.*}   → KEYCODE_F4 (134) — lower side key</li>
 * </ul>
 *
 * This receiver is declared in the manifest so it is invoked even when the app is in the
 * background.  It forwards events to {@link MumlaService} via start-service intents.
 */
public class PTTBroadcastReceiver extends BroadcastReceiver {

    public static final String ACTION_UNIPRO_PTT_DOWN  = "unipro.hotkey.ptt.down";
    public static final String ACTION_UNIPRO_PTT_UP    = "unipro.hotkey.ptt.up";
    public static final String ACTION_ZELLO_PTT_DOWN   = "com.zello.ptt.down";
    public static final String ACTION_ZELLO_PTT_UP     = "com.zello.ptt.up";
    public static final String ACTION_UNIPRO_HELP_DOWN = "unipro.hotkey.help.down";
    public static final String ACTION_UNIPRO_HELP_UP   = "unipro.hotkey.help.up";
    public static final String ACTION_UNIPRO_P2_DOWN   = "unipro.hotkey.p2.down";
    public static final String ACTION_UNIPRO_P2_UP     = "unipro.hotkey.p2.up";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null) return;

        String action = intent.getAction();
        if (action == null) return;

        String serviceAction;
        switch (action) {
            case ACTION_UNIPRO_PTT_DOWN:
            case ACTION_ZELLO_PTT_DOWN:
                if (!isPttKeyConfigured(context, KeyEvent.KEYCODE_F5)) return;
                serviceAction = MumlaService.ACTION_PTT_DOWN;
                break;
            case ACTION_UNIPRO_PTT_UP:
            case ACTION_ZELLO_PTT_UP:
                if (!isPttKeyConfigured(context, KeyEvent.KEYCODE_F5)) return;
                serviceAction = MumlaService.ACTION_PTT_UP;
                break;
            case ACTION_UNIPRO_HELP_DOWN:
                if (!isPttKeyConfigured(context, KeyEvent.KEYCODE_F3)) return;
                serviceAction = MumlaService.ACTION_PTT_DOWN;
                break;
            case ACTION_UNIPRO_HELP_UP:
                if (!isPttKeyConfigured(context, KeyEvent.KEYCODE_F3)) return;
                serviceAction = MumlaService.ACTION_PTT_UP;
                break;
            case ACTION_UNIPRO_P2_DOWN:
                if (!isPttKeyConfigured(context, KeyEvent.KEYCODE_F4)) return;
                serviceAction = MumlaService.ACTION_PTT_DOWN;
                break;
            case ACTION_UNIPRO_P2_UP:
                if (!isPttKeyConfigured(context, KeyEvent.KEYCODE_F4)) return;
                serviceAction = MumlaService.ACTION_PTT_UP;
                break;
            default:
                return;
        }

        Intent serviceIntent = new Intent(context, MumlaService.class);
        serviceIntent.setAction(serviceAction);
        context.startService(serviceIntent);
    }

    private boolean isPttKeyConfigured(Context context, int keyCode) {
        Settings settings = Settings.getInstance(context);
        return settings.getPushToTalkKey() == keyCode;
    }
}
