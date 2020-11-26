package dimeo.triplea;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.Callable;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.jooq.lambda.Seq;

import dimeo.triplea.excel.GameCodec;
import dimeo.triplea.excel.Units;
import generated.Attachment;
import generated.Game;
import lombok.val;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(name = "railroads", description = {
	"For games that use canals through land as a railroad system, this will automate much of the boilerplate"
})
public class RailroadHelper implements Callable<Void> {
	@Option(names = {"-g", "--gameFile"}, description = "The original game XML file", required = true)
	private String gameFile;
	
	private Set<String> rrZones;
	
	@Override
	public Void call() throws IOException {
		File gf = new File(gameFile);
		
		val codec = new GameCodec();
		val game  = codec.load(gf);
		addCanals(game);
		movementRestrictions(game);
		codec.save(game, gf);
		
		return null;
	}
	
	private void addCanals(Game g) {
		rrZones = new TreeSet<>();
		
		val rrToOtherRR = new HashMap<String, Set<String>>();
		val rrToLand = new HashMap<String, String>();
		g.getMap().getConnection().forEach(c -> {
			val rr1 = c.getT1().startsWith("RR");
			val rr2 = c.getT2().startsWith("RR");
			if (rr1 && rr2) {
				rrToOtherRR.computeIfAbsent(c.getT1(), $ -> new HashSet<>()).add(c.getT2());
			} else if (rr1 ^ rr2) {
				String sz = rr1? c.getT1() : c.getT2();
				String lz = rr1? c.getT2() : c.getT1();
				rrToLand.put(sz, lz);
			}
		});
		
		val canals = new ArrayList<Attachment>();
		rrToOtherRR.forEach((sz, otherSzs) -> {
			rrZones.add(sz);
			otherSzs.forEach(otherSz -> {
				rrZones.add(otherSz);
				
				val lz1 = rrToLand.get(sz);
				val lz2 = rrToLand.get(otherSz);
				
				val name = StringUtils.deleteWhitespace(sz + "to" + otherSz);
				canals.add(canalAttachment(sz, name, lz1, lz2));
				canals.add(canalAttachment(otherSz, name, lz1, lz2));	
			});
		});
		
		canals.sort(Comparator.comparing(Attachment::getName));
		
		g.getAttachmentList().getAttachment().removeIf($ -> $.getName().contains("RR"));
		g.getAttachmentList().getAttachment().addAll(canals);
	}
	
	private void movementRestrictions(Game g) {
		val rrZonesStr = Seq.seq(rrZones).toString(":");
		g.getAttachmentList().getAttachment().forEach(a -> {
			if (a.getJavaClass().equals(Units.ATTACH_CLASS) && Seq.seq(a.getOption()).anyMatch(o -> o.getName().equals("isSea") && BooleanUtils.toBoolean(o.getValue()))) {
				addOrUpdateOption(a, a.getAttachTo().contains("Train")
					|| a.getAttachTo().contains("Railgun")? "unitPlacementOnlyAllowedIn" : "unitPlacementRestrictions", rrZonesStr);
			}
		});
	}
	
	private static Attachment canalAttachment(String sz, String name, String lz1, String lz2) {
		return new Attachment()
			.withJavaClass("games.strategy.triplea.attachments.CanalAttachment")
			.withAttachTo(sz)
			.withName("canalAttachment" + name)
			.withType("territory")
			.withOption(Units.toOption("canalName", name), Units.toOption("landTerritories", lz1 + ":" + lz2));
	}
	
	private static void addOrUpdateOption(Attachment a, String name, String value) {
		Seq.seq(a.getOption()).filter(o -> o.getName().equals(name)).findAny().orElseGet(() -> {
			val o = Units.toOption(name, value);
			a.getOption().add(o);
			return o;
		}).setValue(value);
	}
}
