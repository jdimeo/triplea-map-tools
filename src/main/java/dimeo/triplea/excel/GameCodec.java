/*******************************************************************************
 * Copyright (c) 2017 Elder Research, Inc.
 * All rights reserved.
 *******************************************************************************/
package dimeo.triplea.excel;

import java.io.IOException;

import javax.xml.bind.JAXBException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;

import com.elderresearch.commons.xsd.XMLCodec;

import generated.Game;
import generated.ObjectFactory;

public class GameCodec extends XMLCodec<Game> {
	public GameCodec() throws IOException {
		super(Game.class, null);
	}
	
	@Override
	protected void customizeTransformer(Transformer t) throws JAXBException {
		super.customizeTransformer(t);
		t.setOutputProperty(OutputKeys.STANDALONE, "no");
		t.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, "game.dtd");
	}
}
