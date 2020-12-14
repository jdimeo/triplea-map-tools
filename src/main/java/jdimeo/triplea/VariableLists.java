package jdimeo.triplea;

import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.Callable;
import java.util.function.Predicate;

import org.apache.commons.lang3.BooleanUtils;
import org.jooq.lambda.Seq;

import generated.Element;
import generated.Game;
import generated.Territory;
import generated.Variable;
import generated.VariableList;
import jdimeo.triplea.util.GameCodec;
import lombok.val;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(name = "vars", description =
	"Create a few standard/convenience variables automatically (like a all land territories) for use in triggers")
public class VariableLists implements Callable<Void> {
	@Option(names = {"-g", "--gameFile"}, description = "The original game XML file", required = true)
	private Path gameFile;
	
	private boolean addAllPlayers = true;
	private boolean addAllLand = true;
	private boolean addAllSea = false;
	
	@Override
	public Void call() throws IOException {
		val codec = new GameCodec();
		val game = codec.load(gameFile.toFile());
		if (addAllPlayers) { addOrUpdate(game, allPlayers(game)); }
		if (addAllLand)    { addOrUpdate(game, allLand(game));    }
		if (addAllSea)     { addOrUpdate(game, allSea(game));     }
		codec.save(game, gameFile.toFile());
		return null;
	}
	
	private static void addOrUpdate(Game game, Variable v) {
		if (game.getVariableList() == null) {
			game.withVariableList(new VariableList().withVariable(v));
			return;
		}
		
		val existing = Seq.seq(game.getVariableList().getVariable()).findFirst($ -> $.getName().equals(v.getName()));
		if (existing.isPresent()) {
			existing.get().getElement().clear();
			existing.get().withElement(v.getElement());
		} else {
			game.getVariableList().withVariable(v);
		}
	}
	
	private static Variable allPlayers(Game game) {
		return new Variable().withName("allPlayers").withElement(
			Seq.seq(game.getPlayerList().getPlayer()).map(p -> new Element().withName(p.getName())).toList()
		);
	}
	private static Variable allLand(Game game) {
		return fromTerritories(game, "allLand",
			t -> !BooleanUtils.toBoolean(t.getWater()));
	}
	private static Variable allSea(Game game) {
		return fromTerritories(game, "allSea",
			t -> BooleanUtils.toBoolean(t.getWater()));
	}
	private static Variable fromTerritories(Game g, String name, Predicate<Territory> filter) {
		return new Variable().withName(name).withElement(
			Seq.seq(g.getMap().getTerritory()).filter(filter).map(t -> new Element().withName(t.getName())).toList()
		);
	}
}
