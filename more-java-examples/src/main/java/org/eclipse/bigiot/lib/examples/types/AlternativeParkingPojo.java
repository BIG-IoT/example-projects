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
