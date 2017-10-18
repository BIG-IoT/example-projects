/**
 * Copyright (c) 2016-2017 BIG IoT Project Consortium and others (see below).
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/index.php
 *
 * Contributors in alphabetical order
 * (for individual contributions, please refer to git log):
 *
 * - Bosch Software Innovations GmbH
 *     > Denis Kramer
 * - Robert Bosch GmbH
 *     > Stefan Schmid (stefan.schmid@bosch.com)
 * - Siemens AG
 *     > Andreas Ziller (andreas.ziller@siemens.com)
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
