/**
 *      Copyright (c) 2017 by Contributors of the BIG IoT Project Consortium (see below).
 *      All rights reserved.
 *
 *      This source code is licensed under the MIT license found in the
 *      LICENSE file in the root directory of this source tree.
 *
 */

package org.eclipse.bigiot.lib.examples.types;

import org.eclipse.bigiot.lib.model.Location;

public class AlternativeParkingPojo {
	
	public Location coordinates;
	
	public double meters;
	
	public AlternativeParkingPojo() {
		
	}

	@Override
	public String toString() {
		return "coordinates=" + coordinates + ", meters=" + meters;
	}
	
	

}
