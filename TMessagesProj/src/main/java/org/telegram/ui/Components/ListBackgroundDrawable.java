package org.telegram.ui.Components;

import android.graphics.drawable.GradientDrawable;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.ui.ActionBar.Theme;

public class ListBackgroundDrawable extends GradientDrawable {
    float radius = 10;
    public ListBackgroundDrawable(boolean isTopRow, boolean isBottomRow, boolean isShadowRow) {
        super();

        float radiusPx = AndroidUtilities.dp(radius);
        float[] radii;

        setShape(GradientDrawable.RECTANGLE);
        setColor(Theme.getColor(Theme.key_windowBackgroundWhite));

        if (isTopRow) {
            radii = new float[]{
                    radiusPx, radiusPx,
                    radiusPx, radiusPx,
                    0,0,
                    0,0,
            };

            if(isBottomRow) {
                radii = new float[]{
                        radiusPx, radiusPx,
                        radiusPx, radiusPx,
                        radiusPx, radiusPx,
                        radiusPx, radiusPx,
                };
            }

            setCornerRadii(radii);
        } else if(isBottomRow) {
            radii = new float[]{
                    0,0,
                    0,0,
                    radiusPx, radiusPx,
                    radiusPx, radiusPx,
            };
            setCornerRadii(radii);
        }

        if(isShadowRow) {
            setColor(Theme.getColor(Theme.key_windowBackgroundGray));
        }
    }
}
