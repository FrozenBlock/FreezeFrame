Please clear changelog after each release.
Put the changelog BELOW the dashes. ANYTHING ABOVE IS IGNORED.
-----------------
- Added a config option to render Photo items in third-person, similarly to their first-person counterpart.
- Photo rendering can now render a backside.
  - Added the `FRAME_BACK` Frame Type.
- Added separate textures for the Camera item, resolving an issue that locked mipmapping to 3 levels.
- Fixed an issue that would cause an error about the `ContainerComponentManipulators` class to log, despite working as intended.
