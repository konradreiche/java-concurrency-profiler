package de.fu.profiler.controller;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import javax.swing.JComboBox;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.sun.tools.attach.AgentInitializationException;
import com.sun.tools.attach.AgentLoadException;
import com.sun.tools.attach.AttachNotSupportedException;
import com.sun.tools.attach.VirtualMachine;

import de.fu.profiler.model.JVM;
import de.fu.profiler.model.Monitor;
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
		this.view.addMonitorSelectionListener(new MonitorSelectionListener());
		this.view.addNextEventListener(new NextEventListener());
		this.view.addPreviousEventListener(new PreviousEventListener());
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

	public class MonitorSelectionListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			JComboBox monitorSelection = (JComboBox) e.getSource();
			Object selection = monitorSelection.getSelectedItem();
			Long id = null;

			if (selection != null) {
				id = (Long) selection;
			}

			JVM currentJvm = model.getCurrentJVM();
			if (currentJvm != null && id != null) {
				Monitor monitor = currentJvm.getMonitors().get(id);
				view.setMonitorLabels(monitor.getEntryCount(),
						monitor.getWaiterCount(),
						monitor.getNotifyWaiterCount());
			}
		}
	}

	public class NextEventListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			int newEventIndex = model.getCurrentEventHistory().indexOf(
					model.getCurrentEvent());
			if (newEventIndex == model.getCurrentEventHistory().size() - 1) {
				view.setEnabledPreviousEventButton(false);
			} else {
				view.setEnabledPreviousEventButton(true);
				++newEventIndex;
				model.setCurrentEvent(newEventIndex);
			}
			model.applyDataUntilEvent(model.getCurrentEvent());
		}
	}

	public class PreviousEventListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {

			int newEventIndex = model.getCurrentEventHistory().indexOf(
					model.getCurrentEvent());
			if (newEventIndex == 0) {
				view.setEnabledPreviousEventButton(false);
			} else {
				view.setEnabledPreviousEventButton(true);
				--newEventIndex;
				model.setCurrentEvent(newEventIndex);
			}
			model.applyDataUntilEvent(model.getCurrentEvent());
		}
	}
}
