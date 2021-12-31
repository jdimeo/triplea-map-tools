package jdimeo.triplea.util;

import java.awt.Point;
import java.awt.Polygon;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.jooq.lambda.Seq;
import org.locationtech.jts.awt.ShapeReader;
import org.locationtech.jts.awt.ShapeWriter;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;

import lombok.Getter;
import lombok.Setter;
import lombok.val;
import lombok.experimental.Accessors;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Getter @Setter @Accessors(chain = true)
public class TerritoryGeo implements Comparable<TerritoryGeo> {
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
	
	public static List<TerritoryGeo> fromMapFolder(Path p) throws IOException {
		log.info("Loading polygons...");
		val ret = fromPolysFile(p.resolve("polygons.txt"));

		log.info("Loading centers...");
		try (val is = Files.newInputStream(p.resolve("centers.txt"))) {
			val centers = PointFileReaderWriter.readOneToOneCenters(is);
			ret.forEach($ -> $.setCenter(centers.get($.getName())));
		}
		
		return ret;
	}
	
	public static List<TerritoryGeo> fromPolysFile(Path p) throws IOException {
		Map<String, List<Polygon>> polys;
		try (val is = Files.newInputStream(p)) {
			polys = PointFileReaderWriter.readOneToManyPolygons(is);
		}
		
		val ret = new ArrayList<TerritoryGeo>();
		polys.forEach((key, list) -> ret.add(new TerritoryGeo()
			.setName(key)
			.setPolys(list)
			.setGeo(GEO_FACTORY.createGeometryCollection(
				Seq.seq(list).map($ -> $.getPathIterator(null)).map(SHAPE_READER::read).toArray(Geometry[]::new)
			))
		));
		ret.sort(Comparator.naturalOrder());
	
		try (val os = Files.newOutputStream(p)) {
			PointFileReaderWriter.writeOneToManyPolygons(os, polys);
		} catch (Exception e) {
			throw new IOException(e);
		}
		
		return ret;
	}
	
	@Override
	public int compareTo(TerritoryGeo o) {
		return String.CASE_INSENSITIVE_ORDER.compare(sortableName(getName()), sortableName(o.getName()));
	}
	
	private static String sortableName(String s) {
		// Railroads should be top in Z-order (so they always draw on top of their "container" territory)
		if (s.startsWith("RR ")) { return "3" + s; }
		// Sea zones should be bottom of Z-order (so islands always draw on top of water)
		if (s.startsWith("SZ ")) { return "1" + s; }
		return "2" + s;
	}
	
	public String asPlaceString() {
		return name + Seq.seq(placements).map($ -> "  (" + $.x + "," + $.y + ")");
	}
}
