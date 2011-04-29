package de.fu.profiler.controller;

import java.io.IOException;

import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.sun.tools.attach.AgentInitializationException;
import com.sun.tools.attach.AgentLoadException;
import com.sun.tools.attach.AttachNotSupportedException;
import com.sun.tools.attach.VirtualMachine;

import de.fu.profiler.model.ProfilerModel;
import de.fu.profiler.view.ProfilerView;

public class ProfilerController {

	private ProfilerView view;
	ProfilerModel model;

	public ProfilerController(ProfilerView view, ProfilerModel model) {
		super();
		this.view = view;
		this.model = model;
		this.view.addJVMSelectionListener(new JVMSelectionListener());
	}

	public class JVMSelectionListener implements ListSelectionListener {

		@Override
		public void valueChanged(ListSelectionEvent e) {

			if (!e.getValueIsAdjusting()) {

				try {
					String pid = VirtualMachine.list().get(0).id();
					VirtualMachine vm = VirtualMachine.attach(pid);
					vm.loadAgentLibrary("agent");
				} catch (AgentLoadException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (AgentInitializationException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (AttachNotSupportedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		}

	}
}
