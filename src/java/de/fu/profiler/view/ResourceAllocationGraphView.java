package de.fu.profiler.view;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;

import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.apache.commons.collections15.Transformer;

import de.fu.profiler.model.JVM;
import de.fu.profiler.model.MonitorInfo;
import de.fu.profiler.model.Node;
import de.fu.profiler.model.ProfilerModel;
import de.fu.profiler.model.ThreadInfo;
import edu.uci.ics.jung.algorithms.layout.FRLayout;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.visualization.BasicVisualizationServer;
import edu.uci.ics.jung.visualization.RenderContext;
import edu.uci.ics.jung.visualization.renderers.Renderer;
import edu.uci.ics.jung.visualization.transform.shape.GraphicsDecorator;

public class ResourceAllocationGraphView extends JPanel {

	JVM jvm;

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	Transformer<Node<?>, String> transformer = new Transformer<Node<?>, String>() {

		@Override
		public String transform(Node<?> vertex) {

			if (vertex instanceof ThreadInfo) {
				ThreadInfo thread = (ThreadInfo) vertex;
				String label = thread.getName() + " (ID: " + thread.getId()
						+ ")";
				return label;
			} else if (vertex instanceof MonitorInfo) {
				MonitorInfo monitor = (MonitorInfo) vertex;
				String label = monitor.getClassName() + " (ID: "
						+ monitor.getId() + ")";
				return label;
			} else {
				throw new IllegalStateException();
			}

		}
	};

	private class RessourceAllocationGraphRenderer implements
			Renderer.Vertex<Node<?>, Long> {

		@Override
		public void paintVertex(RenderContext<Node<?>, Long> rc,
				Layout<Node<?>, Long> layout, Node<?> vertex) {

			GraphicsDecorator graphicsContext = rc.getGraphicsContext();
			Point2D center = layout.transform(vertex);
			Shape shape = null;
			Color color = null;

			if (vertex instanceof ThreadInfo) {
				shape = new Ellipse2D.Double(center.getX() - 10,
						center.getY() - 10, 20, 20);
				color = Color.RED;
			} else if (vertex instanceof MonitorInfo) {
				shape = new Rectangle((int) center.getX() - 10,
						(int) center.getY() - 10, 20, 20);
				color = Color.LIGHT_GRAY;

			} else {
				throw new IllegalStateException();
			}

			graphicsContext.setPaint(color);
			graphicsContext.fill(shape);
		}
	}

	public ResourceAllocationGraphView(ProfilerModel model, JVM jvm) {
		super(new GridLayout(1, 1));
		this.jvm = jvm;

		Layout<Node<?>, Long> layout = new FRLayout<Node<?>, Long>(
				jvm.getResourceAllocationGraph());
		layout.setSize(new Dimension(1200, 600));
		BasicVisualizationServer<Node<?>, Long> vv = new BasicVisualizationServer<Node<?>, Long>(
				layout);
		vv.getRenderContext().setVertexLabelTransformer(transformer);
		vv.getRenderer().setVertexRenderer(
				new RessourceAllocationGraphRenderer());

		vv.setPreferredSize(new Dimension(1200, 600));
		super.add(new JScrollPane(vv));
	}
}
