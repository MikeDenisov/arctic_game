package com.goodcodeforfun.iceplorers;

import android.location.Location;
import android.text.TextUtils;

import com.yayandroid.locationmanager.constants.FailType;
import com.yayandroid.locationmanager.constants.ProcessType;

public class LocationPresenter {

    private SampleView sampleView;

    public LocationPresenter(SampleView view) {
        this.sampleView = view;
    }

    public void destroy() {
        sampleView = null;
    }

    public void onLocationChanged(Location location) {
        sampleView.dismissProgress();
        setText(location);
    }

    public void onLocationFailed(@FailType int failType) {

    }

    public void onProcessTypeChanged(@ProcessType int newProcess) {

    }

    private void setText(Location location) {
        String appendValue = location.getLatitude() + ", " + location.getLongitude() + "\n";
        String newValue;
        CharSequence current = sampleView.getText();

        if (!TextUtils.isEmpty(current)) {
            newValue = current + appendValue;
        } else {
            newValue = appendValue;
        }

        sampleView.setText(newValue);
    }

    public interface SampleView {

        String getText();

        void setText(String text);

        void updateProgress(String text);

        void dismissProgress();

    }

}
