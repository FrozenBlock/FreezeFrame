Please clear changelog after each release.
Put the changelog BELOW the dashes. ANYTHING ABOVE IS IGNORED.
-----------------
- Ported to multiloader.
- The photograph tracker now keeps track of the time at which a photograph was deleted.
  - Thanks to this change, only the names of photographs that have been deleted since a player's last login will be sent to their client for client-sided deletion.
  - Players who haven't previously logged in will no longer be sent a photograph deletion packet.
