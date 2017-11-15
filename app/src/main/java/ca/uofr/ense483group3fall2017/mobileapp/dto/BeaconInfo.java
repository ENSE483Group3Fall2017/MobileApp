package ca.uofr.ense483group3fall2017.mobileapp.dto;

import java.text.DecimalFormat;

/**
 * Created by bahram.aliyev on 2017-11-14.
 */

public class BeaconInfo {

    private final static DecimalFormat mDecimalFormat = new DecimalFormat("###.###");

    public String getRegionId() {
        return mRegion;
    }

    public String getBeaonId() {
        return mBeaconId;
    }

    public double getProximity() {
        return mProximity;
    }

    public String getProximityAsString() {
        return mDecimalFormat.format(mProximity);
    }

    private String mRegion;
    private String mBeaconId;
    private double mProximity;

    public BeaconInfo(String regionId, String beaconId, double proximity) {
        mRegion = regionId;
        mBeaconId = beaconId;
        mProximity = proximity;
    }
}
