package ru.pinkponies.protocol;

import static org.junit.Assert.*;

import org.junit.Test;

public class LocationTestCase {

	@Test
	public void testDistanceTo() {
		Location beijing = new Location(116.391667 / 180 * Math.PI, 39.913889 / 180 * Math.PI, 0.0);
		Location moscow = new Location(37.616667 / 180 * Math.PI, 55.75 / 180 * Math.PI, 0.0);
		double distance = 5807 * 1000;

		assertEquals(distance, beijing.distanceTo(moscow), 20 * 1000);
	}

	@Test
	public void testMoveBy() {
		Location moscow = new Location(37.616667 / 180 * Math.PI, 55.75 / 180 * Math.PI, 0.0);
		double distance = 10;
		double bearing = Math.PI / 4;
		Location newLoc = moscow.moveBy(distance, bearing);

		assertEquals(distance, moscow.distanceTo(newLoc), 0.01);
	}
}
