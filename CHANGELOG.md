Please clear changelog after each release.
Put the changelog BELOW the dashes. ANYTHING ABOVE IS IGNORED.
-----------------
- Bumped Freeze Frame's protocol version to 2.
- Added a statistic for interact with a Developing Table.
- Added a config option to render Photo items in third-person, similarly to their first-person counterpart.
- Photo rendering can now render a backside.
  - Added the `FRAME_BACK` Frame Type.
- Added separate textures for the Camera item, resolving an issue that locked mipmapping to 3 levels.
- Resetting a scope item's zoom to default now plays multiple increment sounds in succession instead of one, relative to the amount of zoom steps away from the default.
- Fixed an issue that would cause an error about the `ContainerComponentManipulators` class to log, despite working as intended.
- The Film Capacity Upgrade Recipe now supports a custom Source, Material, and Output item like other Vanilla transmute recipes.
- The Film Capacity Upgrade Recipe now shows up in the Crafting Recipe Book.
