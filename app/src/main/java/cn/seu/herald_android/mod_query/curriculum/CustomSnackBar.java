package cn.seu.herald_android.mod_query.curriculum;

import android.graphics.Color;
import android.graphics.Typeface;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.widget.TextView;

public class CustomSnackBar {

    enum SnackBarDuration {
        SHORT, LONG, INDEFINITE
    }

    // Default snack bar will be showed for short duration
    private SnackBarDuration mSnackBarDuration = SnackBarDuration.SHORT;

    private OnActionClickListener mOnActionClickListener;

    private View mView;

    private String mTitleText;

    private String mActionText;

    private int mTitleTextColor = Color.WHITE;

    private int mActionTextColor = Color.YELLOW;

    private int mBackgroundColor = Color.BLACK;

    private Typeface mTfTitle = Typeface.DEFAULT;

    private Typeface mTfAction = Typeface.DEFAULT;

    private boolean isNeedClickEvent;

    // Assigning the view for which Snackbar responds
    public CustomSnackBar view(View view) {
        this.mView = view;
        return this;
    }

    // Assigning the title and action text

    public CustomSnackBar text(String titleText, String actionText) {
        this.mTitleText = titleText;
        this.mActionText = actionText;
        return this;
    }

    // To customize title and action text colors

    public CustomSnackBar textColors(int titleTextColor, int actionTextColor) {
        this.mTitleTextColor = titleTextColor;
        this.mActionTextColor = actionTextColor;
        return this;
    }

    // To customize background color of snack bar
    public CustomSnackBar backgroundColor(int backgroundColor) {
        this.mBackgroundColor = backgroundColor;
        return this;
    }

    // Change duration whether SnackBar should be shown for short, long or Indefinite
    public CustomSnackBar duration(SnackBarDuration snackBarDuration) {
        this.mSnackBarDuration = snackBarDuration;
        return this;
    }

    public Snackbar getSnackBar() {

        int duration = 0;

        switch (mSnackBarDuration) {

            case SHORT:
                duration = Snackbar.LENGTH_SHORT;
                break;

            case LONG:
                duration = Snackbar.LENGTH_LONG;
                break;

            case INDEFINITE:
                duration = Snackbar.LENGTH_INDEFINITE;
                break;
        }


        Snackbar snackbar = Snackbar
                .make(mView, mTitleText, duration)
                .setAction(mActionText, view -> {

                    if (isNeedClickEvent) {
                        mOnActionClickListener.onClick(view);
                    }
                });


        View sbView = snackbar.getView();
        TextView txtTitle = (TextView) sbView.findViewById(android.support.design.R.id.snackbar_text);
        TextView txtAction = (TextView) sbView.findViewById(android.support.design.R.id.snackbar_action);

        // Changing message text color
        txtTitle.setTextColor(mTitleTextColor);


        // Changing action button text color
        txtAction.setTextColor(mActionTextColor);

        // Changing background color
        sbView.setBackgroundColor(mBackgroundColor);

        // Changing font style for title
        txtTitle.setTypeface(mTfTitle);

        // Changing font style for action
        txtAction.setTypeface(mTfAction);

        return snackbar;
    }

    // To show the Snack bar in UI.
    public void show() {
        getSnackBar().show();
    }


    // Listeners to handle SnackBar Action click
    public CustomSnackBar setOnClickListener(boolean isNeedClickEvent, OnActionClickListener onActionClickListener) {
        this.isNeedClickEvent = isNeedClickEvent;
        this.mOnActionClickListener = onActionClickListener;
        return this;
    }

    public interface OnActionClickListener {
        void onClick(View view);
    }
}