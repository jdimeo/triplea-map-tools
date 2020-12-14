package jdimeo.triplea.excel;

import lombok.extern.log4j.Log4j2;

public interface SyncLog {
	void logChange(String name, String prop, Object oldVal, Object newVal);
	void logSet(String name, String prop, Object val);
	void warn(String message, Object... args);
	
	@Log4j2
	class SyncLogger implements SyncLog {
		@Override
		public void logChange(String name, String prop, Object oldVal, Object newVal) {
			log.info("{} {} changed from {} to {}", name, prop, oldVal, newVal);
		}
		
		@Override
		public void logSet(String name, String prop, Object val) {
			log.info("Set {} {} to {}", name, prop, val);
		}
		
		@Override
		public void warn(String message, Object... args) {
			log.warn(message, args);
		}
	}
	
	static SyncLogger DEFAULT = new SyncLogger();
}
