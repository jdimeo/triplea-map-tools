package jdimeo.triplea.util;

import java.awt.Point;
import java.awt.Polygon;
import java.awt.geom.PathIterator;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.jooq.lambda.Seq;
import org.locationtech.jts.awt.ShapeReader;
import org.locationtech.jts.awt.ShapeWriter;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryCollection;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;

import lombok.Getter;
import lombok.Setter;
import lombok.val;
import lombok.experimental.Accessors;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Getter @Setter @Accessors(chain = true)
public class TerritoryGeo {
	public static final GeometryFactory GEO_FACTORY = new GeometryFactory(new PrecisionModel(PrecisionModel.FIXED));
	private static final ShapeReader SHAPE_READER = new ShapeReader(GEO_FACTORY);
	private static final ShapeWriter SHAPE_WRITER = new ShapeWriter();
	static {
		SHAPE_WRITER.setRemoveDuplicatePoints(true);
	}
	
	private String name;
	private Point center;
	private List<Polygon> polys;
	private Geometry geo;
	private List<Point> placements = new LinkedList<>();
	
	public static List<TerritoryGeo> fromPolysFile(Path p) throws IOException {
		Map<String, List<Polygon>> polys;
		try (val is = Files.newInputStream(p)) {
			polys = PointFileReaderWriter.readOneToManyPolygons(is);
		}
		
		val ret = new ArrayList<TerritoryGeo>();
		polys.forEach((key, list) -> ret.add(new TerritoryGeo()
			.setName(key)
			.setPolys(list)
			.setGeo(new GeometryCollection(
				Seq.seq(list).map($ -> $.getPathIterator(null)).map(SHAPE_READER::read).toArray(Geometry[]::new)
			, GEO_FACTORY))
		));
		ret.sort(Comparator.comparing($ -> $.getName().toLowerCase()));
	
		// Ensure that no territory completely encloses another
		for (int i = 0; i < ret.size(); i++) {
			val t1 = ret.get(i);
			val poly1 = t1.geo.union();
			for (int j = i + 1; j < ret.size(); j++) {
				val t2 = ret.get(j);
				val poly2 = t2.geo.union();
				if (poly1.covers(poly2)) {
					subtract(t1, poly1, t2, poly2, polys);
				} else if (poly2.covers(poly1)) {
					subtract(t2, poly2, t1, poly1, polys);
				}
			}
		}
		
		try (val os = Files.newOutputStream(p)) {
			PointFileReaderWriter.writeOneToManyPolygons(os, polys);
		} catch (Exception e) {
			throw new IOException(e);
		}
		
		return ret;
	}
	
	public String asPlaceString() {
		return name + Seq.seq(placements).map($ -> "  (" + $.x + "," + $.y + ")");
	}
	
	private static void subtract(TerritoryGeo t1, Geometry g1, TerritoryGeo t2, Geometry g2, Map<String, List<Polygon>> polyMap) {
		log.info("{} covers {}, substracting inner poly from outer", t1.name, t2.name);
		t1.geo = g1.difference(g2);
		
		val shape = SHAPE_WRITER.toShape(t1.geo);
		val poly  = new Polygon();
		val iter  = shape.getPathIterator(null);
		val arr   = new float[2];
		for (int i = 0; !iter.isDone(); i++) {
			try {
				if (iter.currentSegment(arr) == PathIterator.SEG_LINETO || i == 0) {
					poly.addPoint(Math.round(arr[0]), Math.round(arr[1]));		
				}
			} catch (ArrayIndexOutOfBoundsException e) {
				log.warn("Error updating geometry", e);
				return;
			}
			iter.next();
		}
		
		polyMap.put(t1.name, Arrays.asList(poly));
	}
}
