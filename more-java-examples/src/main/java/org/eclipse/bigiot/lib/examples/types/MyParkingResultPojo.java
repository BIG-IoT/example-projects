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
	
	public static class Coordinate {
		
		public double latitude;
		public double longitude;
		@Override
		public String toString() {
			return "Coordinate [latitude=" + latitude + ", longitude=" + longitude + "]";
		}
		
		
	}
	public MyParkingResultPojo.Coordinate myCoordinate;
	public double myDistance;
	public String myStatus;
	@Override
	public String toString() {
		return "MyParkingResultPojo [myCoordinate=" + myCoordinate + ", myDistance=" + myDistance + ", myStatus="
				+ myStatus + "]";
	}
	
	

}
