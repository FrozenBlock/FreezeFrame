package net.lunade.camera.menu.impl;

public interface ContainerMenuFilmInterface {
	default void cameraPort$setSelectedFilmPhotographIndex(int slotIndex, int selectedPhotographIndex) {
		throw new AssertionError();
	}
}
