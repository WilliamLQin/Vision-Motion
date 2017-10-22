package com.thacks2.motionsensor;

public class UnitConversion {
    private double objLenPx = 0;
    private double disLenPx = 0;
    private double objLenUnit = 0;

    public UnitConversion(double objectLenPx, double disLenPx, double objLenUnit) {
        this.objLenPx = objectLenPx;
        this.disLenPx = disLenPx;
        this.objLenUnit = objLenUnit;
    }

    private double getDistanceInUnits() {
        return (objLenUnit / objLenPx) * disLenPx;
    }
}
