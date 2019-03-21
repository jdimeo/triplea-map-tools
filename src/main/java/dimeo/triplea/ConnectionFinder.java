package dimeo.triplea;

import java.awt.Polygon;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.jooq.lambda.Seq;
import org.locationtech.jts.awt.ShapeReader;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryCollection;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;
import org.locationtech.jts.geom.TopologyException;

import com.elderresearch.commons.lang.Utilities;

import games.strategy.util.PointFileReaderWriter;
import lombok.val;

public class ConnectionFinder {
	@SuppressWarnings("resource")
	public static void main(String... args) throws IOException {
		val pw = new PrintWriter("connections.txt");
		
		Map<String, List<Polygon>> polys;
		try (val is = new FileInputStream(Utilities.first(args))) {
			polys = PointFileReaderWriter.readOneToManyPolygons(is);
		}
		
		val gf = new GeometryFactory(new PrecisionModel(PrecisionModel.FIXED));
		val sr = new ShapeReader(gf);
		
		Map<String, Geometry> geoms = new TreeMap<>();
		polys.forEach((key, list) -> {
			geoms.put(key, new GeometryCollection(
				Seq.seq(list).map($ -> $.getPathIterator(null)).map(sr::read).toArray(Geometry[]::new)
			, gf));
		});
		
		val strArr = new String[geoms.size()];
		val geoArr = new Geometry[geoms.size()];
		int i = 0;
		for (val e : geoms.entrySet()) {
			strArr[i] = e.getKey();
			geoArr[i++] = e.getValue().buffer(2.0).norm();
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
						pw.format("        <connection t1=\"%s\" t2=\"%s\"/>%n", strThis, strOther);
					}
				} catch (TopologyException e) {
					System.err.println("Error with " + strThis + " <-> " + strOther + ": " + e.getMessage());
				}
			}
			System.out.println();
		}
		pw.close();
	}
}
