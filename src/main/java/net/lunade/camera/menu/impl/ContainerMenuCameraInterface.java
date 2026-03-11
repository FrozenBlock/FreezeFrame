package net.lunade.camera.menu.impl;

public interface ContainerMenuCameraInterface {
	default void cameraPort$setSelectedCameraFilmIndex(int slotIndex, int selectedFilmIndex) {
		throw new AssertionError();
	}
}
