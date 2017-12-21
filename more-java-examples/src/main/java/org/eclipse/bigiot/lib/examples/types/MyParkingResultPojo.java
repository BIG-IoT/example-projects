/**
 *      Copyright (c) 2017 by Contributors of the BIG IoT Project Consortium (see below).
 *      All rights reserved.
 *
 *      This source code is licensed under the MIT license found in the
 *      LICENSE file in the root directory of this source tree.
 *
 */

package org.eclipse.bigiot.lib.examples.types;

public class MyParkingResultPojo {
	
    public double latitude;
    public double longitude;
    public double distance;
    public String status;

    @Override
    public String toString() {
        return "MyParkingResultPojo [longitude=" + longitude + ", latitude=" + latitude + ", distance=" + distance
                + ", status=" + status + "]";
    }

}
