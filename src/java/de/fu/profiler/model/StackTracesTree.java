package de.fu.profiler.model;

import java.util.List;

import javax.swing.tree.DefaultMutableTreeNode;

/**
 * @author Konrad Johannes Reiche
 * 
 */
public class StackTracesTree {

	DefaultMutableTreeNode root;

	public StackTracesTree() {
		super();
		root = new DefaultMutableTreeNode("Stack Traces");
	}

	public void createTree(List<StackTrace> stackTraces) {

		root.removeAllChildren();
		DefaultMutableTreeNode thread = null;
		DefaultMutableTreeNode stackTraceElement = null;

		for (StackTrace st : stackTraces) {

			thread = new DefaultMutableTreeNode(st.threadInfo.getName());
			root.add(thread);

			for (StackTraceElement ste : st.stackTrace) {
				stackTraceElement = new DefaultMutableTreeNode(ste);
				thread.add(stackTraceElement);
			}
		}

	}


	public DefaultMutableTreeNode getRoot() {
		return root;
	}

}
