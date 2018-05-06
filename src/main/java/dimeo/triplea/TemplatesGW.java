/*******************************************************************************
 * Copyright (c) 2017 Elder Research, Inc.
 * All rights reserved.
 *******************************************************************************/
package dimeo.triplea;

import org.apache.commons.lang3.ArrayUtils;

public class TemplatesGW {
	private static final String[] DEVELOP_UNITS = {
		"unit:Bomber", "unit:Siege-Howitzer", "unit:Railway-Howitzer", "unit:Carrier", "unit:Tank", "unit:Mech-Infantry"
	};
	private static final String[] PLAYERS = {
		"player:Germans", "player:British", "player:French", "player:Russians", "player:Austrians", "player:Ottomans", "player:Americans", "player:Italians"
	};
	private static final String[] REMOVE_AFTER_BATTLE_UNITS = {
		"unit:Munitions"
	};
	private static final String[] INFRA_DURING_COMBAT_MOVE_UNITS = {
		"unit:Trenches"
	};

	public static void main(String[] args) {
		Template.main(ArrayUtils.addAll(DEVELOP_UNITS, "-t", "unit-develop-1.xml"));
		Template.main(ArrayUtils.addAll(ArrayUtils.addAll(DEVELOP_UNITS, PLAYERS), "-t", "unit-develop-2.xml"));
//		Template.main(ArrayUtils.addAll(ArrayUtils.addAll(REMOVE_AFTER_BATTLE_UNITS, PLAYERS), "-t", "remove-all-after-combat.xml"));
		Template.main(ArrayUtils.addAll(ArrayUtils.addAll(INFRA_DURING_COMBAT_MOVE_UNITS, PLAYERS), "-t", "infra-during-combat-move.xml"));
	}
}
