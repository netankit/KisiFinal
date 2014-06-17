package de.kisi.android.api;

import java.util.List;

import de.kisi.android.model.Event;

public interface LogsCallback {
	public void onLogsResult(List<Event> events);
}
