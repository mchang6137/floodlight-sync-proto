package net.floodlightcontroller.storage.distmem;

public interface TableSource {
	MemoryTable getTable(String name);
	MemoryTable createTable(String name);
}
