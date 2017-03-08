/*******************************************************************************
 * Copyright (c) 2017 Elder Research, Inc.
 * All rights reserved.
 *******************************************************************************/
package dimeo.triplea;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.util.IOUtils;

import com.datamininglab.commons.xsd.XMLCodec;

import generated.Game;

public class GameCodec extends XMLCodec<Game> {
	private static final String PREAMBLE = String.format("<!DOCTYPE game SYSTEM \"game.dtd\">%n");

	public GameCodec() throws IOException { super(Game.class); }
	
	@Override
	protected void customizeMarshaller(Marshaller m) throws JAXBException {
		super.customizeMarshaller(m);
		m.setProperty("com.sun.xml.bind.xmlHeaders", PREAMBLE);
	}
	
	@Override
	public void save(Game elem, File f) throws IOException {
		super.save(elem, f);
		
		// Hack to remove standalone="yes" which breaks TripleA parsing
		String s;
		try (FileInputStream fis = new FileInputStream(f)) {
			s = new String(IOUtils.toByteArray(fis), StandardCharsets.UTF_8);
		}
		s = StringUtils.replace(s, "standalone=\"yes\"?>", "?>");
		try (FileOutputStream fos = new FileOutputStream(f)) {
			fos.write(s.getBytes());
		}
	}
}
