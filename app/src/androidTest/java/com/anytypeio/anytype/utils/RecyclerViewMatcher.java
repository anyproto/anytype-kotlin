package com.anytypeio.anytype.utils;

import android.content.res.Resources;
import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

public class RecyclerViewMatcher {

    final int recyclerViewId;

    public RecyclerViewMatcher(int recyclerViewId) {
        this.recyclerViewId = recyclerViewId;
    }

    public Matcher<View> atPosition(final int position) {
        return atPositionOnView(position, -1);
    }

    public Matcher<View> atPositionOnView(final int position, final int targetViewId) {

        return new TypeSafeMatcher<View>() {
            Resources resources = null;
            View childView;

            public void describeTo(Description description) {
                String idRecyclerDescription = Integer.toString(recyclerViewId);
                String idTargetViewDescription = Integer.toString(targetViewId);
                if (this.resources != null) {
                    try {
                        idRecyclerDescription = this.resources.getResourceName(recyclerViewId);
                    } catch (Resources.NotFoundException e) {
                        idRecyclerDescription = String.format("%s (resource name not found)", recyclerViewId);
                    }
                    try {
                        idTargetViewDescription = this.resources.getResourceName(targetViewId);
                    } catch (Resources.NotFoundException e) {
                        idTargetViewDescription = String.format("%s (resource name not found)", targetViewId);
                    }
                }
                description.appendText("\nwith id: ["  + idTargetViewDescription + "] inside: [" + idRecyclerDescription + "]");
            }

            public boolean matchesSafely(View view) {

                this.resources = view.getResources();

                if (childView == null) {
                    RecyclerView recyclerView =
                            view.getRootView().findViewById(recyclerViewId);
                    if (recyclerView != null && recyclerView.getId() == recyclerViewId) {
                        RecyclerView.ViewHolder holder = recyclerView.findViewHolderForAdapterPosition(position);
                        if (holder == null) {
                            throw new IllegalStateException(
                                    "No view holder found at position: " + position +
                                            ". Actual child count: " + recyclerView.getChildCount()
                            );
                        }
                        childView = holder.itemView;
                    } else {
                        return false;
                    }
                }

                if (targetViewId == -1) {
                    return view == childView;
                } else {
                    View targetView = childView.findViewById(targetViewId);
                    return view == targetView;
                }

            }
        };
    }
}
