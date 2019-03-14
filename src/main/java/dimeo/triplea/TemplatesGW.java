/*******************************************************************************
 * Copyright (c) 2017 Elder Research, Inc.
 * All rights reserved.
 *******************************************************************************/
package dimeo.triplea;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.apache.commons.lang3.ArrayUtils;

public class TemplatesGW {
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
		"player:Austrians", "player:Ottomans", "player:Americans"	
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
		Template.main(ArrayUtils.addAll(PLAYERS_COLONIES,
			"-t", "production.xml"));
		Template.main(ArrayUtils.addAll(PLAYERS_WITH_TECH,
			"-t", "production-techs.xml"));
		Template.main(ArrayUtils.addAll(PLAYERS_ALL,
			"-t", "tech-mech-inf.xml"));
		Template.main(ArrayUtils.addAll(DEVELOP_UNITS,
			"-t", "tech-unit-attach.xml"));
		Template.main(ArrayUtils.addAll(ArrayUtils.addAll(DEVELOP_UNITS, PLAYERS_OTHER),
			"-t", "tech-frontier-change.xml"));
		Template.main(ArrayUtils.addAll(ArrayUtils.addAll(DEVELOP_UNITS, ArrayUtils.addAll(PLAYERS_COLONIAL, PLAYERS_COLONY_MAP)),
			"-t", "tech-frontier-change-colony.xml"));
		Template.main(ArrayUtils.addAll(ArrayUtils.addAll(REMOVE_AFTER_BATTLE_UNITS, PLAYERS_ALL),
			"-t", "remove-all-after-combat.xml"));
		Template.main(ArrayUtils.addAll(ArrayUtils.addAll(INFRA_DURING_COMBAT_MOVE_UNITS, PLAYERS_ALL),
			"-t", "infra-during-combat-move.xml"));
	}
}
