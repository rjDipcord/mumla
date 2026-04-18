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
 * <p>The dedicated PTT button (F5 / KEYCODE_F5) always triggers PTT via:
 * <ul>
 *   <li>{@code unipro.hotkey.ptt.down} / {@code unipro.hotkey.ptt.up} — TID/Unipro firmware</li>
 *   <li>{@code com.zello.ptt.down} / {@code com.zello.ptt.up} — Zello-compatible firmware</li>
 * </ul>
 *
 * <p>The two programmable side keys are only forwarded as PTT if the user has configured the
 * matching key code in Settings → Push-to-talk key:
 * <ul>
 *   <li>{@code unipro.hotkey.help.down} / {@code unipro.hotkey.help.up} — F3 / KEYCODE_F3 (133)</li>
 *   <li>{@code unipro.hotkey.p2.down}   / {@code unipro.hotkey.p2.up}   — F4 / KEYCODE_F4 (134)</li>
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
            // Dedicated PTT button (F5) — always active.
            case ACTION_UNIPRO_PTT_DOWN:
            case ACTION_ZELLO_PTT_DOWN:
                serviceAction = MumlaService.ACTION_PTT_DOWN;
                break;
            case ACTION_UNIPRO_PTT_UP:
            case ACTION_ZELLO_PTT_UP:
                serviceAction = MumlaService.ACTION_PTT_UP;
                break;

            // F3 side key — only active when the user has assigned KEYCODE_F3 (133) as PTT.
            case ACTION_UNIPRO_HELP_DOWN:
                if (!isPttKeyConfigured(context, KeyEvent.KEYCODE_F3)) return;
                serviceAction = MumlaService.ACTION_PTT_DOWN;
                break;
            case ACTION_UNIPRO_HELP_UP:
                if (!isPttKeyConfigured(context, KeyEvent.KEYCODE_F3)) return;
                serviceAction = MumlaService.ACTION_PTT_UP;
                break;

            // F4 side key — only active when the user has assigned KEYCODE_F4 (134) as PTT.
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
