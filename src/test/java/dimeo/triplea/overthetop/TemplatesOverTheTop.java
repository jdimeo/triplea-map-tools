package dimeo.triplea.overthetop;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.apache.commons.lang3.ArrayUtils;

import dimeo.triplea.Template;

public class TemplatesOverTheTop {
	private static final String[] DEVELOP_UNITS = {
		"unit:Bomber", "unit:Carrier", "unit:Heavy-Artillery", "unit:Mech-Infantry", "unit:Railgun", "unit:Tank"
	};
	private static final String[] PLAYERS_COLONIAL = {
		"player:Germans", "player:British", "player:French", "player:Russians"
	};
	private static final String[] PLAYERS_COLONIES = {
		"player:Imperial-German-Afrika", "player:British-Commonwealth", "player:French-Colonial-Africa", "player:Bolsheviks"	
	};
	private static final String[] PLAYERS_COLONY_MAP = {
		"colonyGermans:Imperial-German-Afrika", "colonyBritish:British-Commonwealth", "colonyFrench:French-Colonial-Africa",
		// Not a "colony" but Bolsheviks have the same tech as Russia since they are Russians
		"colonyRussians:Bolsheviks"
	};
	private static final String[] PLAYERS_OTHER = {
		"player:Austrians", "player:Ottomans", "player:Italians", "player:Americans"
	};
	
	private static final String[] PLAYERS_WITH_TECH = ArrayUtils.addAll(PLAYERS_COLONIAL, PLAYERS_OTHER);
	private static final String[] PLAYERS_ALL = ArrayUtils.addAll(PLAYERS_WITH_TECH, PLAYERS_COLONIES);
	
	private static final String[] REMOVE_AFTER_BATTLE_UNITS = {
		"unit:Munitions"
	};
	private static final String[] INFRA_DURING_COMBAT_MOVE_UNITS = {
		"unit:Trenches"
	};

	public static void main(String[] args) throws IOException {
		Files.deleteIfExists(Paths.get("out.xml"));
		
		new Template("turn-order.xml",
			"player:Germans", "player:Imperial-German-Afrika",
			"player:French",   "player:French-Colonial-Africa",
			"player:Russians",
			"player:Austrians",
			"player:Ottomans",
			"player:British",  "player:British-Commonwealth",
			"player:Italians",
			"player:Americans",
			"player:Bolsheviks").call();
		
		new Template("production.xml", PLAYERS_COLONIES).call();
		new Template("production-techs.xml", PLAYERS_WITH_TECH).call();
		new Template("production-player.xml", PLAYERS_ALL).call();
		new Template("production-repair.xml", PLAYERS_ALL).call();
		new Template("tech-mech-inf.xml", PLAYERS_ALL).call();
		new Template("tech-unit-attach.xml", DEVELOP_UNITS).call();
		new Template("tech-frontier-change.xml", ArrayUtils.addAll(DEVELOP_UNITS, PLAYERS_OTHER)).call();
		new Template("tech-frontier-change-colony.xml", ArrayUtils.addAll(ArrayUtils.addAll(
			DEVELOP_UNITS, ArrayUtils.addAll(PLAYERS_COLONIAL, PLAYERS_COLONY_MAP)))).call();
		new Template("remove-all-after-combat.xml", ArrayUtils.addAll(ArrayUtils.addAll(
			REMOVE_AFTER_BATTLE_UNITS, PLAYERS_ALL))).call();
		new Template("infra-during-combat-move.xml", ArrayUtils.addAll(ArrayUtils.addAll(
			INFRA_DURING_COMBAT_MOVE_UNITS, PLAYERS_ALL))).call();
	}
}
