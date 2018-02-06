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

import org.eclipse.bigiot.lib.offering.mapping.ResponseMappingType;

public class MyParkingResultPojoAnnotated {

    @ResponseMappingType("schema:longitude")
    public double longitude;

    @ResponseMappingType("schema:latitude")
    public double latitude;

    @ResponseMappingType("datex:distanceFromParkingSpace")
    public double distance;

    @ResponseMappingType("datex:parkingSpaceStatus")
    public String status;

    @Override
    public String toString() {
        return "MyParkingResultPojoAnnotated [longitude=" + longitude + ", latitude=" + latitude + ", distance="
                + distance + ", status=" + status + "]";
    }

}
