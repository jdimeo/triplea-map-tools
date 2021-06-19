package jdimeo.triplea;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.Callable;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.TopologyException;

import jdimeo.triplea.util.TerritoryGeo;
import lombok.val;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

@Command(name = "connections", description = {
	"Automatically generate connections from polygons.txt",
	"This will write out a connections.txt file with <connection> tags in it that you can insert in your game file."	
})
public class ConnectionFinder implements Callable<Void> {
	@Parameters(index = "0", arity = "1", description = "The polygons.txt file to process")
	private Path input;
	
	@Override
	public Void call() throws IOException {
		val territories = TerritoryGeo.fromPolysFile(input);
		try (val pw = new PrintWriter("connections.txt")) {
			computeConnections(territories, pw);
		}
		return null;
	}
	
	private static void computeConnections(List<TerritoryGeo> territories, PrintWriter out) {
		val strArr = new String[territories.size()];
		val geoArr = new Geometry[territories.size()];
		int i = 0;
		for (val t : territories) {
			strArr[i] = t.getName();
			geoArr[i++] = t.getGeo().buffer(2.0).norm();
		}

		for (i = 0; i < strArr.length; i++) {
			val geoThis = geoArr[i];
			val strThis = strArr[i];
			System.out.print("Connections for " + strThis);
			
			for (int j = i + 1; j < strArr.length; j++) {
				val geoOther = geoArr[j];
				val strOther = strArr[j];
				
				val rrThis  = strThis.startsWith("RR");
				val rrOther = strOther.startsWith("RR");
				
				val minArea = rrThis && rrOther? 5 : 30;
				try {
					if (geoThis.intersects(geoOther) && geoThis.intersection(geoOther).getArea() > minArea) {
						System.out.print(" | " + strArr[j]);
						out.format("        <connection t1=\"%s\" t2=\"%s\"/>%n", strThis, strOther);
					}
				} catch (TopologyException e) {
					System.err.println("Error with " + strThis + " <-> " + strOther + ": " + e.getMessage());
				}
			}
			System.out.println();
		}
	}
}
