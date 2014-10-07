/*
 * Copyright 2014, The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.managedprovisioning;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Button;

/**
 * Dialog used to notify the user that the admin will have full control over the profile/device.
 * Custom runnables can be passed that are run on consent or cancel.
 */
public class UserConsentDialog extends DialogFragment {
    public static final int PROFILE_OWNER = 1;
    public static final int DEVICE_OWNER = 2;

    public static final String LEARN_MORE_URL_PROFILE_OWNER =
            "https://support.google.com/android/work/answer/6090512";
    // TODO: replace by the final device owner learn more link.
    public static final String LEARN_MORE_URL_DEVICE_OWNER =
            "https://support.google.com/android/work/answer/6090512";

    // Only urls starting with this base can be visisted in the device owner case.
    public static final String LEARN_MORE_ALLOWED_BASE_URL =
            "https://support.google.com/";

    private final Context mContext;
    private final Runnable mOnUserConsentRunnable;
    private final Runnable mOnCancelRunnable;
    private final int mOwnerType;

    public UserConsentDialog(final Context context, int ownerType,
            final Runnable onUserConsentRunnable, final Runnable onCancelRunnable) {
        super();
        mContext = context;
        if (ownerType != PROFILE_OWNER && ownerType != DEVICE_OWNER) {
            throw new IllegalArgumentException("Illegal value for argument ownerType.");
        }
        mOwnerType = ownerType;
        mOnUserConsentRunnable = onUserConsentRunnable;
        mOnCancelRunnable = onCancelRunnable;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final Dialog dialog = new Dialog(mContext, R.style.ManagedProvisioningDialogTheme);
        dialog.setContentView(R.layout.learn_more_dialog);
        dialog.setCanceledOnTouchOutside(false);

        TextView text1 = (TextView) dialog.findViewById(R.id.learn_more_text1);
        if (mOwnerType == PROFILE_OWNER) {
            text1.setText(R.string.admin_has_ability_to_monitor_profile);
        } else if (mOwnerType == DEVICE_OWNER) {
            text1.setText(R.string.admin_has_ability_to_monitor_device);
        }

        final TextView linkText = (TextView) dialog.findViewById(R.id.learn_more_link);
        if (mOwnerType == PROFILE_OWNER) {
            linkText.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent browserIntent = new Intent(Intent.ACTION_VIEW,
                                Uri.parse(LEARN_MORE_URL_PROFILE_OWNER));
                        mContext.startActivity(browserIntent);
                    }
                });
        } else if (mOwnerType == DEVICE_OWNER) {
            linkText.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent webIntent = new Intent(mContext, WebActivity.class);
                        webIntent.putExtra(WebActivity.EXTRA_URL, LEARN_MORE_URL_DEVICE_OWNER);
                        webIntent.putExtra(WebActivity.EXTRA_ALLOWED_URL_BASE,
                                LEARN_MORE_ALLOWED_BASE_URL);
                        mContext.startActivity(webIntent);
                    }
                });
        }

        Button positiveButton = (Button) dialog.findViewById(R.id.positive_button);
        positiveButton.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                    if (mOnUserConsentRunnable != null) {
                        mOnUserConsentRunnable.run();
                    }
                }
            });

        Button negativeButton = (Button) dialog.findViewById(R.id.negative_button);
        negativeButton.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                    if (mOnCancelRunnable != null) {
                        mOnCancelRunnable.run();
                    }
                }
            });

        return dialog;
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        if (mOnCancelRunnable != null) {
            mOnCancelRunnable.run();
        }
    }
}