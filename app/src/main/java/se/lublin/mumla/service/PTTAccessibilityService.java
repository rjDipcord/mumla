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

import android.accessibilityservice.AccessibilityService;
import android.content.Intent;
import android.view.KeyEvent;
import android.view.accessibility.AccessibilityEvent;

import se.lublin.mumla.Settings;

/**
 * Intercepts hardware PTT button key events globally via the Android Accessibility framework.
 * Unlike Activity key handlers, an AccessibilityService with FLAG_REQUEST_FILTER_KEY_EVENTS
 * receives key events regardless of which app is in the foreground and regardless of whether
 * the screen is on or off (provided the CPU is kept awake by a wake lock).
 *
 * When the configured PTT key is pressed or released, this service forwards the event to
 * {@link MumlaService} via a start-service intent.  MumlaService then calls
 * {@link MumlaService#onTalkKeyDown()} / {@link MumlaService#onTalkKeyUp()} as normal.
 *
 * The user must enable this service once in Settings → Accessibility → Mumla PTT.
 */
public class PTTAccessibilityService extends AccessibilityService {

    @Override
    protected boolean onKeyEvent(KeyEvent event) {
        Settings settings = Settings.getInstance(this);
        int pttKey = settings.getPushToTalkKey();

        // No PTT key configured, or a different key — pass the event through unchanged.
        if (pttKey == Settings.DEFAULT_PUSH_KEY || event.getKeyCode() != pttKey) {
            return false;
        }

        String action;
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            action = MumlaService.ACTION_PTT_DOWN;
        } else if (event.getAction() == KeyEvent.ACTION_UP) {
            action = MumlaService.ACTION_PTT_UP;
        } else {
            return false;
        }

        Intent intent = new Intent(this, MumlaService.class);
        intent.setAction(action);
        startService(intent);

        // Return true to consume the event so it is not also delivered to the foreground app.
        return true;
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        // Not needed — this service only intercepts key events.
    }

    @Override
    public void onInterrupt() {
    }
}
