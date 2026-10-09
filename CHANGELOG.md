Please clear changelog after each release.
Put the changelog BELOW the dashes. ANYTHING ABOVE IS IGNORED.
-----------------
- Added the `Minecraft Snap` advancement for taking a photo of a mob.
- Added the `A Permanent Memory` advancement for taking a photo and printing it out.
- Added the `Ender-Vision` advancement for taking a photo of an Enderman using the `Inverted` Film Filter.
- Added the `freezeframe:take_photograph` Advancement Criteria Trigger Type with the following format:
  - `player` (Optional): An Entity Predicate to test against the Player.
  - `item` (Optional): An Item Predicate to test against the Item used to take the photograph.
- Added the `freezeframe:print_photograph` Advancement Criteria Trigger Type with the following format:
  - `player` (Optional): An Entity Predicate to test against the Player.
  - `player_must_be_photographer`: Whether the Player printing the photograph must also be its photographer.
  - `allow_copy`: Whether the photograph being printed can be a copy of another photograph.
- Added the `freezeframe:film_filter` Data Component Predicate Type with the following format:
  - `layers` (Optional): A list of Film Filter Layer Predicates to test against a Film Filter Component.
    - The format for each Film Filter Layer Predicate is simply the format of a Film Filter Layer itself.
- Very slightly improved the performance cost of rendering photographs.
