package de.toem.eclipse.derived.dnd;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.dnd.DragSourceEffect;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.views.navigator.LocalSelectionTransfer;

@SuppressWarnings("restriction")
public class ControlDragAdapter extends DragSourceEffect {

	private Image dragSourceImage = null;

	
	public ControlDragAdapter(Control control) {
		super(control);
	}

	@Override
	public void dragStart(DragSourceEvent event) {
		super.dragStart(event);
//		System.out.println("drag");
		LocalSelectionTransfer.getInstance().setSelection(getSelection());
		event.image = getDragSourceImage(event);
	}


	@Override
	public void dragFinished(DragSourceEvent event) {
		if (dragSourceImage != null)
			dragSourceImage.dispose();
		dragSourceImage = null;
	}
	
	protected Image getDragSourceImage(DragSourceEvent event) {
		return null;
//		if (dragSourceImage != null && !dragSourceImage.isDisposed())
//			dragSourceImage.dispose();
//
//		if (getControl() instanceof Tree)
//			dragSourceImage = getTreeDragSourceImage((Tree) getControl());
//		if (getControl() instanceof Table)
//			dragSourceImage = getTreeDragSourceImage((Tree) getControl());
//		return dragSourceImage;
	}
//
//	Image getTreeDragSourceImage(Tree tree) {
//		Image dragSourceImage = null;
//
//		if (OS.GTK_VERSION < OS.VERSION(2, 2, 0))
//			return null;
//		// TEMPORARY CODE
//		if (tree.isListening(SWT.EraseItem) || tree.isListening(SWT.PaintItem))
//			return null;
//		/*
//		 * Bug in GTK. gtk_tree_selection_get_selected_rows() segmentation faults in versions smaller than 2.2.4 if the model is NULL. The fix is to give a
//		 * valid pointer instead.
//		 */
//		long /* int */handle = tree.handle;
//		long /* int */selection = OS.gtk_tree_view_get_selection(handle);
//		long /* int */[] model = OS.GTK_VERSION < OS.VERSION(2, 2, 4) ? new long /* int */[1] : null;
//		long /* int */list = OS.gtk_tree_selection_get_selected_rows(selection, model);
//		if (list == 0)
//			return null;
//		int count = Math.min(10, OS.g_list_length(list));
//
//		Display display = tree.getDisplay();
//		if (count == 1) {
//			long /* int */path = OS.g_list_nth_data(list, 0);
//			long /* int */pixmap = OS.gtk_tree_view_create_row_drag_icon(handle, path);
//			dragSourceImage = Image.gtk_new(display, SWT.ICON, pixmap, 0);
//		} else {
//			int width = 0, height = 0;
//			int[] w = new int[1], h = new int[1];
//			int[] yy = new int[count], hh = new int[count];
//			long /* int */[] pixmaps = new long /* int */[count];
//			GdkRectangle rect = new GdkRectangle();
//			for (int i = 0; i < count; i++) {
//				long /* int */path = OS.g_list_nth_data(list, i);
//				OS.gtk_tree_view_get_cell_area(handle, path, 0, rect);
//				pixmaps[i] = OS.gtk_tree_view_create_row_drag_icon(handle, path);
//				OS.gdk_drawable_get_size(pixmaps[i], w, h);
//				width = Math.max(width, w[0]);
//				height = rect.y + h[0] - yy[0];
//				yy[i] = rect.y;
//				hh[i] = h[0];
//			}
//			long /* int */source = OS.gdk_pixmap_new(OS.GDK_ROOT_PARENT(), width, height, -1);
//			long /* int */gcSource = OS.gdk_gc_new(source);
//			long /* int */mask = OS.gdk_pixmap_new(OS.GDK_ROOT_PARENT(), width, height, 1);
//			long /* int */gcMask = OS.gdk_gc_new(mask);
//			GdkColor color = new GdkColor();
//			color.pixel = 0;
//			OS.gdk_gc_set_foreground(gcMask, color);
//			OS.gdk_draw_rectangle(mask, gcMask, 1, 0, 0, width, height);
//			color.pixel = 1;
//			OS.gdk_gc_set_foreground(gcMask, color);
//			for (int i = 0; i < count; i++) {
//				OS.gdk_draw_drawable(source, gcSource, pixmaps[i], 0, 0, 0, yy[i] - yy[0], -1, -1);
//				OS.gdk_draw_rectangle(mask, gcMask, 1, 0, yy[i] - yy[0], width, hh[i]);
//				OS.g_object_unref(pixmaps[i]);
//			}
//			OS.g_object_unref(gcSource);
//			OS.g_object_unref(gcMask);
//			dragSourceImage = Image.gtk_new(display, SWT.ICON, source, mask);
//		}
//		OS.g_list_free(list);
//
//		return dragSourceImage;
//	}
//
//	Image getDragSourceImage(Table table) {
//		Image dragSourceImage = null;
//
//		if (OS.GTK_VERSION < OS.VERSION(2, 2, 0))
//			return null;
//		// TEMPORARY CODE
//		if (table.isListening(SWT.EraseItem) || table.isListening(SWT.PaintItem))
//			return null;
//		/*
//		 * Bug in GTK. gtk_tree_selection_get_selected_rows() segmentation faults in versions smaller than 2.2.4 if the model is NULL. The fix is to give a
//		 * valid pointer instead.
//		 */
//		long /* int */handle = table.handle;
//		long /* int */selection = OS.gtk_tree_view_get_selection(handle);
//		long /* int */[] model = OS.GTK_VERSION < OS.VERSION(2, 2, 4) ? new long /* int */[1] : null;
//		long /* int */list = OS.gtk_tree_selection_get_selected_rows(selection, model);
//		if (list == 0)
//			return null;
//		int count = Math.min(10, OS.g_list_length(list));
//
//		Display display = table.getDisplay();
//		if (count == 1) {
//			long /* int */path = OS.g_list_nth_data(list, 0);
//			long /* int */pixmap = OS.gtk_tree_view_create_row_drag_icon(handle, path);
//			dragSourceImage = Image.gtk_new(display, SWT.ICON, pixmap, 0);
//		} else {
//			int width = 0, height = 0;
//			int[] w = new int[1], h = new int[1];
//			int[] yy = new int[count], hh = new int[count];
//			long /* int */[] pixmaps = new long /* int */[count];
//			GdkRectangle rect = new GdkRectangle();
//			for (int i = 0; i < count; i++) {
//				long /* int */path = OS.g_list_nth_data(list, i);
//				OS.gtk_tree_view_get_cell_area(handle, path, 0, rect);
//				pixmaps[i] = OS.gtk_tree_view_create_row_drag_icon(handle, path);
//				OS.gdk_drawable_get_size(pixmaps[i], w, h);
//				width = Math.max(width, w[0]);
//				height = rect.y + h[0] - yy[0];
//				yy[i] = rect.y;
//				hh[i] = h[0];
//			}
//			long /* int */source = OS.gdk_pixmap_new(OS.GDK_ROOT_PARENT(), width, height, -1);
//			long /* int */gcSource = OS.gdk_gc_new(source);
//			long /* int */mask = OS.gdk_pixmap_new(OS.GDK_ROOT_PARENT(), width, height, 1);
//			long /* int */gcMask = OS.gdk_gc_new(mask);
//			GdkColor color = new GdkColor();
//			color.pixel = 0;
//			OS.gdk_gc_set_foreground(gcMask, color);
//			OS.gdk_draw_rectangle(mask, gcMask, 1, 0, 0, width, height);
//			color.pixel = 1;
//			OS.gdk_gc_set_foreground(gcMask, color);
//			for (int i = 0; i < count; i++) {
//				OS.gdk_draw_drawable(source, gcSource, pixmaps[i], 0, 0, 0, yy[i] - yy[0], -1, -1);
//				OS.gdk_draw_rectangle(mask, gcMask, 1, 0, yy[i] - yy[0], width, hh[i]);
//				OS.g_object_unref(pixmaps[i]);
//			}
//			OS.g_object_unref(gcSource);
//			OS.g_object_unref(gcMask);
//			dragSourceImage = Image.gtk_new(display, SWT.ICON, source, mask);
//		}
//		OS.g_list_free(list);
//
//		return dragSourceImage;
//	}

	protected ISelection getSelection() {
		return null;
	}
}
