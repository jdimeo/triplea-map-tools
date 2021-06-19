package jdimeo.triplea.util;

import java.awt.Point;
import java.awt.Polygon;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.jooq.lambda.Seq;
import org.locationtech.jts.awt.ShapeReader;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryCollection;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;

import lombok.Data;
import lombok.val;

@Data
public class TerritoryGeo {
	private static final GeometryFactory GEO_FACTORY = new GeometryFactory(new PrecisionModel(PrecisionModel.FIXED));
	private static final ShapeReader SHAPE_READER = new ShapeReader(GEO_FACTORY);
	
	private String name;
	private Point center;
	private List<Polygon> polys;
	private Geometry geo;
	
	public static List<TerritoryGeo> fromPolysFile(Path p) throws IOException {
		Map<String, List<Polygon>> polys;
		try (val is = Files.newInputStream(p)) {
			polys = PointFileReaderWriter.readOneToManyPolygons(is);
		}
		
		val ret = new ArrayList<TerritoryGeo>();
		polys.forEach((key, list) -> {
			TerritoryGeo t = new TerritoryGeo();
			t.setName(key);
			t.setPolys(list);
			t.setGeo(new GeometryCollection(
				Seq.seq(list).map($ -> $.getPathIterator(null)).map(SHAPE_READER::read).toArray(Geometry[]::new)
			, GEO_FACTORY));
			ret.add(t);
		});
		ret.sort(Comparator.comparing(TerritoryGeo::getName));
		return ret;
	}
}
