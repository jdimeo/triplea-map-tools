package dimeo.triplea;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import com.beust.jcommander.Parameter;
import com.elderresearch.commons.lang.CLIUtils;

import generated.Attachment;
import generated.Game;
import lombok.val;

public class RailroadHelper {
	@Parameter(names = {"-g", "--gameFile"}, description = "The original game XML file", required = true)
	private String gameFile;
	
	public void run() throws IOException {
		File gf = new File(gameFile);
		
		val codec = new GameCodec();
		val game  = codec.load(gf);
		addCanals(game);
		codec.save(game, gf);
	}
	
	private void addCanals(Game g) {
		val rrToOtherRR = new HashMap<String, Set<String>>();
		val rrToLand = new HashMap<String, String>();
		g.getMap().getConnection().forEach(c -> {
			val rr1 = c.getT1().startsWith("RR");
			val rr2 = c.getT2().startsWith("RR");
			if (rr1 && rr2) {
				rrToOtherRR.computeIfAbsent(c.getT1(), $ -> new HashSet<>()).add(c.getT2());
				rrToOtherRR.computeIfAbsent(c.getT2(), $ -> new HashSet<>()).add(c.getT1());
			} else if (rr1 ^ rr2) {
				String sz = rr1? c.getT1() : c.getT2();
				String lz = rr1? c.getT2() : c.getT1();
				rrToLand.put(sz, lz);
			}
		});
		
		val canals = new ArrayList<Attachment>();
		rrToLand.forEach((sz, lz) -> {
			rrToOtherRR.get(sz).forEach(otherSz -> {
				val name = StringUtils.deleteWhitespace(sz + "to" + otherSz);
				canals.add(canalAttachment(sz, name, lz));
				canals.add(canalAttachment(otherSz, name, lz));
			});
		});
		
		canals.sort(Comparator.comparing(Attachment::getName));
		
		g.getAttachmentList().getAttachment().removeIf($ -> $.getName().contains("RR"));
		g.getAttachmentList().getAttachment().addAll(canals);
	}
	
	private static Attachment canalAttachment(String sz, String name, String lz) {
		val a = new Attachment();
		a.setJavaClass("games.strategy.triplea.attachments.CanalAttachment");
		a.setAttachTo(sz);
		a.setName("canalAttachment" + name);
		a.setType("territory");
		a.getOption().add(Units.toOption("canalName", name));
		a.getOption().add(Units.toOption("landTerritories", lz));
		return a;
	}
	
	public static void main(String[] args) throws IOException {
		RailroadHelper uc = new RailroadHelper();
		if (CLIUtils.parseArgs(args, uc)) { uc.run(); }
	}
}
