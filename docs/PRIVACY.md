# Privacy and data handling

## Android
- Requests coarse/fine location only when the user presses the location button.
- Coordinates are shown on the device. When the user chooses map/share actions, the coordinates are included in the destination URL or text the user chooses to send.
- No location is transmitted to a project server by the Android app in this starter.
- No background location permission or background tracking is included.
- Phone input is held in app memory and is not uploaded by this implementation.

## Web and backend starter
- The consent page requests browser location only after a click and browser permission.
- The backend accepts coordinates after consent and stores the latest position in process memory; memory is lost on restart.
- Invitation tokens are random and their hashes are held server-side, but the token itself is present in the invitation URL. Keep tokens private.
- The starter does not provide a location-reading endpoint, durable history, or complete identity authorization. It is not ready for real-world sensitive data.

## Production requirements
Add privacy notice and explicit consent text, authenticated requester/recipient accounts, database encryption/access controls, retention limits, deletion APIs, audit logs without coordinates or tokens, abuse reporting, and documented data-controller/contact information. Never log request bodies for geolocation endpoints. Use HTTPS only. Delete precise coordinates promptly after the session expires or is revoked.
