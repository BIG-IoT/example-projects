/**
 *      Copyright (c) 2017 by Contributors of the BIG IoT Project Consortium (see below).
 *      All rights reserved.
 *
 *      This source code is licensed under the MIT license found in the
 *      LICENSE file in the root directory of this source tree.
 *
 */

package org.eclipse.bigiot.lib.examples.types;

import org.eclipse.bigiot.lib.offering.mapping.ResponseMappingType;

public class MyParkingResultPojoAnnotated {
	
	public static class Coordinate {
		
		public double latitude;
		public double longitude;
		@Override
		public String toString() {
			return "Coordinate [latitude=" + latitude + ", longitude=" + longitude + "]";
		}
		
		
	}
	
	@ResponseMappingType( "schema:geoCoordinates" ) 
	public MyParkingResultPojoAnnotated.Coordinate myCoordinate;
	
	@ResponseMappingType( "datex:distanceFromParkingSpace" )
	public double myDistance;
	
	@ResponseMappingType( "datex:parkingSpaceStatus" )
	public String myStatus;

	@Override
	public String toString() {
		return "MyParkingResultPojoAnnotated [myCoordinate=" + myCoordinate + ", myDistance=" + myDistance
				+ ", myStatus=" + myStatus + "]";
	}
	

}
