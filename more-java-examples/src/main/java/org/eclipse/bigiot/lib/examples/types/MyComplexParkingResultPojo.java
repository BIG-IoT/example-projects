/**
 * Copyright (c) 2016-2017 in alphabetical order:
 * Bosch Software Innovations GmbH, Robert Bosch GmbH, Siemens AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Denis Kramer     (Bosch Software Innovations GmbH)
 *    Stefan Schmid    (Robert Bosch GmbH)
 *    Andreas Ziller   (Siemens AG)
 */
package org.eclipse.bigiot.lib.examples.types;

public class MyComplexParkingResultPojo {

    public static class Coordinate {

        public double latitude;
        public double longitude;

        @Override
        public String toString() {
            return "Coordinate [latitude=" + latitude + ", longitude=" + longitude + "]";
        }

    }

    public MyComplexParkingResultPojo.Coordinate myCoordinate;
    public double myDistance;
    public String myStatus;

    @Override
    public String toString() {
        return "MyParkingResultPojo [myCoordinate=" + myCoordinate + ", myDistance=" + myDistance + ", myStatus="
                + myStatus + "]";
    }

}
