package de.fu.profiler.controller;

import de.fu.profiler.model.ProfilerModel;
import de.fu.profiler.view.ProfilerView;

public class ProfilerController {

	ProfilerView view;
	ProfilerModel model;

	public ProfilerController(ProfilerView view, ProfilerModel model) {
		super();
		this.view = view;
		this.model = model;
	}
}
