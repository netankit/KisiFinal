package de.kisi.android.vicinity;

/**
 * List of all Vicinity Managers, this is be used to decide
 * which actor each Manager have to use
 */
public enum VicinityTypeEnum {
	NFC,
	// Bluetooth can run in two modes. Either the suggest to unlock
	// mode or the auto unlock mode, so we have to differentiate them
	BluetoothLE, 
	BluetoothLEAutoUnlock,
	
	Geofence
}
